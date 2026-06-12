"""Live Claude validation for every FutureMe mission and decision scenario."""

from __future__ import annotations

import os
import platform
import subprocess
import sys
import time
from dataclasses import dataclass
from typing import Callable

from backend.providers.llm import (
    AnthropicApiError,
    AnthropicLlmProvider,
    ClaudeModel,
    LlmResponse,
)


@dataclass(frozen=True)
class ValidationCase:
    name: str
    expected_model: ClaudeModel
    invoke: Callable[[], LlmResponse]
    required_text: str | None = None


MISSION_CONTEXTS = [
    {
        "missionId": "mission-home",
        "title": "Buy a Home",
        "readinessScore": 68,
        "targetDate": "2027-09-01",
        "nextActionTitle": "Pay off $3,000 of credit card debt",
        "nextActionReadinessGain": 5,
        "blockers": ["Debt utilization is above 10%."],
        "blockedActions": [{"title": "Improve mortgage readiness"}],
        "unlockedActions": [{"title": "Pay off $3,000 of credit card debt"}],
        "readinessFactors": [{"title": "Debt readiness", "score": 35}],
    },
    {
        "missionId": "mission-child",
        "title": "Have a Child",
        "readinessScore": 78,
        "targetDate": "2027-06-01",
        "nextActionTitle": "Build a $15,000 family transition fund",
        "nextActionReadinessGain": 7,
        "blockers": ["Childcare and leave costs need dedicated reserves."],
        "blockedActions": [{"title": "Confirm the reduced-income plan"}],
        "unlockedActions": [{"title": "Build a $15,000 family transition fund"}],
        "readinessFactors": [{"title": "Cash-flow readiness", "score": 63}],
    },
    {
        "missionId": "mission-relocation",
        "title": "Relocate",
        "readinessScore": 77,
        "targetDate": "2027-03-01",
        "nextActionTitle": "Confirm compensation and housing costs",
        "nextActionReadinessGain": 6,
        "blockers": ["The post-move housing budget is not confirmed."],
        "blockedActions": [{"title": "Commit to the moving date"}],
        "unlockedActions": [{"title": "Confirm compensation and housing costs"}],
        "readinessFactors": [{"title": "Planning readiness", "score": 59}],
    },
    {
        "missionId": "mission-retirement",
        "title": "Retire Early",
        "readinessScore": 67,
        "targetDate": "2048-01-01",
        "nextActionTitle": "Capture the full employer match",
        "nextActionReadinessGain": 9,
        "blockers": ["The current contribution rate misses part of the employer match."],
        "blockedActions": [{"title": "Advance the retirement date"}],
        "unlockedActions": [{"title": "Capture the full employer match"}],
        "readinessFactors": [{"title": "Investment readiness", "score": 52}],
    },
    {
        "missionId": "mission-debt-free",
        "title": "Become Debt Free",
        "readinessScore": 80,
        "targetDate": "2027-01-01",
        "nextActionTitle": "Eliminate the highest-rate balance",
        "nextActionReadinessGain": 8,
        "blockers": ["High-interest revolving debt is still active."],
        "blockedActions": [{"title": "Redirect debt payments to investing"}],
        "unlockedActions": [{"title": "Eliminate the highest-rate balance"}],
        "readinessFactors": [{"title": "Debt readiness", "score": 60}],
    },
    {
        "missionId": "mission-emergency",
        "title": "Build Emergency Fund",
        "readinessScore": 73,
        "targetDate": "2027-02-01",
        "nextActionTitle": "Reach the next full month of runway",
        "nextActionReadinessGain": 6,
        "blockers": ["Emergency reserves remain below the six-month target."],
        "blockedActions": [{"title": "Move surplus cash to investing"}],
        "unlockedActions": [{"title": "Reach the next full month of runway"}],
        "readinessFactors": [{"title": "Emergency-fund readiness", "score": 58}],
    },
    {
        "missionId": "mission-parents",
        "title": "Support Parents",
        "readinessScore": 77,
        "targetDate": "2027-07-01",
        "nextActionTitle": "Fund the first quarter of parent support",
        "nextActionReadinessGain": 7,
        "blockers": ["The recurring support amount needs a protected reserve."],
        "blockedActions": [{"title": "Begin recurring parent support"}],
        "unlockedActions": [{"title": "Fund the first quarter of parent support"}],
        "readinessFactors": [{"title": "Liquidity readiness", "score": 61}],
    },
    {
        "missionId": "mission-business",
        "title": "Start a Business",
        "readinessScore": 61,
        "targetDate": "2028-01-01",
        "nextActionTitle": "Separate startup capital from household reserves",
        "nextActionReadinessGain": 8,
        "blockers": ["Startup capital would currently reduce household emergency reserves."],
        "blockedActions": [{"title": "Leave salaried employment"}],
        "unlockedActions": [{"title": "Separate startup capital from household reserves"}],
        "readinessFactors": [{"title": "Liquidity readiness", "score": 42}],
    },
]

SCENARIO_QUESTIONS = [
    ("Buy a $700K home", "mission-home", "Can I buy a $700,000 home now?"),
    ("Refinance mortgage", "mission-home", "What changes if I refinance my mortgage?"),
    ("Pay off debt", "mission-debt-free", "What happens if I pay off debt first?"),
    ("Lose my job", "mission-emergency", "How does losing my job affect this mission?"),
    (
        "Spouse stops working",
        "mission-child",
        "What happens if my spouse stops working?",
    ),
    ("Move to another state", "mission-relocation", "Can we afford to move to Texas?"),
    ("Have another child", "mission-child", "Are we ready for another child?"),
    ("Start a business", "mission-business", "Can I realistically start this business?"),
    (
        "Increase investments",
        "mission-retirement",
        "What changes if I increase retirement investments?",
    ),
]


def key_from_environment() -> str | None:
    key = os.getenv("ANTHROPIC_API_KEY")
    if key:
        return key
    if platform.system() != "Darwin":
        return None
    result = subprocess.run(
        ["launchctl", "getenv", "ANTHROPIC_API_KEY"],
        capture_output=True,
        check=False,
        text=True,
    )
    return result.stdout.strip() or None


def build_cases(provider: AnthropicLlmProvider) -> list[ValidationCase]:
    contexts = {context["missionId"]: context for context in MISSION_CONTEXTS}
    cases = [
        ValidationCase(
            name=f"Mission explanation: {context['title']}",
            expected_model=ClaudeModel.SONNET,
            invoke=lambda context=context: provider.explain_mission(context),
            required_text=str(context["readinessScore"]),
        )
        for context in MISSION_CONTEXTS
    ]
    home = contexts["mission-home"]
    cases.extend(
        [
            ValidationCase(
                "Readiness explanation",
                ClaudeModel.SONNET,
                lambda: provider.explain_readiness(home),
                str(home["readinessScore"]),
            ),
            ValidationCase(
                "Blocker explanation",
                ClaudeModel.SONNET,
                lambda: provider.explain_blockers(home),
            ),
            ValidationCase(
                "Timeline explanation",
                ClaudeModel.SONNET,
                lambda: provider.explain_timeline(home),
            ),
            ValidationCase(
                "Recommendation explanation",
                ClaudeModel.SONNET,
                lambda: provider.explain_recommendation(home),
            ),
        ]
    )
    cases.extend(
        ValidationCase(
            name=f"Scenario question: {name}",
            expected_model=ClaudeModel.OPUS,
            invoke=lambda question=question, context=contexts[mission_id]: (
                provider.answer_mission_question(question, context)
            ),
        )
        for name, mission_id, question in SCENARIO_QUESTIONS
    )
    cases.append(
        ValidationCase(
            name="Quick insight summary",
            expected_model=ClaudeModel.HAIKU,
            invoke=lambda: provider.generate_insight_summary(
                [
                    {"title": "Pay off high-interest debt"},
                    {"title": "Capture the full employer match"},
                    {"title": "Increase emergency reserves"},
                ]
            ),
        )
    )
    return cases


def invoke_with_retry(case: ValidationCase) -> LlmResponse:
    for attempt in range(1, 4):
        try:
            return case.invoke()
        except AnthropicApiError as error:
            if error.status_code not in (429, 529) or attempt == 3:
                raise
            time.sleep(attempt * 2)
    raise AssertionError("Retry loop ended unexpectedly.")


def main() -> int:
    api_key = key_from_environment()
    if not api_key:
        print(
            "ANTHROPIC_API_KEY is not available to this process. "
            "Use the secure launchctl handoff described by Codex.",
            file=sys.stderr,
        )
        return 2

    cases = build_cases(AnthropicLlmProvider(api_key=api_key))
    failures: list[str] = []
    print(f"Running {len(cases)} live Claude validations...")
    for index, case in enumerate(cases, start=1):
        try:
            response = invoke_with_retry(case)
            if response.provider != "anthropic":
                raise AssertionError(f"provider was {response.provider}")
            if response.model != case.expected_model:
                raise AssertionError(
                    f"model was {response.model.value}, expected {case.expected_model.value}"
                )
            if not response.text.strip():
                raise AssertionError("response text was empty")
            if case.required_text and case.required_text not in response.text:
                raise AssertionError(
                    f"response did not preserve required value {case.required_text}"
                )
            print(
                f"[{index:02}/{len(cases)}] PASS {case.name} "
                f"({response.model.value})"
            )
        except (AnthropicApiError, AssertionError, OSError, ValueError) as error:
            failures.append(f"{case.name}: {error}")
            print(f"[{index:02}/{len(cases)}] FAIL {case.name}: {error}")

    if failures:
        print(f"\n{len(failures)} validation(s) failed:")
        for failure in failures:
            print(f"- {failure}")
        return 1

    print("\nAll live Claude mission and scenario validations passed.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
