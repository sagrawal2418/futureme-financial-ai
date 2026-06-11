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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.futureme.financialai.presentation.FutureMeContent
import com.futureme.shared.models.Scenario
import com.futureme.financialai.ui.components.Eyebrow
import com.futureme.financialai.ui.components.HealthScoreCard
import com.futureme.financialai.ui.components.MetricCard
import com.futureme.financialai.ui.components.ProjectionCard
import com.futureme.financialai.ui.components.ScenarioCard
import com.futureme.financialai.ui.components.SectionTitle
import com.futureme.financialai.util.compactMoney
import com.futureme.financialai.util.money
import com.futureme.financialai.util.oneDecimal
import com.futureme.shared.models.InsightSeverity

@Composable
fun DashboardScreen(
    content: FutureMeContent,
    onScenarioClick: (Scenario) -> Unit,
    onOpenAssistant: () -> Unit,
    onOpenLifeEvents: () -> Unit,
    onOpenMoneyLeaks: () -> Unit,
    onImproveOutlook: () -> Unit,
) {
    val dashboard = content.dashboard
    val profile = content.profile
    Column {
        HealthScoreCard(
            score = dashboard.healthScore.value,
            label = dashboard.healthScore.label,
            summary = dashboard.healthScore.summary,
            monthlySurplus = money(dashboard.monthlyCashFlow),
            runway = "${oneDecimal(dashboard.emergencyFundMonths)} mo",
        )

        WeeklyCheckupCard(
            content = content,
            modifier = Modifier.padding(top = 14.dp),
        )

        FinancialGpsCard(
            content = content,
            onImproveOutlook = onImproveOutlook,
            modifier = Modifier.padding(top = 14.dp),
        )

        Row(
            modifier = Modifier.padding(top = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            MetricCard(
                badge = "NW",
                label = "Current net worth",
                value = money(dashboard.currentNetWorth),
                hint = "Assets minus liabilities",
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                badge = "EF",
                label = "Emergency fund",
                value = "${oneDecimal(dashboard.emergencyFundMonths)} mo",
                hint = "${money(profile.liquidSavings)} liquid",
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
                value = dashboard.debtPayoffMonths?.let { "$it months" } ?: "Review",
                hint = "${money(profile.creditCardDebt)} remaining",
                modifier = Modifier.weight(1f),
            )
            MetricCard(
                badge = "INV",
                label = "Investing monthly",
                value = money(profile.monthlyRetirementContribution),
                hint = "${money(profile.investmentBalance)} invested",
                modifier = Modifier.weight(1f),
            )
        }

        AlertsCard(
            alerts = dashboard.alerts,
            modifier = Modifier.padding(top = 14.dp),
        )

        MoneyLeakPreview(
            content = content,
            onOpenMoneyLeaks = onOpenMoneyLeaks,
            modifier = Modifier.padding(top = 14.dp),
        )

        GoalReadinessPreview(
            content = content,
            modifier = Modifier.padding(top = 14.dp),
        )

        SectionTitle(
            eyebrow = "Quick actions",
            title = "Move your plan forward",
            modifier = Modifier.padding(top = 28.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(onClick = { content.scenarios.firstOrNull()?.let(onScenarioClick) }) {
                Text("Simulate decision")
            }
            OutlinedButton(onClick = onOpenAssistant) {
                Text("Ask FutureMe")
            }
            OutlinedButton(onClick = onImproveOutlook) {
                Text("Improve outlook")
            }
            OutlinedButton(onClick = onOpenLifeEvents) {
                Text("Plan life event")
            }
        }

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
            content.scenarios.take(4).forEach { scenario ->
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
private fun WeeklyCheckupCard(
    content: FutureMeContent,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Eyebrow("This week's financial checkup")
            Text("Three signals worth your attention", style = MaterialTheme.typography.titleMedium)
            content.insights.take(3).forEach { insight ->
                val prefix = when (insight.severity) {
                    InsightSeverity.CRITICAL -> "Urgent"
                    InsightSeverity.WARNING -> "Watch"
                    InsightSeverity.OPPORTUNITY -> "Opportunity"
                    InsightSeverity.INFO -> "On track"
                }
                Text(
                    "$prefix · ${insight.title}",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 12.dp),
                )
                Text(
                    insight.summary,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
        }
    }
}

@Composable
private fun FinancialGpsCard(
    content: FutureMeContent,
    onImproveOutlook: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val gps = content.financialGps
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Eyebrow("Financial GPS")
            Text("A better route is available", style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MetricCard(
                    badge = "NOW",
                    label = "Current trajectory",
                    value = compactMoney(gps.currentFiveYearNetWorth),
                    hint = "Five-year net worth",
                    modifier = Modifier.weight(1f),
                )
                MetricCard(
                    badge = "GPS",
                    label = "Improved trajectory",
                    value = compactMoney(gps.improvedFiveYearNetWorth),
                    hint = "+${money(gps.difference)}",
                    modifier = Modifier.weight(1f),
                )
            }
            gps.monthlyActionPlan.forEach { action ->
                Text(
                    "•  $action",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            OutlinedButton(
                onClick = onImproveOutlook,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Text("Explain my improved route")
            }
        }
    }
}

@Composable
private fun MoneyLeakPreview(
    content: FutureMeContent,
    onOpenMoneyLeaks: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val annualLoss = content.moneyLeaks.sumOf { it.estimatedAnnualLoss }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Eyebrow("Money leaks")
            Text("${money(annualLoss)} in annual opportunity", style = MaterialTheme.typography.titleMedium)
            content.moneyLeaks.take(2).forEach { leak ->
                Text(
                    "${leak.title} · ${money(leak.estimatedMonthlyLoss)}/mo",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 9.dp),
                )
            }
            OutlinedButton(
                onClick = onOpenMoneyLeaks,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Text("Review all money leaks")
            }
        }
    }
}

@Composable
private fun GoalReadinessPreview(
    content: FutureMeContent,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Eyebrow("Goal readiness")
            Text("How close your next chapter is", style = MaterialTheme.typography.titleMedium)
            content.goals.take(3).forEach { goal ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(goal.title, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "${goal.probabilityPercentage}%",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
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
