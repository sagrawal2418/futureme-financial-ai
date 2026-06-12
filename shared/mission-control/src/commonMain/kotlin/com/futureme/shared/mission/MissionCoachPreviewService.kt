package com.futureme.shared.mission

import com.futureme.shared.models.FinancialGpsResult
import com.futureme.shared.models.Mission
import com.futureme.shared.models.MissionCoachBriefing
import com.futureme.shared.models.MissionCoachQuestion
import com.futureme.shared.models.MissionExecutionCenter
import com.futureme.shared.models.MissionExplanationHistoryEntry
import com.futureme.shared.models.MissionType
import kotlin.math.abs

/**
 * Offline explanation fallback for native and static web demos.
 *
 * Production clients receive the same contract from the backend MissionCoachService,
 * which may use AnthropicLlmProvider. This fallback never performs new calculations.
 */
class MissionCoachPreviewService {
    fun briefings(
        missions: List<Mission>,
        execution: MissionExecutionCenter,
        financialGps: FinancialGpsResult,
    ): List<MissionCoachBriefing> = missions.map { mission ->
        val plan = execution.plans.first { it.missionId == mission.missionId }
        val nextAction = plan.actionPlan.nextAction ?: plan.actionPlan.actions.first()
        val weakest = mission.readinessFactors.minByOrNull { it.score }
        val readinessChange = plan.history.points.last().readinessScore -
            plan.history.points.first().readinessScore
        val previousScore = plan.history.points.first().readinessScore
        val strength = mission.strengths.firstOrNull()?.trim()?.trimEnd('.')
            ?: "Current cash flow supports the plan"
        val currentSummary = buildString {
            append("${mission.title} is ${mission.readinessScore}% ready. ")
            append("${weakest?.title ?: "The weakest readiness factor"} is the main constraint. ")
            append("$strength. Focus on ${nextAction.title.lowercase()} next.")
        }
        val previousSummary = "${mission.title} was $previousScore% ready. " +
            "The plan was still building enough momentum to clear its primary blocker."
        val risk = plan.actionPlan.blockedActions.firstOrNull()?.blockerMessage
            ?: mission.blockers.first()
        val opportunity = "${nextAction.title} may add ${nextAction.readinessGain} readiness points. " +
            "Financial GPS upside: ${dollars(financialGps.difference)} over five years."
        val noActionGrowth = plan.roadmap.stages.maxOf { it.expectedReadinessGrowth }

        MissionCoachBriefing(
            missionId = mission.missionId,
            coachingSummary = currentSummary,
            recommendedFocusArea = nextAction.title,
            topRisk = risk,
            topOpportunity = opportunity,
            suggestedActions = plan.actionPlan.unlockedActions.take(3).map { it.title },
            whyNotReady = "Your ${mission.readinessScore}% readiness is primarily limited by " +
                "${weakest?.title?.lowercase() ?: "the weakest modeled factor"} at " +
                "${weakest?.score ?: mission.readinessScore}%. ${mission.blockers.first()}",
            whatImprovedRecently = "Readiness improved $readinessChange points since March. " +
                (plan.actionPlan.actions.firstOrNull {
                    it.completionStatus.name == "COMPLETED"
                }?.let { "${it.title} is the clearest completed contributor." }
                    ?: "Improved financial metrics are the main modeled contributor."),
            whatIsHurtingProgress = "$risk " +
                "The mission health engine currently rates the plan ${plan.health.status.name.lowercase()}.",
            whatShouldIFocusOn = "${nextAction.title}. The action engine estimates a " +
                "${nextAction.readinessGain}-point readiness gain and marks it as " +
                "${nextAction.impact.name.lowercase()} impact.",
            howCanIAccelerateTimeline = "Complete ${nextAction.title.lowercase()} before " +
                "${nextAction.targetDate}. The 90-day roadmap contains " +
                "${plan.roadmap.stages[1].upcomingActions.size} upcoming actions.",
            whatHappensIfIDoNothing = "Without completing the current action plan, the modeled " +
                "$noActionGrowth-point near-term readiness opportunity remains unrealized and " +
                "the target stays ${mission.targetDate}.",
            suggestedQuestions = questions(mission.missionType),
            latestExplanation = MissionExplanationHistoryEntry(
                explanationId = "${mission.missionId}-explanation-current",
                generatedAt = "2026-06-12T09:00:00-04:00",
                readinessScore = mission.readinessScore,
                coachingSummary = currentSummary,
                recommendedFocusArea = nextAction.title,
                topRisk = risk,
            ),
            previousExplanation = MissionExplanationHistoryEntry(
                explanationId = "${mission.missionId}-explanation-previous",
                generatedAt = "2026-05-12T09:00:00-04:00",
                readinessScore = previousScore,
                coachingSummary = previousSummary,
                recommendedFocusArea = mission.nextAction.title,
                topRisk = mission.blockers.first(),
            ),
            whatChanged = listOf(
                "Readiness increased $readinessChange points.",
                "Mission progress is now ${plan.progress.progressPercentage}%.",
                "The current focus is ${nextAction.title.lowercase()}.",
            ),
            providerLabel = "Claude explanation layer",
            modelLabel = "Mock fallback · Sonnet strategy",
            isFallback = true,
        )
    }

    private fun questions(type: MissionType): List<MissionCoachQuestion> = when (type) {
        MissionType.BUY_HOME -> listOf(
            question("realistic-home", "Can I buy this home?", "Can I realistically buy this home?"),
            question("home-risk", "What is my biggest risk?", "What is my biggest risk?"),
            question("home-sooner", "Become ready sooner", "How do I become ready sooner?"),
        )
        MissionType.HAVE_CHILD -> listOf(
            question("child-ready", "Are we ready?", "Are we financially ready for another child?"),
            question("child-first", "Prepare first", "What should we prepare for first?"),
            question("child-wait", "What if we wait?", "What happens if we wait?"),
        )
        MissionType.RELOCATE -> listOf(
            question("move-afford", "Can we afford it?", "Can we afford the move?"),
            question("move-change", "What changes?", "What changes after relocation?"),
            question("move-risk", "Biggest move risk", "What is my biggest relocation risk?"),
        )
        MissionType.RETIRE_EARLY -> listOf(
            question("retire-track", "Am I on track?", "Am I still on track to retire early?"),
            question("retire-score", "Improve score most", "What improves my score the most?"),
            question("retire-faster", "Accelerate timeline", "How can I accelerate my timeline?"),
        )
        MissionType.BECOME_DEBT_FREE -> listOf(
            question("debt-first", "What first?", "What debt action should I do first?"),
            question("debt-impact", "Largest impact", "What improves my score the most?"),
            question("debt-delay", "Cost of waiting", "What happens if I do nothing?"),
        )
        MissionType.BUILD_EMERGENCY_FUND -> listOf(
            question("fund-gap", "Why is readiness low?", "Why is my readiness low?"),
            question("fund-first", "What first?", "What should I do first?"),
            question("fund-faster", "Build it faster", "How can I become ready faster?"),
        )
        MissionType.SUPPORT_PARENTS -> listOf(
            question("parents-ready", "Can we support them?", "Are we ready to support my parents?"),
            question("parents-risk", "Biggest risk", "What is my biggest blocker?"),
            question("parents-first", "Prepare first", "What should we prepare for first?"),
        )
        MissionType.START_BUSINESS -> listOf(
            question("business-ready", "Can I start now?", "Can I realistically start this business?"),
            question("business-risk", "Biggest risk", "What is my biggest risk?"),
            question("business-wait", "What if I wait?", "What happens if I wait?"),
        )
    }

    private fun question(id: String, title: String, prompt: String) =
        MissionCoachQuestion(id, title, prompt)

    private fun dollars(value: Double): String {
        val grouped = abs(value.toLong())
            .toString()
            .reversed()
            .chunked(3)
            .joinToString(",")
            .reversed()
        return (if (value < 0) "-$" else "$") + grouped
    }
}
