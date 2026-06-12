"""Backend-only Claude providers for explaining deterministic mission output."""

from __future__ import annotations

import json
import os
import ssl
from abc import ABC, abstractmethod
from dataclasses import dataclass
from enum import Enum
from pathlib import Path
from typing import Any, Callable
from urllib.error import HTTPError
from urllib.request import Request, urlopen


class LlmTask(str, Enum):
    MISSION_EXPLANATION = "mission_explanation"
    READINESS_EXPLANATION = "readiness_explanation"
    BLOCKER_EXPLANATION = "blocker_explanation"
    TIMELINE_EXPLANATION = "timeline_explanation"
    RECOMMENDATION_EXPLANATION = "recommendation_explanation"
    MISSION_QUESTION = "mission_question"
    QUICK_SUMMARY = "quick_summary"


class ClaudeModel(str, Enum):
    HAIKU = "claude-haiku-4-5-20251001"
    SONNET = "claude-sonnet-4-6"
    OPUS = "claude-opus-4-8"


@dataclass(frozen=True)
class LlmResponse:
    text: str
    model: ClaudeModel
    provider: str


class AnthropicApiError(RuntimeError):
    """Safe Anthropic error that excludes credentials and request content."""

    def __init__(
        self,
        status_code: int,
        error_type: str,
        message: str,
        request_id: str | None = None,
    ) -> None:
        request_suffix = f" Request ID: {request_id}." if request_id else ""
        super().__init__(
            f"Anthropic API error {status_code} ({error_type}): "
            f"{message}{request_suffix}"
        )
        self.status_code = status_code
        self.error_type = error_type
        self.request_id = request_id


AnthropicTransport = Callable[
    [str, dict[str, str], dict[str, Any]],
    dict[str, Any],
]


class MissionPromptBuilder:
    """Builds auditable prompts containing facts already calculated by FutureMe."""

    system_prompt = (
        "You are the FutureMe Mission Coach, a concise financial strategist. "
        "Use only the supplied deterministic engine output. Never calculate or "
        "change readiness scores, timelines, dollar values, probabilities, risks, "
        "or outcomes. Never invent missing facts. Explain what the values mean, "
        "why they matter, and which supplied action deserves attention. Clearly "
        "state uncertainty. This is educational guidance, not financial advice."
    )

    def build(
        self,
        task: LlmTask,
        mission_context: dict[str, Any],
        question: str | None = None,
    ) -> dict[str, Any]:
        payload = {
            "task": task.value,
            "question": question,
            "mission_context": mission_context,
            "response_rules": [
                "Preserve every supplied number exactly.",
                "Use two short paragraphs or fewer.",
                "Prioritize the supplied next action when it answers the question.",
                "Do not introduce products, rates, balances, or assumptions.",
            ],
        }
        return {
            "system": self.system_prompt,
            "messages": [
                {
                    "role": "user",
                    "content": json.dumps(payload, separators=(",", ":"), sort_keys=True),
                }
            ],
        }


class AnthropicResponseParser:
    """Parses the Messages API while rejecting empty or malformed responses."""

    def parse(
        self,
        payload: dict[str, Any],
        model: ClaudeModel,
    ) -> LlmResponse:
        blocks = payload.get("content")
        if not isinstance(blocks, list):
            raise ValueError("Anthropic response is missing content blocks.")
        text = "\n".join(
            block.get("text", "").strip()
            for block in blocks
            if isinstance(block, dict) and block.get("type") == "text"
        ).strip()
        if not text:
            raise ValueError("Anthropic response did not contain explanation text.")
        return LlmResponse(text=text, model=model, provider="anthropic")


class LlmProvider(ABC):
    @abstractmethod
    def explain_mission(self, mission_context: dict[str, Any]) -> LlmResponse:
        raise NotImplementedError

    @abstractmethod
    def explain_readiness(self, mission_context: dict[str, Any]) -> LlmResponse:
        raise NotImplementedError

    @abstractmethod
    def explain_blockers(self, mission_context: dict[str, Any]) -> LlmResponse:
        raise NotImplementedError

    @abstractmethod
    def explain_timeline(self, mission_context: dict[str, Any]) -> LlmResponse:
        raise NotImplementedError

    @abstractmethod
    def explain_recommendation(
        self,
        mission_context: dict[str, Any],
    ) -> LlmResponse:
        raise NotImplementedError

    @abstractmethod
    def answer_mission_question(
        self,
        question: str,
        mission_context: dict[str, Any],
    ) -> LlmResponse:
        raise NotImplementedError

    # Compatibility methods for the original provider-facing routes.
    def generate_financial_explanation(
        self,
        structured_output: dict[str, Any],
    ) -> LlmResponse:
        return self.explain_recommendation(structured_output)

    def generate_scenario_answer(
        self,
        question: str,
        structured_output: dict[str, Any],
    ) -> LlmResponse:
        return self.answer_mission_question(question, structured_output)

    def generate_insight_summary(
        self,
        insights: list[dict[str, Any]],
    ) -> LlmResponse:
        return self.explain_mission({"insights": insights})


class MockLlmProvider(LlmProvider):
    """Stable offline fallback that explains supplied facts without recalculation."""

    def explain_mission(self, mission_context: dict[str, Any]) -> LlmResponse:
        title = mission_context.get("title", "This mission")
        readiness = mission_context.get("readinessScore", 0)
        next_action = self._next_action(mission_context)
        return self._response(
            f"{title} is {readiness}% ready. The strongest current focus is "
            f"{next_action.lower()} because it is the next unlocked action in the plan."
        )

    def explain_readiness(self, mission_context: dict[str, Any]) -> LlmResponse:
        readiness = mission_context.get("readinessScore", 0)
        weakest = self._weakest_factor(mission_context)
        blocker = self._first(mission_context.get("blockers"), "No critical blocker is modeled.")
        return self._response(
            f"Readiness is {readiness}%. {weakest} is the weakest supplied factor. "
            f"The engine identifies this constraint: {blocker}"
        )

    def explain_blockers(self, mission_context: dict[str, Any]) -> LlmResponse:
        blocker = self._first(mission_context.get("blockers"), "No critical blocker is modeled.")
        blocked = self._first(
            mission_context.get("blockedActions"),
            "No action is currently dependency-blocked.",
        )
        return self._response(
            f"The main blocker is {self._sentence(blocker)} {self._sentence(blocked)} "
            "Clearing the prerequisite "
            "protects the mission from advancing with unresolved risk."
        )

    def explain_timeline(self, mission_context: dict[str, Any]) -> LlmResponse:
        target = mission_context.get("targetDate", "the modeled target date")
        next_action = self._next_action(mission_context)
        gain = mission_context.get("nextActionReadinessGain", 0)
        return self._response(
            f"The current target is {target}. Completing {next_action.lower()} is "
            f"modeled to add {gain} readiness points and is the clearest supplied "
            "way to accelerate the roadmap."
        )

    def explain_recommendation(
        self,
        mission_context: dict[str, Any],
    ) -> LlmResponse:
        next_action = self._next_action(mission_context)
        gain = mission_context.get("nextActionReadinessGain", 0)
        return self._response(
            f"Focus on {next_action}. The deterministic action engine estimates a "
            f"{gain}-point readiness gain, so it has the clearest modeled effect now."
        )

    def answer_mission_question(
        self,
        question: str,
        mission_context: dict[str, Any],
    ) -> LlmResponse:
        title = mission_context.get("title", "this mission")
        next_action = self._next_action(mission_context)
        blocker = self._first(mission_context.get("blockers"), "no critical blocker")
        return self._response(
            f"For '{question}', the supplied {title} plan points to {next_action.lower()}. "
            f"The main constraint remains {self._sentence(blocker)}"
        )

    def generate_financial_explanation(
        self,
        structured_output: dict[str, Any],
    ) -> LlmResponse:
        if "difference" in structured_output:
            difference = structured_output["difference"]
            return self._response(
                f"The modeled plan changes five-year net worth by ${difference:,.0f}. "
                "This explanation preserves calculator output and does not recalculate it."
            )
        return super().generate_financial_explanation(structured_output)

    def generate_insight_summary(
        self,
        insights: list[dict[str, Any]],
    ) -> LlmResponse:
        titles = ", ".join(item.get("title", "Untitled insight") for item in insights[:3])
        return LlmResponse(
            text=f"Top priorities: {titles}.",
            model=ClaudeModel.HAIKU,
            provider="mock",
        )

    def _response(self, text: str) -> LlmResponse:
        return LlmResponse(text=text, model=ClaudeModel.SONNET, provider="mock")

    def _next_action(self, context: dict[str, Any]) -> str:
        return context.get("nextActionTitle") or "review the next unlocked action"

    def _weakest_factor(self, context: dict[str, Any]) -> str:
        factors = context.get("readinessFactors") or []
        if not factors:
            return "The lowest readiness factor"
        weakest = min(factors, key=lambda item: item.get("score", 100))
        return f"{weakest.get('title', 'The lowest readiness factor')} at {weakest.get('score', 0)}%"

    def _first(self, values: Any, fallback: str) -> str:
        if isinstance(values, list) and values:
            value = values[0]
            if isinstance(value, dict):
                return value.get("blockerMessage") or value.get("title") or fallback
            return str(value)
        return fallback

    def _sentence(self, value: str) -> str:
        return value if value.endswith((".", "!", "?")) else f"{value}."


class AnthropicLlmProvider(LlmProvider):
    """Claude Messages API provider. API credentials remain backend-only."""

    endpoint = "https://api.anthropic.com/v1/messages"

    def __init__(
        self,
        api_key: str | None = None,
        transport: AnthropicTransport | None = None,
        prompt_builder: MissionPromptBuilder | None = None,
        response_parser: AnthropicResponseParser | None = None,
    ) -> None:
        self._api_key = api_key or os.getenv("ANTHROPIC_API_KEY")
        self._transport = transport or self._http_transport
        self._prompt_builder = prompt_builder or MissionPromptBuilder()
        self._response_parser = response_parser or AnthropicResponseParser()

    def build_request(
        self,
        task: LlmTask,
        structured_output: dict[str, Any],
        question: str | None = None,
    ) -> dict[str, Any]:
        model = self._model_for(task)
        prompt = self._prompt_builder.build(task, structured_output, question)
        return {
            "model": model.value,
            "max_tokens": 800 if model == ClaudeModel.OPUS else 500,
            "system": prompt["system"],
            "messages": prompt["messages"],
        }

    def explain_mission(self, mission_context: dict[str, Any]) -> LlmResponse:
        return self._invoke(LlmTask.MISSION_EXPLANATION, mission_context)

    def explain_readiness(self, mission_context: dict[str, Any]) -> LlmResponse:
        return self._invoke(LlmTask.READINESS_EXPLANATION, mission_context)

    def explain_blockers(self, mission_context: dict[str, Any]) -> LlmResponse:
        return self._invoke(LlmTask.BLOCKER_EXPLANATION, mission_context)

    def explain_timeline(self, mission_context: dict[str, Any]) -> LlmResponse:
        return self._invoke(LlmTask.TIMELINE_EXPLANATION, mission_context)

    def explain_recommendation(
        self,
        mission_context: dict[str, Any],
    ) -> LlmResponse:
        return self._invoke(LlmTask.RECOMMENDATION_EXPLANATION, mission_context)

    def answer_mission_question(
        self,
        question: str,
        mission_context: dict[str, Any],
    ) -> LlmResponse:
        return self._invoke(LlmTask.MISSION_QUESTION, mission_context, question)

    def generate_insight_summary(
        self,
        insights: list[dict[str, Any]],
    ) -> LlmResponse:
        return self._invoke(LlmTask.QUICK_SUMMARY, {"insights": insights})

    def _invoke(
        self,
        task: LlmTask,
        mission_context: dict[str, Any],
        question: str | None = None,
    ) -> LlmResponse:
        if not self._api_key:
            raise RuntimeError("ANTHROPIC_API_KEY is not configured on the backend.")
        request_body = self.build_request(task, mission_context, question)
        response_body = self._transport(
            self.endpoint,
            {
                "x-api-key": self._api_key,
                "anthropic-version": "2023-06-01",
                "content-type": "application/json",
            },
            request_body,
        )
        return self._response_parser.parse(response_body, self._model_for(task))

    def _model_for(self, task: LlmTask) -> ClaudeModel:
        if task == LlmTask.MISSION_QUESTION:
            return ClaudeModel.OPUS
        if task == LlmTask.QUICK_SUMMARY:
            return ClaudeModel.HAIKU
        return ClaudeModel.SONNET

    def _http_transport(
        self,
        url: str,
        headers: dict[str, str],
        body: dict[str, Any],
    ) -> dict[str, Any]:
        request = Request(
            url,
            data=json.dumps(body).encode("utf-8"),
            headers=headers,
            method="POST",
        )
        try:
            with urlopen(
                request,
                timeout=30,
                context=self._ssl_context(),
            ) as response:
                return json.loads(response.read().decode("utf-8"))
        except HTTPError as error:
            raise self._api_error(error) from None

    def _api_error(self, error: HTTPError) -> AnthropicApiError:
        try:
            payload = json.loads(error.read().decode("utf-8"))
        except (UnicodeDecodeError, json.JSONDecodeError):
            payload = {}
        details = payload.get("error") if isinstance(payload, dict) else None
        details = details if isinstance(details, dict) else {}
        return AnthropicApiError(
            status_code=error.code,
            error_type=str(details.get("type", "http_error")),
            message=str(details.get("message", error.reason or "Request failed.")),
            request_id=payload.get("request_id") if isinstance(payload, dict) else None,
        )

    def _ssl_context(self) -> ssl.SSLContext:
        verify_paths = ssl.get_default_verify_paths()
        candidates = [
            os.getenv("SSL_CERT_FILE"),
            verify_paths.cafile,
            "/etc/ssl/cert.pem",
            "/private/etc/ssl/cert.pem",
        ]
        ca_file = next(
            (
                candidate
                for candidate in candidates
                if candidate and Path(candidate).is_file()
            ),
            None,
        )
        return ssl.create_default_context(cafile=ca_file)
