# Client Feature Parity

FutureMe treats Android, iOS, and web as three native presentations of one product, not three independent products. Every financial value and recommendation input comes from `FutureMeProduct`; platform code owns only interaction and rendering.

## Version 2 Contract

| Capability | Android | iOS | Web |
| --- | --- | --- | --- |
| Financial health dashboard | Compose overview | SwiftUI overview | React overview |
| Proactive insights | Full Plan hub plus dashboard top three | Full Plan hub plus dashboard top three | Expandable full list plus dashboard top three |
| Financial GPS | Current/improved chart, actions, confidence, explanation | Current/improved chart, actions, confidence, explanation | Current/improved chart, actions, confidence, explanation |
| Goal readiness | All goals, blockers, actions, monthly gap, ready date | All goals with detail screens | All goals with expandable details |
| Life-event planning | Event details and linked scenario action | Event details and linked scenario action | Selected event details and linked scenario action |
| Money-leak detector | Dedicated complete list | Complete Plan section | Complete dashboard section |
| Scenario lab | Complete list and scenario detail | Complete list and overview detail | Expandable complete list and inline detail |
| Risk explanation | Factor-level scenario detail | Factor-level overview detail | Factor-level inline detail |
| Scenario comparison | Dynamic Option A and B selectors | Dynamic Option A and B pickers | Dynamic Option A and B selectors |
| FutureMe assistant | Floating entry, suggestions, free-form prompt | Dedicated tab, suggestions, free-form prompt | Floating drawer, suggestions, free-form prompt |
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
