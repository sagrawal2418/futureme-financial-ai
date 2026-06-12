package com.futureme.shared

import com.futureme.shared.domain.FutureMeProduct
import com.futureme.shared.models.ImpactDimension
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class BankingIntelligenceEngineTest {
    private val product = FutureMeProduct()

    @Test
    fun opportunitiesAreRankedAndProduceOneNextAction() {
        val recommendations = product.opportunityRecommendations()
        val action = product.nextBestAction()

        assertTrue(recommendations.size >= 8)
        assertEquals((1..recommendations.size).toList(), recommendations.map { it.priorityRanking })
        assertEquals(recommendations.first().id, action.recommendationId)
        assertTrue(action.fiveYearImpact > 0.0)
    }

    @Test
    fun explainabilityReconcilesToTheCurrentScore() {
        val explanation = product.financialExplainability()

        assertEquals(
            explanation.currentScore - explanation.previousScore,
            explanation.factors.sumOf { it.pointImpact },
        )
        assertTrue(explanation.factors.any { it.pointImpact < 0 })
        assertTrue(explanation.factors.any { it.pointImpact > 0 })
    }

    @Test
    fun everyScenarioHasACompleteImpactHeatmap() {
        val heatmaps = product.scenarioImpactHeatmaps()

        assertEquals(product.scenarios().size, heatmaps.size)
        heatmaps.forEach { heatmap ->
            assertEquals(ImpactDimension.entries.toSet(), heatmap.cells.map { it.dimension }.toSet())
        }
    }

    @Test
    fun reviewsJournalAndAnalyticsRetainHistory() {
        assertEquals(3, product.monthlyFinancialReviews().size)
        assertTrue(product.decisionJournal().all { it.actualFiveYearImpact != null })

        val saved = product.saveDecision("pay-off-cards")
        val event = product.recordAnalyticsEvent("recommendation_accepted", "opportunity-debt")

        assertEquals("pay-off-cards", saved.relatedScenarioId)
        assertNotNull(product.decisionJournal().firstOrNull { it.id == saved.id })
        assertNotNull(product.analyticsEvents().firstOrNull { it.id == event.id })
    }
}
