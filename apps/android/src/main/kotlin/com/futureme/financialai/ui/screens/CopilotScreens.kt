package com.futureme.financialai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.futureme.financialai.ui.components.Eyebrow
import com.futureme.financialai.ui.components.SectionTitle
import com.futureme.financialai.util.money
import com.futureme.shared.models.LifeEventPlan
import com.futureme.shared.models.MoneyLeak

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
) {
    Column {
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
                    event.recommendedPreparationSteps.take(3).forEach { step ->
                        Text(
                            "•  $step",
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
