package com.futureme.financialai.data

import com.futureme.financialai.model.FinancialProfile
import com.futureme.financialai.model.Scenario
import com.futureme.financialai.repository.FinancialRepository

class MockFinancialRepository : FinancialRepository {
    override fun getProfile(): FinancialProfile = DemoFinancialData.profile

    override fun getBaselineScenario(): Scenario = DemoFinancialData.baseline

    override fun getScenarios(): List<Scenario> = DemoFinancialData.scenarios
}
