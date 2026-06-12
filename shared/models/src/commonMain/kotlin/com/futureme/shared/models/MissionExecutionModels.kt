package com.futureme.shared.models

import kotlinx.serialization.Serializable

@Serializable
enum class MissionActionCategory {
    DEBT,
    SAVINGS,
    CASH_FLOW,
    EMERGENCY_FUND,
    INVESTING,
    RISK,
    PLANNING,
}

@Serializable
enum class MissionActionEffort {
    LOW,
    MEDIUM,
    HIGH,
}

@Serializable
enum class MissionActionImpact {
    LOW,
    MEDIUM,
    HIGH,
}

@Serializable
enum class MissionActionStatus {
    LOCKED,
    AVAILABLE,
    IN_PROGRESS,
    COMPLETED,
    MISSED,
}

@Serializable
data class MissionAction(
    val actionId: String,
    val missionId: String,
    val title: String,
    val description: String,
    val category: MissionActionCategory,
    val effort: MissionActionEffort,
    val impact: MissionActionImpact,
    val readinessGain: Int,
    val targetDate: String,
    val completionStatus: MissionActionStatus,
    val dependencyActionIds: List<String> = emptyList(),
    val blockerMessage: String? = null,
    val metricLabel: String,
    val currentMetricValue: Double,
    val targetMetricValue: Double,
    val metricProgressPercentage: Int,
)

@Serializable
data class MissionActionPlan(
    val missionId: String,
    val actions: List<MissionAction>,
    val nextAction: MissionAction?,
    val blockedActions: List<MissionAction>,
    val unlockedActions: List<MissionAction>,
)

@Serializable
data class MissionProgressSnapshot(
    val missionId: String,
    val progressPercentage: Int,
    val completedActions: Int,
    val totalActions: Int,
    val actionProgressPercentage: Int,
    val metricProgressPercentage: Int,
    val readinessContributionPercentage: Int,
    val summary: String,
)

@Serializable
enum class MissionRoadmapHorizon {
    THIRTY_DAYS,
    NINETY_DAYS,
    ONE_YEAR,
}

@Serializable
data class MissionRoadmapStage(
    val horizon: MissionRoadmapHorizon,
    val label: String,
    val currentStatus: String,
    val upcomingActions: List<MissionAction>,
    val completedActions: List<MissionAction>,
    val expectedReadinessGrowth: Int,
    val projectedCompletionDate: String,
)

@Serializable
data class MissionRoadmap(
    val missionId: String,
    val stages: List<MissionRoadmapStage>,
)

@Serializable
enum class MissionHealthStatus {
    GREEN,
    YELLOW,
    RED,
}

@Serializable
data class MissionHealthFactor(
    val id: String,
    val title: String,
    val triggered: Boolean,
    val explanation: String,
    val penaltyPoints: Int,
)

@Serializable
data class MissionHealthResult(
    val missionId: String,
    val status: MissionHealthStatus,
    val score: Int,
    val factors: List<MissionHealthFactor>,
    val summary: String,
)

@Serializable
enum class MissionNotificationType {
    ACTION_UNLOCKED,
    MISSION_AT_RISK,
    READINESS_IMPROVED,
    MILESTONE_COMPLETED,
    TIMELINE_ACCELERATED,
    MISSION_COMPLETED,
}

@Serializable
data class MissionNotification(
    val notificationId: String,
    val missionId: String,
    val type: MissionNotificationType,
    val title: String,
    val message: String,
    val createdAt: String,
    val isRead: Boolean,
)

@Serializable
enum class MissionHistoryEventType {
    READINESS_CHANGED,
    ACTION_COMPLETED,
    TIMELINE_CHANGED,
    RISK_CHANGED,
    HEALTH_CHANGED,
}

@Serializable
data class MissionHistoryEvent(
    val eventId: String,
    val missionId: String,
    val type: MissionHistoryEventType,
    val occurredAt: String,
    val title: String,
    val detail: String,
)

@Serializable
data class MissionHistoryPoint(
    val date: String,
    val readinessScore: Int,
    val progressPercentage: Int,
    val riskScore: Int,
    val healthStatus: MissionHealthStatus,
)

@Serializable
data class MissionHistory(
    val missionId: String,
    val events: List<MissionHistoryEvent>,
    val points: List<MissionHistoryPoint>,
)

@Serializable
data class MissionScenarioImpact(
    val scenarioId: String,
    val title: String,
    val readinessImpact: Int,
    val timelineImpactMonths: Int,
    val riskImpact: Int,
    val summary: String,
)

@Serializable
data class MissionExecutionPlan(
    val missionId: String,
    val actionPlan: MissionActionPlan,
    val progress: MissionProgressSnapshot,
    val roadmap: MissionRoadmap,
    val health: MissionHealthResult,
    val notifications: List<MissionNotification>,
    val history: MissionHistory,
    val scenarioImpacts: List<MissionScenarioImpact>,
)

@Serializable
data class MissionExecutionCenter(
    val plans: List<MissionExecutionPlan>,
    val notifications: List<MissionNotification>,
    val atRiskMissionCount: Int,
    val actionsDueCount: Int,
    val recentlyCompletedCount: Int,
)
