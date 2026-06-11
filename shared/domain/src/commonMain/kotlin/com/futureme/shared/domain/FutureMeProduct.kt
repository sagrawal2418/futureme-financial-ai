package com.futureme.shared.domain

import com.futureme.shared.assistant.MockAiAssistantService
import com.futureme.shared.design.FutureMeDesignTokens
import com.futureme.shared.goals.GoalProbabilityEngine
import com.futureme.shared.gps.FinancialGpsEngine
import com.futureme.shared.insights.ProactiveInsightsEngine
import com.futureme.shared.lifeevents.LifeEventPlanner
import com.futureme.shared.mock.MockFinancialDataProvider
import com.futureme.shared.models.AssistantPrompt
import com.futureme.shared.models.AssistantResponse
import com.futureme.shared.models.DashboardSnapshot
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.FinancialCopilotContext
import com.futureme.shared.models.ProductBootstrap
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.ScenarioComparison
import com.futureme.shared.models.ScenarioResult
import com.futureme.shared.models.SuggestedQuestion
import com.futureme.shared.models.UserIdentity
import com.futureme.shared.moneyleaks.MoneyLeakDetector
import com.futureme.shared.scenario.ScenarioEngine

class FutureMeProduct {
    private val dataProvider: FinancialDataProvider = MockFinancialDataProvider()
    private val engine = ScenarioEngine(dataProvider.baseline())
    private val assistant: FinancialAssistantProvider = MockAiAssistantService(engine)
    private val moneyLeakDetector = MoneyLeakDetector()
    private val goalEngine = GoalProbabilityEngine()
    private val lifeEventPlanner = LifeEventPlanner()
    private val financialGpsEngine = FinancialGpsEngine(engine)
    private val insightsEngine = ProactiveInsightsEngine()

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

    fun copilotContext(): FinancialCopilotContext {
        val goals = goalEngine.evaluateAll(profile())
        val leaks = moneyLeakDetector.detect(
            profile = profile(),
            transactions = dataProvider.transactions(),
            cashAccounts = dataProvider.cashAccounts(),
            debtAccounts = dataProvider.debtAccounts(),
            investmentAccounts = dataProvider.investmentAccounts(),
            mortgageAccounts = dataProvider.mortgageAccounts(),
        )
        return FinancialCopilotContext(
            insights = insightsEngine.generate(dashboard(), leaks, goals),
            financialGps = financialGpsEngine.calculate(profile(), dataProvider.baseline()),
            goals = goals,
            lifeEvents = lifeEventPlanner.plans(profile()),
            moneyLeaks = leaks,
        )
    }

    fun ask(prompt: AssistantPrompt): AssistantResponse {
        val latest = prompt.latestScenarioId?.let(::simulate)
        return assistant.answer(prompt, profile(), latest, copilotContext())
    }

    fun bootstrap(): ProductBootstrap {
        val context = copilotContext()
        return ProductBootstrap(
            identity = identity(),
            profile = profile(),
            dashboard = dashboard(),
            scenarios = scenarios(),
            recentScenarioResults = listOf(
                simulate("move-to-texas"),
                simulate("pay-off-cards"),
                simulate("buy-home"),
            ),
            transactions = dataProvider.transactions(),
            debtAccounts = dataProvider.debtAccounts(),
            investmentAccounts = dataProvider.investmentAccounts(),
            cashAccounts = dataProvider.cashAccounts(),
            mortgageAccounts = dataProvider.mortgageAccounts(),
            insights = context.insights,
            financialGps = context.financialGps,
            goals = context.goals,
            lifeEvents = context.lifeEvents,
            moneyLeaks = context.moneyLeaks,
            suggestedQuestions = suggestedQuestions(),
            designTokens = FutureMeDesignTokens.current,
            disclaimer = DISCLAIMER,
        )
    }

    companion object {
        const val DISCLAIMER = "Educational simulation only, not financial advice."
    }
}
