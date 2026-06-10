package com.futureme.financialai.data

import com.futureme.financialai.model.FinancialProfile
import com.futureme.financialai.model.Scenario
import com.futureme.financialai.model.ScenarioType

object DemoFinancialData {
    val profile = FinancialProfile(
        profileId = "profile_demo_001",
        householdName = "Lee household",
        stateCode = "NJ",
        annualGrossIncome = 242_000.0,
        monthlyNetIncome = 14_250.0,
        monthlyLivingExpenses = 5_050.0,
        liquidSavings = 96_500.0,
        creditCardDebt = 18_400.0,
        creditCardApr = 0.2099,
        housingPayment = 3_825.0,
        mortgageBalance = 451_000.0,
        propertyValue = 735_000.0,
        investmentBalance = 286_000.0,
        monthlyRetirementContribution = 1_850.0,
        monthlyDebtPayments = 850.0,
        dependents = 1,
    )

    val baseline = Scenario(
        id = "baseline",
        type = ScenarioType.INVEST_MORE,
        title = "Current plan",
        subtitle = "Keep current income, expenses, and contributions",
        rationale = "The baseline keeps current household commitments unchanged.",
    )

    val scenarios = listOf(
        Scenario(
            id = "move-to-austin",
            type = ScenarioType.RELOCATE,
            title = "Move to Austin, TX",
            subtitle = "Lower housing costs with a modest compensation adjustment",
            monthlyIncomeDelta = -650.0,
            monthlyExpenseDelta = -1_900.0,
            upfrontCost = 14_500.0,
            riskAdjustment = 3,
            rationale = "Relocation adds execution risk but lowers recurring housing costs.",
        ),
        Scenario(
            id = "pay-off-cards",
            type = ScenarioType.PAY_OFF_DEBT,
            title = "Pay off credit cards",
            subtitle = "Use savings to eliminate 20.99% APR debt",
            monthlyExpenseDelta = -850.0,
            upfrontCost = 18_400.0,
            debtDelta = -18_400.0,
            riskAdjustment = -12,
            rationale = "Eliminating high-interest debt reduces fixed obligations.",
        ),
        Scenario(
            id = "refinance-now",
            type = ScenarioType.REFINANCE,
            title = "Refinance now",
            subtitle = "Pay closing costs to lower the monthly payment",
            monthlyExpenseDelta = -510.0,
            upfrontCost = 7_600.0,
            riskAdjustment = -4,
            rationale = "Closing costs reduce liquidity while the lower payment improves cash flow.",
        ),
        Scenario(
            id = "have-a-child",
            type = ScenarioType.HAVE_CHILD,
            title = "Grow the family",
            subtitle = "Add childcare, healthcare, and family expenses",
            monthlyExpenseDelta = 2_150.0,
            upfrontCost = 12_500.0,
            riskAdjustment = 14,
            rationale = "Childcare and healthcare add durable monthly commitments.",
        ),
        Scenario(
            id = "invest-more",
            type = ScenarioType.INVEST_MORE,
            title = "Invest $1,200 more",
            subtitle = "Increase automated monthly brokerage contributions",
            monthlyInvestmentDelta = 1_200.0,
            annualInvestmentReturn = 0.07,
            riskAdjustment = 5,
            rationale = "Higher market exposure improves upside but reduces accessible cash.",
        ),
        Scenario(
            id = "job-loss",
            type = ScenarioType.JOB_LOSS,
            title = "Six-month job loss",
            subtitle = "Stress-test the plan with no take-home income",
            monthlyIncomeDelta = -14_250.0,
            incomeShockMonths = 6,
            riskAdjustment = 24,
            rationale = "A complete income interruption tests reserve adequacy.",
        ),
    )
}
