package com.futureme.financialai.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.futureme.financialai.data.AppContainer
import com.futureme.financialai.model.FinancialWorkspace
import com.futureme.financialai.model.Scenario
import com.futureme.financialai.usecase.CompareScenariosUseCase
import com.futureme.financialai.usecase.LoadFinancialWorkspaceUseCase
import com.futureme.financialai.usecase.SimulateScenarioUseCase

enum class FutureMeScreen {
    DASHBOARD,
    SCENARIOS,
    SCENARIO_DETAIL,
    COMPARISON,
}

data class FutureMeContent(
    val workspace: FinancialWorkspace,
    val screen: FutureMeScreen = FutureMeScreen.DASHBOARD,
)

sealed interface FutureMeUiState {
    data object Loading : FutureMeUiState
    data class Content(val data: FutureMeContent) : FutureMeUiState
    data class Empty(val message: String) : FutureMeUiState
    data class Error(val message: String) : FutureMeUiState
}

class FutureMeViewModel(
    private val loadWorkspace: LoadFinancialWorkspaceUseCase,
    private val simulateScenario: SimulateScenarioUseCase,
    private val compareScenarios: CompareScenariosUseCase,
) : ViewModel() {
    var uiState by mutableStateOf<FutureMeUiState>(FutureMeUiState.Loading)
        private set

    init {
        load()
    }

    fun load() {
        uiState = FutureMeUiState.Loading
        uiState = try {
            val workspace = loadWorkspace()
            if (workspace.scenarios.isEmpty()) {
                FutureMeUiState.Empty("No demo scenarios are available yet.")
            } else {
                FutureMeUiState.Content(FutureMeContent(workspace))
            }
        } catch (error: Throwable) {
            FutureMeUiState.Error(
                error.message ?: "FutureMe could not load the demo workspace.",
            )
        }
    }

    fun navigate(screen: FutureMeScreen) {
        val current = (uiState as? FutureMeUiState.Content)?.data ?: return
        uiState = FutureMeUiState.Content(current.copy(screen = screen))
    }

    fun openScenario(scenario: Scenario) {
        val current = (uiState as? FutureMeUiState.Content)?.data ?: return
        val workspace = current.workspace
        uiState = try {
            FutureMeUiState.Content(
                current.copy(
                    workspace = workspace.copy(
                        selectedScenario = scenario,
                        selectedResult = simulateScenario(workspace.profile, scenario),
                    ),
                    screen = FutureMeScreen.SCENARIO_DETAIL,
                ),
            )
        } catch (error: Throwable) {
            FutureMeUiState.Error(error.message ?: "The scenario could not be simulated.")
        }
    }

    fun compare(left: Scenario, right: Scenario) {
        val current = (uiState as? FutureMeUiState.Content)?.data ?: return
        uiState = FutureMeUiState.Content(
            current.copy(
                workspace = current.workspace.copy(
                    comparison = compareScenarios(current.workspace.profile, left, right),
                ),
                screen = FutureMeScreen.COMPARISON,
            ),
        )
    }

    companion object {
        fun factory(container: AppContainer): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    FutureMeViewModel(
                        loadWorkspace = container.loadFinancialWorkspace,
                        simulateScenario = container.simulateScenario,
                        compareScenarios = container.compareScenarios,
                    ) as T
            }
    }
}
