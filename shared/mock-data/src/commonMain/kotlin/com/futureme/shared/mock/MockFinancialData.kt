package com.futureme.shared.mock

import com.futureme.shared.models.CashAccount
import com.futureme.shared.models.DebtAccount
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.InvestmentAccount
import com.futureme.shared.models.MortgageAccount
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.ScenarioType
import com.futureme.shared.models.SuggestedQuestion
import com.futureme.shared.models.Transaction
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
        primaryMonthlyNetIncome = 9_250.0,
        spouseMonthlyNetIncome = 5_000.0,
        monthlyChildcare = 1_800.0,
        monthlyInsurance = 680.0,
        monthlySubscriptions = 228.0,
        monthlyUtilities = 510.0,
    )

    val cashAccounts = listOf(
        CashAccount(
            id = "cash-checking",
            name = "Household checking",
            balance = 32_000.0,
            annualPercentageYield = 0.0001,
            isEmergencyFund = false,
        ),
        CashAccount(
            id = "cash-emergency",
            name = "Emergency reserve",
            balance = 64_500.0,
            annualPercentageYield = 0.043,
            isEmergencyFund = true,
        ),
    )

    val debtAccounts = listOf(
        DebtAccount(
            id = "debt-visa",
            name = "Rewards Visa",
            balance = 12_600.0,
            annualPercentageRate = 0.2199,
            minimumMonthlyPayment = 525.0,
            category = "Credit card",
        ),
        DebtAccount(
            id = "debt-mastercard",
            name = "Travel Mastercard",
            balance = 5_800.0,
            annualPercentageRate = 0.1899,
            minimumMonthlyPayment = 325.0,
            category = "Credit card",
        ),
        DebtAccount(
            id = "debt-auto",
            name = "Family SUV loan",
            balance = 21_400.0,
            annualPercentageRate = 0.0475,
            minimumMonthlyPayment = 540.0,
            category = "Auto loan",
        ),
    )

    val investmentAccounts = listOf(
        InvestmentAccount(
            id = "investment-401k",
            name = "Jordan 401(k)",
            balance = 174_000.0,
            monthlyContribution = 1_150.0,
            employeeContributionPercent = 0.04,
            employerMatchPercent = 0.06,
        ),
        InvestmentAccount(
            id = "investment-403b",
            name = "Taylor 403(b)",
            balance = 68_000.0,
            monthlyContribution = 700.0,
            employeeContributionPercent = 0.06,
            employerMatchPercent = 0.05,
        ),
        InvestmentAccount(
            id = "investment-brokerage",
            name = "Family brokerage",
            balance = 44_000.0,
            monthlyContribution = 0.0,
        ),
    )

    val mortgageAccounts = listOf(
        MortgageAccount(
            id = "mortgage-primary",
            name = "Primary residence mortgage",
            balance = 451_000.0,
            annualPercentageRate = 0.0675,
            monthlyPayment = 3_825.0,
            propertyValue = 735_000.0,
            remainingTermMonths = 326,
        ),
    )

    val transactions: List<Transaction> = transactionDates().mapIndexed { index, date ->
        val template = when {
            index % 30 == 0 -> Triple("Garden State Mortgage", "Housing", 3_825.0)
            index % 15 == 4 -> Triple("Bright Horizons", "Childcare", 900.0)
            index % 15 == 11 -> Triple("Bright Horizons", "Childcare", 900.0)
            index % 30 == 7 -> Triple("NJ Family Insurance", "Insurance", 680.0)
            index % 14 == 6 -> Triple("PSE&G Utilities", "Utilities", 255.0)
            index % 14 == 13 -> Triple("Verizon + Internet", "Utilities", 255.0)
            index % 30 == 2 -> Triple("Streaming bundle", "Subscriptions", 126.0)
            index % 30 == 18 -> Triple("Software memberships", "Subscriptions", 102.0)
            index % 7 == 1 -> Triple("Whole Foods Market", "Groceries", 186.0)
            index % 7 == 5 -> Triple("Costco", "Groceries", 238.0)
            index % 9 == 3 -> Triple("NJ Transit", "Transportation", 84.0)
            index % 8 == 4 -> Triple("Local restaurant", "Dining", 96.0)
            else -> Triple("Household purchase", "General", 42.0 + (index % 5) * 11.0)
        }
        Transaction(
            id = "txn-${(index + 1).toString().padStart(3, '0')}",
            postedDate = date,
            merchant = template.first,
            category = template.second,
            amount = template.third,
            isRecurring = template.second in setOf(
                "Housing",
                "Childcare",
                "Insurance",
                "Utilities",
                "Subscriptions",
            ),
        )
    }

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
            id = "spouse-stops-working",
            type = ScenarioType.SPOUSE_STOPS_WORKING,
            title = "Spouse stops working",
            subtitle = "Stress-test the family plan on one income",
            monthlyIncomeDelta = -5_000.0,
            riskAdjustment = 20,
            tags = listOf("One income", "Family"),
            rationale = "Losing the second income reduces flexibility and increases reliance on reserves.",
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
            id = "start-business",
            type = ScenarioType.START_BUSINESS,
            title = "Start a business",
            subtitle = "Invest $40K and replace part of one salary during launch",
            monthlyIncomeDelta = -4_000.0,
            monthlyExpenseDelta = 800.0,
            upfrontCost = 40_000.0,
            riskAdjustment = 25,
            tags = listOf("Entrepreneurship", "Income risk"),
            rationale = "Startup capital and uncertain income require a longer household runway.",
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
        SuggestedQuestion(
            "mission-next-action",
            "What should I do next?",
            "What should I do next?",
            "Mission Coach",
        ),
        SuggestedQuestion(
            "mission-readiness",
            "Why is readiness low?",
            "Why is my readiness low?",
            "Mission Coach",
        ),
        SuggestedQuestion(
            "mission-blocker",
            "Biggest blocker",
            "What is my biggest blocker?",
            "Mission Coach",
        ),
        SuggestedQuestion(
            "mission-faster",
            "Become ready faster",
            "How can I become ready faster?",
            "Mission Coach",
        ),
        SuggestedQuestion(
            "mission-priority",
            "Prioritize a mission",
            "Which mission should I prioritize?",
            "Mission Coach",
        ),
        SuggestedQuestion(
            "home-blockers",
            "Home mission",
            "What is preventing my Home Mission from succeeding?",
            "Mission Coach",
        ),
        SuggestedQuestion(
            "home-purchase-blockers",
            "Home blockers",
            "What is preventing me from buying a home?",
            "Mission Coach",
        ),
        SuggestedQuestion(
            "weakest-readiness",
            "Weakest readiness",
            "What is my weakest readiness category?",
            "Mission Coach",
        ),
        SuggestedQuestion(
            "monthly-focus",
            "This month's focus",
            "What should I focus on this month?",
            "Mission Coach",
        ),
    )

    fun scenario(id: String): Scenario? = scenarios.firstOrNull { it.id == id }

    private fun transactionDates(): List<String> = buildList {
        (13..31).forEach { add("2026-03-${it.toString().padStart(2, '0')}") }
        (1..30).forEach { add("2026-04-${it.toString().padStart(2, '0')}") }
        (1..31).forEach { add("2026-05-${it.toString().padStart(2, '0')}") }
        (1..10).forEach { add("2026-06-${it.toString().padStart(2, '0')}") }
    }
}
