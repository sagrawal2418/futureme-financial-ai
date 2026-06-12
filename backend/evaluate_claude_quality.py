"""Run the 50-prompt FutureMe AI benchmark with mock or live Claude."""

from __future__ import annotations

import argparse
import json
import sys

from backend.providers.llm import AnthropicLlmProvider, MockLlmProvider
from backend.services.ai_evaluation import AiEvaluationEngine, load_evaluation_cases
from backend.validate_claude_live import MISSION_CONTEXTS, key_from_environment


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--live",
        action="store_true",
        help="Use the Anthropic API. This sends all 50 benchmark prompts.",
    )
    parser.add_argument(
        "--repeat",
        action="store_true",
        help="Call each prompt twice to measure response consistency.",
    )
    parser.add_argument("--output", help="Optional JSON report path.")
    args = parser.parse_args()

    if args.live:
        api_key = key_from_environment()
        if not api_key:
            print("ANTHROPIC_API_KEY is not available.", file=sys.stderr)
            return 2
        provider = AnthropicLlmProvider(api_key=api_key)
    else:
        provider = MockLlmProvider()

    contexts = {context["missionId"]: context for context in MISSION_CONTEXTS}
    engine = AiEvaluationEngine()
    results = []
    cases = load_evaluation_cases()
    for index, case in enumerate(cases, start=1):
        context = contexts[case.mission_id]
        response = provider.answer_mission_question(case.prompt, context).text
        repeated = (
            provider.answer_mission_question(case.prompt, context).text
            if args.repeat
            else None
        )
        result = engine.evaluate(case, response, repeated)
        results.append(result)
        print(
            f"[{index:02}/{len(cases)}] "
            f"{'PASS' if result.passed else 'FAIL'} "
            f"{case.category}: {case.prompt}"
        )

    report = engine.dashboard(results)
    rendered = json.dumps(report, indent=2)
    if args.output:
        from pathlib import Path

        Path(args.output).write_text(rendered + "\n", encoding="utf-8")
    print(rendered)
    return 0 if report["failed"] == 0 else 1


if __name__ == "__main__":
    raise SystemExit(main())
