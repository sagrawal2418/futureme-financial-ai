package com.futureme.financialai.domain

import com.futureme.financialai.model.DashboardSnapshot
import com.futureme.financialai.model.FinancialProfile
import com.futureme.financialai.model.ProjectionPoint
import com.futureme.financialai.model.Scenario
import com.futureme.financialai.model.ScenarioComparison
import com.futureme.financialai.model.ScenarioResult
import kotlin.math.max

class FinancialCalculator(
    private val baseline: Scenario,
    private val explanationProvider: FinancialExplanationProvider,
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
                add("Your monthly surplus can accelerate the next priority.")
            }
        }

        return DashboardSnapshot(
            healthScore = baselineResult.healthScore,
            currentNetWorth = FinancialMath.currentNetWorth(profile),
            projectedNetWorth5Years = baselineResult.projectedNetWorth5Years,
            monthlySurplus = monthlyCashFlow,
            emergencyFundMonths = runway,
            debtPayoffMonths = FinancialMath.debtPayoffMonths(
                profile.creditCardDebt,
                profile.creditCardApr,
                profile.monthlyDebtPayments,
            ),
            alerts = alerts,
        )
    }

    fun simulate(
        profile: FinancialProfile,
        scenario: Scenario,
    ): ScenarioResult {
        val baselineProjection = projectNetWorth(profile, baseline)
        val scenarioProjection = projectNetWorth(profile, scenario)
        val monthlyImpact = FinancialMath.scenarioCashFlowImpact(scenario)
        val projectedSurplus = FinancialMath.monthlyCashFlow(profile) + monthlyImpact
        val emergencyMonths = FinancialMath.emergencyFundRunway(
            liquidSavings = profile.liquidSavings - scenario.upfrontCost,
            essentialMonthlyOutflow = max(
                1.0,
                FinancialMath.essentialMonthlyOutflow(profile) + scenario.monthlyExpenseDelta,
            ),
        )
        val risk = FinancialMath.riskScore(
            profile = profile,
            scenario = scenario,
            monthlySurplus = projectedSurplus,
            emergencyMonths = emergencyMonths,
        )
        val scenarioPoints = scenarioProjection.mapIndexed { index, point ->
            point.copy(baselineNetWorth = baselineProjection[index].scenarioNetWorth)
        }
        val fiveYearDelta =
            scenarioPoints.last().scenarioNetWorth - scenarioPoints.last().baselineNetWorth
        val tradeoffs = buildTradeoffs(profile, scenario, projectedSurplus, emergencyMonths)

        return ScenarioResult(
            scenario = scenario,
            monthlyCashFlowImpact = monthlyImpact,
            projectedMonthlySurplus = projectedSurplus,
            projectedNetWorth1Year = scenarioPoints[1].scenarioNetWorth,
            projectedNetWorth3Years = scenarioPoints[3].scenarioNetWorth,
            projectedNetWorth5Years = scenarioPoints[5].scenarioNetWorth,
            netWorthDelta5Years = fiveYearDelta,
            emergencyFundMonths = emergencyMonths,
            debtPayoffMonths = FinancialMath.debtPayoffMonths(
                debt = max(0.0, profile.creditCardDebt + scenario.debtDelta),
                annualRate = profile.creditCardApr,
                monthlyPayment = profile.monthlyDebtPayments,
            ),
            healthScore = FinancialMath.healthScore(
                profile,
                projectedSurplus,
                emergencyMonths,
                risk.score,
            ),
            risk = risk,
            projections = scenarioPoints,
            tradeoffs = tradeoffs,
            recommendation = explanationProvider.explain(
                scenario,
                projectedSurplus,
                emergencyMonths,
                fiveYearDelta,
                risk.score,
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
        val leftValue = leftResult.netWorthDelta5Years - leftResult.risk.score * 1_000.0
        val rightValue = rightResult.netWorthDelta5Years - rightResult.risk.score * 1_000.0
        val preferred = if (leftValue >= rightValue) leftResult else rightResult
        val alternative = if (preferred === leftResult) rightResult else leftResult
        val difference =
            preferred.projectedNetWorth5Years - alternative.projectedNetWorth5Years

        return ScenarioComparison(
            left = leftResult,
            right = rightResult,
            preferredScenarioId = preferred.scenario.id,
            summary = "${preferred.scenario.title} is the stronger risk-adjusted path, " +
                "with a five-year net-worth difference of ${difference.toLong()}.",
        )
    }

    fun projectNetWorth(
        profile: FinancialProfile,
        scenario: Scenario,
        years: Int = 5,
    ): List<ProjectionPoint> {
        require(years >= 1) { "Projection years must be positive." }

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

        repeat(years * 12) { monthIndex ->
            val incomeDelta = if (
                scenario.incomeShockMonths > 0 &&
                monthIndex >= scenario.incomeShockMonths
            ) {
                0.0
            } else {
                scenario.monthlyIncomeDelta
            }
            val monthlySurplus =
                FinancialMath.monthlyCashFlow(profile) +
                    incomeDelta -
                    scenario.monthlyExpenseDelta -
                    scenario.monthlyInvestmentDelta
            cash += monthlySurplus
            investments = FinancialMath.projectedInvestmentBalance(
                openingBalance = investments,
                annualReturn = scenario.annualInvestmentReturn,
                monthlyContribution =
                    profile.monthlyRetirementContribution + scenario.monthlyInvestmentDelta,
                months = 1,
            )
            property *= 1.0 + 0.03 / 12.0
            mortgage = max(0.0, mortgage - profile.housingPayment * 0.28)
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

    private fun buildTradeoffs(
        profile: FinancialProfile,
        scenario: Scenario,
        monthlySurplus: Double,
        emergencyMonths: Double,
    ): List<String> = buildList {
        if (scenario.upfrontCost > 0.0) {
            add("Uses cash on day one and reduces short-term flexibility.")
        }
        if (scenario.monthlyInvestmentDelta > 0.0) {
            add("Improves long-term compounding while reducing monthly liquidity.")
        }
        if (scenario.monthlyExpenseDelta < 0.0) {
            add("Lower recurring costs create room for other goals.")
        }
        if (monthlySurplus < FinancialMath.monthlyCashFlow(profile)) {
            add("Leaves less monthly room for unplanned spending.")
        }
        if (emergencyMonths < 6.0) {
            add("Cash reserves fall below the six-month planning buffer.")
        } else {
            add("Emergency reserves stay above a six-month planning buffer.")
        }
    }
}
