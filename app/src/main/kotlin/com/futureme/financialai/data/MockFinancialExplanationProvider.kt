package com.futureme.financialai.data

import com.futureme.financialai.domain.FinancialExplanationProvider
import com.futureme.financialai.model.Scenario

/**
 * Deterministic copy keeps MVP behavior testable and prevents an AI model from
 * becoming the source of financial calculations.
 */
class MockFinancialExplanationProvider : FinancialExplanationProvider {
    override fun explain(
        scenario: Scenario,
        monthlySurplus: Double,
        emergencyMonths: Double,
        fiveYearDelta: Double,
        riskScore: Int,
    ): String = when {
        monthlySurplus < 0.0 ->
            "This creates a monthly deficit. Reduce the commitment or delay the decision."
        emergencyMonths < 4.0 ->
            "The long-term case may work, but the cash buffer is too thin to proceed comfortably."
        riskScore >= 60 ->
            "Treat this as a stretch scenario and test a smaller first step."
        fiveYearDelta > 25_000.0 ->
            "${scenario.title} is financially supportable and improves the five-year outlook."
        else ->
            "${scenario.title} is manageable, but flexibility and lifestyle fit should break the tie."
    }

    // TODO(AI): Replace this adapter with a governed Azure OpenAI provider that
    // receives calculator outputs only and never invents financial figures.
}
