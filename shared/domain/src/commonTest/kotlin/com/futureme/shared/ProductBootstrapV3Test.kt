package com.futureme.shared

import com.futureme.shared.domain.FutureMeProduct
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductBootstrapV3Test {
    private val bootstrap = FutureMeProduct().bootstrap()

    @Test
    fun bootstrapSynchronizesTheLifeReadinessPlatform() {
        assertEquals(7, bootstrap.readiness.size)
        assertEquals(7, bootstrap.readinessPlans.size)
        assertEquals(bootstrap.scenarios.size, bootstrap.decisionSimulations.size)
        assertEquals(5, bootstrap.lifeTimeline.size)
        assertEquals(5, bootstrap.executiveDemo.steps.size)
        assertTrue(bootstrap.executiveDemo.personaFacts.isNotEmpty())
    }

    @Test
    fun coachPromptsFocusOnLifeDecisions() {
        val prompts = bootstrap.suggestedQuestions.map { it.prompt.lowercase() }

        assertTrue(prompts.any { "preventing me from buying a home" in it })
        assertTrue(prompts.any { "weakest readiness" in it })
        assertTrue(prompts.any { "focus on this month" in it })
    }
}
