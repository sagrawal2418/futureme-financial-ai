import Foundation
import Shared

struct ScenarioCardModel: Identifiable {
    let id: String
    let title: String
    let subtitle: String
    let code: String
    let source: Scenario
}

struct AssistantMessage: Identifiable {
    let id: UUID
    let text: String
    let isUser: Bool
}

struct FutureMeDashboardContent {
    let displayName: String
    let householdName: String
    let dashboard: DashboardSnapshot
    let scenarios: [ScenarioCardModel]
    let selected: ScenarioCardModel
    let result: ScenarioResult
    let comparison: ScenarioComparison
    let suggestions: [SuggestedQuestion]
    let messages: [AssistantMessage]
    let disclaimer: String
}

enum FutureMeViewState {
    case loading
    case content(FutureMeDashboardContent)
    case empty(String)
    case error(String)
}

@MainActor
final class FutureMeViewModel: ObservableObject {
    @Published private(set) var state: FutureMeViewState = .loading

    private let product: FutureMeProduct
    private let secureStore: SecureStoring

    init(
        product: FutureMeProduct = FutureMeProduct(),
        secureStore: SecureStoring = KeychainSecureStore()
    ) {
        self.product = product
        self.secureStore = secureStore
        load()
    }

    func load() {
        state = .loading

        do {
            let bootstrap = product.bootstrap()
            let mappedScenarios = bootstrap.scenarios.map {
                ScenarioCardModel(
                    id: $0.id,
                    title: $0.title,
                    subtitle: $0.subtitle,
                    code: Self.code(for: $0),
                    source: $0
                )
            }

            guard mappedScenarios.count >= 2 else {
                state = .empty("Add at least two scenarios to unlock comparison mode.")
                return
            }

            let initial = mappedScenarios.first { $0.id == "move-to-texas" } ?? mappedScenarios[0]
            state = .content(
                FutureMeDashboardContent(
                    displayName: bootstrap.identity.displayName,
                    householdName: bootstrap.identity.householdName,
                    dashboard: bootstrap.dashboard,
                    scenarios: mappedScenarios,
                    selected: initial,
                    result: product.simulate(scenarioId: initial.id),
                    comparison: product.compare(
                        leftScenarioId: "move-to-texas",
                        rightScenarioId: "stay-in-new-jersey"
                    ),
                    suggestions: bootstrap.suggestedQuestions,
                    messages: [
                        AssistantMessage(
                            id: UUID(),
                            text: "Ask me about a major decision. I use the same profile, assumptions, and scenario engine as your dashboard.",
                            isUser: false
                        ),
                    ],
                    disclaimer: bootstrap.disclaimer
                )
            )

            // Stores only an opaque demo profile identifier. Financial values remain in memory.
            try? secureStore.put(bootstrap.profile.profileId, for: "active-demo-profile")
        } catch {
            state = .error(error.localizedDescription)
        }
    }

    func select(_ scenario: ScenarioCardModel) {
        guard case let .content(content) = state else {
            return
        }

        state = .content(
            FutureMeDashboardContent(
                displayName: content.displayName,
                householdName: content.householdName,
                dashboard: content.dashboard,
                scenarios: content.scenarios,
                selected: scenario,
                result: product.simulate(scenarioId: scenario.id),
                comparison: content.comparison,
                suggestions: content.suggestions,
                messages: content.messages,
                disclaimer: content.disclaimer
            )
        )
    }

    func ask(_ question: String) {
        let normalized = question.trimmingCharacters(in: .whitespacesAndNewlines)
        guard !normalized.isEmpty, case let .content(content) = state else {
            return
        }

        let response = product.ask(
            prompt: AssistantPrompt(
                question: normalized,
                latestScenarioId: content.selected.id
            )
        )
        let messages = content.messages + [
            AssistantMessage(id: UUID(), text: normalized, isUser: true),
            AssistantMessage(id: UUID(), text: response.answer, isUser: false),
        ]

        state = .content(
            FutureMeDashboardContent(
                displayName: content.displayName,
                householdName: content.householdName,
                dashboard: content.dashboard,
                scenarios: content.scenarios,
                selected: content.selected,
                result: content.result,
                comparison: content.comparison,
                suggestions: content.suggestions,
                messages: messages,
                disclaimer: content.disclaimer
            )
        )
    }

    private static func code(for scenario: Scenario) -> String {
        switch scenario.id {
        case "buy-home", "wait-to-buy": "HOME"
        case "refinance-now", "keep-current-loan": "REFI"
        case "pay-off-cards": "DEBT"
        case "move-to-texas", "stay-in-new-jersey": "MOVE"
        case "have-a-child": "FAM"
        case "invest-more": "INV"
        default: "SAFE"
        }
    }
}
