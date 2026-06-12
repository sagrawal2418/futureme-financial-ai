package com.futureme.shared.models

import kotlinx.serialization.Serializable

@Serializable
enum class OpportunitySource {
    MONEY_LEAK,
    DEBT,
    INVESTMENT,
    GOAL,
    READINESS,
    FINANCIAL_GPS,
    LIFE_EVENT,
}

@Serializable
data class OpportunityRecommendation(
    val id: String,
    val title: String,
    val description: String,
    val source: OpportunitySource,
    val impactScore: Int,
    val effortScore: Int,
    val confidenceScore: Int,
    val annualBenefitEstimate: Double,
    val fiveYearBenefitEstimate: Double,
    val priorityRanking: Int,
    val monthlyCommitment: Double,
    val relatedScenarioId: String? = null,
)

@Serializable
data class NextBestAction(
    val recommendationId: String,
    val title: String,
    val description: String,
    val callout: String,
    val monthlyCommitment: Double,
    val fiveYearImpact: Double,
    val impactScore: Int,
    val confidenceScore: Int,
)

@Serializable
enum class ImpactSentiment {
    POSITIVE,
    NEUTRAL,
    NEGATIVE,
}

@Serializable
data class ExplainabilityFactor(
    val id: String,
    val title: String,
    val description: String,
    val pointImpact: Int,
    val sentiment: ImpactSentiment,
)

@Serializable
data class FinancialExplainability(
    val previousScore: Int,
    val currentScore: Int,
    val netChange: Int,
    val factors: List<ExplainabilityFactor>,
    val summary: String,
)

@Serializable
enum class ImpactDimension {
    CASH_FLOW,
    DEBT,
    EMERGENCY_FUND,
    RETIREMENT,
    READINESS,
    RISK,
}

@Serializable
data class ScenarioImpactCell(
    val dimension: ImpactDimension,
    val sentiment: ImpactSentiment,
    val score: Int,
    val label: String,
)

@Serializable
data class ScenarioImpactHeatmap(
    val scenarioId: String,
    val title: String,
    val cells: List<ScenarioImpactCell>,
)

@Serializable
data class MonthlyFinancialReview(
    val id: String,
    val month: String,
    val label: String,
    val generatedDate: String,
    val wins: List<String>,
    val risks: List<String>,
    val opportunities: List<String>,
    val recommendedActions: List<String>,
    val readinessChanges: List<String>,
    val goalProgress: List<String>,
    val aiSummary: String,
)

@Serializable
enum class DecisionOutcomeStatus {
    PLANNED,
    TRACKING,
    AHEAD,
    ON_TRACK,
    BEHIND,
}

@Serializable
data class DecisionJournalEntry(
    val id: String,
    val type: String,
    val title: String,
    val decisionDate: String,
    val expectedMonthlyImpact: Double,
    val actualMonthlyImpact: Double? = null,
    val expectedFiveYearImpact: Double,
    val actualFiveYearImpact: Double? = null,
    val status: DecisionOutcomeStatus,
    val notes: String,
    val relatedScenarioId: String? = null,
)

@Serializable
data class FutureOutcomeContribution(
    val id: String,
    val title: String,
    val description: String,
    val fiveYearContribution: Double,
    val sharePercentage: Int,
    val source: OpportunitySource,
)

@Serializable
data class BankingVisionStep(
    val order: Int,
    val title: String,
    val description: String,
    val focusTarget: String,
)

@Serializable
data class BankingVisionDemo(
    val title: String,
    val subtitle: String,
    val audiences: List<String>,
    val steps: List<BankingVisionStep>,
)

@Serializable
enum class AnalyticsEventType {
    SCENARIO_CREATED,
    GOAL_ADDED,
    INSIGHT_VIEWED,
    RECOMMENDATION_ACCEPTED,
    READINESS_VIEWED,
    AI_QUESTION_ASKED,
    MONTHLY_REVIEW_OPENED,
}

@Serializable
data class AnalyticsProperty(
    val name: String,
    val value: String,
)

@Serializable
data class AnalyticsEvent(
    val id: String,
    val type: AnalyticsEventType,
    val occurredAt: String,
    val properties: List<AnalyticsProperty>,
)
