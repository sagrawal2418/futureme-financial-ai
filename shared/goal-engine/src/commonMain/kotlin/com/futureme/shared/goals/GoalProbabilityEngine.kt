package com.futureme.shared.goals

import com.futureme.shared.calculators.FinancialMath
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.GoalProbabilityResult
import com.futureme.shared.models.GoalType
import kotlin.math.roundToInt

class GoalProbabilityEngine {
    fun evaluateAll(profile: FinancialProfile): List<GoalProbabilityResult> {
        val surplus = FinancialMath.monthlyCashFlow(profile)
        val runway = FinancialMath.emergencyFundRunway(
            profile.liquidSavings,
            FinancialMath.essentialMonthlyOutflow(profile),
        )
        return listOf(
            goal(
                "goal-home",
                GoalType.BUY_HOME,
                "Buy a larger home",
                score = 58 + readiness(runway, 6.0) + readiness(surplus, 2_500.0),
                blockers = listOf("High-interest card balance", "Target down payment would reduce liquidity"),
                actions = listOf("Eliminate card debt", "Preserve six months of essential expenses"),
                improvement = 1_050.0,
                readyDate = "2027-09-01",
            ),
            goal(
                "goal-emergency",
                GoalType.BUILD_EMERGENCY_FUND,
                "Build a 12-month emergency fund",
                score = 64 + readiness(runway, 8.0),
                blockers = listOf("Current reserve covers fewer than 12 months"),
                actions = listOf("Automate $750 monthly to high-yield savings"),
                improvement = 750.0,
                readyDate = "2027-12-01",
            ),
            goal(
                "goal-debt",
                GoalType.PAY_OFF_DEBT,
                "Pay off high-interest debt",
                score = 72 + readiness(surplus, 2_000.0),
                blockers = listOf("20.99% APR increases carrying cost"),
                actions = listOf("Add $300 to the current monthly payment", "Pause new revolving balances"),
                improvement = 300.0,
                readyDate = "2027-11-01",
            ),
            goal(
                "goal-child",
                GoalType.HAVE_CHILD,
                "Prepare for another child",
                score = 52 + readiness(runway, 6.0) + readiness(surplus, 3_500.0),
                blockers = listOf("Childcare would reduce monthly flexibility", "One-time leave costs are not reserved"),
                actions = listOf("Build a $15,000 family transition fund", "Model dependent-care benefits"),
                improvement = 900.0,
                readyDate = "2028-03-01",
            ),
            goal(
                "goal-move",
                GoalType.MOVE_STATE,
                "Relocate to a lower-cost state",
                score = 75 + readiness(runway, 6.0),
                blockers = listOf("Income and benefits must be confirmed before moving"),
                actions = listOf("Secure compensation details", "Reserve $15,000 for moving costs"),
                improvement = 350.0,
                readyDate = "2027-04-01",
            ),
            goal(
                "goal-retire",
                GoalType.RETIRE_EARLY,
                "Retire five years early",
                score = 41 + readiness(profile.monthlyRetirementContribution, 2_500.0),
                blockers = listOf("Current savings rate is below the early-retirement target"),
                actions = listOf("Capture the full employer match", "Increase investing after card payoff"),
                improvement = 1_400.0,
                readyDate = "2043-06-01",
            ),
        )
    }

    private fun readiness(value: Double, target: Double): Int =
        ((value / target).coerceIn(0.0, 1.0) * 10.0).roundToInt()

    private fun goal(
        id: String,
        type: GoalType,
        title: String,
        score: Int,
        blockers: List<String>,
        actions: List<String>,
        improvement: Double,
        readyDate: String,
    ): GoalProbabilityResult = GoalProbabilityResult(
        id = id,
        type = type,
        title = title,
        probabilityPercentage = score.coerceIn(5, 95),
        blockers = blockers,
        recommendedActions = actions,
        requiredMonthlyImprovement = improvement,
        projectedReadyDate = readyDate,
        explanation = "Probability uses cash flow, reserve coverage, debt pressure, and the deterministic cost of this goal.",
    )
}
