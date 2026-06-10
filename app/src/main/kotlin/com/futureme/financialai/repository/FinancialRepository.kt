package com.futureme.financialai.repository

import com.futureme.financialai.model.FinancialProfile
import com.futureme.financialai.model.Scenario

interface FinancialRepository {
    fun getProfile(): FinancialProfile
    fun getBaselineScenario(): Scenario
    fun getScenarios(): List<Scenario>
}
