package com.futureme.shared.domain

import com.futureme.shared.models.AssistantPrompt
import com.futureme.shared.models.AssistantResponse
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.FinancialCopilotContext
import com.futureme.shared.models.CashAccount
import com.futureme.shared.models.DebtAccount
import com.futureme.shared.models.InvestmentAccount
import com.futureme.shared.models.MortgageAccount
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.ScenarioResult
import com.futureme.shared.models.SuggestedQuestion
import com.futureme.shared.models.Transaction
import com.futureme.shared.models.UserIdentity

interface FinancialDataProvider {
    fun identity(): UserIdentity
    fun profile(): FinancialProfile
    fun baseline(): Scenario
    fun scenarios(): List<Scenario>
    fun suggestedQuestions(): List<SuggestedQuestion>
    fun transactions(): List<Transaction>
    fun debtAccounts(): List<DebtAccount>
    fun investmentAccounts(): List<InvestmentAccount>
    fun cashAccounts(): List<CashAccount>
    fun mortgageAccounts(): List<MortgageAccount>
}

interface FinancialAssistantProvider {
    fun answer(
        prompt: AssistantPrompt,
        profile: FinancialProfile,
        latestScenarioResult: ScenarioResult?,
        context: FinancialCopilotContext,
    ): AssistantResponse
}
