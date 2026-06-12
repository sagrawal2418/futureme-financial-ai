# Claude Mission Coach

Mission Coach turns Mission Control output into concise strategy. It explains deterministic results; it does not calculate readiness, change a timeline, or invent financial facts.

## Briefing Contract

Every mission receives:

- coaching summary
- recommended focus area
- top risk and opportunity
- suggested actions
- six focused explanations
- three mission-specific questions
- latest and previous explanation metadata
- a summary of what changed

The six explanation views answer:

1. Why am I not ready?
2. What improved recently?
3. What is hurting progress?
4. What should I focus on?
5. How can I accelerate the timeline?
6. What happens if I do nothing?

## Client Experience

Android, iOS, and web show a visually distinct Claude Mission Briefing directly after mission readiness. The card keeps the default view concise: one summary, Focus/Risk/Opportunity signals, one selected explanation, and mission-specific question buttons.

Static and offline builds use `MissionCoachPreviewService` and label the result as a demo fallback. Production clients should request the same contract from the backend and never call Anthropic directly.

## Coaching Philosophy

- Start with the next unlocked mission action.
- Explain the blocker before suggesting acceleration.
- Preserve every score, date, dollar amount, and risk value supplied by an engine.
- State uncertainty instead of filling missing data.
- Prefer one useful priority over a long list of generic advice.
- Keep educational guidance distinct from financial advice.

## History

`MissionCoachService` retains up to ten explanations per mission in local backend memory. The history response exposes the latest entry, previous entry, and a concise list of changed focus, risk, or actions. A persistent repository can replace this store without changing the route contract.
