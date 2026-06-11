package com.futureme.shared.assistant

import com.futureme.shared.calculators.FinancialMath
import com.futureme.shared.domain.FinancialAssistantProvider
import com.futureme.shared.models.AssistantPrompt
import com.futureme.shared.models.AssistantResponse
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.FinancialCopilotContext
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
        context: FinancialCopilotContext,
    ): AssistantResponse {
        val question = prompt.question.trim().lowercase()
        if (question.isBlank()) {
            return AssistantResponse(
                answer = "Ask me which life decision you are ready for, what is blocking it, or what to focus on this month.",
            )
        }

        if ("weakest" in question && "readiness" in question) {
            val weakest = context.readiness.minByOrNull { it.readinessScore }
            return AssistantResponse(
                answer = "${weakest?.title ?: "Business startup readiness"} is currently the weakest " +
                    "category at ${weakest?.readinessScore ?: 0}%. " +
                    (weakest?.blockers?.firstOrNull()
                        ?: "Build more liquidity before taking on the decision."),
                suggestedActions = weakest?.recommendedActions.orEmpty().take(3),
            )
        }

        if (
            ("plan" in question || "improve" in question) &&
            ("home" in question || "house" in question)
        ) {
            val home = context.readiness.first { it.category.name == "HOME_PURCHASE" }
            val plan = context.readinessPlans.first { it.category.name == "HOME_PURCHASE" }
            return AssistantResponse(
                answer = "Home Purchase Readiness is ${home.readinessScore}%. " +
                    "The plan targets ${plan.targetScore}% in ${plan.estimatedTimelineMonths} months: " +
                    plan.recommendations.take(4).joinToString(separator = "; "),
                relatedScenarioId = "wait-to-buy",
                suggestedActions = plan.recommendations,
            )
        }

        if (
            ("preventing" in question || "block" in question) &&
            ("home" in question || "house" in question)
        ) {
            val home = context.readiness.first { it.category.name == "HOME_PURCHASE" }
            return AssistantResponse(
                answer = "Home Purchase Readiness is ${home.readinessScore}% " +
                    "(${home.readinessLevel.name.lowercase().replace('_', ' ')}). " +
                    "The main blockers are ${home.blockers.joinToString(separator = "; ")}",
                relatedScenarioId = "wait-to-buy",
                suggestedActions = home.recommendedActions,
            )
        }

        if (
            ("ready" in question || "prepare" in question) &&
            ("child" in question || "baby" in question)
        ) {
            val child = context.readiness.first { it.category.name == "CHILD" }
            val plan = context.readinessPlans.first { it.category.name == "CHILD" }
            return AssistantResponse(
                answer = "Child Readiness is ${child.readinessScore}%. " +
                    "To reach ${plan.targetScore}%, focus on " +
                    plan.recommendations.take(3).joinToString(separator = "; ") +
                    ". The modeled timeline is ${plan.estimatedTimelineMonths} months.",
                relatedScenarioId = "have-a-child",
                suggestedActions = plan.recommendations,
            )
        }

        if ("decision" in question && ("improves" in question || "best" in question)) {
            val best = context.decisionSimulations.maxByOrNull {
                it.readinessImpact * 5_000.0 + it.fiveYearNetWorthImpact
            }
            return AssistantResponse(
                answer = "${best?.title ?: "Pay off credit cards"} creates the strongest " +
                    "modeled improvement. It changes readiness by " +
                    "${best?.readinessImpact?.let { if (it >= 0) "+$it" else "$it" } ?: "+0"} points " +
                    "and five-year net worth by ${best?.fiveYearNetWorthImpact?.asSignedDollars() ?: "$0"}.",
                relatedScenarioId = best?.scenarioId,
                suggestedActions = best?.recommendedActions.orEmpty().take(3),
            )
        }

        if ("focus" in question && ("month" in question || "now" in question)) {
            val weakest = context.readiness.minByOrNull { it.readinessScore }
            val debtAction = "Direct an additional $300 to high-interest debt."
            return AssistantResponse(
                answer = "This month, make debt reduction the first move. $debtAction " +
                    "That improves cash flow, lowers risk, and strengthens " +
                    "${weakest?.title?.lowercase() ?: "your weakest readiness category"}.",
                relatedScenarioId = "pay-off-cards",
                suggestedActions = listOf(
                    debtAction,
                    "Keep six months of essential expenses liquid.",
                    weakest?.recommendedActions?.firstOrNull() ?: "Review the readiness dashboard.",
                ),
            )
        }

        if ("money leak" in question || ("biggest" in question && "leak" in question)) {
            val leak = context.moneyLeaks.first()
            return AssistantResponse(
                answer = "Your largest detected money leak is ${leak.title.lowercase()}, " +
                    "with about ${leak.estimatedAnnualLoss.asDollars()} in annual impact. " +
                    leak.fixRecommendation,
                suggestedActions = listOf(leak.fixRecommendation, "Review all detected money leaks"),
            )
        }

        if (
            "one action" !in question &&
            "improve" in question &&
            ("5-year" in question || "outlook" in question)
        ) {
            val gps = context.financialGps
            return AssistantResponse(
                answer = "The Financial GPS plan improves modeled five-year net worth by " +
                    "${gps.difference.asDollars()}. " +
                    gps.monthlyActionPlan.joinToString(prefix = "The plan is: ", separator = "; ") + ".",
                relatedScenarioId = "pay-off-cards",
                suggestedActions = gps.monthlyActionPlan,
            )
        }

        if ("ready" in question && ("home" in question || "house" in question)) {
            val goal = context.goals.first { it.id == "goal-home" }
            return AssistantResponse(
                answer = "Home readiness is ${goal.probabilityPercentage}%. " +
                    "${goal.blockers.joinToString()}. " +
                    "The modeled improvement needed is ${goal.requiredMonthlyImprovement.asDollars()} per month.",
                relatedScenarioId = "wait-to-buy",
                suggestedActions = goal.recommendedActions,
            )
        }

        if ("before" in question && ("child" in question || "baby" in question)) {
            val event = context.lifeEvents.first { it.id == "event-baby" }
            return AssistantResponse(
                answer = "Before another child, plan for ${event.estimatedMonthlyImpact.asDollars()} " +
                    "in recurring costs and ${event.oneTimeCostLow.asDollars()} to " +
                    "${event.oneTimeCostHigh.asDollars()} upfront. " +
                    event.recommendedPreparationSteps.joinToString(separator = "; ") + ".",
                relatedScenarioId = "have-a-child",
                suggestedActions = event.recommendedPreparationSteps,
            )
        }

        if ("simulate next" in question || ("decision" in question && "next" in question)) {
            val lowestGoal = context.goals.minByOrNull { it.probabilityPercentage }
            return AssistantResponse(
                answer = "Simulate having another child next. ${lowestGoal?.title ?: "That goal"} " +
                    "has the lowest modeled readiness and benefits most from testing timing and preparation.",
                relatedScenarioId = "have-a-child",
                suggestedActions = listOf("Open the new baby plan", "Compare having a child now versus waiting"),
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
            val insight = context.insights.firstOrNull {
                it.category.name.contains("DEBT") || it.category.name.contains("RISK")
            }
            return AssistantResponse(
                answer = "Your largest modeled risk is ${factor?.title?.lowercase() ?: "planning uncertainty"}. " +
                    "${factor?.explanation ?: baseline.riskScore.summary} " +
                    "${insight?.recommendedAction ?: "Paying down high-interest debt is the clearest near-term improvement."}",
                relatedScenarioId = "pay-off-cards",
                suggestedActions = listOf(
                    "Model the debt payoff scenario",
                    "Keep at least six months of essential expenses liquid",
                ),
            )
        }

        if ("one action" in question) {
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
