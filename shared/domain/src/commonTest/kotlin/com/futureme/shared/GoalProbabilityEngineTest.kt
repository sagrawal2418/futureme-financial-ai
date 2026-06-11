package com.futureme.shared

import com.futureme.shared.goals.GoalProbabilityEngine
import com.futureme.shared.mock.MockFinancialData
import com.futureme.shared.models.GoalType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoalProbabilityEngineTest {
    private val goals = GoalProbabilityEngine().evaluateAll(MockFinancialData.profile)

    @Test
    fun supportsAllRequiredGoalFamilies() {
        assertEquals(GoalType.entries.toSet(), goals.map { it.type }.toSet())
    }

    @Test
    fun everyGoalIsBoundedAndActionable() {
        goals.forEach { goal ->
            assertTrue(goal.probabilityPercentage in 0..100)
            assertTrue(goal.recommendedActions.isNotEmpty())
            assertTrue(goal.requiredMonthlyImprovement >= 0.0)
            assertTrue(goal.projectedReadyDate.isNotBlank())
        }
    }
}
