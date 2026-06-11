package com.futureme.shared.mock

import com.futureme.shared.domain.FinancialDataProvider
import com.futureme.shared.models.CashAccount
import com.futureme.shared.models.DebtAccount
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.InvestmentAccount
import com.futureme.shared.models.MortgageAccount
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.SuggestedQuestion
import com.futureme.shared.models.Transaction
import com.futureme.shared.models.UserIdentity

class MockFinancialDataProvider : FinancialDataProvider {
    override fun identity(): UserIdentity = MockFinancialData.identity

    override fun profile(): FinancialProfile = MockFinancialData.profile

    override fun baseline(): Scenario = MockFinancialData.baseline

    override fun scenarios(): List<Scenario> = MockFinancialData.scenarios

    override fun suggestedQuestions(): List<SuggestedQuestion> =
        MockFinancialData.suggestedQuestions

    override fun transactions(): List<Transaction> = MockFinancialData.transactions

    override fun debtAccounts(): List<DebtAccount> = MockFinancialData.debtAccounts

    override fun investmentAccounts(): List<InvestmentAccount> =
        MockFinancialData.investmentAccounts

    override fun cashAccounts(): List<CashAccount> = MockFinancialData.cashAccounts

    override fun mortgageAccounts(): List<MortgageAccount> =
        MockFinancialData.mortgageAccounts
}
