package com.futureme.shared.scenario

import com.futureme.shared.calculators.FinancialMath
import com.futureme.shared.models.DashboardSnapshot
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.ProjectionPoint
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.ScenarioComparison
import com.futureme.shared.models.ScenarioResult
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.roundToInt

data class CalculationAssumptions(
    val projectionYears: Int = 5,
    val propertyAppreciationRate: Double = 0.03,
    val mortgagePrincipalShare: Double = 0.28,
    val riskPenaltyPerPoint: Double = 1_000.0,
)

class ScenarioEngine(
    private val baseline: Scenario,
    private val assumptions: CalculationAssumptions = CalculationAssumptions(),
) {
    fun dashboard(profile: FinancialProfile): DashboardSnapshot {
        val baselineResult = simulate(profile, baseline)
        val monthlyCashFlow = FinancialMath.monthlyCashFlow(profile)
        val runway = FinancialMath.emergencyFundRunway(
            profile.liquidSavings,
            FinancialMath.essentialMonthlyOutflow(profile),
        )
        val alerts = buildList {
            if (profile.creditCardDebt > profile.monthlyNetIncome) {
                add("High-interest debt exceeds one month of take-home pay.")
            }
            if (runway < 6.0) {
                add("Emergency reserves are below the six-month planning target.")
            }
            if (monthlyCashFlow > 0.0) {
                add("${monthlyCashFlow.asWholeDollars()} is available each month for the next priority.")
            }
        }
        return DashboardSnapshot(
            healthScore = baselineResult.healthScore,
            riskScore = baselineResult.riskScore,
            monthlyCashFlow = monthlyCashFlow,
            currentNetWorth = FinancialMath.currentNetWorth(profile),
            projectedNetWorth5Years = baselineResult.projectedNetWorth5Years,
            emergencyFundMonths = runway,
            debtPayoffMonths = FinancialMath.debtPayoffMonths(
                profile.creditCardDebt,
                profile.creditCardApr,
                profile.monthlyDebtPayments,
            ),
            alerts = alerts,
        )
    }

    fun simulate(profile: FinancialProfile, scenario: Scenario): ScenarioResult {
        val baselineProjection = project(profile, baseline)
        val scenarioProjection = project(profile, scenario)
        val monthlyImpact = FinancialMath.scenarioCashFlowImpact(scenario)
        val projectedSurplus = FinancialMath.monthlyCashFlow(profile) + monthlyImpact
        val emergencyMonths = FinancialMath.emergencyFundRunway(
            profile.liquidSavings - scenario.upfrontCost,
            max(
                1.0,
                FinancialMath.essentialMonthlyOutflow(profile) + scenario.monthlyExpenseDelta,
            ),
        )
        val riskScore = FinancialMath.riskScore(
            profile,
            scenario,
            projectedSurplus,
            emergencyMonths,
        )
        val healthScore = FinancialMath.healthScore(
            profile,
            projectedSurplus,
            emergencyMonths,
            riskScore.value,
        )
        val points = scenarioProjection.mapIndexed { index, point ->
            point.copy(baselineNetWorth = baselineProjection[index].scenarioNetWorth)
        }
        val fiveYearDelta =
            points.last().scenarioNetWorth - points.last().baselineNetWorth
        return ScenarioResult(
            scenario = scenario,
            monthlyCashFlowImpact = monthlyImpact,
            projectedMonthlySurplus = projectedSurplus,
            baselineNetWorth = FinancialMath.currentNetWorth(profile),
            projectedNetWorth1Year = points[1].scenarioNetWorth,
            projectedNetWorth3Years = points[3].scenarioNetWorth,
            projectedNetWorth5Years = points[5].scenarioNetWorth,
            netWorthDelta5Years = fiveYearDelta,
            healthScore = healthScore,
            riskScore = riskScore,
            emergencyFundMonths = emergencyMonths,
            debtPayoffMonths = FinancialMath.debtPayoffMonths(
                max(0.0, profile.creditCardDebt + scenario.debtDelta),
                profile.creditCardApr,
                profile.monthlyDebtPayments,
            ),
            projections = points,
            tradeoffs = tradeoffs(profile, scenario, projectedSurplus, emergencyMonths),
            recommendation = recommendation(
                scenario,
                projectedSurplus,
                emergencyMonths,
                fiveYearDelta,
                riskScore.value,
            ),
        )
    }

    fun compare(
        profile: FinancialProfile,
        left: Scenario,
        right: Scenario,
    ): ScenarioComparison {
        val leftResult = simulate(profile, left)
        val rightResult = simulate(profile, right)
        val leftScore =
            leftResult.netWorthDelta5Years - leftResult.riskScore.value * assumptions.riskPenaltyPerPoint
        val rightScore =
            rightResult.netWorthDelta5Years - rightResult.riskScore.value * assumptions.riskPenaltyPerPoint
        val preferred = if (leftScore >= rightScore) leftResult else rightResult
        val alternative = if (preferred === leftResult) rightResult else leftResult
        val difference =
            preferred.projectedNetWorth5Years - alternative.projectedNetWorth5Years
        return ScenarioComparison(
            left = leftResult,
            right = rightResult,
            preferredScenarioId = preferred.scenario.id,
            summary = "${preferred.scenario.title} is the stronger risk-adjusted path, " +
                "with a five-year net-worth difference of ${difference.asWholeDollars()}.",
        )
    }

    private fun project(
        profile: FinancialProfile,
        scenario: Scenario,
    ): List<ProjectionPoint> {
        var cash = max(0.0, profile.liquidSavings - scenario.upfrontCost)
        var investments = profile.investmentBalance
        var property = max(0.0, profile.propertyValue + scenario.propertyValueDelta)
        var mortgage = max(0.0, profile.mortgageBalance + scenario.mortgageDelta)
        var cardDebt = max(0.0, profile.creditCardDebt + scenario.debtDelta)
        val points = mutableListOf(
            ProjectionPoint(
                year = 0,
                baselineNetWorth = FinancialMath.currentNetWorth(profile),
                scenarioNetWorth = cash + investments + property - mortgage - cardDebt,
            ),
        )

        repeat(assumptions.projectionYears * 12) { monthIndex ->
            val incomeDelta = if (
                scenario.incomeShockMonths > 0 &&
                monthIndex >= scenario.incomeShockMonths
            ) {
                0.0
            } else {
                scenario.monthlyIncomeDelta
            }
            cash += FinancialMath.monthlyCashFlow(profile) +
                incomeDelta -
                scenario.monthlyExpenseDelta -
                scenario.monthlyInvestmentDelta
            investments = FinancialMath.compoundMonthly(
                investments,
                scenario.annualInvestmentReturn,
                profile.monthlyRetirementContribution + scenario.monthlyInvestmentDelta,
                1,
            )
            property *= 1.0 + assumptions.propertyAppreciationRate / 12.0
            mortgage = max(
                0.0,
                mortgage - profile.housingPayment * assumptions.mortgagePrincipalShare,
            )
            if (cardDebt > 0.0) {
                cardDebt = max(
                    0.0,
                    cardDebt * (1.0 + profile.creditCardApr / 12.0) -
                        profile.monthlyDebtPayments,
                )
            }
            if ((monthIndex + 1) % 12 == 0) {
                points += ProjectionPoint(
                    year = (monthIndex + 1) / 12,
                    baselineNetWorth = 0.0,
                    scenarioNetWorth = cash + investments + property - mortgage - cardDebt,
                )
            }
        }
        return points
    }

    private fun tradeoffs(
        profile: FinancialProfile,
        scenario: Scenario,
        projectedSurplus: Double,
        emergencyMonths: Double,
    ): List<String> = buildList {
        if (scenario.upfrontCost > 0.0) {
            add("${scenario.upfrontCost.asWholeDollars()} leaves liquid savings on day one.")
        }
        if (scenario.monthlyInvestmentDelta > 0.0) {
            add("Higher automated investing improves compounding but reduces flexibility.")
        }
        if (scenario.monthlyExpenseDelta < 0.0) {
            add("Lower recurring costs create room to rebuild reserves or accelerate other goals.")
        }
        if (projectedSurplus < FinancialMath.monthlyCashFlow(profile)) {
            add("Monthly flexibility falls by ${(FinancialMath.monthlyCashFlow(profile) - projectedSurplus).asWholeDollars()}.")
        }
        add(
            if (emergencyMonths < 6.0) {
                "Cash reserves fall below the recommended six-month planning buffer."
            } else {
                "Emergency reserves remain above a six-month planning buffer."
            },
        )
    }

    private fun recommendation(
        scenario: Scenario,
        monthlySurplus: Double,
        emergencyMonths: Double,
        fiveYearDelta: Double,
        riskScore: Int,
    ): String = when {
        monthlySurplus < 0.0 ->
            "This creates a monthly deficit. Reduce the commitment or delay the decision."
        emergencyMonths < 4.0 ->
            "The long-term case may work, but the cash buffer is too thin to proceed comfortably."
        riskScore >= 60 ->
            "Treat this as a stretch scenario and test a smaller first step."
        fiveYearDelta > 25_000.0 ->
            "${scenario.title} is financially supportable and improves the five-year outlook."
        else ->
            "${scenario.title} is manageable, but flexibility and lifestyle fit should break the tie."
    }
}

private fun Double.asWholeDollars(): String {
    val absolute = abs(roundToInt())
    val grouped = absolute.toString().reversed().chunked(3).joinToString(",").reversed()
    return (if (this < 0.0) "-$" else "$") + grouped
}
