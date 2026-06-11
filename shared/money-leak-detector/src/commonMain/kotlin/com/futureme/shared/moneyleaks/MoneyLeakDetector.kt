package com.futureme.shared.moneyleaks

import com.futureme.shared.models.ActionDifficulty
import com.futureme.shared.models.CashAccount
import com.futureme.shared.models.DebtAccount
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.InvestmentAccount
import com.futureme.shared.models.MoneyLeak
import com.futureme.shared.models.MoneyLeakType
import com.futureme.shared.models.MortgageAccount
import com.futureme.shared.models.Transaction
import kotlin.math.max

class MoneyLeakDetector {
    fun detect(
        profile: FinancialProfile,
        transactions: List<Transaction>,
        cashAccounts: List<CashAccount>,
        debtAccounts: List<DebtAccount>,
        investmentAccounts: List<InvestmentAccount>,
        mortgageAccounts: List<MortgageAccount>,
    ): List<MoneyLeak> {
        val leaks = mutableListOf<MoneyLeak>()
        val monthlySubscriptions = transactions
            .filter { it.category == "Subscriptions" }
            .sumOf(Transaction::amount) / 3.0
        if (monthlySubscriptions > 150.0) {
            leaks += leak(
                id = "subscriptions",
                type = MoneyLeakType.HIGH_SUBSCRIPTION_SPEND,
                title = "Subscription creep",
                summary = "Recurring media, software, and membership charges are above the household target.",
                monthlyLoss = monthlySubscriptions - 120.0,
                recommendation = "Cancel or downgrade two low-use subscriptions this week.",
                difficulty = ActionDifficulty.EASY,
            )
        }

        val excessChecking = cashAccounts
            .filterNot(CashAccount::isEmergencyFund)
            .sumOf(CashAccount::balance)
            .minus(profile.monthlyLivingExpenses * 1.5)
            .coerceAtLeast(0.0)
        if (excessChecking > 5_000.0) {
            val monthlyOpportunityCost = excessChecking * 0.04 / 12.0
            leaks += leak(
                id = "checking-cash",
                type = MoneyLeakType.EXCESS_CHECKING_CASH,
                title = "Idle checking cash",
                summary = "Cash above the operating buffer is earning almost no interest.",
                monthlyLoss = monthlyOpportunityCost,
                recommendation = "Move $${excessChecking.toInt()} to the emergency fund or a high-yield account.",
                difficulty = ActionDifficulty.EASY,
            )
        }

        debtAccounts.filter { it.annualPercentageRate >= 0.12 && it.balance > 0.0 }
            .maxByOrNull(DebtAccount::annualPercentageRate)
            ?.let { debt ->
                leaks += leak(
                    id = "high-interest-debt",
                    type = MoneyLeakType.HIGH_INTEREST_DEBT,
                    title = "High-interest debt drag",
                    summary = "${debt.name} is compounding at ${(debt.annualPercentageRate * 100).toInt()}% APR.",
                    monthlyLoss = debt.balance * debt.annualPercentageRate / 12.0,
                    recommendation = "Direct an additional $300 monthly to this balance before increasing taxable investing.",
                    difficulty = ActionDifficulty.MODERATE,
                )
            }

        val monthlyInsurance = transactions
            .filter { it.category == "Insurance" }
            .sumOf(Transaction::amount) / 3.0
        if (monthlyInsurance > 500.0) {
            leaks += leak(
                id = "insurance",
                type = MoneyLeakType.INSURANCE_OVERPAYMENT,
                title = "Insurance review opportunity",
                summary = "Combined premiums are above the modeled peer benchmark.",
                monthlyLoss = monthlyInsurance - 500.0,
                recommendation = "Requote home and auto coverage while preserving current limits.",
                difficulty = ActionDifficulty.MODERATE,
            )
        }

        mortgageAccounts.firstOrNull { it.annualPercentageRate >= 0.0625 }?.let { mortgage ->
            val monthlySavings = max(0.0, mortgage.monthlyPayment * 0.12)
            leaks += leak(
                id = "mortgage-refinance",
                type = MoneyLeakType.REFINANCE_OPPORTUNITY,
                title = "Mortgage refinance watch",
                summary = "The current mortgage rate is high enough to monitor against a 1-point rate improvement.",
                monthlyLoss = monthlySavings,
                recommendation = "Set a rate alert and model break-even after closing costs before refinancing.",
                difficulty = ActionDifficulty.INVOLVED,
            )
        }

        investmentAccounts.firstOrNull {
            it.employerMatchPercent > it.employeeContributionPercent
        }?.let { account ->
            val gap = account.employerMatchPercent - account.employeeContributionPercent
            val monthlyMissedMatch = profile.annualGrossIncome * gap / 12.0
            leaks += leak(
                id = "employer-match",
                type = MoneyLeakType.MISSED_EMPLOYER_MATCH,
                title = "Unclaimed employer match",
                summary = "Retirement contributions are below the available employer match.",
                monthlyLoss = monthlyMissedMatch,
                recommendation = "Increase payroll contributions by ${(gap * 100).toInt()} percentage points.",
                difficulty = ActionDifficulty.EASY,
            )
        }

        return leaks.sortedByDescending(MoneyLeak::estimatedAnnualLoss)
    }

    private fun leak(
        id: String,
        type: MoneyLeakType,
        title: String,
        summary: String,
        monthlyLoss: Double,
        recommendation: String,
        difficulty: ActionDifficulty,
    ): MoneyLeak = MoneyLeak(
        id = id,
        type = type,
        title = title,
        summary = summary,
        estimatedMonthlyLoss = monthlyLoss,
        estimatedAnnualLoss = monthlyLoss * 12.0,
        estimatedFiveYearLoss = monthlyLoss * 60.0,
        fixRecommendation = recommendation,
        difficulty = difficulty,
    )
}
