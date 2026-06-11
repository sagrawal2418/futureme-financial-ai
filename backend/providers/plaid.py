"""Plaid provider boundary with mock data and a sandbox placeholder."""

from __future__ import annotations

from abc import ABC, abstractmethod
from typing import Any


class PlaidProvider(ABC):
    @abstractmethod
    def create_link_token(self, user_id: str) -> dict[str, Any]:
        raise NotImplementedError

    @abstractmethod
    def exchange_public_token(self, public_token: str) -> dict[str, Any]:
        raise NotImplementedError

    @abstractmethod
    def get_accounts(self) -> list[dict[str, Any]]:
        raise NotImplementedError

    @abstractmethod
    def get_transactions(self) -> list[dict[str, Any]]:
        raise NotImplementedError

    @abstractmethod
    def get_liabilities(self) -> list[dict[str, Any]]:
        raise NotImplementedError

    @abstractmethod
    def get_investments(self) -> list[dict[str, Any]]:
        raise NotImplementedError


class MockPlaidProvider(PlaidProvider):
    def create_link_token(self, user_id: str) -> dict[str, Any]:
        return {"link_token": f"link-sandbox-{user_id}", "expiration": "2026-06-11T12:00:00Z"}

    def exchange_public_token(self, public_token: str) -> dict[str, Any]:
        return {"item_id": "mock-item-001", "access_token_reference": "vault://mock-item-001"}

    def get_accounts(self) -> list[dict[str, Any]]:
        return [
            {"account_id": "checking", "name": "Household checking", "type": "depository", "subtype": "checking", "balance": 32000.0},
            {"account_id": "emergency", "name": "Emergency reserve", "type": "depository", "subtype": "savings", "balance": 64500.0},
            {"account_id": "visa", "name": "Rewards Visa", "type": "credit", "subtype": "credit card", "balance": 12600.0},
            {"account_id": "mortgage", "name": "Primary mortgage", "type": "loan", "subtype": "mortgage", "balance": 451000.0},
        ]

    def get_transactions(self) -> list[dict[str, Any]]:
        return [
            {"transaction_id": "txn-001", "date": "2026-06-10", "merchant_name": "Whole Foods Market", "category": "Groceries", "amount": 186.0},
            {"transaction_id": "txn-002", "date": "2026-06-09", "merchant_name": "Streaming bundle", "category": "Subscriptions", "amount": 76.0},
        ]

    def get_liabilities(self) -> list[dict[str, Any]]:
        return [
            {"account_id": "visa", "apr": 0.2199, "minimum_payment": 525.0},
            {"account_id": "mortgage", "apr": 0.0675, "monthly_payment": 3825.0, "property_value": 735000.0},
        ]

    def get_investments(self) -> list[dict[str, Any]]:
        return [
            {"account_id": "401k", "name": "Jordan 401(k)", "balance": 174000.0, "monthly_contribution": 1150.0},
            {"account_id": "brokerage", "name": "Family brokerage", "balance": 44000.0, "monthly_contribution": 0.0},
        ]


class PlaidSandboxProvider(PlaidProvider):
    """Placeholder for server-side Plaid SDK calls and token vault integration."""

    def _not_configured(self) -> None:
        raise RuntimeError("Plaid Sandbox is not configured in V2.")

    def create_link_token(self, user_id: str) -> dict[str, Any]:
        self._not_configured()

    def exchange_public_token(self, public_token: str) -> dict[str, Any]:
        self._not_configured()

    def get_accounts(self) -> list[dict[str, Any]]:
        self._not_configured()

    def get_transactions(self) -> list[dict[str, Any]]:
        self._not_configured()

    def get_liabilities(self) -> list[dict[str, Any]]:
        self._not_configured()

    def get_investments(self) -> list[dict[str, Any]]:
        self._not_configured()
