package com.futureme.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class UserIdentity(
    val ownerId: String,
    val displayName: String,
    val householdName: String,
)

@Serializable
data class FinancialProfile(
    val profileId: String,
    val ownerId: String,
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
    val primaryMonthlyNetIncome: Double = monthlyNetIncome,
    val spouseMonthlyNetIncome: Double = 0.0,
    val monthlyChildcare: Double = 0.0,
    val monthlyInsurance: Double = 0.0,
    val monthlySubscriptions: Double = 0.0,
    val monthlyUtilities: Double = 0.0,
)

@Serializable
data class Transaction(
    val id: String,
    val postedDate: String,
    val merchant: String,
    val category: String,
    val amount: Double,
    val isRecurring: Boolean = false,
)

@Serializable
data class DebtAccount(
    val id: String,
    val name: String,
    val balance: Double,
    val annualPercentageRate: Double,
    val minimumMonthlyPayment: Double,
    val category: String,
)

@Serializable
data class InvestmentAccount(
    val id: String,
    val name: String,
    val balance: Double,
    val monthlyContribution: Double,
    val employeeContributionPercent: Double = 0.0,
    val employerMatchPercent: Double = 0.0,
)

@Serializable
data class CashAccount(
    val id: String,
    val name: String,
    val balance: Double,
    val annualPercentageYield: Double,
    val isEmergencyFund: Boolean,
)

@Serializable
data class MortgageAccount(
    val id: String,
    val name: String,
    val balance: Double,
    val annualPercentageRate: Double,
    val monthlyPayment: Double,
    val propertyValue: Double,
    val remainingTermMonths: Int,
)

@Serializable
enum class ScenarioType {
    BUY_HOME,
    REFINANCE_MORTGAGE,
    PAY_OFF_DEBT,
    JOB_LOSS,
    SPOUSE_STOPS_WORKING,
    RELOCATE,
    HAVE_CHILD,
    START_BUSINESS,
    INCREASE_INVESTMENTS,
}

@Serializable
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
    val tags: List<String> = emptyList(),
    val rationale: String,
)

@Serializable
data class ProjectionPoint(
    val year: Int,
    val baselineNetWorth: Double,
    val scenarioNetWorth: Double,
)

@Serializable
enum class RiskLevel {
    LOW,
    MODERATE,
    ELEVATED,
    HIGH,
}

@Serializable
data class RiskFactor(
    val id: String,
    val title: String,
    val explanation: String,
    val points: Int,
)

@Serializable
data class RiskScore(
    val value: Int,
    val level: RiskLevel,
    val summary: String,
    val factors: List<RiskFactor>,
)

@Serializable
data class FinancialHealthScore(
    val value: Int,
    val label: String,
    val summary: String,
)

@Serializable
data class ScenarioResult(
    val scenario: Scenario,
    val monthlyCashFlowImpact: Double,
    val projectedMonthlySurplus: Double,
    val baselineNetWorth: Double,
    val projectedNetWorth1Year: Double,
    val projectedNetWorth3Years: Double,
    val projectedNetWorth5Years: Double,
    val netWorthDelta5Years: Double,
    val healthScore: FinancialHealthScore,
    val riskScore: RiskScore,
    val emergencyFundMonths: Double,
    val debtPayoffMonths: Int?,
    val projections: List<ProjectionPoint>,
    val tradeoffs: List<String>,
    val recommendation: String,
)

@Serializable
data class DashboardSnapshot(
    val healthScore: FinancialHealthScore,
    val riskScore: RiskScore,
    val monthlyCashFlow: Double,
    val currentNetWorth: Double,
    val projectedNetWorth5Years: Double,
    val emergencyFundMonths: Double,
    val debtPayoffMonths: Int?,
    val alerts: List<String>,
)

@Serializable
data class ScenarioComparison(
    val left: ScenarioResult,
    val right: ScenarioResult,
    val preferredScenarioId: String,
    val summary: String,
)
