package com.futureme.financialai.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.futureme.financialai.presentation.FutureMeContent
import com.futureme.financialai.presentation.FutureMeScreen
import com.futureme.financialai.presentation.FutureMeUiState
import com.futureme.financialai.presentation.FutureMeViewModel
import com.futureme.financialai.ui.components.Eyebrow
import com.futureme.financialai.ui.screens.ComparisonScreen
import com.futureme.financialai.ui.screens.DashboardScreen
import com.futureme.financialai.ui.screens.LoadingScreen
import com.futureme.financialai.ui.screens.MessageScreen
import com.futureme.financialai.ui.screens.ScenarioDetailScreen
import com.futureme.financialai.ui.screens.ScenarioListScreen

@Composable
fun FutureMeApp(viewModel: FutureMeViewModel) {
    when (val state = viewModel.uiState) {
        FutureMeUiState.Loading -> LoadingScreen()
        is FutureMeUiState.Empty -> MessageScreen(
            title = "No scenarios yet",
            message = state.message,
            action = "Reload demo workspace",
            onAction = viewModel::load,
        )
        is FutureMeUiState.Error -> MessageScreen(
            title = "We could not load FutureMe",
            message = state.message,
            action = "Try again",
            onAction = viewModel::load,
        )
        is FutureMeUiState.Content -> ContentScaffold(
            content = state.data,
            onNavigate = viewModel::navigate,
            onScenarioClick = viewModel::openScenario,
        )
    }
}

@Composable
private fun ContentScaffold(
    content: FutureMeContent,
    onNavigate: (FutureMeScreen) -> Unit,
    onScenarioClick: (com.futureme.financialai.model.Scenario) -> Unit,
) {
    val workspace = content.workspace
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                modifier = Modifier.navigationBarsPadding(),
                containerColor = MaterialTheme.colorScheme.surface,
            ) {
                listOf(
                    FutureMeScreen.DASHBOARD to "Overview",
                    FutureMeScreen.SCENARIOS to "Scenarios",
                    FutureMeScreen.COMPARISON to "Compare",
                ).forEach { (screen, label) ->
                    NavigationBarItem(
                        selected = content.screen == screen,
                        onClick = { onNavigate(screen) },
                        icon = {
                            Text(
                                label.first().toString(),
                                fontWeight = FontWeight.Bold,
                            )
                        },
                        label = { Text(label) },
                        modifier = Modifier.semantics {
                            contentDescription = "Open $label"
                        },
                    )
                }
            }
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .statusBarsPadding()
                .padding(horizontal = 20.dp),
        ) {
            item {
                AppHeader(
                    householdName = workspace.profile.householdName,
                    modifier = Modifier.padding(vertical = 18.dp),
                )
            }
            item {
                when (content.screen) {
                    FutureMeScreen.DASHBOARD -> DashboardScreen(
                        workspace = workspace,
                        onScenarioClick = onScenarioClick,
                    )
                    FutureMeScreen.SCENARIOS -> ScenarioListScreen(
                        scenarios = workspace.scenarios,
                        onScenarioClick = onScenarioClick,
                    )
                    FutureMeScreen.SCENARIO_DETAIL -> ScenarioDetailScreen(
                        result = workspace.selectedResult,
                        onBack = { onNavigate(FutureMeScreen.SCENARIOS) },
                        onCompare = { onNavigate(FutureMeScreen.COMPARISON) },
                    )
                    FutureMeScreen.COMPARISON -> ComparisonScreen(workspace.comparison)
                }
            }
        }
    }
}

@Composable
private fun AppHeader(
    householdName: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "FM",
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(
                    MaterialTheme.colorScheme.primary,
                    androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                )
                .padding(12.dp),
        )
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Eyebrow("Financial digital twin")
            Text(
                "Your future, modeled clearly.",
                style = MaterialTheme.typography.titleLarge,
            )
            Text(
                householdName,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}
