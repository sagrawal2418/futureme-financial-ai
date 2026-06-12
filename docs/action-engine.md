# Mission Action Engine

`MissionActionEngine` creates actionable steps for every existing mission.

Each `MissionAction` includes:

- `actionId`
- `title`
- `description`
- `category`
- `effort`
- `impact`
- `readinessGain`
- `targetDate`
- `completionStatus`
- dependency identifiers
- a measurable current value, target value, and progress percentage

## Status

- `AVAILABLE`: ready to start
- `IN_PROGRESS`: the linked metric is at least halfway to target
- `COMPLETED`: the linked metric meets its target
- `LOCKED`: one or more prerequisite actions are incomplete
- `MISSED`: the target date passed before completion

`MissionDependencyEngine` resolves dependencies after action generation. A locked action includes a human-readable explanation naming the prerequisite work.

## Design Rules

- Actions use financial values already present in the shared profile and dashboard.
- Planning checkpoints use explicit completion targets rather than pretending they are financial metrics.
- Clients display engine output but do not recalculate status or progress.
- Completing a prerequisite is sufficient to unlock dependent actions on the next evaluation.

