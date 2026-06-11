package com.futureme.shared.models

import kotlinx.serialization.Serializable

@Serializable
enum class InsightSeverity {
    INFO,
    OPPORTUNITY,
    WARNING,
    CRITICAL,
}

@Serializable
enum class InsightCategory {
    MONEY_LEAK,
    RISK_WARNING,
    OPPORTUNITY,
    GOAL_PROGRESS,
    CASH_FLOW_ALERT,
    DEBT_ALERT,
    EMERGENCY_FUND_ALERT,
    LIFE_EVENT_RECOMMENDATION,
}

@Serializable
data class Insight(
    val id: String,
    val title: String,
    val summary: String,
    val severity: InsightSeverity,
    val category: InsightCategory,
    val estimatedDollarImpact: Double,
    val recommendedAction: String,
    val relatedScenarioType: ScenarioType? = null,
    val createdDate: String,
)

@Serializable
enum class MoneyLeakType {
    HIGH_SUBSCRIPTION_SPEND,
    EXCESS_CHECKING_CASH,
    HIGH_INTEREST_DEBT,
    INSURANCE_OVERPAYMENT,
    REFINANCE_OPPORTUNITY,
    MISSED_EMPLOYER_MATCH,
}

@Serializable
enum class ActionDifficulty {
    EASY,
    MODERATE,
    INVOLVED,
}

@Serializable
data class MoneyLeak(
    val id: String,
    val type: MoneyLeakType,
    val title: String,
    val summary: String,
    val estimatedMonthlyLoss: Double,
    val estimatedAnnualLoss: Double,
    val estimatedFiveYearLoss: Double,
    val fixRecommendation: String,
    val difficulty: ActionDifficulty,
)

@Serializable
enum class ConfidenceLevel {
    HIGH,
    MEDIUM,
    LOW,
}

@Serializable
data class FinancialGpsResult(
    val currentFiveYearNetWorth: Double,
    val improvedFiveYearNetWorth: Double,
    val difference: Double,
    val monthlyActionPlan: List<String>,
    val confidenceLevel: ConfidenceLevel,
    val explanation: String,
    val currentTrajectory: List<ProjectionPoint>,
    val improvedTrajectory: List<ProjectionPoint>,
)

@Serializable
enum class GoalType {
    BUY_HOME,
    BUILD_EMERGENCY_FUND,
    PAY_OFF_DEBT,
    HAVE_CHILD,
    MOVE_STATE,
    RETIRE_EARLY,
}

@Serializable
data class GoalProbabilityResult(
    val id: String,
    val type: GoalType,
    val title: String,
    val probabilityPercentage: Int,
    val blockers: List<String>,
    val recommendedActions: List<String>,
    val requiredMonthlyImprovement: Double,
    val projectedReadyDate: String,
    val explanation: String,
)

@Serializable
enum class LifeEventType {
    NEW_BABY,
    HOME_PURCHASE,
    RELOCATION,
    JOB_LOSS,
    PARENT_SUPPORT,
    MAJOR_MEDICAL_EXPENSE,
}

@Serializable
data class LifeEventPlan(
    val id: String,
    val type: LifeEventType,
    val title: String,
    val subtitle: String,
    val estimatedMonthlyImpact: Double,
    val oneTimeCostLow: Double,
    val oneTimeCostHigh: Double,
    val riskImpact: Int,
    val recommendedPreparationSteps: List<String>,
    val relatedInsights: List<String>,
    val suggestedScenarioIds: List<String>,
)

@Serializable
data class FinancialCopilotContext(
    val insights: List<Insight>,
    val financialGps: FinancialGpsResult,
    val goals: List<GoalProbabilityResult>,
    val lifeEvents: List<LifeEventPlan>,
    val moneyLeaks: List<MoneyLeak>,
)
