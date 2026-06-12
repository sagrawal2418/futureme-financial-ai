"""Mission coaching orchestration over immutable deterministic engine output."""

from __future__ import annotations

from dataclasses import asdict, dataclass
from datetime import datetime, timezone
from typing import Any

from backend.providers.llm import (
    LlmProvider,
    LlmResponse,
    MockLlmProvider,
)


@dataclass(frozen=True)
class MissionCoachResult:
    mission_id: str
    coaching_summary: str
    recommended_focus_area: str
    top_risk: str
    top_opportunity: str
    suggested_actions: list[str]
    readiness_explanation: str
    blocker_explanation: str
    timeline_explanation: str
    provider: str
    model: str
    generated_at: str
    used_fallback: bool


@dataclass(frozen=True)
class MissionExplanationHistory:
    mission_id: str
    latest: MissionCoachResult
    previous: MissionCoachResult | None
    what_changed: list[str]


class MissionCoachService:
    """Explains engine facts and falls back locally when Claude is unavailable."""

    def __init__(
        self,
        provider: LlmProvider,
        fallback_provider: LlmProvider | None = None,
    ) -> None:
        self._provider = provider
        self._fallback = fallback_provider or MockLlmProvider()
        self._history: dict[str, list[MissionCoachResult]] = {}

    def coach(self, mission_context: dict[str, Any]) -> MissionCoachResult:
        mission_id = str(mission_context["missionId"])
        try:
            result = self._generate(mission_context, self._provider, False)
        except (RuntimeError, ValueError, OSError):
            result = self._generate(mission_context, self._fallback, True)

        history = self._history.setdefault(mission_id, [])
        history.insert(0, result)
        del history[10:]
        return result

    def answer(
        self,
        question: str,
        mission_context: dict[str, Any],
    ) -> LlmResponse:
        try:
            return self._provider.answer_mission_question(question, mission_context)
        except (RuntimeError, ValueError, OSError):
            return self._fallback.answer_mission_question(question, mission_context)

    def history(self, mission_id: str) -> MissionExplanationHistory | None:
        entries = self._history.get(mission_id, [])
        if not entries:
            return None
        latest = entries[0]
        previous = entries[1] if len(entries) > 1 else None
        return MissionExplanationHistory(
            mission_id=mission_id,
            latest=latest,
            previous=previous,
            what_changed=self._changes(latest, previous),
        )

    def history_dict(self, mission_id: str) -> dict[str, Any] | None:
        record = self.history(mission_id)
        return asdict(record) if record else None

    def _generate(
        self,
        context: dict[str, Any],
        provider: LlmProvider,
        used_fallback: bool,
    ) -> MissionCoachResult:
        summary = provider.explain_mission(context)
        readiness = provider.explain_readiness(context)
        blockers = provider.explain_blockers(context)
        timeline = provider.explain_timeline(context)
        recommendation = provider.explain_recommendation(context)
        actions = [
            str(action.get("title"))
            for action in context.get("unlockedActions", [])
            if action.get("title")
        ][:3]
        opportunity = context.get("topOpportunity") or recommendation.text
        return MissionCoachResult(
            mission_id=str(context["missionId"]),
            coaching_summary=summary.text,
            recommended_focus_area=recommendation.text,
            top_risk=blockers.text,
            top_opportunity=str(opportunity),
            suggested_actions=actions,
            readiness_explanation=readiness.text,
            blocker_explanation=blockers.text,
            timeline_explanation=timeline.text,
            provider=summary.provider,
            model=summary.model.value,
            generated_at=datetime.now(timezone.utc).isoformat(),
            used_fallback=used_fallback,
        )

    def _changes(
        self,
        latest: MissionCoachResult,
        previous: MissionCoachResult | None,
    ) -> list[str]:
        if previous is None:
            return ["This is the first stored explanation for this mission."]
        changes = []
        if latest.recommended_focus_area != previous.recommended_focus_area:
            changes.append("The recommended focus area changed.")
        if latest.top_risk != previous.top_risk:
            changes.append("The top risk explanation changed.")
        if latest.suggested_actions != previous.suggested_actions:
            changes.append("The suggested action sequence changed.")
        return changes or ["The mission explanation is unchanged."]


def fallback_coach_service(provider: LlmProvider) -> MissionCoachService:
    """Convenience constructor used by backend composition roots."""

    return MissionCoachService(provider, MockLlmProvider())
