"""Normalizes Plaid-shaped records into FutureMe shared-contract dictionaries."""

from __future__ import annotations

from typing import Any


class FinancialDataNormalizer:
    def normalize_accounts(
        self,
        accounts: list[dict[str, Any]],
        liabilities: list[dict[str, Any]],
        investments: list[dict[str, Any]],
    ) -> dict[str, list[dict[str, Any]]]:
        liability_by_id = {item["account_id"]: item for item in liabilities}
        cash_accounts: list[dict[str, Any]] = []
        debt_accounts: list[dict[str, Any]] = []
        mortgage_accounts: list[dict[str, Any]] = []

        for account in accounts:
            account_id = account["account_id"]
            if account["type"] == "depository":
                cash_accounts.append(
                    {
                        "id": account_id,
                        "name": account["name"],
                        "balance": float(account["balance"]),
                        "annualPercentageYield": 0.0,
                        "isEmergencyFund": account["subtype"] == "savings",
                    }
                )
            elif account["subtype"] == "mortgage":
                liability = liability_by_id.get(account_id, {})
                mortgage_accounts.append(
                    {
                        "id": account_id,
                        "name": account["name"],
                        "balance": float(account["balance"]),
                        "annualPercentageRate": float(liability.get("apr", 0)),
                        "monthlyPayment": float(liability.get("monthly_payment", 0)),
                        "propertyValue": float(liability.get("property_value", 0)),
                        "remainingTermMonths": int(liability.get("remaining_term_months", 0)),
                    }
                )
            elif account["type"] in {"credit", "loan"}:
                liability = liability_by_id.get(account_id, {})
                debt_accounts.append(
                    {
                        "id": account_id,
                        "name": account["name"],
                        "balance": float(account["balance"]),
                        "annualPercentageRate": float(liability.get("apr", 0)),
                        "minimumMonthlyPayment": float(liability.get("minimum_payment", 0)),
                        "category": account["subtype"],
                    }
                )

        investment_accounts = [
            {
                "id": item["account_id"],
                "name": item["name"],
                "balance": float(item["balance"]),
                "monthlyContribution": float(item.get("monthly_contribution", 0)),
            }
            for item in investments
        ]
        return {
            "cashAccounts": cash_accounts,
            "debtAccounts": debt_accounts,
            "mortgageAccounts": mortgage_accounts,
            "investmentAccounts": investment_accounts,
        }

    def normalize_transactions(
        self, transactions: list[dict[str, Any]]
    ) -> list[dict[str, Any]]:
        return [
            {
                "id": item["transaction_id"],
                "postedDate": item["date"],
                "merchant": item.get("merchant_name") or "Unknown merchant",
                "category": item.get("category") or "Other",
                "amount": float(item["amount"]),
                "isRecurring": bool(item.get("is_recurring", False)),
            }
            for item in transactions
        ]

    def build_financial_profile(
        self,
        owner_id: str,
        normalized: dict[str, list[dict[str, Any]]],
        monthly_net_income: float,
        monthly_living_expenses: float,
    ) -> dict[str, Any]:
        cash = sum(item["balance"] for item in normalized["cashAccounts"])
        investments = sum(item["balance"] for item in normalized["investmentAccounts"])
        debt = sum(item["balance"] for item in normalized["debtAccounts"])
        mortgages = normalized["mortgageAccounts"]
        return {
            "profileId": f"normalized-{owner_id}",
            "ownerId": owner_id,
            "monthlyNetIncome": monthly_net_income,
            "monthlyLivingExpenses": monthly_living_expenses,
            "liquidSavings": cash,
            "creditCardDebt": debt,
            "investmentBalance": investments,
            "mortgageBalance": sum(item["balance"] for item in mortgages),
            "propertyValue": sum(item["propertyValue"] for item in mortgages),
        }
