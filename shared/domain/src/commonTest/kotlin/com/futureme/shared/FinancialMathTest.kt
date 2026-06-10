package com.futureme.shared

import com.futureme.shared.calculators.FinancialMath
import com.futureme.shared.mock.MockFinancialData
import com.futureme.shared.models.RiskLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FinancialMathTest {
    private val profile = MockFinancialData.profile

    @Test
    fun currentNetWorthSubtractsEveryLiability() {
        assertEquals(648_100.0, FinancialMath.currentNetWorth(profile))
    }

    @Test
    fun monthlyCashFlowSubtractsEveryCommitment() {
        assertEquals(2_675.0, FinancialMath.monthlyCashFlow(profile))
    }

    @Test
    fun essentialOutflowExcludesInvestingAndDiscretionarySurplus() {
        assertEquals(9_725.0, FinancialMath.essentialMonthlyOutflow(profile))
    }

    @Test
    fun emergencyRunwayUsesLiquidSavingsAndEssentialOutflow() {
        assertEquals(
            9.922879177377892,
            FinancialMath.emergencyFundRunway(
                profile.liquidSavings,
                FinancialMath.essentialMonthlyOutflow(profile),
            ),
        )
        assertEquals(0.0, FinancialMath.emergencyFundRunway(1_000.0, 0.0))
    }

    @Test
    fun debtPayoffHandlesPaidAndNegativeAmortizationCases() {
        assertEquals(28, FinancialMath.debtPayoffMonths(18_400.0, 0.2099, 850.0))
        assertEquals(0, FinancialMath.debtPayoffMonths(0.0, 0.2, 500.0))
        assertNull(FinancialMath.debtPayoffMonths(10_000.0, 0.24, 100.0))
    }

    @Test
    fun scenarioCashFlowCombinesIncomeExpensesAndInvesting() {
        assertEquals(
            1_250.0,
            FinancialMath.scenarioCashFlowImpact(
                requireNotNull(MockFinancialData.scenario("move-to-texas")),
            ),
        )
    }

    @Test
    fun monthlyCompoundingAppliesReturnBeforeContribution() {
        assertEquals(
            10_402.0,
            FinancialMath.compoundMonthly(
                openingBalance = 10_000.0,
                annualReturn = 0.12,
                monthlyContribution = 100.0,
                months = 2,
            ),
        )
    }

    @Test
    fun riskScoreExplainsSevereStress() {
        val result = FinancialMath.riskScore(
            profile,
            requireNotNull(MockFinancialData.scenario("job-loss")),
            monthlySurplus = -11_575.0,
            emergencyMonths = 2.0,
        )

        assertEquals(95, result.value)
        assertEquals(RiskLevel.HIGH, result.level)
        assertTrue(result.factors.any { it.id == "cash-flow" })
        assertTrue(result.factors.any { it.id == "reserves" })
    }

    @Test
    fun healthScoreRewardsCashFlowReservesAndRetirementSaving() {
        val health = FinancialMath.healthScore(
            profile = profile,
            monthlySurplus = 2_675.0,
            emergencyMonths = 9.9,
            riskScore = 24,
        )

        assertEquals(90, health.value)
        assertEquals("Strong foundation", health.label)
    }
}
