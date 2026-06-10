package com.futureme.shared.calculators

import com.futureme.shared.models.FinancialHealthScore
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.RiskFactor
import com.futureme.shared.models.RiskLevel
import com.futureme.shared.models.RiskScore
import com.futureme.shared.models.Scenario
import kotlin.math.max
import kotlin.math.round

object FinancialMath {
    fun currentNetWorth(profile: FinancialProfile): Double =
        profile.liquidSavings +
            profile.investmentBalance +
            profile.propertyValue -
            profile.creditCardDebt -
            profile.mortgageBalance

    fun monthlyCashFlow(profile: FinancialProfile): Double =
        profile.monthlyNetIncome -
            profile.monthlyLivingExpenses -
            profile.housingPayment -
            profile.monthlyDebtPayments -
            profile.monthlyRetirementContribution

    fun essentialMonthlyOutflow(profile: FinancialProfile): Double =
        profile.monthlyLivingExpenses +
            profile.housingPayment +
            profile.monthlyDebtPayments

    fun emergencyFundRunway(
        liquidSavings: Double,
        essentialMonthlyOutflow: Double,
    ): Double = if (essentialMonthlyOutflow <= 0.0) {
        0.0
    } else {
        max(0.0, liquidSavings) / essentialMonthlyOutflow
    }

    fun scenarioCashFlowImpact(scenario: Scenario): Double =
        scenario.monthlyIncomeDelta -
            scenario.monthlyExpenseDelta -
            scenario.monthlyInvestmentDelta

    fun compoundMonthly(
        openingBalance: Double,
        annualReturn: Double,
        monthlyContribution: Double,
        months: Int,
    ): Double {
        var balance = openingBalance
        repeat(max(0, months)) {
            balance = balance * (1.0 + annualReturn / 12.0) + monthlyContribution
        }
        return balance
    }

    fun debtPayoffMonths(
        debt: Double,
        annualRate: Double,
        monthlyPayment: Double,
        maximumMonths: Int = 600,
    ): Int? {
        if (debt <= 0.0) return 0
        if (monthlyPayment <= 0.0 || maximumMonths <= 0) return null
        val monthlyRate = annualRate / 12.0
        if (monthlyPayment <= debt * monthlyRate) return null

        var balance = debt
        repeat(maximumMonths) { month ->
            balance = balance * (1.0 + monthlyRate) - monthlyPayment
            if (balance <= 0.0) return month + 1
        }
        return null
    }

    fun riskScore(
        profile: FinancialProfile,
        scenario: Scenario,
        monthlySurplus: Double,
        emergencyMonths: Double,
    ): RiskScore {
        val factors = buildList {
            add(
                RiskFactor(
                    id = "base",
                    title = "Planning uncertainty",
                    explanation = "Long-range income, expense, and market assumptions can change.",
                    points = 24,
                ),
            )
            if (scenario.riskAdjustment != 0) {
                add(
                    RiskFactor(
                        id = "scenario",
                        title = "Scenario complexity",
                        explanation = scenario.rationale,
                        points = scenario.riskAdjustment,
                    ),
                )
            }
            val cashFlowPoints = when {
                monthlySurplus < 0.0 -> 30
                monthlySurplus < 1_000.0 -> 15
                monthlySurplus < 2_000.0 -> 7
                else -> 0
            }
            if (cashFlowPoints > 0) {
                add(
                    RiskFactor(
                        id = "cash-flow",
                        title = "Monthly cash-flow pressure",
                        explanation = if (monthlySurplus < 0.0) {
                            "This scenario creates a recurring monthly deficit."
                        } else {
                            "The remaining monthly buffer is below the planning target."
                        },
                        points = cashFlowPoints,
                    ),
                )
            }
            val reservePoints = when {
                emergencyMonths < 3.0 -> 24
                emergencyMonths < 6.0 -> 12
                emergencyMonths < 9.0 -> 5
                else -> 0
            }
            if (reservePoints > 0) {
                add(
                    RiskFactor(
                        id = "reserves",
                        title = "Emergency reserve coverage",
                        explanation = "Liquid reserves cover ${oneDecimal(emergencyMonths)} months of essential expenses.",
                        points = reservePoints,
                    ),
                )
            }
            if (profile.creditCardDebt > profile.annualGrossIncome * 0.10) {
                add(
                    RiskFactor(
                        id = "revolving-debt",
                        title = "High-interest revolving debt",
                        explanation = "Credit-card debt exceeds 10% of annual gross income.",
                        points = 8,
                    ),
                )
            }
            if (scenario.upfrontCost > profile.liquidSavings * 0.70) {
                add(
                    RiskFactor(
                        id = "liquidity",
                        title = "Large liquidity draw",
                        explanation = "The upfront cost uses more than 70% of liquid savings.",
                        points = 10,
                    ),
                )
            }
        }
        val value = factors.sumOf { it.points }.coerceIn(5, 95)
        val level = when (value) {
            in 0..29 -> RiskLevel.LOW
            in 30..49 -> RiskLevel.MODERATE
            in 50..69 -> RiskLevel.ELEVATED
            else -> RiskLevel.HIGH
        }
        val summary = when (level) {
            RiskLevel.LOW -> "Strong cash flow and reserves keep this scenario resilient."
            RiskLevel.MODERATE -> "The scenario is workable with a few items to monitor."
            RiskLevel.ELEVATED -> "Reduce the commitment or strengthen reserves first."
            RiskLevel.HIGH -> "This scenario materially weakens near-term resilience."
        }
        return RiskScore(
            value = value,
            level = level,
            summary = summary,
            factors = factors.sortedByDescending { it.points },
        )
    }

    fun healthScore(
        profile: FinancialProfile,
        monthlySurplus: Double,
        emergencyMonths: Double,
        riskScore: Int,
    ): FinancialHealthScore {
        var value = 100 - riskScore
        if (monthlySurplus > profile.monthlyNetIncome * 0.15) value += 5
        if (emergencyMonths >= 6.0) value += 5
        if (
            monthlySurplus >= 0.0 &&
            profile.monthlyRetirementContribution >= profile.monthlyNetIncome * 0.10
        ) {
            value += 4
        }
        value = value.coerceIn(10, 96)
        val label = when {
            value >= 85 -> "Strong foundation"
            value >= 70 -> "Healthy with watch items"
            value >= 55 -> "Needs attention"
            else -> "Financially vulnerable"
        }
        val summary = when {
            value >= 85 -> "The household has resilient cash flow and reserve coverage."
            value >= 70 -> "The plan is workable, with a few priorities to address."
            value >= 55 -> "Improve cash flow or reserves before taking on another commitment."
            else -> "Focus on liquidity and fixed obligations before major decisions."
        }
        return FinancialHealthScore(value, label, summary)
    }

    private fun oneDecimal(value: Double): String =
        (round(value * 10.0) / 10.0).toString()
}
