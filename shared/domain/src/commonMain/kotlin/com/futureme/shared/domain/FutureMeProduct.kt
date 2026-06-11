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
import com.futureme.shared.models.ExecutiveDemoExperience
import com.futureme.shared.models.ExecutiveDemoStep
import com.futureme.shared.models.LifeDecisionSimulation
import com.futureme.shared.models.LifeReadinessResult
import com.futureme.shared.models.LifeTimelinePoint
import com.futureme.shared.models.ProductBootstrap
import com.futureme.shared.models.ReadinessCategory
import com.futureme.shared.models.ReadinessImprovementPlan
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.ScenarioComparison
import com.futureme.shared.models.ScenarioResult
import com.futureme.shared.models.SuggestedQuestion
import com.futureme.shared.models.UserIdentity
import com.futureme.shared.moneyleaks.MoneyLeakDetector
import com.futureme.shared.readiness.LifeDecisionSimulator
import com.futureme.shared.readiness.LifeReadinessEngine
import com.futureme.shared.readiness.LifeTimelineEngine
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
    private val readinessEngine = LifeReadinessEngine()
    private val decisionSimulator = LifeDecisionSimulator(readinessEngine)
    private val timelineEngine = LifeTimelineEngine(readinessEngine)

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

    fun readiness(): List<LifeReadinessResult> = readinessEngine.evaluateAll(profile())

    fun readinessPlan(category: ReadinessCategory): ReadinessImprovementPlan =
        readinessEngine.improvementPlan(profile(), category)

    fun decisionSimulation(scenarioId: String): LifeDecisionSimulation =
        decisionSimulator.simulate(profile(), dashboard(), simulate(scenarioId))

    fun lifeTimeline(): List<LifeTimelinePoint> =
        timelineEngine.build(
            profile(),
            engine.simulate(profile(), dataProvider.baseline()),
        )

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
        val readiness = readiness()
        val simulations = scenarios().map { decisionSimulation(it.id) }
        return FinancialCopilotContext(
            insights = insightsEngine.generate(dashboard(), leaks, goals),
            financialGps = financialGpsEngine.calculate(profile(), dataProvider.baseline()),
            goals = goals,
            lifeEvents = lifeEventPlanner.plans(profile()),
            moneyLeaks = leaks,
            readiness = readiness,
            readinessPlans = readiness.map { readinessEngine.improvementPlan(profile(), it.category) },
            decisionSimulations = simulations,
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
            readiness = context.readiness,
            readinessPlans = context.readinessPlans,
            decisionSimulations = context.decisionSimulations,
            lifeTimeline = lifeTimeline(),
            executiveDemo = executiveDemo(),
            suggestedQuestions = suggestedQuestions(),
            designTokens = FutureMeDesignTokens.current,
            disclaimer = DISCLAIMER,
        )
    }

    private fun executiveDemo(): ExecutiveDemoExperience = ExecutiveDemoExperience(
        personaTitle = "The Lee household",
        personaSummary = "A dual-income family with one child, a mortgage, credit-card debt, and retirement savings.",
        personaFacts = listOf(
            "$242,000 annual household income",
            "One child with active childcare costs",
            "$96,500 liquid savings and $18,400 credit-card debt",
            "$286,000 invested for retirement",
        ),
        steps = listOf(
            ExecutiveDemoStep(
                1,
                "Home readiness",
                ReadinessCategory.HOME_PURCHASE,
                "See whether a larger home is supportable and what blocks the decision.",
                "What is preventing me from buying a home?",
            ),
            ExecutiveDemoStep(
                2,
                "Child readiness",
                ReadinessCategory.CHILD,
                "Understand childcare, leave, healthcare, and reserve requirements.",
                "How can I become ready for another child?",
            ),
            ExecutiveDemoStep(
                3,
                "Relocation readiness",
                ReadinessCategory.RELOCATION,
                "Compare the readiness and financial impact of moving to Texas.",
                "What changes if we move to Texas?",
            ),
            ExecutiveDemoStep(
                4,
                "AI coaching",
                null,
                "Ask the strategist which decision and monthly action matter most.",
                "What should I focus on this month?",
            ),
            ExecutiveDemoStep(
                5,
                "Improvement plan",
                ReadinessCategory.HOME_PURCHASE,
                "Turn readiness gaps into a sequenced plan and target date.",
                "Build my home readiness improvement plan.",
            ),
        ),
    )

    companion object {
        const val DISCLAIMER = "Educational simulation only, not financial advice."
    }
}
