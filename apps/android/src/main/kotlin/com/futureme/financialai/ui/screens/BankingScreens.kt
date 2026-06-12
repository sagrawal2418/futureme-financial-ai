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
import androidx.compose.ui.unit.dp
import com.futureme.financialai.presentation.FutureMeContent
import com.futureme.financialai.ui.components.Eyebrow
import com.futureme.financialai.ui.components.SectionTitle
import com.futureme.financialai.util.compactMoney
import com.futureme.financialai.util.money
import com.futureme.shared.models.ImpactSentiment
import com.futureme.shared.models.ScenarioImpactHeatmap

@Composable
fun BankingIntelligenceScreen(
    content: FutureMeContent,
    onAcceptRecommendation: () -> Unit,
    onAskAssistant: (String) -> Unit,
) {
    Column {
        SectionTitle(
            eyebrow = "Banking intelligence",
            title = "What matters next",
        )
        HighestActionDetail(content, onAcceptRecommendation)

        SectionTitle(
            eyebrow = "Ranked opportunities",
            title = "Impact, effort, and confidence",
            modifier = Modifier.padding(top = 26.dp),
        )
        content.opportunities.take(5).forEach { opportunity ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Text(
                        opportunity.priorityRanking.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(opportunity.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            "${money(opportunity.fiveYearBenefitEstimate)} potential over 5 years",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                        )
                    }
                    Text(
                        opportunity.impactScore.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
        }

        ExplainabilityCard(content, Modifier.padding(top = 24.dp))

        SectionTitle(
            eyebrow = "What improved my future?",
            title = "Top value contributors",
            modifier = Modifier.padding(top = 26.dp),
        )
        content.futureOutcomeContributions.take(4).forEach { contribution ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(contribution.title, style = MaterialTheme.typography.titleSmall)
                    Text(
                        "${contribution.sharePercentage}% of modeled improvement",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Text(
                    compactMoney(contribution.fiveYearContribution),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Button(
            onClick = {
                onAskAssistant("If I can only do one thing this month, what should it be?")
            },
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) {
            Text("Ask my financial strategist")
        }

        SectionTitle(
            eyebrow = "Executive banking demo",
            title = content.bankingVisionDemo.title,
            modifier = Modifier.padding(top = 28.dp),
        )
        content.bankingVisionDemo.steps.forEach { step ->
            Text(
                "${step.order}. ${step.title} · ${step.focusTarget}",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
    }
}

@Composable
private fun HighestActionDetail(
    content: FutureMeContent,
    onAccept: () -> Unit,
) {
    val action = content.nextBestAction
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Eyebrow("My highest impact action")
            Text(action.title, style = MaterialTheme.typography.headlineMedium)
            Text(
                action.callout,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Button(
                onClick = onAccept,
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            ) {
                Text("Make this my focus")
            }
        }
    }
}

@Composable
private fun ExplainabilityCard(
    content: FutureMeContent,
    modifier: Modifier = Modifier,
) {
    val explanation = content.financialExplainability
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Eyebrow("Why my score changed")
            Text(
                "${explanation.previousScore} to ${explanation.currentScore} " +
                    "(${if (explanation.netChange >= 0) "+" else ""}${explanation.netChange})",
                style = MaterialTheme.typography.headlineMedium,
            )
            Text(
                explanation.summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 7.dp),
            )
            explanation.factors.forEach { factor ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(factor.title, style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${if (factor.pointImpact > 0) "+" else ""}${factor.pointImpact}",
                        color = if (factor.sentiment == ImpactSentiment.POSITIVE) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
fun ScenarioImpactHeatmapCard(
    heatmap: ScenarioImpactHeatmap,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Eyebrow("Scenario impact heatmap")
            Text(heatmap.title, style = MaterialTheme.typography.titleLarge)
            heatmap.cells.chunked(2).forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    row.forEach { cell ->
                        val container = when (cell.sentiment) {
                            ImpactSentiment.POSITIVE -> MaterialTheme.colorScheme.primaryContainer
                            ImpactSentiment.NEGATIVE -> MaterialTheme.colorScheme.errorContainer
                            ImpactSentiment.NEUTRAL -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(12.dp),
                        ) {
                            Text(
                                cell.dimension.name.replace('_', ' '),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelSmall,
                            )
                            Text(
                                cell.label,
                                color = when (cell.sentiment) {
                                    ImpactSentiment.POSITIVE -> MaterialTheme.colorScheme.primary
                                    ImpactSentiment.NEGATIVE -> MaterialTheme.colorScheme.error
                                    ImpactSentiment.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MonthlyReviewScreen(content: FutureMeContent) {
    val review = content.monthlyReviews.first()
    Column {
        SectionTitle(
            eyebrow = "Monthly financial review",
            title = review.label,
        )
        Text(
            review.aiSummary,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp),
        )
        ReviewGroup("Wins", review.wins)
        ReviewGroup("Risks", review.risks)
        ReviewGroup("Recommended actions", review.recommendedActions)
        ReviewGroup("Readiness changes", review.readinessChanges)
        ReviewGroup("Goal progress", review.goalProgress)

        SectionTitle(
            eyebrow = "Financial decision journal",
            title = "Expected versus actual",
            modifier = Modifier.padding(top = 28.dp),
        )
        content.decisionJournal.forEach { entry ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(entry.title, style = MaterialTheme.typography.titleMedium)
                        Text(
                            entry.status.name.replace('_', ' '),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                    Text(
                        "Expected ${compactMoney(entry.expectedFiveYearImpact)} · " +
                            "Actual ${entry.actualFiveYearImpact?.let(::compactMoney) ?: "Tracking"}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
        }
        Text(
            "${content.analyticsEvents.size} banking events stored locally",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(vertical = 22.dp),
        )
    }
}

@Composable
private fun ReviewGroup(title: String, items: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Eyebrow(title)
            items.take(3).forEach { item ->
                Text(
                    "•  $item",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 7.dp),
                )
            }
        }
    }
}
