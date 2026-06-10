package com.futureme.shared.domain

import com.futureme.shared.models.AssistantPrompt
import com.futureme.shared.models.AssistantResponse
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.ScenarioResult
import com.futureme.shared.models.SuggestedQuestion
import com.futureme.shared.models.UserIdentity

interface FinancialDataProvider {
    fun identity(): UserIdentity
    fun profile(): FinancialProfile
    fun baseline(): Scenario
    fun scenarios(): List<Scenario>
    fun suggestedQuestions(): List<SuggestedQuestion>
}

interface FinancialAssistantProvider {
    fun answer(
        prompt: AssistantPrompt,
        profile: FinancialProfile,
        latestScenarioResult: ScenarioResult?,
    ): AssistantResponse
}
