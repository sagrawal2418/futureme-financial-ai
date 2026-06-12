# Client Feature Parity

FutureMe treats Android, iOS, and web as three native presentations of one product, not three independent products. Every financial value and recommendation input comes from `FutureMeProduct`; platform code owns only interaction and rendering.

## Mission Control Contract

| Capability | Android | iOS | Web |
| --- | --- | --- | --- |
| Mission Control landing | Primary Mission tab | Primary Mission tab | Primary landing dashboard |
| Eight active missions | Shared selectable cards | Shared selectable cards | Shared selectable cards |
| Mission detail | Score, status, factors, blocker | Score, status, factors, blocker | Score, status, factors, blocker |
| Mission timeline | Today through three years | Today through three years | Today through three years |
| Mission Coach | Dedicated Coach route | Dedicated Coach tab | Mission Coach drawer |
| Mission readiness | Six shared dimensions | Six shared dimensions | Six shared dimensions |
| Mission analytics | Trends, actions, months saved | Trends, actions, months saved | Trends, actions, months saved |
| Mission risks and opportunities | Mission Control sections | Mission Control sections | Mission Control sections |
| Highest-impact action | Dashboard and Actions screen | Overview and Banking tab | Dashboard top card |
| Ranked opportunities | Actions screen | Banking tab | Banking intelligence panel |
| Financial health explainability | Factor-level Actions card | Factor-level Banking card | Score change panel |
| Scenario impact heatmap | Scenario detail | Simulator | Inline simulator heatmap |
| Monthly financial review | Review screen | Banking tab | Monthly review section |
| Decision journal | Review screen and scenario save | Banking tab and simulator save | Review section and simulator save |
| What improved my future | Actions screen | Banking tab | Outcome contribution cards |
| FutureMe Banking Vision | Seven-step Actions demo | Seven-step Banking demo | Seven-step executive demo |
| Local analytics events | Shared product session | Shared product session | Shared product session |
| Life readiness dashboard | Dedicated dashboard and overview preview | Dedicated Readiness tab and overview hero | Dedicated responsive dashboard |
| Seven readiness categories | Shared engine output | Shared engine output | Shared engine output |
| Readiness detail | Score, level, trend, blockers, confidence, ready date | Score, level, trend, blockers, confidence, ready date | Score, level, trend, blockers, confidence, ready date |
| Readiness improvement plan | Current/target, actions, commitment, timeline | Current/target, actions, commitment, timeline | Current/target, actions, commitment, timeline |
| Life decision simulator | Scenario list, financial/risk/readiness/timeline impact | Scenario list, financial/risk/readiness/timeline impact | Scenario list, financial/risk/readiness/timeline impact |
| Life timeline | Today through five years | Today through five years with chart | Today through five years |
| FutureMe AI Coach | Floating strategist entry and prompts | Dedicated Coach tab and prompts | Floating coach drawer and prompts |
| Executive demo experience | Guided persona and five-step flow | Guided persona and five-step flow | Guided persona and five-step flow |
| Financial health dashboard | Compose overview | SwiftUI overview | React overview |
| Proactive insights | Full Plan hub plus dashboard top three | Full Plan hub plus dashboard top three | Expandable full list plus dashboard top three |
| Financial GPS | Current/improved chart, actions, confidence, explanation | Current/improved chart, actions, confidence, explanation | Current/improved chart, actions, confidence, explanation |
| Goal readiness | All goals, blockers, actions, monthly gap, ready date | All goals with detail screens | All goals with expandable details |
| Life-event planning | Event details and linked scenario action | Event details and linked scenario action | Selected event details and linked scenario action |
| Money-leak detector | Dedicated complete list | Complete Plan section | Complete dashboard section |
| Scenario lab | Nine scenarios and scenario detail | Nine scenarios and overview detail | Nine scenarios and inline detail |
| Risk explanation | Factor-level scenario detail | Factor-level overview detail | Factor-level inline detail |
| Scenario comparison | Dynamic Option A and B selectors | Dynamic Option A and B pickers | Dynamic Option A and B selectors |
| FutureMe assistant foundation | Shared grounded response service | Shared grounded response service | Shared grounded response service |
| Loading, error, and empty states | Yes | Yes | Yes |
| Dark mode and accessibility | System theme and Compose semantics | System theme and SwiftUI labels | Theme control, semantic HTML, ARIA |

## Parity Rules

1. New calculations and seeded data are added to the Kotlin Multiplatform core first.
2. A product capability is complete only after all three clients expose it.
3. Platform layouts may differ, but inputs, outputs, available actions, and labels remain equivalent.
4. Controls that imply navigation or action must be wired before release.
5. `backend/tests/test_client_parity.py` guards the source-level contract; platform builds and UI tests remain the behavioral authority.

## Intentional Platform Differences

- Android uses bottom navigation and a vertically composed Plan hub.
- iOS uses native tabs, navigation stacks, and detail destinations.
- Web uses anchor navigation, responsive dashboard sections, and expandable detail.

These are presentation choices, not feature differences.
