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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.futureme.financialai.presentation.FutureMeContent
import com.futureme.financialai.ui.components.Eyebrow
import com.futureme.financialai.ui.components.SectionTitle
import com.futureme.financialai.util.compactMoney
import com.futureme.shared.models.Mission
import com.futureme.shared.models.MissionActionStatus
import com.futureme.shared.models.MissionCoachBriefing
import com.futureme.shared.models.MissionExecutionPlan
import com.futureme.shared.models.MissionSignal

@Composable
fun MissionControlScreen(
    content: FutureMeContent,
    onOpenTimeline: () -> Unit,
    onOpenActions: () -> Unit,
    onOpenSimulator: () -> Unit,
    onOpenCoach: () -> Unit,
    onAskCoach: (String) -> Unit,
    onAcceptAction: (String) -> Unit,
) {
    var selectedMissionId by remember {
        mutableStateOf(content.missionControl.activeMissions.first().missionId)
    }
    val mission = content.missions.first { it.missionId == selectedMissionId }
    val execution = content.missionExecution.plans.first { it.missionId == selectedMissionId }
    val briefing = content.missionCoachBriefings.first { it.missionId == selectedMissionId }

    Column {
        Eyebrow("MISSION CONTROL")
        Text(
            "How ready are you for your next major life decision?",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Text(
            "Your goals, readiness, blockers, next action, and timeline in one place.",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp),
        )

        MissionSummaryCard(content, Modifier.padding(top = 18.dp))

        SectionTitle(
            eyebrow = "ACTIVE MISSIONS",
            title = "Choose what matters now",
            modifier = Modifier.padding(top = 28.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            content.missionControl.activeMissions.forEach { item ->
                MissionPickerCard(
                    mission = item,
                    health = content.missionExecution.plans
                        .first { it.missionId == item.missionId }
                        .health.status.name,
                    selected = item.missionId == selectedMissionId,
                    onClick = { selectedMissionId = item.missionId },
                )
            }
        }

        MissionDetailCard(mission, execution, Modifier.padding(top = 16.dp))
        ClaudeMissionBriefingCard(
            briefing = briefing,
            onAskCoach = onAskCoach,
            modifier = Modifier.padding(top = 14.dp),
        )
        MissionNextActionCard(
            mission = mission,
            hasNextAction = execution.actionPlan.nextAction != null,
            onAccept = {
                execution.actionPlan.nextAction?.actionId?.let(onAcceptAction)
            },
            modifier = Modifier.padding(top = 14.dp),
        )

        MissionActionPlanCard(execution, Modifier.padding(top = 22.dp))

        SectionTitle(
            eyebrow = "MISSION ROADMAPS",
            title = "${mission.title} path forward",
            modifier = Modifier.padding(top = 28.dp),
        )
        execution.roadmap.stages.forEach { stage ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stage.label, fontWeight = FontWeight.Bold)
                        Text(
                            stage.upcomingActions.joinToString(" • ") { it.title }
                                .ifBlank { "Maintain completed actions." },
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        Text(
                            "${stage.completedActions.size} complete • projected " +
                                stage.projectedCompletionDate,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 7.dp),
                        )
                    }
                    Text(
                        "+${stage.expectedReadinessGrowth}",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
        OutlinedButton(
            onClick = onOpenTimeline,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            Text("Open Mission Timeline")
        }

        MissionHealthCard(execution, Modifier.padding(top = 22.dp))
        MissionNotificationCenter(content, Modifier.padding(top = 22.dp))
        MissionHistoryCard(execution, Modifier.padding(top = 22.dp))
        MissionScenarioCard(execution, onOpenSimulator, Modifier.padding(top = 22.dp))

        SectionTitle(
            eyebrow = "MISSION RISKS",
            title = "What needs attention",
            modifier = Modifier.padding(top = 28.dp),
        )
        content.missionControl.risks.forEach { signal ->
            MissionSignalCard(signal, Modifier.padding(top = 10.dp))
        }

        SectionTitle(
            eyebrow = "MISSION OPPORTUNITIES",
            title = "Ways to move faster",
            modifier = Modifier.padding(top = 28.dp),
        )
        content.missionControl.opportunities.forEach { signal ->
            MissionSignalCard(signal, Modifier.padding(top = 10.dp))
        }

        MissionAnalyticsCard(content, Modifier.padding(top = 22.dp))

        SectionTitle(
            eyebrow = "SUPPORTING SERVICES",
            title = "Go deeper when you need the numbers",
            modifier = Modifier.padding(top = 28.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Button(onClick = onOpenCoach) { Text("Mission Coach") }
            OutlinedButton(onClick = onOpenActions) { Text("Financial actions") }
            OutlinedButton(onClick = onOpenSimulator) { Text("Simulate decision") }
        }
        Spacer(Modifier.height(28.dp))
    }
}

@Composable
private fun ClaudeMissionBriefingCard(
    briefing: MissionCoachBriefing,
    onAskCoach: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val explanations = listOf(
        "Why not ready?" to briefing.whyNotReady,
        "What improved?" to briefing.whatImprovedRecently,
        "What is hurting?" to briefing.whatIsHurtingProgress,
        "What should I focus on?" to briefing.whatShouldIFocusOn,
        "How do I move faster?" to briefing.howCanIAccelerateTimeline,
        "What if I do nothing?" to briefing.whatHappensIfIDoNothing,
    )
    var selectedExplanation by remember(briefing.missionId) { mutableStateOf(3) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF30235F),
            contentColor = Color.White,
        ),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "CLAUDE MISSION COACH",
                color = Color(0xFFC9BFFF),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "Your mission briefing",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                briefing.coachingSummary,
                color = Color(0xFFF2EEFF),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 12.dp),
            )

            CoachSignal("FOCUS NOW", briefing.recommendedFocusArea)
            CoachSignal("TOP RISK", briefing.topRisk)
            CoachSignal("BEST OPPORTUNITY", briefing.topOpportunity)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 15.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                explanations.forEachIndexed { index, explanation ->
                    OutlinedButton(onClick = { selectedExplanation = index }) {
                        Text(explanation.first)
                    }
                }
            }
            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F2FF),
                    contentColor = Color(0xFF30235F),
                ),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        explanations[selectedExplanation].first.uppercase(),
                        color = Color(0xFF6B55B6),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        explanations[selectedExplanation].second,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
            }
            Text(
                "WHAT CHANGED",
                color = Color(0xFFC9BFFF),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 15.dp),
            )
            Text(
                briefing.whatChanged.first(),
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 3.dp),
            )

            Text(
                "SUGGESTED QUESTIONS",
                color = Color(0xFFC9BFFF),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 17.dp),
            )
            briefing.suggestedQuestions.forEach { question ->
                OutlinedButton(
                    onClick = { onAskCoach(question.prompt) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) {
                    Text(question.title)
                }
            }
            Text(
                if (briefing.isFallback) "Demo fallback • ${briefing.modelLabel}" else briefing.modelLabel,
                color = Color(0xFFB8AEEB),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun CoachSignal(label: String, value: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 14.dp),
    ) {
        Text(
            label,
            color = Color(0xFFC9BFFF),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            value,
            color = Color.White,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 3.dp),
        )
    }
}

@Composable
private fun MissionSummaryCard(
    content: FutureMeContent,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "MISSION PROGRESS",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.72f),
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                "${content.missionControl.missionProgressPercentage}%",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
            )
            LinearProgressIndicator(
                progress = { content.missionControl.missionProgressPercentage / 100f },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "MOST READY",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        content.missionControl.highestReadinessMission.title,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "NEEDS FOCUS",
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                        style = MaterialTheme.typography.labelSmall,
                    )
                    Text(
                        content.missionControl.lowestReadinessMission.title,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionPickerCard(
    mission: Mission,
    health: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = Modifier.width(190.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
        ),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(mission.title, fontWeight = FontWeight.Bold)
            Text(
                "${health.lowercase()} health",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 3.dp),
            )
            Text(
                "${mission.readinessScore}%",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 14.dp),
            )
            LinearProgressIndicator(
                progress = { mission.progressPercentage / 100f },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun MissionDetailCard(
    mission: Mission,
    execution: MissionExecutionPlan,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Eyebrow("MISSION DETAIL")
            Text(
                mission.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )
            Text(
                mission.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                "Mission Readiness ${mission.readinessScore}% • " +
                    "${execution.health.status.name.lowercase()} health",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 18.dp),
            )
            mission.readinessFactors.forEach { factor ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 11.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(factor.title, style = MaterialTheme.typography.bodyMedium)
                    Text("${factor.score}%", fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { factor.score / 100f },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }
            Text(
                "BIGGEST BLOCKER",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(top = 20.dp),
            )
            Text(
                mission.blockers.first(),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun MissionActionPlanCard(
    execution: MissionExecutionPlan,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Eyebrow("MISSION ACTION ENGINE")
            Text(
                "Your dynamic action plan",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                "${execution.progress.completedActions} of ${execution.progress.totalActions} complete",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 4.dp),
            )
            execution.actionPlan.actions.forEachIndexed { index, action ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (action.completionStatus == MissionActionStatus.LOCKED) {
                            MaterialTheme.colorScheme.surfaceVariant
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                    ),
                ) {
                    Column(modifier = Modifier.padding(15.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                "${index + 1}. ${action.title}",
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                action.completionStatus.name.replace('_', ' ').lowercase(),
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                        Text(
                            action.blockerMessage ?: action.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 5.dp),
                        )
                        Text(
                            "+${action.readinessGain} readiness • ${action.effort.name.lowercase()} effort",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(top = 8.dp),
                        )
                        LinearProgressIndicator(
                            progress = { action.metricProgressPercentage / 100f },
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MissionHealthCard(
    execution: MissionExecutionPlan,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Eyebrow("MISSION HEALTH")
            Text(
                "${execution.health.status.name} • ${execution.health.score}/100",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                execution.health.summary,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 5.dp),
            )
            execution.health.factors.forEach { factor ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(factor.title)
                    Text(
                        if (factor.triggered) factor.explanation else "Clear",
                        color = if (factor.triggered) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f).padding(start = 12.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionNotificationCenter(
    content: FutureMeContent,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Eyebrow("MISSION NOTIFICATIONS")
            Text(
                "Notification center",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            content.missionExecution.notifications.take(5).forEach { notification ->
                Column(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                    Text(notification.title, fontWeight = FontWeight.Bold)
                    Text(
                        notification.message,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun MissionHistoryCard(
    execution: MissionExecutionPlan,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Eyebrow("MISSION HISTORY")
            Text(
                "Readiness history graph",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            execution.history.points.forEach { point ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(point.date)
                    Text("${point.readinessScore}%", fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { point.readinessScore / 100f },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                )
            }
        }
    }
}

@Composable
private fun MissionScenarioCard(
    execution: MissionExecutionPlan,
    onOpenSimulator: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Eyebrow("MISSION SCENARIOS")
            Text(
                "Evaluate the decision",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            execution.scenarioImpacts.forEach { scenario ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(scenario.title, fontWeight = FontWeight.Bold)
                    Text(
                        "${if (scenario.readinessImpact >= 0) "+" else ""}" +
                            "${scenario.readinessImpact} ready • " +
                            "${scenario.timelineImpactMonths} mo",
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            OutlinedButton(
                onClick = onOpenSimulator,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text("Open mission scenario evaluator")
            }
        }
    }
}

@Composable
private fun MissionNextActionCard(
    mission: Mission,
    hasNextAction: Boolean,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Eyebrow("NEXT BEST ACTION")
            Text(
                mission.nextAction.title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Text(
                mission.nextAction.description,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(top = 7.dp),
            )
            Text(
                "+${mission.nextAction.estimatedReadinessIncrease} readiness points  •  " +
                    "${mission.nextAction.estimatedTimelineReductionMonths} months faster  •  " +
                    "${compactMoney(mission.nextAction.fiveYearBenefitEstimate)} at 5 years",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 14.dp),
            )
            Button(
                onClick = onAccept,
                enabled = hasNextAction,
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text(if (hasNextAction) "Mark action complete" else "Mission complete")
            }
        }
    }
}

@Composable
private fun MissionSignalCard(
    signal: MissionSignal,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(signal.title, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text(signal.impactLabel, color = MaterialTheme.colorScheme.primary)
            }
            Text(
                signal.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
    }
}

@Composable
private fun MissionAnalyticsCard(
    content: FutureMeContent,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
            Eyebrow("MISSION ANALYTICS")
            Text(
                "Progress across every mission",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("${content.missionAnalytics.readinessImprovements} readiness gains")
                Text("${content.missionAnalytics.actionsCompleted} actions done")
                Text("${content.missionAnalytics.timelineImprovements} months saved")
            }
            content.missionAnalytics.trends.take(3).forEach { trend ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(trend.title)
                    Text(
                        "+${trend.readinessChange} pts",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}
