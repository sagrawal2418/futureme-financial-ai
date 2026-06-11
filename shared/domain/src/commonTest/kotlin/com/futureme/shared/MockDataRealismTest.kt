package com.futureme.shared

import com.futureme.shared.mock.MockFinancialData
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockDataRealismTest {
    @Test
    fun transactionHistorySpansNinetyDays() {
        assertEquals(90, MockFinancialData.transactions.size)
        assertEquals("2026-03-13", MockFinancialData.transactions.first().postedDate)
        assertEquals("2026-06-10", MockFinancialData.transactions.last().postedDate)
        assertTrue(MockFinancialData.transactions.map { it.category }.toSet().size >= 8)
    }

    @Test
    fun accountBalancesReconcileToProfileTotals() {
        assertEquals(
            MockFinancialData.profile.liquidSavings,
            MockFinancialData.cashAccounts.sumOf { it.balance },
        )
        assertEquals(
            MockFinancialData.profile.investmentBalance,
            MockFinancialData.investmentAccounts.sumOf { it.balance },
        )
        assertEquals(
            MockFinancialData.profile.creditCardDebt,
            MockFinancialData.debtAccounts
                .filter { it.category == "Credit card" }
                .sumOf { it.balance },
        )
    }
}
