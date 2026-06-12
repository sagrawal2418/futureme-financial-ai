package com.futureme.shared.mission

import com.futureme.shared.models.ConfidenceLevel
import com.futureme.shared.models.DashboardSnapshot
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.GoalProbabilityResult
import com.futureme.shared.models.LifeReadinessResult
import com.futureme.shared.models.MissionReadinessCategory
import com.futureme.shared.models.MissionReadinessFactor
import com.futureme.shared.models.MissionType
import kotlin.math.roundToInt

data class MissionReadinessAssessment(
    val score: Int,
    val factors: List<MissionReadinessFactor>,
    val strengths: List<String>,
    val weaknesses: List<String>,
    val blockers: List<String>,
    val confidenceLevel: ConfidenceLevel,
    val goalProbabilityPercentage: Int,
)

class MissionReadinessCalculator {
    fun calculate(
        definition: MissionDefinition,
        profile: FinancialProfile,
        dashboard: DashboardSnapshot,
        readiness: LifeReadinessResult?,
        goal: GoalProbabilityResult?,
    ): MissionReadinessAssessment {
        val factorScores = factorScores(definition.missionType, profile, dashboard, goal, readiness)
        val factorAverage = factorScores.map { it.score }.average()
        val anchor = readiness?.readinessScore ?: goal?.probabilityPercentage ?: factorAverage.roundToInt()
        val score = (anchor * 0.65 + factorAverage * 0.35).roundToInt().coerceIn(0, 100)
        val strengths = (
            readiness?.strengths.orEmpty() +
                factorScores.filter { it.score >= 75 }.map { "${it.title} is supporting this mission." }
            ).distinct().take(4)
        val weaknesses = (
            readiness?.weaknesses.orEmpty() +
                factorScores.filter { it.score < 60 }.map { "${it.title} needs preparation." }
            ).distinct().take(4)
        val blockers = (
            readiness?.blockers.orEmpty() +
                goal?.blockers.orEmpty() +
                factorScores.filter { it.score < 45 }.map { "${it.title} is below the mission planning floor." }
            ).distinct().take(4)

        return MissionReadinessAssessment(
            score = score,
            factors = factorScores,
            strengths = strengths.ifEmpty { listOf("The household has a measurable starting plan.") },
            weaknesses = weaknesses,
            blockers = blockers.ifEmpty { listOf("No critical blocker is modeled today.") },
            confidenceLevel = readiness?.confidenceLevel ?: ConfidenceLevel.MEDIUM,
            goalProbabilityPercentage = goal?.probabilityPercentage ?: score,
        )
    }

    private fun factorScores(
        missionType: MissionType,
        profile: FinancialProfile,
        dashboard: DashboardSnapshot,
        goal: GoalProbabilityResult?,
        readiness: LifeReadinessResult?,
    ): List<MissionReadinessFactor> {
        val cashFlowTarget = when (missionType) {
            MissionType.HAVE_CHILD -> 3_500.0
            MissionType.START_BUSINESS -> 5_000.0
            MissionType.SUPPORT_PARENTS -> 3_000.0
            else -> 4_000.0
        }
        val emergencyTarget = when (missionType) {
            MissionType.START_BUSINESS -> 18.0
            MissionType.BUILD_EMERGENCY_FUND -> 12.0
            else -> 6.0
        }
        val debtScore = (
            100.0 - percent(profile.creditCardDebt, profile.monthlyNetIncome * 2.0)
            ).roundToInt().coerceIn(0, 100)

        return listOf(
            factor(
                MissionReadinessCategory.FINANCIAL,
                "Financial readiness",
                dashboard.healthScore.value,
                "Uses the shared financial health score.",
            ),
            factor(
                MissionReadinessCategory.CASH_FLOW,
                "Cash flow readiness",
                percent(dashboard.monthlyCashFlow, cashFlowTarget).roundToInt(),
                "Measures monthly surplus against this mission's planning need.",
            ),
            factor(
                MissionReadinessCategory.RISK,
                "Risk readiness",
                100 - dashboard.riskScore.value,
                "Rewards lower household risk and fewer fixed obligations.",
            ),
            factor(
                MissionReadinessCategory.EMERGENCY_FUND,
                "Emergency fund readiness",
                percent(dashboard.emergencyFundMonths, emergencyTarget).roundToInt(),
                "Compares liquid runway with the mission reserve target.",
            ),
            factor(
                MissionReadinessCategory.DEBT,
                "Debt readiness",
                debtScore,
                "Measures how much revolving debt limits flexibility.",
            ),
            factor(
                MissionReadinessCategory.GOAL,
                "Goal readiness",
                goal?.probabilityPercentage ?: readiness?.readinessScore ?: dashboard.healthScore.value,
                "Uses the linked goal probability or life-readiness result.",
            ),
        )
    }

    private fun factor(
        category: MissionReadinessCategory,
        title: String,
        score: Int,
        explanation: String,
    ) = MissionReadinessFactor(category, title, score.coerceIn(0, 100), explanation)

    private fun percent(value: Double, target: Double): Double =
        if (target <= 0.0) 0.0 else (value / target * 100.0).coerceIn(0.0, 100.0)
}
