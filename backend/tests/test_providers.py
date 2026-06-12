from __future__ import annotations

import json
import unittest

from backend.normalizers.financial_data import FinancialDataNormalizer
from backend.api.routes import BackendApi
from backend.providers.llm import (
    AnthropicLlmProvider,
    AnthropicResponseParser,
    ClaudeModel,
    LlmTask,
    MissionPromptBuilder,
    MockLlmProvider,
)
from backend.providers.plaid import MockPlaidProvider
from backend.services.provider_service import ProviderService


class MockLlmProviderTest(unittest.TestCase):
    def setUp(self) -> None:
        self.context = {
            "missionId": "mission-home",
            "title": "Buy Home",
            "readinessScore": 64,
            "targetDate": "2027-08-01",
            "nextActionTitle": "Pay off $3,000 credit card debt",
            "nextActionReadinessGain": 5,
            "blockers": ["Debt utilization is above 10%."],
            "blockedActions": [{"title": "Improve mortgage readiness"}],
            "readinessFactors": [
                {"title": "Debt readiness", "score": 48},
                {"title": "Savings readiness", "score": 71},
            ],
        }

    def test_explanation_preserves_structured_value(self) -> None:
        response = MockLlmProvider().generate_financial_explanation(
            {"difference": 42500}
        )
        self.assertIn("$42,500", response.text)
        self.assertEqual(ClaudeModel.SONNET, response.model)

    def test_implements_every_mission_explanation_method(self) -> None:
        provider = MockLlmProvider()
        responses = [
            provider.explain_mission(self.context),
            provider.explain_readiness(self.context),
            provider.explain_blockers(self.context),
            provider.explain_timeline(self.context),
            provider.explain_recommendation(self.context),
            provider.answer_mission_question("What should I do?", self.context),
        ]

        self.assertTrue(all(response.text for response in responses))
        self.assertTrue(all(response.provider == "mock" for response in responses))
        self.assertIn("64%", responses[0].text)
        self.assertIn("$3,000", responses[0].text)


class MissionPromptBuilderTest(unittest.TestCase):
    def test_serializes_immutable_engine_facts_and_guardrails(self) -> None:
        prompt = MissionPromptBuilder().build(
            LlmTask.READINESS_EXPLANATION,
            {"readinessScore": 64, "balance": 3000.25},
        )
        payload = json.loads(prompt["messages"][0]["content"])

        self.assertEqual(64, payload["mission_context"]["readinessScore"])
        self.assertEqual(3000.25, payload["mission_context"]["balance"])
        self.assertIn("Never calculate", prompt["system"])
        self.assertIn("Preserve every supplied number exactly.", payload["response_rules"])


class AnthropicRequestGenerationTest(unittest.TestCase):
    def test_routes_mission_question_to_opus_without_transport(self) -> None:
        request = AnthropicLlmProvider().build_request(
            LlmTask.MISSION_QUESTION,
            {"riskScore": 61, "recommendation": "Wait"},
            question="Can I afford this home?",
        )
        self.assertEqual(ClaudeModel.OPUS.value, request["model"])
        content = json.loads(request["messages"][0]["content"])
        self.assertEqual(
            "Can I afford this home?",
            content["question"],
        )
        self.assertIn("Never calculate", request["system"])

    def test_routes_summary_to_haiku(self) -> None:
        request = AnthropicLlmProvider().build_request(
            LlmTask.QUICK_SUMMARY, {"insights": []}
        )
        self.assertEqual(ClaudeModel.HAIKU.value, request["model"])

    def test_routes_mission_explanations_to_sonnet(self) -> None:
        request = AnthropicLlmProvider().build_request(
            LlmTask.MISSION_EXPLANATION,
            {"missionId": "mission-home"},
        )
        self.assertEqual(ClaudeModel.SONNET.value, request["model"])

    def test_invokes_messages_api_with_backend_key_and_parses_response(self) -> None:
        captured: dict[str, object] = {}

        def transport(url, headers, body):
            captured.update(url=url, headers=headers, body=body)
            return {"content": [{"type": "text", "text": "Grounded explanation."}]}

        provider = AnthropicLlmProvider(api_key="backend-secret", transport=transport)
        response = provider.explain_mission({"missionId": "mission-home"})

        self.assertEqual("Grounded explanation.", response.text)
        self.assertEqual("backend-secret", captured["headers"]["x-api-key"])
        self.assertNotIn("backend-secret", json.dumps(captured["body"]))

    def test_requires_backend_api_key_before_transport(self) -> None:
        provider = AnthropicLlmProvider(api_key="")
        provider._api_key = None
        with self.assertRaises(RuntimeError):
            provider.explain_mission({"missionId": "mission-home"})


class AnthropicResponseParserTest(unittest.TestCase):
    def test_combines_text_blocks(self) -> None:
        response = AnthropicResponseParser().parse(
            {
                "content": [
                    {"type": "text", "text": "First."},
                    {"type": "tool_use", "name": "ignored"},
                    {"type": "text", "text": "Second."},
                ]
            },
            ClaudeModel.SONNET,
        )
        self.assertEqual("First.\nSecond.", response.text)

    def test_rejects_malformed_response(self) -> None:
        with self.assertRaises(ValueError):
            AnthropicResponseParser().parse({"content": []}, ClaudeModel.SONNET)


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
