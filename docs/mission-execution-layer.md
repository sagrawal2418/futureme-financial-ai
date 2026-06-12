# Mission Execution Layer

The Mission Execution Layer turns each Mission Control readiness assessment into a continuously evaluated action plan. It does not introduce new mission types, dashboards, or financial formulas.

## Shared Flow

1. `MissionActionEngine` generates four measurable actions for each existing mission.
2. `MissionDependencyEngine` marks actions as unlocked or blocked.
3. `MissionProgressEngine` derives progress from actions, metrics, and readiness.
4. `MissionHealthEngine` evaluates missed work, readiness movement, delays, and risk.
5. `MissionRoadmapService` groups work into 30-day, 90-day, and one-year horizons.
6. `MissionScenarioEvaluator` connects relevant existing simulations to the mission.
7. `MissionHistoryService` records readiness, action, timeline, risk, and health movement.
8. `MissionNotificationService` creates local execution updates.

`MissionExecutionService` orchestrates these components and returns one `MissionExecutionPlan` per mission. Android, iOS, and web consume this shared model without duplicating calculations.

## Progress

Mission progress is derived from:

- 45% action completion state
- 35% measurable action metrics
- 20% current mission readiness

Completed, in-progress, available, locked, and missed actions receive different action-progress weights. Metric progress compares the current household value with the action target.

## Local State

Version 1 of the execution layer is deterministic and local. Notifications and history are generated from the current demo household and remain on the device. A persistent repository can replace this local implementation without changing the client contract.

## Coach

Mission Coach grounds its answers in the selected execution plan. It prioritizes:

- the next unlocked action
- dependency blockers
- mission health
- readiness history
- the 90-day roadmap
- mission-specific scenario impact

