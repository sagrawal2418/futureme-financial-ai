package com.futureme.shared

import com.futureme.shared.domain.FutureMeProduct
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductBootstrapV4Test {
    private val bootstrap = FutureMeProduct().bootstrap()

    @Test
    fun bootstrapSynchronizesBankingIntelligence() {
        assertTrue(bootstrap.opportunities.isNotEmpty())
        assertEquals(bootstrap.opportunities.first().id, bootstrap.nextBestAction.recommendationId)
        assertEquals(bootstrap.scenarios.size, bootstrap.scenarioImpactHeatmaps.size)
        assertEquals(3, bootstrap.monthlyReviews.size)
        assertTrue(bootstrap.decisionJournal.isNotEmpty())
        assertTrue(bootstrap.futureOutcomeContributions.isNotEmpty())
        assertEquals(7, bootstrap.bankingVisionDemo.steps.size)
    }

    @Test
    fun coachCanAnswerTheSingleActionQuestion() {
        val response = FutureMeProduct().ask(
            com.futureme.shared.models.AssistantPrompt(
                "If I can only do one thing this month, what should it be?",
            ),
        )

        assertTrue("five-year outlook" in response.answer.lowercase())
        assertTrue(response.suggestedActions.isNotEmpty())
    }
}
