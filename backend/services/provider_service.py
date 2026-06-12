"""Provider-facing service methods corresponding to the OpenAPI routes."""

from __future__ import annotations

from typing import Any

from backend.normalizers.financial_data import FinancialDataNormalizer
from backend.providers.llm import LlmProvider
from backend.providers.plaid import PlaidProvider
from backend.services.mission_coach import MissionCoachService


class ProviderService:
    def __init__(
        self,
        plaid: PlaidProvider,
        llm: LlmProvider,
        normalizer: FinancialDataNormalizer | None = None,
    ) -> None:
        self._plaid = plaid
        self._llm = llm
        self._normalizer = normalizer or FinancialDataNormalizer()
        self._mission_coach = MissionCoachService(llm)

    def plaid_link_token(self, user_id: str) -> dict[str, Any]:
        return self._plaid.create_link_token(user_id)

    def plaid_exchange_public_token(self, public_token: str) -> dict[str, Any]:
        return self._plaid.exchange_public_token(public_token)

    def normalized_financial_data(self) -> dict[str, Any]:
        normalized = self._normalizer.normalize_accounts(
            self._plaid.get_accounts(),
            self._plaid.get_liabilities(),
            self._plaid.get_investments(),
        )
        normalized["transactions"] = self._normalizer.normalize_transactions(
            self._plaid.get_transactions()
        )
        return normalized

    def plaid_accounts(self) -> list[dict[str, Any]]:
        return self._plaid.get_accounts()

    def plaid_transactions(self) -> list[dict[str, Any]]:
        return self._plaid.get_transactions()

    def plaid_liabilities(self) -> list[dict[str, Any]]:
        return self._plaid.get_liabilities()

    def plaid_investments(self) -> list[dict[str, Any]]:
        return self._plaid.get_investments()

    def explain(self, structured_output: dict[str, Any]) -> dict[str, Any]:
        response = self._llm.generate_financial_explanation(structured_output)
        return {
            "answer": response.text,
            "model": response.model.value,
            "provider": response.provider,
        }

    def mission_coaching(self, mission_context: dict[str, Any]) -> dict[str, Any]:
        result = self._mission_coach.coach(mission_context)
        return {
            "missionId": result.mission_id,
            "coachingSummary": result.coaching_summary,
            "recommendedFocusArea": result.recommended_focus_area,
            "topRisk": result.top_risk,
            "topOpportunity": result.top_opportunity,
            "suggestedActions": result.suggested_actions,
            "readinessExplanation": result.readiness_explanation,
            "blockerExplanation": result.blocker_explanation,
            "timelineExplanation": result.timeline_explanation,
            "provider": result.provider,
            "model": result.model,
            "generatedAt": result.generated_at,
            "usedFallback": result.used_fallback,
        }

    def answer_mission_question(
        self,
        question: str,
        mission_context: dict[str, Any],
    ) -> dict[str, Any]:
        response = self._mission_coach.answer(question, mission_context)
        return {
            "answer": response.text,
            "model": response.model.value,
            "provider": response.provider,
        }

    def mission_explanation_history(
        self,
        mission_id: str,
    ) -> dict[str, Any] | None:
        return self._mission_coach.history_dict(mission_id)
