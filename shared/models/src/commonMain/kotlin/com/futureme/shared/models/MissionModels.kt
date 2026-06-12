package com.futureme.shared.models

import kotlinx.serialization.Serializable

@Serializable
enum class MissionType {
    BUY_HOME,
    HAVE_CHILD,
    RELOCATE,
    RETIRE_EARLY,
    BECOME_DEBT_FREE,
    BUILD_EMERGENCY_FUND,
    SUPPORT_PARENTS,
    START_BUSINESS,
}

@Serializable
enum class MissionStatus {
    NOT_STARTED,
    IN_PROGRESS,
    AT_RISK,
    ON_TRACK,
    COMPLETED,
}

@Serializable
enum class MissionReadinessCategory {
    FINANCIAL,
    CASH_FLOW,
    RISK,
    EMERGENCY_FUND,
    DEBT,
    GOAL,
}

@Serializable
data class MissionReadinessFactor(
    val category: MissionReadinessCategory,
    val title: String,
    val score: Int,
    val explanation: String,
)

@Serializable
enum class MissionTimelineHorizon {
    TODAY,
    THIRTY_DAYS,
    NINETY_DAYS,
    ONE_YEAR,
    THREE_YEARS,
}

@Serializable
data class MissionTimelinePoint(
    val horizon: MissionTimelineHorizon,
    val label: String,
    val monthsFromNow: Int,
    val readinessScore: Int,
    val progressPercentage: Int,
    val completedActions: Int,
    val milestone: String,
    val projectedCompletionDate: String,
)

@Serializable
data class MissionNextAction(
    val id: String,
    val title: String,
    val description: String,
    val estimatedReadinessIncrease: Int,
    val estimatedTimelineReductionMonths: Int,
    val annualBenefitEstimate: Double,
    val fiveYearBenefitEstimate: Double,
    val impactScore: Int,
    val confidenceScore: Int,
    val relatedScenarioId: String? = null,
)

@Serializable
data class Mission(
    val missionId: String,
    val missionType: MissionType,
    val title: String,
    val description: String,
    val targetDate: String,
    val readinessScore: Int,
    val progressPercentage: Int,
    val riskLevel: RiskLevel,
    val estimatedCost: Double,
    val projectedBenefit: Double,
    val blockers: List<String>,
    val recommendations: List<String>,
    val nextAction: MissionNextAction,
    val timeline: List<MissionTimelinePoint>,
    val createdDate: String,
    val updatedDate: String,
    val status: MissionStatus,
    val readinessFactors: List<MissionReadinessFactor>,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val confidenceLevel: ConfidenceLevel,
    val goalProbabilityPercentage: Int,
)

@Serializable
data class MissionSignal(
    val id: String,
    val missionId: String,
    val title: String,
    val description: String,
    val impactLabel: String,
)

@Serializable
data class MissionControlSnapshot(
    val activeMissions: List<Mission>,
    val highestReadinessMission: Mission,
    val lowestReadinessMission: Mission,
    val missionProgressPercentage: Int,
    val missionTimeline: List<MissionTimelinePoint>,
    val nextBestAction: MissionNextAction,
    val risks: List<MissionSignal>,
    val opportunities: List<MissionSignal>,
)

@Serializable
data class MissionAnalyticsTrend(
    val missionId: String,
    val title: String,
    val startingReadinessScore: Int,
    val currentReadinessScore: Int,
    val readinessChange: Int,
    val timelineReductionMonths: Int,
    val actionsCompleted: Int,
)

@Serializable
data class MissionAnalyticsSnapshot(
    val missionsCreated: Int,
    val missionsCompleted: Int,
    val readinessImprovements: Int,
    val timelineImprovements: Int,
    val actionsCompleted: Int,
    val goalsAchieved: Int,
    val trends: List<MissionAnalyticsTrend>,
)
