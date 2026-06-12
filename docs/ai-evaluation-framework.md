# AI Evaluation Framework

## Purpose

FutureMe evaluates whether Claude explains deterministic financial results accurately, consistently, safely, and usefully. The language model is not graded on recalculating financial values because it is prohibited from doing so.

## Benchmark

The canonical benchmark is [evaluation/ai-evaluation-prompts.json](../evaluation/ai-evaluation-prompts.json).

It contains 50 prompts across:

- Mission readiness: 8
- Home purchase: 7
- Child readiness: 7
- Debt reduction: 7
- Retirement: 7
- Relocation: 7
- Risk analysis: 7

Every case includes:

- A realistic customer prompt
- The mission context to use
- Expected reasoning
- Expected response qualities
- Explicit pass/fail criteria
- Facts that must be preserved

## Metrics

### Response Quality

Checks that the response is present, concise enough for a consumer product, and structurally readable.

### Reasoning Quality

Checks that required supplied facts are preserved and the answer follows the expected decision logic.

### Consistency

Compares repeated answers, with particular weight on whether numeric claims remain identical.

### Hallucination Risk

Detects numbers in the answer that were not supplied by the deterministic engine or evaluation case.

### Explanation Usefulness

Checks for a next action, an explanation of why it matters, and appropriate uncertainty language.

## Pass Criteria

Each metric must score at least 70. Missing required facts or unsupported numeric claims are recorded as explicit failures.

The deterministic evaluator is intentionally conservative. Human review remains required for tone, fairness, and nuanced suitability.

## Running The Benchmark

Offline with the local mock provider:

```bash
python3 -m backend.evaluate_claude_quality
```

Live against Claude:

```bash
python3 -m backend.evaluate_claude_quality --live --output build/claude-quality.json
```

Consistency mode calls every prompt twice:

```bash
python3 -m backend.evaluate_claude_quality --live --repeat
```

Live mode sends 50 prompts, or 100 with `--repeat`, and therefore consumes Anthropic API credits.

## Release Gate

Before a production model or prompt change:

1. Provider and prompt-builder tests pass.
2. The 22-case integration suite passes.
3. The 50-prompt quality benchmark meets thresholds.
4. No required numeric fact is altered.
5. No high-severity unsupported claim is accepted.
6. Compliance reviews changed recommendation language.
7. A human reviews a stratified response sample.

## Dashboard

The Coach experience exposes benchmark readiness and category coverage. Scores remain pending until a live report is produced; the product must not present seeded or invented scores as observed model performance.
