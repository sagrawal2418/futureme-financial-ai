"""Deterministic quality scoring for FutureMe AI explanations."""

from __future__ import annotations

import json
import re
from dataclasses import asdict, dataclass
from pathlib import Path
from typing import Iterable


BENCHMARK_PATH = (
    Path(__file__).resolve().parents[2]
    / "evaluation"
    / "ai-evaluation-prompts.json"
)
NUMBER_PATTERN = re.compile(r"(?:\$?\d[\d,]*(?:\.\d+)?%?)")
ACTION_WORDS = {
    "focus",
    "pay",
    "save",
    "build",
    "confirm",
    "review",
    "reduce",
    "increase",
    "protect",
    "prioritize",
}
EXPLANATION_WORDS = {"because", "so", "means", "therefore", "which"}
UNCERTAINTY_WORDS = {"modeled", "estimate", "may", "could", "not supplied", "unknown"}


@dataclass(frozen=True)
class AiEvaluationCase:
    id: str
    category: str
    mission_id: str
    prompt: str
    sample_input: str
    expected_reasoning: list[str]
    expected_response_quality: list[str]
    pass_criteria: list[str]
    required_facts: list[str]


@dataclass(frozen=True)
class AiEvaluationResult:
    case_id: str
    category: str
    response_quality_score: int
    reasoning_quality_score: int
    consistency_score: int
    hallucination_risk_score: int
    explanation_usefulness_score: int
    passed: bool
    failures: list[str]


def load_evaluation_cases(path: Path = BENCHMARK_PATH) -> list[AiEvaluationCase]:
    payload = json.loads(path.read_text(encoding="utf-8"))
    return [
        AiEvaluationCase(
            id=item["id"],
            category=item["category"],
            mission_id=item["missionId"],
            prompt=item["prompt"],
            sample_input=item["sampleInput"],
            expected_reasoning=item["expectedReasoning"],
            expected_response_quality=item["expectedResponseQuality"],
            pass_criteria=item["passCriteria"],
            required_facts=item["requiredFacts"],
        )
        for item in payload
    ]


class AiEvaluationEngine:
    """Scores responses without using a second model as a judge."""

    def evaluate(
        self,
        case: AiEvaluationCase,
        response: str,
        repeated_response: str | None = None,
    ) -> AiEvaluationResult:
        normalized = response.strip()
        quality = self._response_quality(normalized)
        reasoning, missing_facts = self._reasoning_quality(case, normalized)
        consistency = self._consistency(normalized, repeated_response)
        hallucination, unsupported_numbers = self._hallucination_risk(case, normalized)
        usefulness = self._usefulness(normalized)
        failures = []
        if missing_facts:
            failures.append("Missing required facts: " + ", ".join(missing_facts))
        if unsupported_numbers:
            failures.append(
                "Unsupported numeric claims: " + ", ".join(unsupported_numbers)
            )
        thresholds = {
            "response quality": quality,
            "reasoning quality": reasoning,
            "consistency": consistency,
            "hallucination risk": hallucination,
            "explanation usefulness": usefulness,
        }
        failures.extend(
            f"{label} scored {score}, below 70"
            for label, score in thresholds.items()
            if score < 70
        )
        return AiEvaluationResult(
            case_id=case.id,
            category=case.category,
            response_quality_score=quality,
            reasoning_quality_score=reasoning,
            consistency_score=consistency,
            hallucination_risk_score=hallucination,
            explanation_usefulness_score=usefulness,
            passed=not failures,
            failures=failures,
        )

    def dashboard(self, results: Iterable[AiEvaluationResult]) -> dict[str, object]:
        records = list(results)
        grouped: dict[str, list[AiEvaluationResult]] = {}
        for result in records:
            grouped.setdefault(result.category, []).append(result)
        return {
            "totalPrompts": len(records),
            "passed": sum(result.passed for result in records),
            "failed": sum(not result.passed for result in records),
            "responseQualityScore": self._average(
                result.response_quality_score for result in records
            ),
            "reasoningQualityScore": self._average(
                result.reasoning_quality_score for result in records
            ),
            "consistencyScore": self._average(
                result.consistency_score for result in records
            ),
            "hallucinationRiskScore": self._average(
                result.hallucination_risk_score for result in records
            ),
            "explanationUsefulnessScore": self._average(
                result.explanation_usefulness_score for result in records
            ),
            "categories": {
                category: {
                    "promptCount": len(category_results),
                    "passRate": round(
                        100
                        * sum(result.passed for result in category_results)
                        / len(category_results)
                    ),
                }
                for category, category_results in sorted(grouped.items())
            },
            "results": [asdict(result) for result in records],
        }

    def _response_quality(self, response: str) -> int:
        if not response:
            return 0
        score = 100
        if len(response) < 80:
            score -= 25
        if len(response) > 1_200:
            score -= 15
        if response.count("\n") > 8:
            score -= 10
        if not any(mark in response for mark in ".!?"):
            score -= 15
        return max(0, score)

    def _reasoning_quality(
        self,
        case: AiEvaluationCase,
        response: str,
    ) -> tuple[int, list[str]]:
        missing = [fact for fact in case.required_facts if fact not in response]
        if not case.required_facts:
            return (85 if response else 0), []
        preserved = len(case.required_facts) - len(missing)
        score = round(55 + 45 * preserved / len(case.required_facts))
        return score, missing

    def _consistency(self, response: str, repeated_response: str | None) -> int:
        if repeated_response is None:
            return 100
        first_numbers = set(NUMBER_PATTERN.findall(response))
        second_numbers = set(NUMBER_PATTERN.findall(repeated_response))
        numeric_score = 70 if first_numbers == second_numbers else 20
        first_words = set(response.lower().split())
        second_words = set(repeated_response.lower().split())
        union = first_words | second_words
        overlap = len(first_words & second_words) / len(union) if union else 1.0
        return round(numeric_score + 30 * overlap)

    def _hallucination_risk(
        self,
        case: AiEvaluationCase,
        response: str,
    ) -> tuple[int, list[str]]:
        allowed = set(NUMBER_PATTERN.findall(case.sample_input))
        allowed.update(
            fact
            for fact in case.required_facts
            if NUMBER_PATTERN.fullmatch(fact)
        )
        response_numbers = set(NUMBER_PATTERN.findall(response))
        unsupported = sorted(response_numbers - allowed)
        return max(0, 100 - 20 * len(unsupported)), unsupported

    def _usefulness(self, response: str) -> int:
        lowered = response.lower()
        score = 40
        if any(word in lowered for word in ACTION_WORDS):
            score += 25
        if any(word in lowered for word in EXPLANATION_WORDS):
            score += 20
        if any(word in lowered for word in UNCERTAINTY_WORDS):
            score += 15
        return min(100, score)

    def _average(self, values: Iterable[int]) -> int:
        records = list(values)
        return round(sum(records) / len(records)) if records else 0
