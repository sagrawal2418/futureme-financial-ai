package com.futureme.shared.insights

import com.futureme.shared.models.DashboardSnapshot
import com.futureme.shared.models.GoalProbabilityResult
import com.futureme.shared.models.Insight
import com.futureme.shared.models.InsightCategory
import com.futureme.shared.models.InsightSeverity
import com.futureme.shared.models.MoneyLeak
import com.futureme.shared.models.ScenarioType

class ProactiveInsightsEngine {
    fun generate(
        dashboard: DashboardSnapshot,
        moneyLeaks: List<MoneyLeak>,
        goals: List<GoalProbabilityResult>,
    ): List<Insight> {
        val insights = mutableListOf<Insight>()
        moneyLeaks.take(2).forEach { leak ->
            insights += Insight(
                id = "insight-${leak.id}",
                title = leak.title,
                summary = leak.summary,
                severity = if (leak.estimatedAnnualLoss >= 2_000.0) {
                    InsightSeverity.WARNING
                } else {
                    InsightSeverity.OPPORTUNITY
                },
                category = InsightCategory.MONEY_LEAK,
                estimatedDollarImpact = leak.estimatedAnnualLoss,
                recommendedAction = leak.fixRecommendation,
                relatedScenarioType = when (leak.id) {
                    "high-interest-debt" -> ScenarioType.PAY_OFF_DEBT
                    "mortgage-refinance" -> ScenarioType.REFINANCE_MORTGAGE
                    else -> null
                },
                createdDate = "2026-06-11",
            )
        }
        if (dashboard.emergencyFundMonths < 12.0) {
            insights += Insight(
                id = "insight-runway",
                title = "Reserves are strong, but short of the 12-month goal",
                summary = "Current liquid savings cover ${oneDecimal(dashboard.emergencyFundMonths)} months of essential expenses.",
                severity = InsightSeverity.INFO,
                category = InsightCategory.EMERGENCY_FUND_ALERT,
                estimatedDollarImpact = 0.0,
                recommendedAction = "Automate $750 monthly until the reserve reaches 12 months.",
                relatedScenarioType = ScenarioType.JOB_LOSS,
                createdDate = "2026-06-11",
            )
        }
        goals.minByOrNull(GoalProbabilityResult::probabilityPercentage)?.let { goal ->
            insights += Insight(
                id = "insight-goal-${goal.id}",
                title = "${goal.title} needs the most preparation",
                summary = "Modeled readiness is ${goal.probabilityPercentage}% based on current cash flow and reserves.",
                severity = InsightSeverity.OPPORTUNITY,
                category = InsightCategory.GOAL_PROGRESS,
                estimatedDollarImpact = goal.requiredMonthlyImprovement,
                recommendedAction = goal.recommendedActions.first(),
                createdDate = "2026-06-11",
            )
        }
        insights += Insight(
            id = "insight-weekly-cashflow",
            title = "This week's plan remains cash-flow positive",
            summary = "The household has $${dashboard.monthlyCashFlow.toInt()} of modeled monthly flexibility.",
            severity = InsightSeverity.INFO,
            category = InsightCategory.CASH_FLOW_ALERT,
            estimatedDollarImpact = dashboard.monthlyCashFlow,
            recommendedAction = "Assign the next dollar to debt, reserves, or the highest-priority goal.",
            createdDate = "2026-06-11",
        )
        return insights.sortedByDescending { it.severity.ordinal }
    }

    private fun oneDecimal(value: Double): String =
        ((value * 10.0).toInt() / 10.0).toString()
}
