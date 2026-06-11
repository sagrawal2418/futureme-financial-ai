"""Backend-only LLM providers for explaining deterministic financial output."""

from __future__ import annotations

from abc import ABC, abstractmethod
from dataclasses import dataclass
from enum import Enum
from typing import Any


class LlmTask(str, Enum):
    FINANCIAL_EXPLANATION = "financial_explanation"
    SCENARIO_ANSWER = "scenario_answer"
    INSIGHT_SUMMARY = "insight_summary"


class ClaudeModel(str, Enum):
    HAIKU = "claude-haiku"
    SONNET = "claude-sonnet"
    OPUS = "claude-opus"


@dataclass(frozen=True)
class LlmResponse:
    text: str
    model: ClaudeModel
    provider: str


class LlmProvider(ABC):
    @abstractmethod
    def generate_financial_explanation(
        self, structured_output: dict[str, Any]
    ) -> LlmResponse:
        raise NotImplementedError

    @abstractmethod
    def generate_scenario_answer(
        self, question: str, structured_output: dict[str, Any]
    ) -> LlmResponse:
        raise NotImplementedError

    @abstractmethod
    def generate_insight_summary(
        self, insights: list[dict[str, Any]]
    ) -> LlmResponse:
        raise NotImplementedError


class MockLlmProvider(LlmProvider):
    """Produces stable explanations from values already calculated by the core."""

    def generate_financial_explanation(
        self, structured_output: dict[str, Any]
    ) -> LlmResponse:
        difference = structured_output.get("difference", 0)
        return LlmResponse(
            text=(
                f"The modeled plan changes five-year net worth by ${difference:,.0f}. "
                "This explanation uses calculator output and does not recalculate it."
            ),
            model=ClaudeModel.SONNET,
            provider="mock",
        )

    def generate_scenario_answer(
        self, question: str, structured_output: dict[str, Any]
    ) -> LlmResponse:
        recommendation = structured_output.get(
            "recommendation", "Review the structured scenario result."
        )
        return LlmResponse(
            text=f"For '{question}', the deterministic engine recommends: {recommendation}",
            model=ClaudeModel.OPUS,
            provider="mock",
        )

    def generate_insight_summary(
        self, insights: list[dict[str, Any]]
    ) -> LlmResponse:
        titles = ", ".join(item.get("title", "Untitled insight") for item in insights[:3])
        return LlmResponse(
            text=f"Top priorities: {titles}.",
            model=ClaudeModel.HAIKU,
            provider="mock",
        )


class AnthropicLlmProvider(LlmProvider):
    """Builds Anthropic requests. Transport is intentionally not enabled in V2."""

    endpoint = "https://api.anthropic.com/v1/messages"

    def __init__(self, api_key: str | None = None) -> None:
        self._api_key = api_key

    def build_request(
        self,
        task: LlmTask,
        structured_output: dict[str, Any],
        question: str | None = None,
    ) -> dict[str, Any]:
        model = {
            LlmTask.FINANCIAL_EXPLANATION: ClaudeModel.SONNET,
            LlmTask.SCENARIO_ANSWER: ClaudeModel.OPUS,
            LlmTask.INSIGHT_SUMMARY: ClaudeModel.HAIKU,
        }[task]
        return {
            "model": model.value,
            "max_tokens": 700 if model == ClaudeModel.OPUS else 400,
            "system": (
                "You are FutureMe Financial. Explain only the supplied deterministic "
                "financial output. Never calculate, alter values, or present financial advice."
            ),
            "messages": [
                {
                    "role": "user",
                    "content": {
                        "task": task.value,
                        "question": question,
                        "structured_output": structured_output,
                    },
                }
            ],
        }

    def generate_financial_explanation(
        self, structured_output: dict[str, Any]
    ) -> LlmResponse:
        raise RuntimeError("Anthropic transport is disabled. Use build_request in V2.")

    def generate_scenario_answer(
        self, question: str, structured_output: dict[str, Any]
    ) -> LlmResponse:
        raise RuntimeError("Anthropic transport is disabled. Use build_request in V2.")

    def generate_insight_summary(
        self, insights: list[dict[str, Any]]
    ) -> LlmResponse:
        raise RuntimeError("Anthropic transport is disabled. Use build_request in V2.")
