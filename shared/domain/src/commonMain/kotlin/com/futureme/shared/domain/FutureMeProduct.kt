package com.futureme.shared.domain

import com.futureme.shared.assistant.MockAiAssistantService
import com.futureme.shared.design.FutureMeDesignTokens
import com.futureme.shared.mock.MockFinancialDataProvider
import com.futureme.shared.models.AssistantPrompt
import com.futureme.shared.models.AssistantResponse
import com.futureme.shared.models.DashboardSnapshot
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.ProductBootstrap
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.ScenarioComparison
import com.futureme.shared.models.ScenarioResult
import com.futureme.shared.models.SuggestedQuestion
import com.futureme.shared.models.UserIdentity
import com.futureme.shared.scenario.ScenarioEngine

class FutureMeProduct {
    private val dataProvider: FinancialDataProvider = MockFinancialDataProvider()
    private val engine = ScenarioEngine(dataProvider.baseline())
    private val assistant: FinancialAssistantProvider = MockAiAssistantService(engine)

    fun identity(): UserIdentity = dataProvider.identity()

    fun profile(): FinancialProfile = dataProvider.profile()

    fun dashboard(): DashboardSnapshot = engine.dashboard(profile())

    fun scenarios(): List<Scenario> = dataProvider.scenarios()

    fun scenario(id: String): Scenario? = scenarios().firstOrNull { it.id == id }

    fun simulate(scenarioId: String): ScenarioResult =
        engine.simulate(
            profile(),
            requireNotNull(scenario(scenarioId)) { "Unknown scenario: $scenarioId" },
        )

    fun compare(leftScenarioId: String, rightScenarioId: String): ScenarioComparison =
        engine.compare(
            profile(),
            requireNotNull(scenario(leftScenarioId)) { "Unknown scenario: $leftScenarioId" },
            requireNotNull(scenario(rightScenarioId)) { "Unknown scenario: $rightScenarioId" },
        )

    fun suggestedQuestions(): List<SuggestedQuestion> = dataProvider.suggestedQuestions()

    fun ask(prompt: AssistantPrompt): AssistantResponse {
        val latest = prompt.latestScenarioId?.let(::simulate)
        return assistant.answer(prompt, profile(), latest)
    }

    fun bootstrap(): ProductBootstrap = ProductBootstrap(
        identity = identity(),
        profile = profile(),
        dashboard = dashboard(),
        scenarios = scenarios(),
        recentScenarioResults = listOf(
            simulate("move-to-texas"),
            simulate("pay-off-cards"),
            simulate("buy-home"),
        ),
        suggestedQuestions = suggestedQuestions(),
        designTokens = FutureMeDesignTokens.current,
        disclaimer = DISCLAIMER,
    )

    companion object {
        const val DISCLAIMER = "Educational simulation only, not financial advice."
    }
}
