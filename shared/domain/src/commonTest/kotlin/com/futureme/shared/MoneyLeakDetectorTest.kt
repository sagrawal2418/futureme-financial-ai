package com.futureme.shared

import com.futureme.shared.mock.MockFinancialData
import com.futureme.shared.models.MoneyLeakType
import com.futureme.shared.moneyleaks.MoneyLeakDetector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MoneyLeakDetectorTest {
    private val leaks = MoneyLeakDetector().detect(
        profile = MockFinancialData.profile,
        transactions = MockFinancialData.transactions,
        cashAccounts = MockFinancialData.cashAccounts,
        debtAccounts = MockFinancialData.debtAccounts,
        investmentAccounts = MockFinancialData.investmentAccounts,
        mortgageAccounts = MockFinancialData.mortgageAccounts,
    )

    @Test
    fun detectsEveryConfiguredLeakFamily() {
        assertEquals(6, leaks.map { it.type }.toSet().size)
        assertTrue(leaks.any { it.type == MoneyLeakType.HIGH_INTEREST_DEBT })
        assertTrue(leaks.any { it.type == MoneyLeakType.MISSED_EMPLOYER_MATCH })
    }

    @Test
    fun annualAndFiveYearLossesAreDerivedFromMonthlyLoss() {
        leaks.forEach { leak ->
            assertEquals(leak.estimatedMonthlyLoss * 12.0, leak.estimatedAnnualLoss)
            assertEquals(leak.estimatedMonthlyLoss * 60.0, leak.estimatedFiveYearLoss)
        }
    }
}
