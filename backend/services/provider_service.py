"""Provider-facing service methods corresponding to the OpenAPI routes."""

from __future__ import annotations

from typing import Any

from backend.normalizers.financial_data import FinancialDataNormalizer
from backend.providers.llm import LlmProvider
from backend.providers.plaid import PlaidProvider


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
