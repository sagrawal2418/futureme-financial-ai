package com.futureme.shared

import com.futureme.shared.domain.FutureMeProduct
import com.futureme.shared.models.InsightCategory
import kotlin.test.Test
import kotlin.test.assertTrue

class ProactiveInsightsEngineTest {
    private val insights = FutureMeProduct().copilotContext().insights

    @Test
    fun createsRankedActionableInsights() {
        assertTrue(insights.size >= 4)
        assertTrue(insights.all { it.title.isNotBlank() && it.recommendedAction.isNotBlank() })
        assertTrue(insights.any { it.category == InsightCategory.MONEY_LEAK })
        assertTrue(insights.any { it.category == InsightCategory.GOAL_PROGRESS })
    }
}
