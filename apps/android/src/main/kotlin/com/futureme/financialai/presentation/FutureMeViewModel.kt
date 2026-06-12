package com.futureme.financialai.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.futureme.shared.domain.FutureMeProduct
import com.futureme.shared.models.AssistantPrompt
import com.futureme.shared.models.AnalyticsEvent
import com.futureme.shared.models.BankingVisionDemo
import com.futureme.shared.models.DashboardSnapshot
import com.futureme.shared.models.DecisionJournalEntry
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.FinancialGpsResult
import com.futureme.shared.models.FinancialExplainability
import com.futureme.shared.models.FutureOutcomeContribution
import com.futureme.shared.models.GoalProbabilityResult
import com.futureme.shared.models.Insight
import com.futureme.shared.models.ExecutiveDemoExperience
import com.futureme.shared.models.LifeDecisionSimulation
import com.futureme.shared.models.LifeEventPlan
import com.futureme.shared.models.LifeReadinessResult
import com.futureme.shared.models.LifeTimelinePoint
import com.futureme.shared.models.MoneyLeak
import com.futureme.shared.models.MonthlyFinancialReview
import com.futureme.shared.models.Mission
import com.futureme.shared.models.MissionAnalyticsSnapshot
import com.futureme.shared.models.MissionControlSnapshot
import com.futureme.shared.models.MissionExecutionCenter
import com.futureme.shared.models.NextBestAction
import com.futureme.shared.models.OpportunityRecommendation
import com.futureme.shared.models.ReadinessImprovementPlan
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.ScenarioComparison
import com.futureme.shared.models.ScenarioImpactHeatmap
import com.futureme.shared.models.ScenarioResult
import com.futureme.shared.models.SuggestedQuestion
import com.futureme.shared.models.UserIdentity

enum class FutureMeScreen {
    DASHBOARD,
    BANKING,
    READINESS,
    TIMELINE,
    SCENARIOS,
    SCENARIO_DETAIL,
    COMPARISON,
    LIFE_EVENTS,
    MONEY_LEAKS,
    REVIEW,
    ASSISTANT,
}

data class ChatMessage(
    val id: Long,
    val text: String,
    val isUser: Boolean,
)

data class FutureMeContent(
    val identity: UserIdentity,
    val profile: FinancialProfile,
    val dashboard: DashboardSnapshot,
    val scenarios: List<Scenario>,
    val recentResults: List<ScenarioResult>,
    val insights: List<Insight>,
    val financialGps: FinancialGpsResult,
    val goals: List<GoalProbabilityResult>,
    val lifeEvents: List<LifeEventPlan>,
    val moneyLeaks: List<MoneyLeak>,
    val readiness: List<LifeReadinessResult>,
    val readinessPlans: List<ReadinessImprovementPlan>,
    val decisionSimulations: List<LifeDecisionSimulation>,
    val lifeTimeline: List<LifeTimelinePoint>,
    val executiveDemo: ExecutiveDemoExperience,
    val opportunities: List<OpportunityRecommendation>,
    val nextBestAction: NextBestAction,
    val financialExplainability: FinancialExplainability,
    val scenarioImpactHeatmaps: List<ScenarioImpactHeatmap>,
    val monthlyReviews: List<MonthlyFinancialReview>,
    val decisionJournal: List<DecisionJournalEntry>,
    val futureOutcomeContributions: List<FutureOutcomeContribution>,
    val bankingVisionDemo: BankingVisionDemo,
    val analyticsEvents: List<AnalyticsEvent>,
    val missions: List<Mission>,
    val missionControl: MissionControlSnapshot,
    val missionExecution: MissionExecutionCenter,
    val missionAnalytics: MissionAnalyticsSnapshot,
    val suggestedQuestions: List<SuggestedQuestion>,
    val disclaimer: String,
    val selectedScenario: Scenario? = null,
    val selectedResult: ScenarioResult? = null,
    val comparison: ScenarioComparison,
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            id = 0L,
            text = "Tell me which mission matters now. I will explain the blocker, timeline, " +
                "and highest-impact action.",
            isUser = false,
        ),
    ),
    val screen: FutureMeScreen = FutureMeScreen.DASHBOARD,
)

sealed interface FutureMeUiState {
    data object Loading : FutureMeUiState
    data class Content(val data: FutureMeContent) : FutureMeUiState
    data class Empty(val message: String) : FutureMeUiState
    data class Error(val message: String) : FutureMeUiState
}

class FutureMeViewModel(
    private val product: FutureMeProduct,
) : ViewModel() {
    var uiState by mutableStateOf<FutureMeUiState>(FutureMeUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = FutureMeUiState.Loading
        uiState = try {
            val bootstrap = product.bootstrap()
            if (bootstrap.scenarios.isEmpty()) {
                FutureMeUiState.Empty("No demo scenarios are available yet.")
            } else {
                FutureMeUiState.Content(
                    FutureMeContent(
                        identity = bootstrap.identity,
                        profile = bootstrap.profile,
                        dashboard = bootstrap.dashboard,
                        scenarios = bootstrap.scenarios,
                        recentResults = bootstrap.recentScenarioResults,
                        insights = bootstrap.insights,
                        financialGps = bootstrap.financialGps,
                        goals = bootstrap.goals,
                        lifeEvents = bootstrap.lifeEvents,
                        moneyLeaks = bootstrap.moneyLeaks,
                        readiness = bootstrap.readiness,
                        readinessPlans = bootstrap.readinessPlans,
                        decisionSimulations = bootstrap.decisionSimulations,
                        lifeTimeline = bootstrap.lifeTimeline,
                        executiveDemo = bootstrap.executiveDemo,
                        opportunities = bootstrap.opportunities,
                        nextBestAction = bootstrap.nextBestAction,
                        financialExplainability = bootstrap.financialExplainability,
                        scenarioImpactHeatmaps = bootstrap.scenarioImpactHeatmaps,
                        monthlyReviews = bootstrap.monthlyReviews,
                        decisionJournal = bootstrap.decisionJournal,
                        futureOutcomeContributions = bootstrap.futureOutcomeContributions,
                        bankingVisionDemo = bootstrap.bankingVisionDemo,
                        analyticsEvents = bootstrap.analyticsEvents,
                        missions = bootstrap.missions,
                        missionControl = bootstrap.missionControl,
                        missionExecution = bootstrap.missionExecution,
                        missionAnalytics = bootstrap.missionAnalytics,
                        suggestedQuestions = bootstrap.suggestedQuestions,
                        disclaimer = bootstrap.disclaimer,
                        comparison = product.compare("move-to-texas", "stay-in-new-jersey"),
                    ),
                )
            }
        } catch (error: Throwable) {
            FutureMeUiState.Error(error.message ?: "FutureMe could not load the demo workspace.")
        }
    }

    fun navigate(screen: FutureMeScreen) {
        when (screen) {
            FutureMeScreen.READINESS -> product.recordAnalyticsEvent("readiness_viewed")
            FutureMeScreen.REVIEW -> product.recordAnalyticsEvent("monthly_review_opened")
            else -> Unit
        }
        updateContent { it.copy(screen = screen) }
    }

    fun openScenario(scenario: Scenario) {
        runCatching {
            product.simulate(scenario.id)
        }.onSuccess { result ->
            product.recordAnalyticsEvent("scenario_created", scenario.id)
            updateContent {
                it.copy(
                    selectedScenario = scenario,
                    selectedResult = result,
                    screen = FutureMeScreen.SCENARIO_DETAIL,
                )
            }
        }.onFailure { error ->
            uiState = FutureMeUiState.Error(
                error.message ?: "The scenario could not be simulated.",
            )
        }
    }

    fun compare(left: Scenario, right: Scenario) {
        runCatching {
            product.compare(left.id, right.id)
        }.onSuccess { comparison ->
            updateContent {
                it.copy(
                    comparison = comparison,
                    screen = FutureMeScreen.COMPARISON,
                )
            }
        }.onFailure { error ->
            uiState = FutureMeUiState.Error(
                error.message ?: "The scenarios could not be compared.",
            )
        }
    }

    fun askAssistant(question: String) {
        val normalized = question.trim()
        if (normalized.isEmpty()) return

        val current = (uiState as? FutureMeUiState.Content)?.data ?: return
        val nextId = (current.messages.maxOfOrNull(ChatMessage::id) ?: 0L) + 1L
        val response = runCatching {
            product.ask(
                AssistantPrompt(
                    question = normalized,
                    latestScenarioId = current.selectedScenario?.id,
                ),
            )
        }.getOrElse { error ->
            updateContent {
                it.copy(
                    messages = it.messages + ChatMessage(
                        id = nextId,
                        text = normalized,
                        isUser = true,
                    ) + ChatMessage(
                        id = nextId + 1L,
                        text = error.message ?: "I could not model that question.",
                        isUser = false,
                    ),
                    screen = FutureMeScreen.ASSISTANT,
                )
            }
            return
        }

        updateContent {
            it.copy(
                messages = it.messages + ChatMessage(
                    id = nextId,
                    text = normalized,
                    isUser = true,
                ) + ChatMessage(
                    id = nextId + 1L,
                    text = response.answer,
                    isUser = false,
                ),
                screen = FutureMeScreen.ASSISTANT,
            )
        }
    }

    fun acceptRecommendation() {
        val current = (uiState as? FutureMeUiState.Content)?.data ?: return
        product.recordAnalyticsEvent(
            "recommendation_accepted",
            current.nextBestAction.recommendationId,
        )
        updateContent { it.copy(analyticsEvents = product.analyticsEvents()) }
    }

    fun acceptMissionAction(actionId: String) {
        val bootstrap = product.completeMissionAction(actionId)
        updateContent {
            it.copy(
                missions = bootstrap.missions,
                missionControl = bootstrap.missionControl,
                missionExecution = bootstrap.missionExecution,
                missionAnalytics = bootstrap.missionAnalytics,
                analyticsEvents = bootstrap.analyticsEvents,
            )
        }
    }

    fun saveDecision() {
        val current = (uiState as? FutureMeUiState.Content)?.data ?: return
        val scenarioId = current.selectedScenario?.id ?: return
        product.saveDecision(scenarioId)
        updateContent {
            it.copy(
                decisionJournal = product.decisionJournal(),
                analyticsEvents = product.analyticsEvents(),
            )
        }
    }

    private fun updateContent(transform: (FutureMeContent) -> FutureMeContent) {
        val current = (uiState as? FutureMeUiState.Content)?.data ?: return
        uiState = FutureMeUiState.Content(transform(current))
    }

    companion object {
        fun factory(product: FutureMeProduct): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FutureMeViewModel(product) as T
            }
    }
}
