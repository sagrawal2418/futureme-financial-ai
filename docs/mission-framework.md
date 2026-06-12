# Mission Framework

Mission Control organizes FutureMe around the household decisions people are actually trying to make.

## Mission Types

- Buy a Home
- Have a Child
- Relocate
- Retire Early
- Become Debt Free
- Build Emergency Fund
- Support Parents
- Start a Business

Every mission uses one shared `Mission` contract with identity, target date, readiness, progress, risk, estimated cost, projected benefit, blockers, recommendations, one next action, timeline, status, and audit dates.

## Status

| Status | Meaning |
| --- | --- |
| Not Started | No measurable progress is modeled yet. |
| In Progress | Preparation is active but readiness is below the ready range. |
| At Risk | Readiness or household risk is below the mission planning floor. |
| On Track | Readiness is at least 80 and no critical risk is present. |
| Completed | Readiness and progress have both reached the completion threshold. |

## Orchestration

`MissionEngine` creates one mission from shared inputs. `MissionService` builds the complete portfolio and the Mission Control snapshot.

Supporting services retain their existing responsibilities:

- Financial GPS supplies current and improved trajectories.
- Life Readiness supplies decision-specific readiness evidence.
- Goal Probability supplies target probability and projected dates.
- Money Leak Detection supplies avoidable losses and fixes.
- Opportunity Ranking supplies impact, effort, confidence, and benefit.
- Life Event Planning supplies cost ranges and preparation steps.
- AI explains the deterministic mission output.

## Next Best Action

Each mission has exactly one highest-impact action. The action includes:

- Expected readiness increase
- Expected timeline reduction
- Annual and five-year benefit
- Impact and confidence
- Optional related scenario

Mission Control prioritizes across missions by combining action impact with the urgency created by lower readiness.

## Timeline

Every mission exposes Today, 30 Days, 90 Days, 1 Year, and 3 Years. Each point includes readiness, progress, completed actions, the next milestone, and projected completion date.

## Analytics

The local event model tracks mission creation, completion, readiness improvement, timeline improvement, action completion, and goal achievement. The Mission Analytics snapshot displays readiness trends, completed actions, and months removed from projected timelines.
