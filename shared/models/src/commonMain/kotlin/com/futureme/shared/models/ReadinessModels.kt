package com.futureme.shared.models

import kotlinx.serialization.Serializable

@Serializable
enum class ReadinessCategory {
    HOME_PURCHASE,
    CHILD,
    RELOCATION,
    RETIREMENT,
    BUSINESS_STARTUP,
    PARENT_SUPPORT,
    EDUCATION_FUNDING,
}

@Serializable
enum class ReadinessLevel {
    NOT_READY,
    NEEDS_PREPARATION,
    ALMOST_READY,
    READY,
}

@Serializable
enum class ReadinessTrend {
    IMPROVING,
    STABLE,
    DECLINING,
}

@Serializable
data class LifeReadinessResult(
    val id: String,
    val category: ReadinessCategory,
    val title: String,
    val readinessScore: Int,
    val readinessLevel: ReadinessLevel,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val blockers: List<String>,
    val confidenceLevel: ConfidenceLevel,
    val recommendedActions: List<String>,
    val projectedReadyDate: String,
    val estimatedMonthsToReady: Int,
    val trend: ReadinessTrend,
    val trendDelta: Int,
)

@Serializable
data class ReadinessImprovementPlan(
    val id: String,
    val category: ReadinessCategory,
    val title: String,
    val currentScore: Int,
    val targetScore: Int,
    val scoreGap: Int,
    val recommendations: List<String>,
    val monthlyCommitment: Double,
    val estimatedTimelineMonths: Int,
    val projectedTargetDate: String,
)

@Serializable
data class LifeDecisionSimulation(
    val scenarioId: String,
    val title: String,
    val category: ReadinessCategory,
    val readinessScoreBefore: Int,
    val readinessScoreAfter: Int,
    val readinessImpact: Int,
    val monthlyCashFlowImpact: Double,
    val fiveYearNetWorthImpact: Double,
    val riskScoreBefore: Int,
    val riskScoreAfter: Int,
    val riskChange: Int,
    val timelineChangeMonths: Int,
    val summary: String,
    val recommendedActions: List<String>,
)

@Serializable
enum class TimelineHorizon {
    TODAY,
    SIX_MONTHS,
    ONE_YEAR,
    THREE_YEARS,
    FIVE_YEARS,
}

@Serializable
data class TimelineReadinessScore(
    val category: ReadinessCategory,
    val score: Int,
)

@Serializable
data class LifeTimelinePoint(
    val horizon: TimelineHorizon,
    val label: String,
    val monthsFromNow: Int,
    val netWorth: Double,
    val debtBalance: Double,
    val investmentBalance: Double,
    val readinessScores: List<TimelineReadinessScore>,
    val completedGoals: List<String>,
)

@Serializable
data class ExecutiveDemoStep(
    val order: Int,
    val title: String,
    val category: ReadinessCategory?,
    val description: String,
    val coachPrompt: String,
)

@Serializable
data class ExecutiveDemoExperience(
    val personaTitle: String,
    val personaSummary: String,
    val personaFacts: List<String>,
    val steps: List<ExecutiveDemoStep>,
)
