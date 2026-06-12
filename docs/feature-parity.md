# Client Feature Parity

Android, iOS, and web are three native presentations of one product. Financial values, mission state, personas, navigation intent, and evaluation metadata come from `FutureMeProduct`.

## Primary Navigation

| Destination | Android | iOS | Web |
| --- | --- | --- | --- |
| Home | Bottom tab | Native tab | Sidebar destination |
| Missions | Bottom tab | Native tab | Sidebar destination |
| Insights | Bottom tab | Native tab | Sidebar destination |
| Coach | Bottom tab | Native tab | Sidebar destination |
| Profile | Bottom tab | Native tab | Sidebar destination |

## Shared Product Contract

| Capability | Android | iOS | Web |
| --- | --- | --- | --- |
| Highest-impact action | Home | Home | Home |
| Readiness change, risk, opportunity | Home | Home | Home |
| Mission readiness and blocker | Missions | Missions | Missions |
| Dependency-aware action plan | Missions | Missions | Missions |
| Roadmap, history, and scenarios | Mission detail | Mission detail | Expandable mission detail |
| Money leaks and monthly review | Insights | Insights | Insights |
| Financial explainability | Insights | Insights | Insights |
| Mission Coach | Coach | Coach | Coach |
| 50-prompt AI evaluation coverage | Coach | Coach | Coach |
| Financial and family profile | Profile | Profile | Profile |
| Five customer personas | Profile | Profile | Profile |
| Executive demo story | Profile | Profile | Profile |
| Loading, error, and empty states | Yes | Yes | Yes |
| Accessibility semantics | Compose | SwiftUI | Semantic HTML and ARIA |

## Supporting Capabilities

Financial GPS, goal probability, life-event planning, readiness plans, timelines, scenario heatmaps, comparisons, decision journal, and outcome attribution remain in the shared product model. They are rendered within a mission, insight, or profile workflow instead of becoming additional top-level destinations.

## Parity Rules

1. Shared calculations and seeded data are implemented in Kotlin first.
2. The five primary destinations remain consistent across clients.
3. Supporting details may use platform-native progressive disclosure.
4. AI never owns financial arithmetic.
5. `backend/tests/test_client_parity.py` checks the simplified source contract; platform builds and UI tests remain the behavioral authority.

## Intentional Differences

- Android uses Compose bottom navigation and vertical cards.
- iOS uses native tabs and navigation stacks.
- Web uses a persistent desktop sidebar and mobile drawer.

These are presentation choices, not product-architecture differences.
