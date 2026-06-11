package com.futureme.shared

import com.futureme.shared.gps.FinancialGpsEngine
import com.futureme.shared.mock.MockFinancialData
import com.futureme.shared.scenario.ScenarioEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FinancialGpsEngineTest {
    private val result = FinancialGpsEngine(
        ScenarioEngine(MockFinancialData.baseline),
    ).calculate(MockFinancialData.profile, MockFinancialData.baseline)

    @Test
    fun improvedTrajectoryUsesAllThreeMonthlyActions() {
        assertEquals(3, result.monthlyActionPlan.size)
        assertTrue(result.difference > 0.0)
        assertEquals(
            result.currentFiveYearNetWorth + result.difference,
            result.improvedFiveYearNetWorth,
        )
    }

    @Test
    fun trajectoriesRemainAlignedByYear() {
        assertEquals(
            result.currentTrajectory.map { it.year },
            result.improvedTrajectory.map { it.year },
        )
        assertEquals(6, result.improvedTrajectory.size)
    }
}
