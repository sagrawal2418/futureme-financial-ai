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
    let insights: [Insight]
    let financialGps: FinancialGpsResult
    let goals: [GoalProbabilityResult]
    let lifeEvents: [LifeEventPlan]
    let moneyLeaks: [MoneyLeak]
    let readiness: [LifeReadinessResult]
    let readinessPlans: [ReadinessImprovementPlan]
    let decisionSimulations: [LifeDecisionSimulation]
    let lifeTimeline: [LifeTimelinePoint]
    let executiveDemo: ExecutiveDemoExperience
    let opportunities: [OpportunityRecommendation]
    let nextBestAction: NextBestAction
    let financialExplainability: FinancialExplainability
    let scenarioImpactHeatmaps: [ScenarioImpactHeatmap]
    let monthlyReviews: [MonthlyFinancialReview]
    let decisionJournal: [DecisionJournalEntry]
    let futureOutcomeContributions: [FutureOutcomeContribution]
    let bankingVisionDemo: BankingVisionDemo
    let analyticsEvents: [AnalyticsEvent]
    let missions: [Mission]
    let missionControl: MissionControlSnapshot
    let missionAnalytics: MissionAnalyticsSnapshot
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
                insights: bootstrap.insights,
                financialGps: bootstrap.financialGps,
                goals: bootstrap.goals,
                lifeEvents: bootstrap.lifeEvents,
                moneyLeaks: bootstrap.moneyLeaks,
                readiness: bootstrap.readiness,
                readinessPlans: bootstrap.readinessPlans,
                decisionSimulations: bootstrap.decisionSimulations,
                lifeTimeline: bootstrap.lifeTimeline,
                executiveDemo: bootstrap.executiveDemo,
                opportunities: bootstrap.opportunities,
                nextBestAction: bootstrap.nextBestAction,
                financialExplainability: bootstrap.financialExplainability,
                scenarioImpactHeatmaps: bootstrap.scenarioImpactHeatmaps,
                monthlyReviews: bootstrap.monthlyReviews,
                decisionJournal: bootstrap.decisionJournal,
                futureOutcomeContributions: bootstrap.futureOutcomeContributions,
                bankingVisionDemo: bootstrap.bankingVisionDemo,
                analyticsEvents: bootstrap.analyticsEvents,
                missions: bootstrap.missions,
                missionControl: bootstrap.missionControl,
                missionAnalytics: bootstrap.missionAnalytics,
                suggestions: bootstrap.suggestedQuestions,
                messages: [
                    AssistantMessage(
                        id: UUID(),
                        text: "Tell me which mission matters now. I will explain the blocker, timeline, and highest-impact action.",
                        isUser: false
                    ),
                ],
                disclaimer: bootstrap.disclaimer
            )
        )

        // Stores only an opaque demo profile identifier. Financial values remain in memory.
        try? secureStore.put(bootstrap.profile.profileId, for: "active-demo-profile")
    }

    func select(_ scenario: ScenarioCardModel) {
        guard case let .content(content) = state else {
            return
        }

        _ = product.recordAnalyticsEvent(typeCode: "scenario_created", subjectId: scenario.id)
        state = .content(
            FutureMeDashboardContent(
                displayName: content.displayName,
                householdName: content.householdName,
                dashboard: content.dashboard,
                scenarios: content.scenarios,
                selected: scenario,
                result: product.simulate(scenarioId: scenario.id),
                comparison: content.comparison,
                insights: content.insights,
                financialGps: content.financialGps,
                goals: content.goals,
                lifeEvents: content.lifeEvents,
                moneyLeaks: content.moneyLeaks,
                readiness: content.readiness,
                readinessPlans: content.readinessPlans,
                decisionSimulations: content.decisionSimulations,
                lifeTimeline: content.lifeTimeline,
                executiveDemo: content.executiveDemo,
                opportunities: content.opportunities,
                nextBestAction: content.nextBestAction,
                financialExplainability: content.financialExplainability,
                scenarioImpactHeatmaps: content.scenarioImpactHeatmaps,
                monthlyReviews: content.monthlyReviews,
                decisionJournal: content.decisionJournal,
                futureOutcomeContributions: content.futureOutcomeContributions,
                bankingVisionDemo: content.bankingVisionDemo,
                analyticsEvents: product.analyticsEvents(),
                missions: content.missions,
                missionControl: content.missionControl,
                missionAnalytics: content.missionAnalytics,
                suggestions: content.suggestions,
                messages: content.messages,
                disclaimer: content.disclaimer
            )
        )
    }

    func compare(_ left: ScenarioCardModel, _ right: ScenarioCardModel) {
        guard left.id != right.id, case let .content(content) = state else {
            return
        }

        state = .content(
            FutureMeDashboardContent(
                displayName: content.displayName,
                householdName: content.householdName,
                dashboard: content.dashboard,
                scenarios: content.scenarios,
                selected: content.selected,
                result: content.result,
                comparison: product.compare(
                    leftScenarioId: left.id,
                    rightScenarioId: right.id
                ),
                insights: content.insights,
                financialGps: content.financialGps,
                goals: content.goals,
                lifeEvents: content.lifeEvents,
                moneyLeaks: content.moneyLeaks,
                readiness: content.readiness,
                readinessPlans: content.readinessPlans,
                decisionSimulations: content.decisionSimulations,
                lifeTimeline: content.lifeTimeline,
                executiveDemo: content.executiveDemo,
                opportunities: content.opportunities,
                nextBestAction: content.nextBestAction,
                financialExplainability: content.financialExplainability,
                scenarioImpactHeatmaps: content.scenarioImpactHeatmaps,
                monthlyReviews: content.monthlyReviews,
                decisionJournal: content.decisionJournal,
                futureOutcomeContributions: content.futureOutcomeContributions,
                bankingVisionDemo: content.bankingVisionDemo,
                analyticsEvents: content.analyticsEvents,
                missions: content.missions,
                missionControl: content.missionControl,
                missionAnalytics: content.missionAnalytics,
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
                insights: content.insights,
                financialGps: content.financialGps,
                goals: content.goals,
                lifeEvents: content.lifeEvents,
                moneyLeaks: content.moneyLeaks,
                readiness: content.readiness,
                readinessPlans: content.readinessPlans,
                decisionSimulations: content.decisionSimulations,
                lifeTimeline: content.lifeTimeline,
                executiveDemo: content.executiveDemo,
                opportunities: content.opportunities,
                nextBestAction: content.nextBestAction,
                financialExplainability: content.financialExplainability,
                scenarioImpactHeatmaps: content.scenarioImpactHeatmaps,
                monthlyReviews: content.monthlyReviews,
                decisionJournal: content.decisionJournal,
                futureOutcomeContributions: content.futureOutcomeContributions,
                bankingVisionDemo: content.bankingVisionDemo,
                analyticsEvents: product.analyticsEvents(),
                missions: content.missions,
                missionControl: content.missionControl,
                missionAnalytics: content.missionAnalytics,
                suggestions: content.suggestions,
                messages: messages,
                disclaimer: content.disclaimer
            )
        )
    }

    func acceptRecommendation() {
        guard case let .content(content) = state else {
            return
        }
        _ = product.recordAnalyticsEvent(
            typeCode: "recommendation_accepted",
            subjectId: content.nextBestAction.recommendationId
        )
        replace(content, analyticsEvents: product.analyticsEvents())
    }

    func acceptMissionAction(_ actionId: String) {
        guard case let .content(content) = state else {
            return
        }
        _ = product.recordAnalyticsEvent(
            typeCode: "mission_action_completed",
            subjectId: actionId
        )
        replace(content, analyticsEvents: product.analyticsEvents())
    }

    func saveDecision() {
        guard case let .content(content) = state else {
            return
        }
        _ = product.saveDecision(scenarioId: content.selected.id)
        replace(
            content,
            decisionJournal: product.decisionJournal(),
            analyticsEvents: product.analyticsEvents()
        )
    }

    private func replace(
        _ content: FutureMeDashboardContent,
        decisionJournal: [DecisionJournalEntry]? = nil,
        analyticsEvents: [AnalyticsEvent]? = nil
    ) {
        state = .content(
            FutureMeDashboardContent(
                displayName: content.displayName,
                householdName: content.householdName,
                dashboard: content.dashboard,
                scenarios: content.scenarios,
                selected: content.selected,
                result: content.result,
                comparison: content.comparison,
                insights: content.insights,
                financialGps: content.financialGps,
                goals: content.goals,
                lifeEvents: content.lifeEvents,
                moneyLeaks: content.moneyLeaks,
                readiness: content.readiness,
                readinessPlans: content.readinessPlans,
                decisionSimulations: content.decisionSimulations,
                lifeTimeline: content.lifeTimeline,
                executiveDemo: content.executiveDemo,
                opportunities: content.opportunities,
                nextBestAction: content.nextBestAction,
                financialExplainability: content.financialExplainability,
                scenarioImpactHeatmaps: content.scenarioImpactHeatmaps,
                monthlyReviews: content.monthlyReviews,
                decisionJournal: decisionJournal ?? content.decisionJournal,
                futureOutcomeContributions: content.futureOutcomeContributions,
                bankingVisionDemo: content.bankingVisionDemo,
                analyticsEvents: analyticsEvents ?? content.analyticsEvents,
                missions: content.missions,
                missionControl: content.missionControl,
                missionAnalytics: content.missionAnalytics,
                suggestions: content.suggestions,
                messages: content.messages,
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
        case "spouse-stops-working": "ONE"
        case "start-business": "BIZ"
        case "invest-more": "INV"
        default: "SAFE"
        }
    }
}
