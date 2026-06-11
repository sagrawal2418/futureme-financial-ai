package com.futureme.shared.models

import com.futureme.shared.design.DesignTokens
import kotlinx.serialization.Serializable

@Serializable
data class ProductBootstrap(
    val identity: UserIdentity,
    val profile: FinancialProfile,
    val dashboard: DashboardSnapshot,
    val scenarios: List<Scenario>,
    val recentScenarioResults: List<ScenarioResult>,
    val transactions: List<Transaction>,
    val debtAccounts: List<DebtAccount>,
    val investmentAccounts: List<InvestmentAccount>,
    val cashAccounts: List<CashAccount>,
    val mortgageAccounts: List<MortgageAccount>,
    val insights: List<Insight>,
    val financialGps: FinancialGpsResult,
    val goals: List<GoalProbabilityResult>,
    val lifeEvents: List<LifeEventPlan>,
    val moneyLeaks: List<MoneyLeak>,
    val readiness: List<LifeReadinessResult>,
    val readinessPlans: List<ReadinessImprovementPlan>,
    val decisionSimulations: List<LifeDecisionSimulation>,
    val lifeTimeline: List<LifeTimelinePoint>,
    val executiveDemo: ExecutiveDemoExperience,
    val suggestedQuestions: List<SuggestedQuestion>,
    val designTokens: DesignTokens,
    val disclaimer: String,
)
