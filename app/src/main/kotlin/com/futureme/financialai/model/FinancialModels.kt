package com.futureme.financialai.model

data class FinancialProfile(
    val profileId: String,
    val householdName: String,
    val stateCode: String,
    val annualGrossIncome: Double,
    val monthlyNetIncome: Double,
    val monthlyLivingExpenses: Double,
    val liquidSavings: Double,
    val creditCardDebt: Double,
    val creditCardApr: Double,
    val housingPayment: Double,
    val mortgageBalance: Double,
    val propertyValue: Double,
    val investmentBalance: Double,
    val monthlyRetirementContribution: Double,
    val monthlyDebtPayments: Double,
    val dependents: Int,
)

enum class ScenarioType {
    RELOCATE,
    PAY_OFF_DEBT,
    REFINANCE,
    HAVE_CHILD,
    INVEST_MORE,
    JOB_LOSS,
}

data class Scenario(
    val id: String,
    val type: ScenarioType,
    val title: String,
    val subtitle: String,
    val monthlyIncomeDelta: Double = 0.0,
    val monthlyExpenseDelta: Double = 0.0,
    val upfrontCost: Double = 0.0,
    val debtDelta: Double = 0.0,
    val mortgageDelta: Double = 0.0,
    val propertyValueDelta: Double = 0.0,
    val monthlyInvestmentDelta: Double = 0.0,
    val annualInvestmentReturn: Double = 0.06,
    val riskAdjustment: Int = 0,
    val incomeShockMonths: Int = 0,
    val rationale: String,
)

data class ProjectionPoint(
    val year: Int,
    val baselineNetWorth: Double,
    val scenarioNetWorth: Double,
)

enum class RiskLevel {
    LOW,
    MODERATE,
    ELEVATED,
    HIGH,
}

data class RiskFactor(
    val id: String,
    val title: String,
    val explanation: String,
    val points: Int,
)

data class RiskAssessment(
    val score: Int,
    val level: RiskLevel,
    val summary: String,
    val factors: List<RiskFactor>,
)

data class ScenarioResult(
    val scenario: Scenario,
    val monthlyCashFlowImpact: Double,
    val projectedMonthlySurplus: Double,
    val projectedNetWorth1Year: Double,
    val projectedNetWorth3Years: Double,
    val projectedNetWorth5Years: Double,
    val netWorthDelta5Years: Double,
    val emergencyFundMonths: Double,
    val debtPayoffMonths: Int?,
    val healthScore: Int,
    val risk: RiskAssessment,
    val projections: List<ProjectionPoint>,
    val tradeoffs: List<String>,
    val recommendation: String,
)

data class DashboardSnapshot(
    val healthScore: Int,
    val currentNetWorth: Double,
    val projectedNetWorth5Years: Double,
    val monthlySurplus: Double,
    val emergencyFundMonths: Double,
    val debtPayoffMonths: Int?,
    val alerts: List<String>,
)

data class ScenarioComparison(
    val left: ScenarioResult,
    val right: ScenarioResult,
    val preferredScenarioId: String,
    val summary: String,
)

data class FinancialWorkspace(
    val profile: FinancialProfile,
    val dashboard: DashboardSnapshot,
    val scenarios: List<Scenario>,
    val selectedScenario: Scenario,
    val selectedResult: ScenarioResult,
    val comparison: ScenarioComparison,
)
