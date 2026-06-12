# Mission Health

`MissionHealthEngine` summarizes whether a mission is executing as planned.

## Levels

- `GREEN`: score 80-100; the mission is on track
- `YELLOW`: score 55-79; the mission needs attention
- `RED`: score 0-54; the mission is at risk

## Factors

The engine evaluates:

- missed actions
- falling readiness
- delayed near-term milestones
- increasing or elevated risk

Each factor returns whether it is triggered, an explanation, and penalty points. The health score starts at 100 and subtracts active penalties.

Mission health is visible on mission cards, mission detail, roadmaps, notification output, and Mission Coach responses.

## Interpretation

Health is an execution signal, not another readiness score. Readiness describes financial capability. Health describes whether the user is completing the work needed to improve that capability on schedule.

