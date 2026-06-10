package com.futureme.financialai.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.futureme.shared.domain.FutureMeProduct
import com.futureme.shared.models.AssistantPrompt
import com.futureme.shared.models.DashboardSnapshot
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.ScenarioComparison
import com.futureme.shared.models.ScenarioResult
import com.futureme.shared.models.SuggestedQuestion
import com.futureme.shared.models.UserIdentity

enum class FutureMeScreen {
    DASHBOARD,
    SCENARIOS,
    SCENARIO_DETAIL,
    COMPARISON,
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
    val suggestedQuestions: List<SuggestedQuestion>,
    val disclaimer: String,
    val selectedScenario: Scenario? = null,
    val selectedResult: ScenarioResult? = null,
    val comparison: ScenarioComparison,
    val messages: List<ChatMessage> = listOf(
        ChatMessage(
            id = 0L,
            text = "Ask me about a major decision. I will use the same profile, assumptions, " +
                "and scenario engine as your dashboard.",
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
        updateContent { it.copy(screen = screen) }
    }

    fun openScenario(scenario: Scenario) {
        runCatching {
            product.simulate(scenario.id)
        }.onSuccess { result ->
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
