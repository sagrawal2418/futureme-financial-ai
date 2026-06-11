package com.futureme.financialai.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.futureme.financialai.presentation.FutureMeContent
import com.futureme.financialai.ui.components.Eyebrow
import com.futureme.financialai.ui.components.SectionTitle
import com.futureme.financialai.util.compactMoney
import com.futureme.financialai.util.money
import com.futureme.shared.models.LifeReadinessResult
import com.futureme.shared.models.LifeTimelinePoint
import com.futureme.shared.models.ReadinessCategory

@Composable
fun LifeReadinessDashboardScreen(
    content: FutureMeContent,
    onAskCoach: (String) -> Unit,
) {
    val dashboardCategories = setOf(
        ReadinessCategory.HOME_PURCHASE,
        ReadinessCategory.CHILD,
        ReadinessCategory.RETIREMENT,
        ReadinessCategory.RELOCATION,
        ReadinessCategory.PARENT_SUPPORT,
    )
    var selectedCategory by remember {
        mutableStateOf(ReadinessCategory.HOME_PURCHASE)
    }
    val selected = content.readiness.first { it.category == selectedCategory }
    val plan = content.readinessPlans.first { it.category == selectedCategory }

    Column {
        SectionTitle(
            eyebrow = "Life readiness dashboard",
            title = "Are you ready for what comes next?",
        )
        Text(
            "One shared model measures cash flow, reserves, debt pressure, income resilience, " +
                "and decision-specific costs.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp),
        )

        content.readiness.filter { it.category in dashboardCategories }.forEach { readiness ->
            ReadinessCard(
                readiness = readiness,
                selected = readiness.category == selectedCategory,
                onClick = { selectedCategory = readiness.category },
            )
        }

        ImprovementPlanCard(
            readiness = selected,
            targetScore = plan.targetScore,
            monthlyCommitment = plan.monthlyCommitment,
            timelineMonths = plan.estimatedTimelineMonths,
            targetDate = plan.projectedTargetDate,
            recommendations = plan.recommendations,
            modifier = Modifier.padding(top = 20.dp),
        )

        Button(
            onClick = { onAskCoach("What should I focus on this month?") },
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
        ) {
            Text("Ask my AI coach")
        }

        SectionTitle(
            eyebrow = "Executive demo",
            title = content.executiveDemo.personaTitle,
            modifier = Modifier.padding(top = 28.dp),
        )
        Text(
            content.executiveDemo.personaSummary,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 6.dp),
        )
        content.executiveDemo.steps.forEach { step ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 9.dp)
                    .clickable { onAskCoach(step.coachPrompt) }
                    .semantics {
                        contentDescription = "Open demo step ${step.order}: ${step.title}"
                    },
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Row(modifier = Modifier.padding(15.dp)) {
                    Text(
                        step.order.toString(),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                    Column(modifier = Modifier.padding(start = 12.dp)) {
                        Text(step.title, style = MaterialTheme.typography.titleSmall)
                        Text(
                            step.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReadinessCard(
    readiness: LifeReadinessResult,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 11.dp)
            .clickable(onClick = onClick)
            .semantics {
                contentDescription =
                    "${readiness.title}, ${readiness.readinessScore} percent, " +
                        readiness.readinessLevel.name.replace('_', ' ')
            },
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(modifier = Modifier.padding(17.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Eyebrow(readiness.confidenceLevel.name + " confidence")
                    Text(readiness.title, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    "${readiness.readinessScore}%",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
            LinearProgressIndicator(
                progress = { readiness.readinessScore / 100f },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            )
            Text(
                readiness.readinessLevel.name.replace('_', ' ').lowercase(),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
            Text(
                "${readiness.trend.name.lowercase()} " +
                    "${if (readiness.trendDelta >= 0) "+" else ""}${readiness.trendDelta} pts / 6 mo",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                readiness.blockers.firstOrNull() ?: "No active blockers in the current model.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 7.dp),
            )
            Text(
                if (readiness.estimatedMonthsToReady == 0) {
                    "Ready now"
                } else {
                    "Estimated ready ${readiness.projectedReadyDate}"
                },
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun ImprovementPlanCard(
    readiness: LifeReadinessResult,
    targetScore: Int,
    monthlyCommitment: Double,
    timelineMonths: Int,
    targetDate: String,
    recommendations: List<String>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            SectionTitle(
                eyebrow = "Readiness improvement plan",
                title = "${readiness.readinessScore}% to $targetScore%",
            )
            Text(
                "$timelineMonths months to the modeled target ($targetDate)",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp),
            )
            recommendations.forEachIndexed { index, action ->
                Text(
                    "${index + 1}. $action",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 9.dp),
                )
            }
            Text(
                "Modeled monthly commitment ${money(monthlyCommitment)}",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 13.dp),
            )
        }
    }
}

@Composable
fun LifeTimelineScreen(points: List<LifeTimelinePoint>) {
    Column {
        SectionTitle(
            eyebrow = "Life timeline",
            title = "See readiness change before the decision arrives",
        )
        Text(
            "Today, 6 months, 1 year, 3 years, and 5 years use the same deterministic profile.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp, bottom = 5.dp),
        )
        points.forEach { point ->
            val average = point.readinessScores.map { it.score }.average().toInt()
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(modifier = Modifier.padding(17.dp)) {
                    Eyebrow(point.label)
                    Text(
                        "${compactMoney(point.netWorth)} net worth",
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TimelineMetric("AVG READINESS", "$average%")
                        TimelineMetric("DEBT", compactMoney(point.debtBalance))
                        TimelineMetric("INVESTMENTS", compactMoney(point.investmentBalance))
                    }
                    if (point.completedGoals.isNotEmpty()) {
                        Text(
                            point.completedGoals.joinToString(" | "),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimelineMetric(label: String, value: String) {
    Column {
        Eyebrow(label)
        Text(value, style = MaterialTheme.typography.labelLarge)
    }
}
