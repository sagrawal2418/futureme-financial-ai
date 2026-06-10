package com.futureme.financialai.domain

import com.futureme.financialai.data.DemoFinancialData
import com.futureme.financialai.data.MockFinancialExplanationProvider
import com.futureme.financialai.model.RiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialMathTest {
    private val profile = DemoFinancialData.profile

    @Test
    fun monthlyCashFlowSubtractsEveryPlannedCommitment() {
        assertEquals(2_675.0, FinancialMath.monthlyCashFlow(profile), 0.001)
    }

    @Test
    fun emergencyFundRunwayUsesLiquidSavingsAndEssentialOutflow() {
        val runway = FinancialMath.emergencyFundRunway(
            liquidSavings = profile.liquidSavings,
            essentialMonthlyOutflow = FinancialMath.essentialMonthlyOutflow(profile),
        )

        assertEquals(9.922879, runway, 0.000001)
        assertEquals(0.0, FinancialMath.emergencyFundRunway(10_000.0, 0.0), 0.0)
    }

    @Test
    fun debtPayoffHandlesNormalPaidAndNegativeAmortizationCases() {
        assertEquals(
            28,
            FinancialMath.debtPayoffMonths(
                debt = profile.creditCardDebt,
                annualRate = profile.creditCardApr,
                monthlyPayment = profile.monthlyDebtPayments,
            ),
        )
        assertEquals(0, FinancialMath.debtPayoffMonths(0.0, 0.20, 500.0))
        assertNull(FinancialMath.debtPayoffMonths(10_000.0, 0.24, 100.0))
    }

    @Test
    fun netWorthProjectionReturnsAnnualPointsThroughYearFive() {
        val calculator = FinancialCalculator(
            baseline = DemoFinancialData.baseline,
            explanationProvider = MockFinancialExplanationProvider(),
        )

        val points = calculator.projectNetWorth(
            profile = profile,
            scenario = DemoFinancialData.baseline,
        )

        assertEquals(listOf(0, 1, 2, 3, 4, 5), points.map { it.year })
        assertEquals(648_100.0, points.first().scenarioNetWorth, 0.001)
        assertTrue(points.last().scenarioNetWorth > points.first().scenarioNetWorth)
    }

    @Test
    fun riskScoreExplainsAndCapsSevereStressScenario() {
        val jobLoss = DemoFinancialData.scenarios.first { it.id == "job-loss" }
        val assessment = FinancialMath.riskScore(
            profile = profile,
            scenario = jobLoss,
            monthlySurplus = -11_575.0,
            emergencyMonths = 2.0,
        )

        assertEquals(95, assessment.score)
        assertEquals(RiskLevel.HIGH, assessment.level)
        assertTrue(assessment.factors.any { it.id == "cash-flow" })
        assertTrue(assessment.factors.any { it.id == "reserves" })
    }
}
