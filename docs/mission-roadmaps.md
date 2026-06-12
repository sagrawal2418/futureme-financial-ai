# Mission Roadmaps

Mission roadmaps organize the action plan into three execution horizons:

- 30 Days
- 90 Days
- 1 Year

Each stage contains:

- current mission health
- upcoming actions
- completed actions
- expected readiness growth
- projected completion date

Actions appear in a horizon when their target date falls on or before that horizon. Completed actions remain visible so the roadmap explains how progress was earned.

## Scenario Impact

Roadmaps sit beside mission-specific scenario evaluation. The existing scenario engine supplies:

- readiness impact
- timeline impact
- risk impact

For the home mission, the execution layer compares buying now, waiting twelve months, increasing the down payment, and paying debt first.

## Platform Contract

Android, iOS, and web render `MissionRoadmap` from the shared Kotlin core. Platform views may adapt layout, but horizon membership, readiness growth, and projected dates must remain identical.

