package com.futureme.financialai.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.futureme.financialai.presentation.FutureMeContent
import com.futureme.financialai.ui.components.Eyebrow
import com.futureme.financialai.ui.components.SectionTitle
import com.futureme.financialai.util.compactMoney
import com.futureme.financialai.util.money
import com.futureme.shared.models.CustomerPersona

@Composable
fun ProductHomeScreen(
    content: FutureMeContent,
    onOpenMissions: () -> Unit,
    onOpenCoach: () -> Unit,
) {
    val risk = content.missionControl.risks.first()
    val opportunity = content.missionControl.opportunities.first()

    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    "YOUR HIGHEST-IMPACT ACTION",
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    content.nextBestAction.title,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 7.dp),
                )
                Text(
                    content.nextBestAction.callout,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.82f),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 9.dp),
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    HomeMetric("5-year impact", compactMoney(content.nextBestAction.fiveYearImpact))
                    HomeMetric("Confidence", "${content.nextBestAction.confidenceScore}%")
                    HomeMetric("Monthly effort", money(content.nextBestAction.monthlyCommitment))
                }
                Button(
                    onClick = onOpenMissions,
                    modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                ) {
                    Text("Add to this month's plan")
                }
            }
        }

        SectionTitle(
            eyebrow = "TODAY",
            title = "The three signals that matter",
            modifier = Modifier.padding(top = 24.dp),
        )
        ProductSignalCard(
            "Readiness change",
            "${if (content.financialExplainability.netChange >= 0) "+" else ""}" +
                "${content.financialExplainability.netChange} points",
            content.financialExplainability.summary,
        )
        ProductSignalCard("Biggest risk", risk.title, risk.description)
        ProductSignalCard("Biggest opportunity", opportunity.title, opportunity.description)

        SectionTitle(
            eyebrow = "ACTIVE MISSIONS",
            title = "What you are preparing for",
            modifier = Modifier.padding(top = 24.dp),
        )
        content.missionControl.activeMissions.take(4).forEach { mission ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(mission.title, fontWeight = FontWeight.Bold)
                    Text(
                        mission.blockers.first(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    mission.readinessScore.toString(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )
            }
        }

        OutlinedButton(
            onClick = onOpenCoach,
            modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 24.dp),
        ) {
            Text("Ask why this action matters")
        }
    }
}

@Composable
fun ProductInsightsScreen(content: FutureMeContent) {
    val review = content.monthlyReviews.first()
    Column {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        ) {
            Column(modifier = Modifier.padding(21.dp)) {
                Text(
                    review.label.uppercase(),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    review.aiSummary,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(top = 7.dp),
                )
            }
        }
        InsightGroup("RISKS", review.risks)
        InsightGroup("OPPORTUNITIES", review.opportunities)
        InsightGroup("RECOMMENDED ACTIONS", review.recommendedActions)

        SectionTitle(
            eyebrow = "MONEY LEAKS",
            title = "Costs worth fixing",
            modifier = Modifier.padding(top = 26.dp),
        )
        content.moneyLeaks.take(4).forEach { leak ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(leak.title, fontWeight = FontWeight.Bold)
                        Text(
                            leak.fixRecommendation,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text(
                        "${money(leak.estimatedAnnualLoss)}/yr",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
fun ProductProfileScreen(content: FutureMeContent) {
    var selectedPersonaId by remember { mutableStateOf(content.customerPersonas.first().id) }
    val persona = content.customerPersonas.first { it.id == selectedPersonaId }

    Column {
        SectionTitle(eyebrow = "FINANCIAL PROFILE", title = content.identity.householdName)
        ProfileCard(
            listOf(
                "Annual income" to money(content.profile.annualGrossIncome),
                "Monthly take-home" to money(content.profile.monthlyNetIncome),
                "Liquid savings" to money(content.profile.liquidSavings),
                "Investments" to money(content.profile.investmentBalance),
            ),
        )

        SectionTitle(
            eyebrow = "REALISTIC CUSTOMER JOURNEYS",
            title = "Test five different households",
            modifier = Modifier.padding(top = 26.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            content.customerPersonas.forEach { item ->
                OutlinedButton(onClick = { selectedPersonaId = item.id }) {
                    Text(item.title)
                }
            }
        }
        PersonaCard(persona, Modifier.padding(top = 12.dp))

        SectionTitle(
            eyebrow = "EXECUTIVE DEMO MODE",
            title = content.executiveDemoStory.title,
            modifier = Modifier.padding(top = 26.dp),
        )
        Text(
            content.executiveDemoStory.opening,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        content.executiveDemoStory.steps.forEachIndexed { index, step ->
            Text(
                "${index + 1}. $step",
                modifier = Modifier.padding(top = 10.dp),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        Text(
            content.executiveDemoStory.closing,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 18.dp, bottom = 24.dp),
        )
    }
}

@Composable
private fun HomeMetric(label: String, value: String) {
    Column {
        Text(
            label,
            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.66f),
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            value,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun ProductSignalCard(eyebrow: String, title: String, body: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Eyebrow(eyebrow)
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                body,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
    }
}

@Composable
private fun InsightGroup(title: String, items: List<String>) {
    SectionTitle(
        eyebrow = title,
        title = title.lowercase().replaceFirstChar { it.uppercase() },
        modifier = Modifier.padding(top = 24.dp),
    )
    items.take(4).forEach { item ->
        Text("• $item", modifier = Modifier.padding(top = 9.dp))
    }
}

@Composable
private fun ProfileCard(rows: List<Pair<String, String>>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            rows.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 7.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(row.first, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(row.second, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun PersonaCard(
    persona: CustomerPersona,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Eyebrow(persona.title)
            Text(persona.summary, style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 14.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                persona.expectedReadinessScores.forEach { score ->
                    Column(modifier = Modifier.padding(end = 14.dp)) {
                        Text(
                            score.score.toString(),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        Text(score.category, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            persona.expectedMissionPlan.forEachIndexed { index, action ->
                Text("${index + 1}. $action", modifier = Modifier.padding(top = 9.dp))
            }
        }
    }
}
