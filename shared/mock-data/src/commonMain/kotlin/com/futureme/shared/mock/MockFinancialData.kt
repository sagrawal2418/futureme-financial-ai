package com.futureme.shared.mock

import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.ScenarioType
import com.futureme.shared.models.SuggestedQuestion
import com.futureme.shared.models.UserIdentity

object MockFinancialData {
    val identity = UserIdentity(
        ownerId = "usr_demo_001",
        displayName = "Jordan Lee",
        householdName = "Lee household",
    )

    val profile = FinancialProfile(
        profileId = "fin_demo_001",
        ownerId = identity.ownerId,
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
        type = ScenarioType.INCREASE_INVESTMENTS,
        title = "Current plan",
        subtitle = "Keep current income, expenses, and contributions",
        tags = listOf("Baseline"),
        rationale = "The baseline keeps current commitments unchanged.",
    )

    val scenarios = listOf(
        Scenario(
            id = "buy-home",
            type = ScenarioType.BUY_HOME,
            title = "Buy a $700K home",
            subtitle = "Use a 20% down payment and take on a new mortgage",
            monthlyExpenseDelta = 2_250.0,
            upfrontCost = 86_000.0,
            mortgageDelta = 560_000.0,
            propertyValueDelta = 700_000.0,
            riskAdjustment = 18,
            tags = listOf("Home", "Long term"),
            rationale = "A larger mortgage and concentrated home equity reduce flexibility.",
        ),
        Scenario(
            id = "wait-to-buy",
            type = ScenarioType.BUY_HOME,
            title = "Wait one year to buy",
            subtitle = "Build the down payment while preserving optionality",
            monthlyExpenseDelta = 350.0,
            monthlyInvestmentDelta = 900.0,
            riskAdjustment = -2,
            tags = listOf("Optionality", "Down payment"),
            rationale = "Waiting preserves liquidity and creates more time to reduce debt.",
        ),
        Scenario(
            id = "refinance-now",
            type = ScenarioType.REFINANCE_MORTGAGE,
            title = "Refinance now",
            subtitle = "Pay closing costs to reduce the monthly payment",
            monthlyExpenseDelta = -510.0,
            upfrontCost = 7_600.0,
            riskAdjustment = -4,
            tags = listOf("Mortgage", "Cash flow"),
            rationale = "Closing costs reduce liquidity while the lower payment improves cash flow.",
        ),
        Scenario(
            id = "keep-current-loan",
            type = ScenarioType.REFINANCE_MORTGAGE,
            title = "Keep current mortgage",
            subtitle = "Preserve cash and keep the current rate",
            riskAdjustment = 2,
            tags = listOf("Stability", "Rates"),
            rationale = "Keeping the loan preserves cash but maintains the current payment.",
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
            tags = listOf("Debt free", "Guaranteed return"),
            rationale = "Eliminating high-interest debt reduces fixed obligations.",
        ),
        Scenario(
            id = "job-loss",
            type = ScenarioType.JOB_LOSS,
            title = "Six-month job loss",
            subtitle = "Stress-test the plan with no take-home income",
            monthlyIncomeDelta = -14_250.0,
            incomeShockMonths = 6,
            riskAdjustment = 24,
            tags = listOf("Emergency", "Stress test"),
            rationale = "A complete income interruption tests reserve adequacy.",
        ),
        Scenario(
            id = "move-to-texas",
            type = ScenarioType.RELOCATE,
            title = "Move to Austin, TX",
            subtitle = "Lower housing costs with a modest compensation adjustment",
            monthlyIncomeDelta = -650.0,
            monthlyExpenseDelta = -1_900.0,
            upfrontCost = 14_500.0,
            riskAdjustment = 3,
            tags = listOf("Relocation", "Lower cost"),
            rationale = "Relocation adds execution risk but lowers recurring housing costs.",
        ),
        Scenario(
            id = "stay-in-new-jersey",
            type = ScenarioType.RELOCATE,
            title = "Stay in New Jersey",
            subtitle = "Keep career momentum and the current support network",
            monthlyExpenseDelta = 250.0,
            riskAdjustment = -2,
            tags = listOf("Stability", "Career"),
            rationale = "Staying preserves career continuity and the household support network.",
        ),
        Scenario(
            id = "have-a-child",
            type = ScenarioType.HAVE_CHILD,
            title = "Have another child",
            subtitle = "Add childcare, healthcare, and family expenses",
            monthlyExpenseDelta = 2_150.0,
            upfrontCost = 12_500.0,
            riskAdjustment = 14,
            tags = listOf("Family", "Childcare"),
            rationale = "Childcare and healthcare add durable monthly commitments.",
        ),
        Scenario(
            id = "invest-more",
            type = ScenarioType.INCREASE_INVESTMENTS,
            title = "Invest $1,200 more",
            subtitle = "Increase automated monthly brokerage contributions",
            monthlyInvestmentDelta = 1_200.0,
            annualInvestmentReturn = 0.07,
            riskAdjustment = 5,
            tags = listOf("Growth", "Automation"),
            rationale = "Higher market exposure improves upside but reduces accessible cash.",
        ),
    )

    val suggestedQuestions = listOf(
        SuggestedQuestion("buy", "Can I afford this house?", "Can I afford to buy a $700k home?", "Home"),
        SuggestedQuestion("refi", "Should I refinance?", "What happens if I refinance my mortgage?", "Mortgage"),
        SuggestedQuestion("runway", "How long will savings last?", "How long will my emergency fund last?", "Safety"),
        SuggestedQuestion("debt", "Debt or invest?", "Should I pay off debt or invest more?", "Priorities"),
        SuggestedQuestion("child", "Can we grow our family?", "What happens financially if I have another child?", "Family"),
        SuggestedQuestion("move", "Can I move to Texas?", "Can I move to Texas and still stay on track?", "Relocation"),
        SuggestedQuestion("risk", "What is my biggest risk?", "What is the biggest financial risk in my profile?", "Risk"),
        SuggestedQuestion("action", "Improve my outlook", "What is one action that improves my 5-year outlook?", "Next step"),
    )

    fun scenario(id: String): Scenario? = scenarios.firstOrNull { it.id == id }
}
