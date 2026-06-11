"""Callable route facade matching backend/api/openapi.yaml.

A future FastAPI adapter can bind these methods without moving provider logic
into the transport layer.
"""

from __future__ import annotations

from typing import Any

from backend.services.provider_service import ProviderService


class BackendApi:
    def __init__(self, service: ProviderService) -> None:
        self._service = service

    def post_plaid_link_token(self, user_id: str) -> dict[str, Any]:
        return self._service.plaid_link_token(user_id)

    def post_plaid_exchange_public_token(
        self, public_token: str
    ) -> dict[str, Any]:
        return self._service.plaid_exchange_public_token(public_token)

    def get_plaid_accounts(self) -> list[dict[str, Any]]:
        return self._service.plaid_accounts()

    def get_plaid_transactions(self) -> list[dict[str, Any]]:
        return self._service.plaid_transactions()

    def get_plaid_liabilities(self) -> list[dict[str, Any]]:
        return self._service.plaid_liabilities()

    def get_plaid_investments(self) -> list[dict[str, Any]]:
        return self._service.plaid_investments()
