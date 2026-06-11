package com.futureme.financialai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.futureme.financialai.presentation.FutureMeContent
import com.futureme.financialai.ui.components.Eyebrow
import com.futureme.financialai.ui.components.GpsTrajectoryChart
import com.futureme.financialai.ui.components.SectionTitle
import com.futureme.financialai.util.compactMoney
import com.futureme.financialai.util.money
import com.futureme.shared.models.GoalProbabilityResult
import com.futureme.shared.models.Insight
import com.futureme.shared.models.LifeEventPlan
import com.futureme.shared.models.MoneyLeak

@Composable
fun CopilotHubScreen(
    content: FutureMeContent,
    onPlanScenario: (String) -> Unit,
    onAskAssistant: (String) -> Unit,
    onOpenMoneyLeaks: () -> Unit,
) {
    Column {
        SectionTitle(
            eyebrow = "Proactive copilot",
            title = "Your complete financial action plan",
        )
        Text(
            "The same deterministic engines power Android, iOS, and web. AI explains the results.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp),
        )

        SectionTitle(
            eyebrow = "Proactive insights",
            title = "Every signal worth reviewing",
            modifier = Modifier.padding(top = 28.dp),
        )
        content.insights.forEach { InsightCard(it) }

        FinancialGpsDetail(
            content = content,
            onAskAssistant = onAskAssistant,
            modifier = Modifier.padding(top = 28.dp),
        )

        SectionTitle(
            eyebrow = "Goal readiness",
            title = "What stands between you and each goal",
            modifier = Modifier.padding(top = 28.dp),
        )
        content.goals.forEach { GoalCard(it) }

        LifeEventPlannerScreen(
            events = content.lifeEvents,
            onPlanScenario = onPlanScenario,
            modifier = Modifier.padding(top = 28.dp),
        )

        OutlinedButton(
            onClick = onOpenMoneyLeaks,
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
        ) {
            Text("Review all money leaks")
        }
    }
}

@Composable
private fun InsightCard(insight: Insight) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .semantics {
                contentDescription = "${insight.title}. ${insight.summary}. ${insight.recommendedAction}"
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Eyebrow(insight.category.name.replace('_', ' '))
                if (insight.estimatedDollarImpact > 0) {
                    Text(
                        money(insight.estimatedDollarImpact),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            Text(insight.title, style = MaterialTheme.typography.titleMedium)
            Text(
                insight.summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 5.dp),
            )
            Text(
                insight.recommendedAction,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 9.dp),
            )
        }
    }
}

@Composable
private fun FinancialGpsDetail(
    content: FutureMeContent,
    onAskAssistant: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val gps = content.financialGps
    Column(modifier = modifier) {
        SectionTitle(
            eyebrow = "Financial GPS",
            title = "Current route versus improved route",
        )
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Eyebrow("Current trajectory")
                        Text(
                            compactMoney(gps.currentFiveYearNetWorth),
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                    Column {
                        Eyebrow("Improved trajectory")
                        Text(
                            compactMoney(gps.improvedFiveYearNetWorth),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge,
                        )
                    }
                }
                GpsTrajectoryChart(
                    gps = gps,
                    modifier = Modifier.fillMaxWidth().height(165.dp).padding(top = 14.dp),
                )
                Text(
                    "${money(gps.difference)} potential five-year lift · " +
                        "${gps.confidenceLevel.name.lowercase()} confidence",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 10.dp),
                )
                Text(
                    gps.explanation,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
                gps.monthlyActionPlan.forEach { action ->
                    Text(
                        "•  $action",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 7.dp),
                    )
                }
                OutlinedButton(
                    onClick = { onAskAssistant("How can I improve my 5-year outlook?") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    Text("Explain my improved route")
                }
            }
        }
    }
}

@Composable
private fun GoalCard(goal: GoalProbabilityResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .semantics {
                contentDescription = "${goal.title}, ${goal.probabilityPercentage} percent ready"
            },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(goal.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${goal.probabilityPercentage}%",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                goal.explanation,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp),
            )
            if (goal.blockers.isNotEmpty()) {
                Text(
                    "BLOCKERS",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(top = 12.dp),
                )
                goal.blockers.forEach { blocker ->
                    Text("•  $blocker", style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                "RECOMMENDED ACTIONS",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 12.dp),
            )
            goal.recommendedActions.forEach { action ->
                Text("•  $action", style = MaterialTheme.typography.bodySmall)
            }
            Text(
                "Monthly improvement ${money(goal.requiredMonthlyImprovement)} · " +
                    "Modeled ready ${goal.projectedReadyDate}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
fun MoneyLeakScreen(leaks: List<MoneyLeak>) {
    Column {
        SectionTitle(
            eyebrow = "Money leak detector",
            title = "Keep more of what you earn",
        )
        Text(
            "Deterministic rules scan mock transactions, rates, cash, insurance, and retirement benefits.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp),
        )
        leaks.forEach { leak ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .semantics { contentDescription = "${leak.title}, ${money(leak.estimatedAnnualLoss)} annual impact" },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(17.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Eyebrow(leak.difficulty.name)
                        Text(
                            "${money(leak.estimatedMonthlyLoss)}/mo",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelLarge,
                        )
                    }
                    Text(leak.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        leak.summary,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                    Text(
                        "Five-year impact: ${money(leak.estimatedFiveYearLoss)}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Text(
                        leak.fixRecommendation,
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun LifeEventPlannerScreen(
    events: List<LifeEventPlan>,
    onPlanScenario: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SectionTitle(
            eyebrow = "Life event planner",
            title = "Plan the moments that change everything",
        )
        events.forEach { event ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(17.dp)) {
                    Eyebrow(event.type.name.replace('_', ' '))
                    Text(event.title, style = MaterialTheme.typography.titleMedium)
                    Text(
                        event.subtitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(
                        "Monthly impact ${money(event.estimatedMonthlyImpact)} · " +
                            "Upfront ${money(event.oneTimeCostLow)}–${money(event.oneTimeCostHigh)}",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 10.dp),
                    )
                    Text(
                        "Risk impact +${event.riskImpact}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                    event.recommendedPreparationSteps.take(3).forEach { step ->
                        Text(
                            "•  $step",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    event.relatedInsights.forEach { relatedInsight ->
                        Text(
                            "Related signal: $relatedInsight",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                    Button(
                        onClick = {
                            event.suggestedScenarioIds.firstOrNull()?.let(onPlanScenario)
                        },
                        enabled = event.suggestedScenarioIds.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp)
                            .semantics { contentDescription = "Plan ${event.title}" },
                    ) {
                        Text("Plan this event")
                    }
                }
            }
        }
    }
}
