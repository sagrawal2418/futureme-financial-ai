package com.futureme.financialai.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.futureme.financialai.model.FinancialWorkspace
import com.futureme.financialai.model.Scenario
import com.futureme.financialai.ui.components.Eyebrow
import com.futureme.financialai.ui.components.HealthScoreCard
import com.futureme.financialai.ui.components.MetricCard
import com.futureme.financialai.ui.components.ProjectionCard
import com.futureme.financialai.ui.components.ScenarioCard
import com.futureme.financialai.ui.components.SectionTitle
import com.futureme.financialai.util.compactMoney
import com.futureme.financialai.util.money
import com.futureme.financialai.util.oneDecimal

@Composable
fun DashboardScreen(
    workspace: FinancialWorkspace,
    onScenarioClick: (Scenario) -> Unit,
) {
    Column {
        HealthScoreCard(
            score = workspace.dashboard.healthScore,
            monthlySurplus = money(workspace.dashboard.monthlySurplus),
            runway = "${oneDecimal(workspace.dashboard.emergencyFundMonths)} mo",
        )

        ProjectionCard(
            result = workspace.selectedResult,
            headline = "Five-year outlook",
            value = compactMoney(workspace.dashboard.projectedNetWorth5Years),
            modifier = Modifier.padding(top = 14.dp),
        )

        Row(
            modifier = Modifier.padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                badge = "NW",
                label = "Current net worth",
                value = money(workspace.dashboard.currentNetWorth),
                hint = "Assets minus liabilities",
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                badge = "EF",
                label = "Emergency fund",
                value = "${oneDecimal(workspace.dashboard.emergencyFundMonths)} mo",
                hint = "${money(workspace.profile.liquidSavings)} liquid",
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                badge = "DT",
                label = "Debt-free estimate",
                value = workspace.dashboard.debtPayoffMonths?.let { "$it months" } ?: "Review",
                hint = "${money(workspace.profile.creditCardDebt)} remaining",
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                badge = "INV",
                label = "Investing monthly",
                value = money(workspace.profile.monthlyRetirementContribution),
                hint = "${money(workspace.profile.investmentBalance)} invested",
                modifier = Modifier.weight(1f),
            )
        }

        AlertsCard(
            alerts = workspace.dashboard.alerts,
            modifier = Modifier.padding(top = 14.dp),
        )

        SectionTitle(
            eyebrow = "Decision lab",
            title = "Explore a different future",
            modifier = Modifier.padding(top = 30.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .horizontalScroll(rememberScrollState()),
        ) {
            workspace.scenarios.take(4).forEach { scenario ->
                ScenarioCard(
                    scenario = scenario,
                    onClick = { onScenarioClick(scenario) },
                    modifier = Modifier.width(220.dp),
                )
                Spacer(Modifier.width(11.dp))
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun AlertsCard(
    alerts: List<String>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Eyebrow("Priority signals")
            Text("What deserves attention", style = MaterialTheme.typography.titleMedium)
            if (alerts.isEmpty()) {
                Text(
                    "No urgent alerts. Your plan is within the configured thresholds.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            } else {
                alerts.forEach { alert ->
                    Text(
                        "•  $alert",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}
