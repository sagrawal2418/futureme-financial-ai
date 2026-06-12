package com.futureme.shared

import com.futureme.shared.domain.ProductStrategyService
import com.futureme.shared.models.ProductPriority
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductStrategyServiceTest {
    private val service = ProductStrategyService()

    @Test
    fun navigationAnswersFiveDistinctCustomerQuestions() {
        val navigation = service.strategy().navigation

        assertEquals(listOf("Home", "Missions", "Insights", "Coach", "Profile"), navigation.map { it.label })
        assertEquals(5, navigation.map { it.customerQuestion }.distinct().size)
    }

    @Test
    fun strategySeparatesCoreCapabilitiesFromRedundantScreens() {
        val recommendations = service.strategy().featureRecommendations

        assertTrue(recommendations.any {
            it.name == "Mission Control" && it.priority == ProductPriority.MUST_HAVE
        })
        assertTrue(recommendations.any {
            it.name == "Standalone readiness dashboard" &&
                it.priority == ProductPriority.POTENTIALLY_REMOVE
        })
    }

    @Test
    fun evaluationAndPersonaDataAreComplete() {
        val evaluation = service.aiEvaluationDashboard()

        assertEquals(50, evaluation.totalPrompts)
        assertEquals(7, evaluation.categories.size)
        assertEquals(5, service.personas().size)
        assertTrue(service.personas().all { it.expectedMissionPlan.isNotEmpty() })
    }
}
