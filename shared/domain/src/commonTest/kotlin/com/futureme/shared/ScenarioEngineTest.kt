package com.futureme.shared

import com.futureme.shared.mock.MockFinancialData
import com.futureme.shared.scenario.ScenarioEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScenarioEngineTest {
    private val engine = ScenarioEngine(MockFinancialData.baseline)

    @Test
    fun projectionReturnsNowAndFiveAnnualPoints() {
        val result = engine.simulate(
            MockFinancialData.profile,
            requireNotNull(MockFinancialData.scenario("invest-more")),
        )

        assertEquals(listOf(0, 1, 2, 3, 4, 5), result.projections.map { it.year })
        assertTrue(result.projectedNetWorth5Years > result.baselineNetWorth)
    }

    @Test
    fun comparisonReturnsOneOfTheRequestedScenarios() {
        val comparison = engine.compare(
            MockFinancialData.profile,
            requireNotNull(MockFinancialData.scenario("move-to-texas")),
            requireNotNull(MockFinancialData.scenario("stay-in-new-jersey")),
        )

        assertTrue(
            comparison.preferredScenarioId in
                setOf("move-to-texas", "stay-in-new-jersey"),
        )
        assertTrue(comparison.summary.isNotBlank())
    }

    @Test
    fun everyRequiredScenarioFamilyIsRepresented() {
        val types = MockFinancialData.scenarios.map { it.type }.toSet()

        assertEquals(9, types.size)
    }
}
