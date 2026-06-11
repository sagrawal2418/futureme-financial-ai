from __future__ import annotations

import unittest

from backend.normalizers.financial_data import FinancialDataNormalizer
from backend.api.routes import BackendApi
from backend.providers.llm import (
    AnthropicLlmProvider,
    ClaudeModel,
    LlmTask,
    MockLlmProvider,
)
from backend.providers.plaid import MockPlaidProvider
from backend.services.provider_service import ProviderService


class MockLlmProviderTest(unittest.TestCase):
    def test_explanation_preserves_structured_value(self) -> None:
        response = MockLlmProvider().generate_financial_explanation(
            {"difference": 42500}
        )
        self.assertIn("$42,500", response.text)
        self.assertEqual(ClaudeModel.SONNET, response.model)


class AnthropicRequestGenerationTest(unittest.TestCase):
    def test_routes_complex_scenario_to_opus_without_transport(self) -> None:
        request = AnthropicLlmProvider().build_request(
            LlmTask.SCENARIO_ANSWER,
            {"riskScore": 61, "recommendation": "Wait"},
            question="Can I afford this home?",
        )
        self.assertEqual(ClaudeModel.OPUS.value, request["model"])
        self.assertEqual(
            "Can I afford this home?",
            request["messages"][0]["content"]["question"],
        )
        self.assertIn("Never calculate", request["system"])

    def test_routes_summary_to_haiku(self) -> None:
        request = AnthropicLlmProvider().build_request(
            LlmTask.INSIGHT_SUMMARY, {"insights": []}
        )
        self.assertEqual(ClaudeModel.HAIKU.value, request["model"])


class MockPlaidProviderTest(unittest.TestCase):
    def test_returns_mock_tokens_and_financial_records(self) -> None:
        provider = MockPlaidProvider()
        self.assertTrue(provider.create_link_token("demo")["link_token"].startswith("link-sandbox"))
        self.assertGreaterEqual(len(provider.get_accounts()), 4)
        self.assertGreaterEqual(len(provider.get_transactions()), 2)
        self.assertNotIn("access_token", provider.exchange_public_token("public-token"))


class FinancialDataNormalizerTest(unittest.TestCase):
    def test_normalizes_plaid_shapes_into_shared_contract_shapes(self) -> None:
        provider = MockPlaidProvider()
        normalizer = FinancialDataNormalizer()
        normalized = normalizer.normalize_accounts(
            provider.get_accounts(),
            provider.get_liabilities(),
            provider.get_investments(),
        )
        transactions = normalizer.normalize_transactions(provider.get_transactions())
        profile = normalizer.build_financial_profile(
            "demo", normalized, 14250.0, 5050.0
        )

        self.assertEqual(96500.0, profile["liquidSavings"])
        self.assertEqual(451000.0, profile["mortgageBalance"])
        self.assertEqual("Whole Foods Market", transactions[0]["merchant"])
        self.assertIn("investmentAccounts", normalized)


class BackendApiTest(unittest.TestCase):
    def test_plaid_routes_delegate_to_backend_provider(self) -> None:
        api = BackendApi(ProviderService(MockPlaidProvider(), MockLlmProvider()))
        self.assertIn("link_token", api.post_plaid_link_token("demo"))
        self.assertGreaterEqual(len(api.get_plaid_accounts()), 4)
        self.assertGreaterEqual(len(api.get_plaid_transactions()), 2)


if __name__ == "__main__":
    unittest.main()
