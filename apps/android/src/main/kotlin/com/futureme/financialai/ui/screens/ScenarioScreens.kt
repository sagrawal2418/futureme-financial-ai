package com.futureme.financialai.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.LifeDecisionSimulation
import com.futureme.shared.models.ScenarioResult
import com.futureme.shared.models.ScenarioImpactHeatmap
import com.futureme.financialai.ui.components.ImpactTile
import com.futureme.financialai.ui.components.InsightCard
import com.futureme.financialai.ui.components.ProjectionCard
import com.futureme.financialai.ui.components.RiskExplanationCard
import com.futureme.financialai.ui.components.ScenarioCard
import com.futureme.financialai.ui.components.SectionTitle
import com.futureme.financialai.util.compactMoney
import com.futureme.financialai.util.money
import com.futureme.financialai.util.oneDecimal
import com.futureme.financialai.util.signedMoney

@Composable
fun ScenarioListScreen(
    scenarios: List<Scenario>,
    onScenarioClick: (Scenario) -> Unit,
) {
    Column {
        SectionTitle(
            eyebrow = "Life decision simulator",
            title = "What happens if you make the move?",
        )
        Text(
            "Every simulation uses the same transparent assumptions and mock household data.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
        )
        scenarios.forEach { scenario ->
            ScenarioCard(
                scenario = scenario,
                onClick = { onScenarioClick(scenario) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 11.dp),
            )
        }
        Spacer(Modifier.height(18.dp))
    }
}

@Composable
fun ScenarioDetailScreen(
    result: ScenarioResult,
    simulation: LifeDecisionSimulation,
    onBack: () -> Unit,
    onCompare: () -> Unit,
    heatmap: ScenarioImpactHeatmap,
    onSaveDecision: () -> Unit,
) {
    Column {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.semantics {
                contentDescription = "Back to scenario list"
            },
        ) {
            Text("<  Scenarios")
        }
        SectionTitle(
            eyebrow = "Life decision simulator",
            title = result.scenario.title,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            result.scenario.subtitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 7.dp),
        )

        Row(
            modifier = Modifier.padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(9.dp),
        ) {
            ImpactTile(
                label = "Monthly impact",
                value = signedMoney(result.monthlyCashFlowImpact),
                positive = result.monthlyCashFlowImpact >= 0.0,
                modifier = Modifier.weight(1f),
            )
            ImpactTile(
                label = "Cash runway",
                value = "${oneDecimal(result.emergencyFundMonths)} mo",
                positive = result.emergencyFundMonths >= 6.0,
                modifier = Modifier.weight(1f),
            )
        }

        ReadinessImpactCard(
            simulation = simulation,
            modifier = Modifier.padding(top = 14.dp),
        )

        ScenarioImpactHeatmapCard(
            heatmap = heatmap,
            modifier = Modifier.padding(top = 14.dp),
        )

        ProjectionCard(
            result = result,
            headline = "Projected net worth",
            value = compactMoney(result.projectedNetWorth5Years),
            modifier = Modifier.padding(top = 14.dp),
        )
        InsightCard(result, Modifier.padding(top = 14.dp))
        RiskExplanationCard(result.riskScore, Modifier.padding(top = 14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            ImpactTile(
                label = "1-year net worth",
                value = money(result.projectedNetWorth1Year),
                positive = true,
                modifier = Modifier.weight(1f),
            )
            ImpactTile(
                label = "3-year net worth",
                value = money(result.projectedNetWorth3Years),
                positive = true,
                modifier = Modifier.weight(1f),
            )
        }

        Button(
            onClick = onCompare,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .semantics {
                    contentDescription = "Open scenario comparison"
                },
        ) {
            Text("Compare scenarios")
        }
        OutlinedButton(
            onClick = onSaveDecision,
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        ) {
            Text("Save to decision journal")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun ReadinessImpactCard(
    simulation: LifeDecisionSimulation,
    modifier: Modifier = Modifier,
) {
    androidx.compose.material3.Card(
        modifier = modifier.fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                eyebrow = "Readiness impact",
                title = "${simulation.readinessScoreBefore}% to ${simulation.readinessScoreAfter}%",
            )
            Text(
                simulation.summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ImpactTile(
                    label = "Readiness",
                    value = "${if (simulation.readinessImpact >= 0) "+" else ""}" +
                        "${simulation.readinessImpact} pts",
                    positive = simulation.readinessImpact >= 0,
                    modifier = Modifier.weight(1f),
                )
                ImpactTile(
                    label = "Risk",
                    value = "${if (simulation.riskChange >= 0) "+" else ""}" +
                        "${simulation.riskChange} pts",
                    positive = simulation.riskChange <= 0,
                    modifier = Modifier.weight(1f),
                )
                ImpactTile(
                    label = "Timeline",
                    value = "${if (simulation.timelineChangeMonths >= 0) "+" else ""}" +
                        "${simulation.timelineChangeMonths} mo",
                    positive = simulation.timelineChangeMonths <= 0,
                    modifier = Modifier.weight(1f),
                )
            }
            simulation.recommendedActions.take(3).forEach { action ->
                Text(
                    "•  $action",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
        }
    }
}
