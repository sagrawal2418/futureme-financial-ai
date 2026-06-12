from __future__ import annotations

import unittest

from backend.services.ai_evaluation import (
    AiEvaluationEngine,
    load_evaluation_cases,
)


class AiEvaluationEngineTest(unittest.TestCase):
    def test_benchmark_contains_fifty_prompts_across_seven_categories(self) -> None:
        cases = load_evaluation_cases()

        self.assertEqual(50, len(cases))
        self.assertEqual(7, len({case.category for case in cases}))
        self.assertTrue(all(case.expected_reasoning for case in cases))
        self.assertTrue(all(case.pass_criteria for case in cases))

    def test_scores_grounded_actionable_response_as_pass(self) -> None:
        case = load_evaluation_cases()[0]
        response = (
            "Home readiness is 68. Focus first on the supplied $3,000 card payoff "
            "because debt utilization above 10% is the current blocker. This modeled "
            "action may improve readiness without assuming a mortgage rate."
        )

        result = AiEvaluationEngine().evaluate(case, response, response)

        self.assertTrue(result.passed)
        self.assertEqual(100, result.hallucination_risk_score)
        self.assertGreaterEqual(result.explanation_usefulness_score, 70)

    def test_flags_unsupported_numeric_claims_and_missing_facts(self) -> None:
        case = load_evaluation_cases()[0]
        response = (
            "You are 75% ready and should borrow at 4.5% because approval is likely."
        )

        result = AiEvaluationEngine().evaluate(case, response)

        self.assertFalse(result.passed)
        self.assertLess(result.hallucination_risk_score, 70)
        self.assertTrue(any("Missing required facts" in item for item in result.failures))


if __name__ == "__main__":
    unittest.main()
