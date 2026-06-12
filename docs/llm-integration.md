# LLM Integration

## Backend Boundary

Only the backend owns `ANTHROPIC_API_KEY`. Android, iOS, web, Kotlin/JS output, local storage, analytics events, and GitHub Pages contain no provider credential.

`AnthropicLlmProvider` sends a Messages API request with:

- `x-api-key`
- `anthropic-version: 2023-06-01`
- `content-type: application/json`
- a system guardrail
- JSON-serialized deterministic mission context

## Model Routing

- Sonnet: mission, readiness, blocker, timeline, and recommendation explanations
- Opus: open-ended mission questions
- Haiku: short insight summaries

Model identifiers are centralized in `ClaudeModel` so upgrades remain backend changes.

## Prompt Rules

`MissionPromptBuilder` instructs the model to preserve supplied numbers, avoid new calculations, avoid invented assumptions, prioritize the supplied next action, and keep answers short. The user content is serialized JSON rather than interpolated prose so values remain auditable.

## Response and Failure Handling

`AnthropicResponseParser` accepts text blocks from the Messages API and rejects empty or malformed payloads. `MissionCoachService` catches transport, configuration, and parsing failures and retries the request through `MockLlmProvider`.

The fallback uses only supplied mission facts and returns the same response shape with `usedFallback: true`.

## Routes

- `POST /v1/missions/{missionId}/coach`
- `POST /v1/missions/{missionId}/questions`
- `GET /v1/missions/{missionId}/explanations`

The route facade is transport-neutral so a FastAPI adapter can expose it without moving provider logic into controllers.
