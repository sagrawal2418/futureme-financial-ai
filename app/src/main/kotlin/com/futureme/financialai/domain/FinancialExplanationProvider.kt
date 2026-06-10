package com.futureme.financialai.domain

import com.futureme.financialai.model.Scenario

interface FinancialExplanationProvider {
    fun explain(
        scenario: Scenario,
        monthlySurplus: Double,
        emergencyMonths: Double,
        fiveYearDelta: Double,
        riskScore: Int,
    ): String
}
