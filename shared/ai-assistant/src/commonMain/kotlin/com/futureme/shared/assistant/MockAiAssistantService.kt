package com.futureme.shared.assistant

import com.futureme.shared.calculators.FinancialMath
import com.futureme.shared.domain.FinancialAssistantProvider
import com.futureme.shared.models.AssistantPrompt
import com.futureme.shared.models.AssistantResponse
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.FinancialCopilotContext
import com.futureme.shared.models.Mission
import com.futureme.shared.models.MissionExecutionPlan
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
                answer = "Ask me which mission to prioritize, what is blocking it, or how to become ready faster.",
            )
        }

        val matchedMission = missionFor(question, context)
        val missionBriefing = matchedMission?.let { mission ->
            context.missionCoachBriefings.firstOrNull { it.missionId == mission.missionId }
        }

        if (missionBriefing != null && "what happens if i do nothing" in question) {
            return AssistantResponse(
                answer = missionBriefing.whatHappensIfIDoNothing,
                relatedScenarioId = matchedMission?.nextAction?.relatedScenarioId,
                suggestedActions = missionBriefing.suggestedActions,
            )
        }

        if (
            missionBriefing != null &&
            (
                "improves my score the most" in question ||
                    "what should we prepare for first" in question ||
                    "what should i focus on" in question
                )
        ) {
            return AssistantResponse(
                answer = missionBriefing.whatShouldIFocusOn,
                relatedScenarioId = matchedMission?.nextAction?.relatedScenarioId,
                suggestedActions = missionBriefing.suggestedActions,
            )
        }

        if (
            missionBriefing != null &&
            (
                "realistically" in question ||
                    "financially ready" in question ||
                    "are we ready" in question
                )
        ) {
            return AssistantResponse(
                answer = "${missionBriefing.coachingSummary} ${missionBriefing.whyNotReady}",
                relatedScenarioId = matchedMission?.nextAction?.relatedScenarioId,
                suggestedActions = missionBriefing.suggestedActions,
            )
        }

        if (
            missionBriefing != null &&
            (
                "accelerate my timeline" in question ||
                    "become ready sooner" in question ||
                    "how do i become ready sooner" in question
                )
        ) {
            return AssistantResponse(
                answer = missionBriefing.howCanIAccelerateTimeline,
                relatedScenarioId = matchedMission?.nextAction?.relatedScenarioId,
                suggestedActions = missionBriefing.suggestedActions,
            )
        }

        if ("why" in question && "readiness" in question && "change" in question) {
            val mission = matchedMission ?: context.missionControl.lowestReadinessMission
            val plan = executionPlan(mission, context)
            val first = plan.history.points.first()
            val latest = plan.history.points.last()
            val change = latest.readinessScore - first.readinessScore
            val action = plan.actionPlan.actions.firstOrNull {
                it.completionStatus.name == "COMPLETED"
            }
            return AssistantResponse(
                answer = "${mission.title} readiness changed by ${signed(change)} points, from " +
                    "${first.readinessScore}% to ${latest.readinessScore}%. " +
                    (action?.let { "${it.title} is the clearest completed contributor." }
                        ?: "Improved metrics are the main contributor; no action milestone is complete yet."),
                relatedScenarioId = mission.nextAction.relatedScenarioId,
                suggestedActions = plan.actionPlan.unlockedActions.take(3).map { it.title },
            )
        }

        if ("still on track" in question || ("on track" in question && "mission" in question)) {
            val mission = matchedMission ?: context.missionControl.lowestReadinessMission
            val plan = executionPlan(mission, context)
            return AssistantResponse(
                answer = "${mission.title} health is ${plan.health.status.name.lowercase()} " +
                    "at ${plan.health.score}/100. ${plan.health.summary} " +
                    "${plan.progress.summary}",
                relatedScenarioId = mission.nextAction.relatedScenarioId,
                suggestedActions = plan.actionPlan.unlockedActions.take(3).map { it.title },
            )
        }

        if ("which mission" in question && ("prioritize" in question || "first" in question)) {
            val mission = context.missionControl.lowestReadinessMission
            return AssistantResponse(
                answer = "Prioritize ${mission.title}. It is ${mission.readinessScore}% ready, " +
                    "and its biggest blocker is ${mission.blockers.first()} " +
                    "The next move is ${mission.nextAction.title.lowercase()}.",
                relatedScenarioId = mission.nextAction.relatedScenarioId,
                suggestedActions = mission.recommendations.take(3),
            )
        }

        if ("biggest blocker" in question) {
            val mission = matchedMission ?: context.missionControl.lowestReadinessMission
            val plan = executionPlan(mission, context)
            val blocked = plan.actionPlan.blockedActions.firstOrNull()
            return AssistantResponse(
                answer = "${mission.title} is ${mission.readinessScore}% ready. Its biggest blocker is " +
                    (blocked?.blockerMessage ?: mission.blockers.first()),
                relatedScenarioId = mission.nextAction.relatedScenarioId,
                suggestedActions = plan.actionPlan.unlockedActions.take(3).map { it.title },
            )
        }

        if ("why" in question && "readiness" in question && "low" in question) {
            val mission = matchedMission ?: context.missionControl.lowestReadinessMission
            val weakest = mission.readinessFactors.minByOrNull { it.score }
            return AssistantResponse(
                answer = "${mission.title} readiness is ${mission.readinessScore}%. " +
                    "${weakest?.title ?: "The weakest factor"} is the main drag at " +
                    "${weakest?.score ?: mission.readinessScore}%. ${mission.blockers.first()}",
                relatedScenarioId = mission.nextAction.relatedScenarioId,
                suggestedActions = mission.recommendations.take(3),
            )
        }

        if (
            "ready faster" in question ||
            "speed up" in question ||
            ("become ready" in question && "faster" in question)
        ) {
            val mission = matchedMission ?: context.missionControl.lowestReadinessMission
            val plan = executionPlan(mission, context)
            val action = plan.actionPlan.nextAction
            return AssistantResponse(
                answer = "For ${mission.title}, ${action?.title?.lowercase() ?: "clear the current blocker"} " +
                    "is the fastest modeled move. It may add ${action?.readinessGain ?: 0} readiness points. " +
                    "Use the ${plan.roadmap.stages[1].upcomingActions.size} upcoming 90-day actions to " +
                    "shorten the timeline.",
                relatedScenarioId = mission.nextAction.relatedScenarioId,
                suggestedActions = plan.actionPlan.unlockedActions.take(3).map { it.title },
            )
        }

        if ("what should i do next" in question) {
            val mission = matchedMission ?: context.missionControl.lowestReadinessMission
            val plan = executionPlan(mission, context)
            val action = plan.actionPlan.nextAction
            return AssistantResponse(
                answer = "For ${mission.title}, do this next: ${action?.title ?: "review the completed plan"}. " +
                    "It may improve readiness by ${action?.readinessGain ?: 0} points. " +
                    "${plan.actionPlan.blockedActions.size} actions remain blocked.",
                relatedScenarioId = mission.nextAction.relatedScenarioId,
                suggestedActions = plan.actionPlan.unlockedActions.take(3).map { it.title },
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
            val best = context.opportunities.firstOrNull()
            return AssistantResponse(
                answer = "${best?.title ?: "Pay off high-interest debt"} ranks first after " +
                    "weighing impact, effort, and confidence. The modeled five-year benefit is " +
                    "${best?.fiveYearBenefitEstimate?.asDollars() ?: "$0"}.",
                relatedScenarioId = best?.relatedScenarioId,
                suggestedActions = context.opportunities.take(3).map { it.title },
            )
        }

        if (
            ("focus" in question && ("month" in question || "now" in question)) ||
            ("one thing" in question && "month" in question)
        ) {
            val action = context.missionControl.nextBestAction
            val mission = context.missions.first { it.nextAction.id == action.id }
            return AssistantResponse(
                answer = "Focus on ${mission.title}: ${action.title}. ${action.description} " +
                    "This is the strongest current move for your five-year outlook.",
                relatedScenarioId = action.relatedScenarioId,
                suggestedActions = mission.recommendations.take(3),
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
            val action = context.missionControl.nextBestAction
            val mission = context.missions.first { it.nextAction.id == action.id }
            return AssistantResponse(
                answer = "${action.title} is the highest-impact action for ${mission.title}. " +
                    "${action.description} It is the strongest current move for your five-year outlook.",
                relatedScenarioId = action.relatedScenarioId,
                suggestedActions = mission.recommendations.take(3),
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

    private fun missionFor(
        question: String,
        context: FinancialCopilotContext,
    ): Mission? = context.missions.firstOrNull { mission ->
        when (mission.missionType.name) {
            "BUY_HOME" -> "home" in question || "house" in question
            "HAVE_CHILD" -> "child" in question || "baby" in question
            "RELOCATE" -> "relocate" in question || "move" in question || "texas" in question
            "RETIRE_EARLY" -> "retire" in question
            "BECOME_DEBT_FREE" -> "debt" in question || "card" in question
            "BUILD_EMERGENCY_FUND" -> "emergency" in question || "runway" in question
            "SUPPORT_PARENTS" -> "parent" in question
            "START_BUSINESS" -> "business" in question || "startup" in question
            else -> false
        }
    }

    private fun executionPlan(
        mission: Mission,
        context: FinancialCopilotContext,
    ): MissionExecutionPlan = context.missionExecution.plans.first {
        it.missionId == mission.missionId
    }
}

private fun Double.asDollars(): String = "$" + abs(roundToInt()).toString()

private fun Double.asSignedDollars(): String =
    (if (this >= 0.0) "+$" else "-$") + abs(roundToInt())

private fun oneDecimal(value: Double): String =
    ((value * 10.0).roundToInt() / 10.0).toString()

private fun signed(value: Int): String = if (value >= 0) "+$value" else value.toString()
