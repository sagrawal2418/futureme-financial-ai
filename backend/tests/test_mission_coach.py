from __future__ import annotations

import unittest

from backend.api.routes import BackendApi
from backend.providers.llm import LlmProvider, MockLlmProvider
from backend.providers.plaid import MockPlaidProvider
from backend.services.mission_coach import MissionCoachService
from backend.services.provider_service import ProviderService


def mission_context(readiness: int = 64) -> dict[str, object]:
    return {
        "missionId": "mission-home",
        "title": "Buy Home",
        "readinessScore": readiness,
        "targetDate": "2027-08-01",
        "nextActionTitle": "Pay off $3,000 credit card debt",
        "nextActionReadinessGain": 5,
        "topOpportunity": "Eliminate revolving interest.",
        "blockers": ["Debt utilization is above 10%."],
        "blockedActions": [{"title": "Improve mortgage readiness"}],
        "unlockedActions": [
            {"title": "Pay off $3,000 credit card debt"},
            {"title": "Save another $500 per month"},
        ],
        "readinessFactors": [{"title": "Debt readiness", "score": 48}],
    }


class FailingLlmProvider(LlmProvider):
    def _fail(self, *args, **kwargs):
        raise RuntimeError("Claude is unavailable")

    explain_mission = _fail
    explain_readiness = _fail
    explain_blockers = _fail
    explain_timeline = _fail
    explain_recommendation = _fail
    answer_mission_question = _fail


class MissionCoachServiceTest(unittest.TestCase):
    def test_builds_complete_briefing_and_tracks_changes(self) -> None:
        service = MissionCoachService(MockLlmProvider())

        first = service.coach(mission_context())
        second = service.coach(mission_context(69))
        history = service.history("mission-home")

        self.assertEqual("mission-home", first.mission_id)
        self.assertIn("64%", first.coaching_summary)
        self.assertEqual(2, len(first.suggested_actions))
        self.assertFalse(first.used_fallback)
        self.assertIsNotNone(history)
        self.assertEqual(second, history.latest)
        self.assertEqual(first, history.previous)
        self.assertTrue(history.what_changed)

    def test_falls_back_when_primary_provider_fails(self) -> None:
        service = MissionCoachService(FailingLlmProvider(), MockLlmProvider())

        briefing = service.coach(mission_context())
        answer = service.answer("What should I do next?", mission_context())

        self.assertTrue(briefing.used_fallback)
        self.assertEqual("mock", briefing.provider)
        self.assertEqual("mock", answer.provider)

    def test_route_facade_exposes_coaching_question_and_history(self) -> None:
        api = BackendApi(
            ProviderService(MockPlaidProvider(), MockLlmProvider())
        )

        briefing = api.post_mission_coaching(mission_context())
        answer = api.post_mission_question(
            "What should I do next?",
            mission_context(),
        )
        history = api.get_mission_explanation_history("mission-home")

        self.assertEqual("mission-home", briefing["missionId"])
        self.assertIn("answer", answer)
        self.assertIsNotNone(history)


if __name__ == "__main__":
    unittest.main()
