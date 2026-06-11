package com.futureme.shared

import com.futureme.shared.lifeevents.LifeEventPlanner
import com.futureme.shared.mock.MockFinancialData
import com.futureme.shared.models.LifeEventType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LifeEventPlannerTest {
    private val plans = LifeEventPlanner().plans(MockFinancialData.profile)

    @Test
    fun supportsAllRequiredLifeEvents() {
        assertEquals(LifeEventType.entries.toSet(), plans.map { it.type }.toSet())
    }

    @Test
    fun everyPlanContainsPreparationAndScenarioLinks() {
        plans.forEach { plan ->
            assertTrue(plan.oneTimeCostHigh >= plan.oneTimeCostLow)
            assertTrue(plan.recommendedPreparationSteps.isNotEmpty())
            assertTrue(plan.relatedInsights.isNotEmpty())
            assertTrue(plan.suggestedScenarioIds.isNotEmpty())
        }
    }
}
