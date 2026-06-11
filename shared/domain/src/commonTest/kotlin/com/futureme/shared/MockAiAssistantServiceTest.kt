package com.futureme.shared

import com.futureme.shared.assistant.MockAiAssistantService
import com.futureme.shared.domain.FutureMeProduct
import com.futureme.shared.mock.MockFinancialData
import com.futureme.shared.models.AssistantPrompt
import com.futureme.shared.scenario.ScenarioEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MockAiAssistantServiceTest {
    private val engine = ScenarioEngine(MockFinancialData.baseline)
    private val assistant = MockAiAssistantService(engine)
    private val context = FutureMeProduct().copilotContext()

    @Test
    fun homeQuestionReturnsScenarioAwareAnswer() {
        val response = assistant.answer(
            AssistantPrompt("Can I afford to buy a $700k home?"),
            MockFinancialData.profile,
            latestScenarioResult = null,
            context = context,
        )

        assertEquals("buy-home", response.relatedScenarioId)
        assertTrue(response.answer.contains("Emergency runway"))
        assertTrue(response.answer.contains("risk"))
    }

    @Test
    fun actionQuestionRecommendsHighInterestDebtPayoff() {
        val response = assistant.answer(
            AssistantPrompt("What is one action that improves my 5-year outlook?"),
            MockFinancialData.profile,
            latestScenarioResult = null,
            context = context,
        )

        assertEquals("pay-off-cards", response.relatedScenarioId)
        assertTrue(response.answer.contains("credit-card"))
    }

    @Test
    fun moneyLeakQuestionExplainsDetectedStructuredOutput() {
        val response = assistant.answer(
            AssistantPrompt("What is my biggest money leak?"),
            MockFinancialData.profile,
            latestScenarioResult = null,
            context = context,
        )

        assertTrue(response.answer.contains("annual impact"))
        assertTrue(response.suggestedActions.isNotEmpty())
    }
}
