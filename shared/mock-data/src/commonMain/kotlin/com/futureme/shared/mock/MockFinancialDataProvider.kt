package com.futureme.shared.mock

import com.futureme.shared.domain.FinancialDataProvider
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.Scenario
import com.futureme.shared.models.SuggestedQuestion
import com.futureme.shared.models.UserIdentity

class MockFinancialDataProvider : FinancialDataProvider {
    override fun identity(): UserIdentity = MockFinancialData.identity

    override fun profile(): FinancialProfile = MockFinancialData.profile

    override fun baseline(): Scenario = MockFinancialData.baseline

    override fun scenarios(): List<Scenario> = MockFinancialData.scenarios

    override fun suggestedQuestions(): List<SuggestedQuestion> =
        MockFinancialData.suggestedQuestions
}
