package com.futureme.shared.gps

import com.futureme.shared.calculators.FinancialMath
import com.futureme.shared.models.ConfidenceLevel
import com.futureme.shared.models.FinancialGpsResult
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.ProjectionPoint
import com.futureme.shared.models.Scenario
import com.futureme.shared.scenario.ScenarioEngine

class FinancialGpsEngine(
    private val scenarioEngine: ScenarioEngine,
) {
    fun calculate(
        profile: FinancialProfile,
        baseline: Scenario,
    ): FinancialGpsResult {
        val current = scenarioEngine.simulate(profile, baseline)
        val spendingGain = FinancialMath.compoundMonthly(0.0, 0.04, 250.0, 60)
        val investmentGain = FinancialMath.compoundMonthly(0.0, 0.07, 200.0, 60)
        val debtInterestGain = debtInterestSaved(
            debt = profile.creditCardDebt,
            annualRate = profile.creditCardApr,
            currentPayment = profile.monthlyDebtPayments,
            improvedPayment = profile.monthlyDebtPayments + 300.0,
        )
        val totalDifference = spendingGain + investmentGain + debtInterestGain
        val improved = current.projections.map { point ->
            val progress = point.year / 5.0
            ProjectionPoint(
                year = point.year,
                baselineNetWorth = point.baselineNetWorth,
                scenarioNetWorth = point.scenarioNetWorth + totalDifference * progress,
            )
        }
        return FinancialGpsResult(
            currentFiveYearNetWorth = current.projectedNetWorth5Years,
            improvedFiveYearNetWorth = current.projectedNetWorth5Years + totalDifference,
            difference = totalDifference,
            monthlyActionPlan = listOf(
                "Reduce flexible spending by $250",
                "Increase automated investments by $200",
                "Pay an additional $300 toward high-interest debt",
            ),
            confidenceLevel = ConfidenceLevel.HIGH,
            explanation = "This path applies three deterministic monthly actions while preserving the current income assumptions.",
            currentTrajectory = current.projections,
            improvedTrajectory = improved,
        )
    }

    private fun debtInterestSaved(
        debt: Double,
        annualRate: Double,
        currentPayment: Double,
        improvedPayment: Double,
    ): Double = interestPaid(debt, annualRate, currentPayment) -
        interestPaid(debt, annualRate, improvedPayment)

    private fun interestPaid(
        openingDebt: Double,
        annualRate: Double,
        payment: Double,
    ): Double {
        var debt = openingDebt
        var interestPaid = 0.0
        repeat(600) {
            if (debt <= 0.0) return interestPaid
            val interest = debt * annualRate / 12.0
            interestPaid += interest
            debt = (debt + interest - payment).coerceAtLeast(0.0)
        }
        return interestPaid
    }
}
