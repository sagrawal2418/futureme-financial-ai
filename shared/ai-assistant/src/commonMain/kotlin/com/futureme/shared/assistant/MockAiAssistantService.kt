package com.futureme.shared.assistant

import com.futureme.shared.calculators.FinancialMath
import com.futureme.shared.domain.FinancialAssistantProvider
import com.futureme.shared.models.AssistantPrompt
import com.futureme.shared.models.AssistantResponse
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.ScenarioResult
import com.futureme.shared.mock.MockFinancialData
import com.futureme.shared.scenario.ScenarioEngine
import kotlin.math.abs
import kotlin.math.roundToInt

class MockAiAssistantService(
    private val scenarioEngine: ScenarioEngine,
) : FinancialAssistantProvider {
    override fun answer(
        prompt: AssistantPrompt,
        profile: FinancialProfile,
        latestScenarioResult: ScenarioResult?,
    ): AssistantResponse {
        val question = prompt.question.trim().lowercase()
        if (question.isBlank()) {
            return AssistantResponse(
                answer = "Ask me about a home purchase, debt payoff, emergency runway, relocation, family change, or investment contribution.",
            )
        }

        val matchedScenarioId = when {
            "house" in question || "home" in question || "buy" in question -> "buy-home"
            "refinance" in question || "mortgage" in question -> "refinance-now"
            "emergency" in question || "savings last" in question || "runway" in question -> "job-loss"
            "debt" in question || "credit card" in question -> "pay-off-cards"
            "child" in question || "baby" in question -> "have-a-child"
            "texas" in question || "move" in question || "relocate" in question -> "move-to-texas"
            "invest" in question || "five-year" in question || "outlook" in question -> "invest-more"
            else -> prompt.latestScenarioId
        }
        val result = matchedScenarioId
            ?.let(MockFinancialData::scenario)
            ?.let { scenarioEngine.simulate(profile, it) }
            ?: latestScenarioResult

        if ("biggest" in question && "risk" in question) {
            val baseline = scenarioEngine.simulate(profile, MockFinancialData.baseline)
            val factor = baseline.riskScore.factors.maxByOrNull { it.points }
            return AssistantResponse(
                answer = "Your largest modeled risk is ${factor?.title?.lowercase() ?: "planning uncertainty"}. " +
                    "${factor?.explanation ?: baseline.riskScore.summary} " +
                    "Paying down high-interest debt is the clearest near-term improvement.",
                relatedScenarioId = "pay-off-cards",
                suggestedActions = listOf(
                    "Model the debt payoff scenario",
                    "Keep at least six months of essential expenses liquid",
                ),
            )
        }

        if ("one action" in question || ("improve" in question && "outlook" in question)) {
            val payoff = scenarioEngine.simulate(
                profile,
                requireNotNull(MockFinancialData.scenario("pay-off-cards")),
            )
            return AssistantResponse(
                answer = "Paying off the credit-card balance is the strongest immediate action. " +
                    "It improves monthly cash flow by ${payoff.monthlyCashFlowImpact.asDollars()} " +
                    "and removes a 20.99% APR obligation while keeping " +
                    "${oneDecimal(payoff.emergencyFundMonths)} months of runway.",
                relatedScenarioId = "pay-off-cards",
                suggestedActions = listOf(
                    "Review the payoff tradeoffs",
                    "Redirect the former debt payment into investing after payoff",
                ),
            )
        }

        if (result != null) {
            val direction = if (result.monthlyCashFlowImpact >= 0.0) "improves" else "reduces"
            return AssistantResponse(
                answer = "${result.scenario.title} $direction monthly cash flow by " +
                    "${abs(result.monthlyCashFlowImpact).asDollars()}. " +
                    "Emergency runway becomes ${oneDecimal(result.emergencyFundMonths)} months, " +
                    "five-year net worth changes by ${result.netWorthDelta5Years.asSignedDollars()}, " +
                    "and modeled risk is ${result.riskScore.level.name.lowercase()} " +
                    "at ${result.riskScore.value}/100. ${result.recommendation}",
                relatedScenarioId = result.scenario.id,
                suggestedActions = result.tradeoffs.take(2),
            )
        }

        val runway = FinancialMath.emergencyFundRunway(
            profile.liquidSavings,
            FinancialMath.essentialMonthlyOutflow(profile),
        )
        return AssistantResponse(
            answer = "Your current plan has ${FinancialMath.monthlyCashFlow(profile).asDollars()} " +
                "of monthly surplus and ${oneDecimal(runway)} months of emergency runway. " +
                "Choose a scenario or ask about a specific decision for a more targeted answer.",
            suggestedActions = listOf(
                "Ask about buying a home",
                "Ask whether to pay off debt or invest more",
            ),
        )
    }
}

private fun Double.asDollars(): String = "$" + abs(roundToInt()).toString()

private fun Double.asSignedDollars(): String =
    (if (this >= 0.0) "+$" else "-$") + abs(roundToInt())

private fun oneDecimal(value: Double): String =
    ((value * 10.0).roundToInt() / 10.0).toString()
