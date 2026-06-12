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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.futureme.financialai.presentation.FutureMeContent
import com.futureme.financialai.ui.components.Eyebrow
import com.futureme.financialai.ui.components.SectionTitle
import com.futureme.financialai.util.compactMoney
import com.futureme.shared.models.Mission
import com.futureme.shared.models.MissionSignal

@Composable
fun MissionControlScreen(
    content: FutureMeContent,
    onOpenTimeline: () -> Unit,
    onOpenActions: () -> Unit,
    onOpenSimulator: () -> Unit,
    onOpenCoach: () -> Unit,
    onAcceptAction: (String) -> Unit,
) {
    var selectedMissionId by remember {
        mutableStateOf(content.missionControl.activeMissions.first().missionId)
    }
    val mission = content.missions.first { it.missionId == selectedMissionId }

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
                    selected = item.missionId == selectedMissionId,
                    onClick = { selectedMissionId = item.missionId },
                )
            }
        }

        MissionDetailCard(mission, Modifier.padding(top = 16.dp))
        MissionNextActionCard(
            mission = mission,
            onAccept = { onAcceptAction(mission.nextAction.id) },
            modifier = Modifier.padding(top = 14.dp),
        )

        SectionTitle(
            eyebrow = "MISSION TIMELINE",
            title = "${mission.title} path forward",
            modifier = Modifier.padding(top = 28.dp),
        )
        mission.timeline.forEach { point ->
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
                        Text(point.label, fontWeight = FontWeight.Bold)
                        Text(
                            point.milestone,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                    Text(
                        "${point.readinessScore}%",
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
                mission.status.name.replace('_', ' ').lowercase(),
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
                "Mission Readiness ${mission.readinessScore}%",
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
private fun MissionNextActionCard(
    mission: Mission,
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
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            ) {
                Text("Make this my focus")
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
