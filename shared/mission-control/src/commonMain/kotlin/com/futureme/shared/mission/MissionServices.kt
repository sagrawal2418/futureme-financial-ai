package com.futureme.shared.mission

import com.futureme.shared.models.DashboardSnapshot
import com.futureme.shared.models.FinancialGpsResult
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.GoalProbabilityResult
import com.futureme.shared.models.LifeDecisionSimulation
import com.futureme.shared.models.LifeEventPlan
import com.futureme.shared.models.LifeReadinessResult
import com.futureme.shared.models.Mission
import com.futureme.shared.models.MissionAnalyticsSnapshot
import com.futureme.shared.models.MissionAnalyticsTrend
import com.futureme.shared.models.MissionControlSnapshot
import com.futureme.shared.models.MissionExecutionCenter
import com.futureme.shared.models.MissionNextAction
import com.futureme.shared.models.MissionSignal
import com.futureme.shared.models.MissionStatus
import com.futureme.shared.models.MissionTimelineHorizon
import com.futureme.shared.models.MissionTimelinePoint
import com.futureme.shared.models.MissionType
import com.futureme.shared.models.MoneyLeak
import com.futureme.shared.models.OpportunityRecommendation
import com.futureme.shared.models.RiskLevel
import kotlin.math.roundToInt

data class MissionInputs(
    val profile: FinancialProfile,
    val dashboard: DashboardSnapshot,
    val financialGps: FinancialGpsResult,
    val goals: List<GoalProbabilityResult>,
    val lifeEvents: List<LifeEventPlan>,
    val moneyLeaks: List<MoneyLeak>,
    val readiness: List<LifeReadinessResult>,
    val opportunities: List<OpportunityRecommendation>,
    val simulations: List<LifeDecisionSimulation>,
)

class MissionProgressTracker {
    fun progress(assessment: MissionReadinessAssessment): Int =
        (
            assessment.score * 0.7 + assessment.goalProbabilityPercentage * 0.3
            ).roundToInt().coerceIn(0, 100)

    fun status(
        readinessScore: Int,
        progressPercentage: Int,
        riskLevel: RiskLevel,
    ): MissionStatus = when {
        readinessScore >= 95 && progressPercentage >= 95 -> MissionStatus.COMPLETED
        riskLevel == RiskLevel.HIGH || readinessScore < 50 -> MissionStatus.AT_RISK
        readinessScore >= 80 -> MissionStatus.ON_TRACK
        progressPercentage > 0 -> MissionStatus.IN_PROGRESS
        else -> MissionStatus.NOT_STARTED
    }

    fun risk(readinessScore: Int): RiskLevel = when (readinessScore) {
        in 0..39 -> RiskLevel.HIGH
        in 40..59 -> RiskLevel.ELEVATED
        in 60..79 -> RiskLevel.MODERATE
        else -> RiskLevel.LOW
    }
}

class MissionRecommendationService {
    fun nextAction(
        missionType: MissionType,
        opportunities: List<OpportunityRecommendation>,
    ): MissionNextAction {
        val relevant = relevantOpportunity(missionType, opportunities)
        val action = actionTemplate(missionType)
        return MissionNextAction(
            id = "mission-action-${missionType.name.lowercase().replace('_', '-')}",
            title = action.title,
            description = action.description,
            estimatedReadinessIncrease = action.readinessIncrease,
            estimatedTimelineReductionMonths = action.timelineReduction,
            annualBenefitEstimate = relevant?.annualBenefitEstimate ?: action.annualBenefit,
            fiveYearBenefitEstimate = relevant?.fiveYearBenefitEstimate ?: action.fiveYearBenefit,
            impactScore = relevant?.impactScore ?: action.impactScore,
            confidenceScore = relevant?.confidenceScore ?: action.confidenceScore,
            relatedScenarioId = action.scenarioId ?: relevant?.relatedScenarioId,
        )
    }

    fun recommendations(
        nextAction: MissionNextAction,
        readiness: LifeReadinessResult?,
        goal: GoalProbabilityResult?,
        event: LifeEventPlan?,
    ): List<String> = (
        listOf(nextAction.title) +
            readiness?.recommendedActions.orEmpty() +
            goal?.recommendedActions.orEmpty() +
            event?.recommendedPreparationSteps.orEmpty()
        ).distinct().take(5)

    private fun relevantOpportunity(
        missionType: MissionType,
        opportunities: List<OpportunityRecommendation>,
    ): OpportunityRecommendation? {
        val terms = when (missionType) {
            MissionType.BUY_HOME -> listOf("debt", "mortgage", "home")
            MissionType.HAVE_CHILD -> listOf("emergency", "spending", "debt")
            MissionType.RELOCATE -> listOf("spending", "emergency", "debt")
            MissionType.RETIRE_EARLY -> listOf("401", "match", "invest")
            MissionType.BECOME_DEBT_FREE -> listOf("debt", "card")
            MissionType.BUILD_EMERGENCY_FUND -> listOf("emergency", "cash", "spending")
            MissionType.SUPPORT_PARENTS -> listOf("emergency", "cash", "spending")
            MissionType.START_BUSINESS -> listOf("emergency", "cash", "debt")
        }
        return opportunities.firstOrNull { opportunity ->
            terms.any { it in opportunity.title.lowercase() || it in opportunity.description.lowercase() }
        } ?: opportunities.firstOrNull()
    }

    private fun actionTemplate(type: MissionType): ActionTemplate = when (type) {
        MissionType.BUY_HOME -> ActionTemplate(
            "Pay down $5,000 of revolving debt",
            "Lowering utilization increases home readiness and preserves borrowing capacity.",
            8,
            4,
            3_800.0,
            22_000.0,
            94,
            92,
            "pay-off-cards",
        )
        MissionType.HAVE_CHILD -> ActionTemplate(
            "Build the $15,000 family transition fund",
            "A dedicated reserve protects leave, healthcare, and childcare flexibility.",
            7,
            3,
            2_400.0,
            18_000.0,
            88,
            86,
            "have-a-child",
        )
        MissionType.RELOCATE -> ActionTemplate(
            "Reserve $15,000 for the move",
            "Funding transition costs keeps the lower-cost relocation from consuming emergency cash.",
            6,
            4,
            3_600.0,
            24_000.0,
            84,
            83,
            "move-to-texas",
        )
        MissionType.RETIRE_EARLY -> ActionTemplate(
            "Capture the full employer match",
            "The unmatched contribution is the highest-confidence way to accelerate retirement.",
            9,
            10,
            6_600.0,
            95_000.0,
            96,
            95,
            "invest-more",
        )
        MissionType.BECOME_DEBT_FREE -> ActionTemplate(
            "Add $300 to the monthly card payment",
            "The extra payment reduces interest and releases cash flow for every other mission.",
            10,
            8,
            3_800.0,
            22_000.0,
            97,
            94,
            "pay-off-cards",
        )
        MissionType.BUILD_EMERGENCY_FUND -> ActionTemplate(
            "Automate $750 monthly to emergency savings",
            "A dedicated transfer closes the twelve-month reserve gap predictably.",
            8,
            6,
            900.0,
            7_500.0,
            82,
            91,
            "job-loss",
        )
        MissionType.SUPPORT_PARENTS -> ActionTemplate(
            "Create a dedicated parent-care reserve",
            "Separating care funding protects retirement and avoids revolving debt.",
            7,
            5,
            1_800.0,
            12_000.0,
            81,
            78,
            null,
        )
        MissionType.START_BUSINESS -> ActionTemplate(
            "Build an 18-month household runway",
            "Separating household reserves from startup capital reduces launch risk.",
            10,
            12,
            4_800.0,
            40_000.0,
            91,
            72,
            "start-business",
        )
    }

    private data class ActionTemplate(
        val title: String,
        val description: String,
        val readinessIncrease: Int,
        val timelineReduction: Int,
        val annualBenefit: Double,
        val fiveYearBenefit: Double,
        val impactScore: Int,
        val confidenceScore: Int,
        val scenarioId: String?,
    )
}

class MissionTimelineService {
    fun build(
        missionType: MissionType,
        targetDate: String,
        readinessScore: Int,
        progressPercentage: Int,
        action: MissionNextAction,
    ): List<MissionTimelinePoint> {
        val points = listOf(
            Triple(MissionTimelineHorizon.TODAY, "Today", 0),
            Triple(MissionTimelineHorizon.THIRTY_DAYS, "30 days", 1),
            Triple(MissionTimelineHorizon.NINETY_DAYS, "90 days", 3),
            Triple(MissionTimelineHorizon.ONE_YEAR, "1 year", 12),
            Triple(MissionTimelineHorizon.THREE_YEARS, "3 years", 36),
        )
        return points.map { (horizon, label, months) ->
            val gain = when (months) {
                0 -> 0
                1 -> (action.estimatedReadinessIncrease * 0.25).roundToInt()
                3 -> (action.estimatedReadinessIncrease * 0.60).roundToInt()
                12 -> action.estimatedReadinessIncrease + 5
                else -> action.estimatedReadinessIncrease + 12
            }
            MissionTimelinePoint(
                horizon = horizon,
                label = label,
                monthsFromNow = months,
                readinessScore = (readinessScore + gain).coerceAtMost(100),
                progressPercentage = (progressPercentage + gain).coerceAtMost(100),
                completedActions = when (months) {
                    0 -> 0
                    1 -> 1
                    3 -> 2
                    12 -> 3
                    else -> 4
                },
                milestone = milestone(missionType, months),
                projectedCompletionDate = targetDate,
            )
        }
    }

    private fun milestone(type: MissionType, months: Int): String = when (months) {
        0 -> "Confirm the mission target and first action."
        1 -> "Complete the highest-impact action."
        3 -> when (type) {
            MissionType.BUY_HOME -> "Reduce debt utilization and protect closing reserves."
            MissionType.HAVE_CHILD -> "Fund leave and childcare transition costs."
            MissionType.RELOCATE -> "Confirm compensation, housing, and moving costs."
            MissionType.RETIRE_EARLY -> "Lock in the higher automated investment rate."
            MissionType.BECOME_DEBT_FREE -> "Eliminate the highest-rate balance."
            MissionType.BUILD_EMERGENCY_FUND -> "Reach the next full month of runway."
            MissionType.SUPPORT_PARENTS -> "Fund the first quarter of parent support."
            MissionType.START_BUSINESS -> "Separate startup capital from household reserves."
        }
        12 -> "Recalculate readiness and advance the target date where possible."
        else -> "Maintain the mission plan and complete the long-term target."
    }
}

class MissionEngine(
    private val readinessCalculator: MissionReadinessCalculator = MissionReadinessCalculator(),
    private val progressTracker: MissionProgressTracker = MissionProgressTracker(),
    private val recommendationService: MissionRecommendationService = MissionRecommendationService(),
    private val timelineService: MissionTimelineService = MissionTimelineService(),
) {
    fun build(definition: MissionDefinition, inputs: MissionInputs): Mission {
        val readiness = definition.readinessCategory?.let { category ->
            inputs.readiness.firstOrNull { it.category == category }
        }
        val goal = definition.goalType?.let { type ->
            inputs.goals.firstOrNull { it.type == type }
        }
        val event = definition.lifeEventType?.let { type ->
            inputs.lifeEvents.firstOrNull { it.type == type }
        }
        val assessment = readinessCalculator.calculate(
            definition,
            inputs.profile,
            inputs.dashboard,
            readiness,
            goal,
        )
        val progress = progressTracker.progress(assessment)
        val risk = progressTracker.risk(assessment.score)
        val nextAction = recommendationService.nextAction(
            definition.missionType,
            inputs.opportunities,
        )
        val recommendations = recommendationService.recommendations(
            nextAction,
            readiness,
            goal,
            event,
        )
        val benefit = nextAction.fiveYearBenefitEstimate +
            relatedSimulationBenefit(definition, inputs.simulations)

        return Mission(
            missionId = definition.missionId,
            missionType = definition.missionType,
            title = definition.title,
            description = definition.description,
            targetDate = definition.targetDate,
            readinessScore = assessment.score,
            progressPercentage = progress,
            riskLevel = risk,
            estimatedCost = definition.estimatedCost,
            projectedBenefit = benefit,
            blockers = assessment.blockers,
            recommendations = recommendations,
            nextAction = nextAction,
            timeline = timelineService.build(
                definition.missionType,
                definition.targetDate,
                assessment.score,
                progress,
                nextAction,
            ),
            createdDate = definition.createdDate,
            updatedDate = "2026-06-11",
            status = progressTracker.status(assessment.score, progress, risk),
            readinessFactors = assessment.factors,
            strengths = assessment.strengths,
            weaknesses = assessment.weaknesses,
            confidenceLevel = assessment.confidenceLevel,
            goalProbabilityPercentage = assessment.goalProbabilityPercentage,
        )
    }

    private fun relatedSimulationBenefit(
        definition: MissionDefinition,
        simulations: List<LifeDecisionSimulation>,
    ): Double = definition.scenarioId
        ?.let { id -> simulations.firstOrNull { it.scenarioId == id } }
        ?.fiveYearNetWorthImpact
        ?.coerceAtLeast(0.0)
        ?: 0.0
}

class MissionService(
    private val repository: MissionRepository = DefaultMissionRepository(),
    private val engine: MissionEngine = MissionEngine(),
) {
    fun missions(inputs: MissionInputs): List<Mission> =
        repository.definitions().map { engine.build(it, inputs) }

    fun missionControl(missions: List<Mission>): MissionControlSnapshot {
        require(missions.isNotEmpty()) { "Mission Control requires at least one mission." }
        val active = missions.filter { it.status != MissionStatus.COMPLETED }
            .ifEmpty { missions }
        val highest = active.maxByOrNull { it.readinessScore } ?: missions.first()
        val lowest = active.minByOrNull { it.readinessScore } ?: missions.first()
        val priority = active.maxByOrNull {
            it.nextAction.impactScore + (100 - it.readinessScore) / 2
        } ?: highest
        val elevatedRisks = active
            .filter { it.riskLevel == RiskLevel.HIGH || it.riskLevel == RiskLevel.ELEVATED }
            .sortedBy { it.readinessScore }
        val riskMissions = elevatedRisks.ifEmpty {
            active.sortedBy { it.readinessScore }.take(3)
        }
        return MissionControlSnapshot(
            activeMissions = active,
            highestReadinessMission = highest,
            lowestReadinessMission = lowest,
            missionProgressPercentage = active.map { it.progressPercentage }.average().roundToInt(),
            missionTimeline = priority.timeline,
            nextBestAction = priority.nextAction,
            risks = riskMissions
                .take(3)
                .map {
                    MissionSignal(
                        "risk-${it.missionId}",
                        it.missionId,
                        it.title,
                        it.blockers.first(),
                        "${it.readinessScore}% ready",
                    )
                },
            opportunities = active
                .sortedByDescending { it.nextAction.impactScore }
                .take(3)
                .map {
                    MissionSignal(
                        "opportunity-${it.missionId}",
                        it.missionId,
                        it.nextAction.title,
                        it.nextAction.description,
                        "+${it.nextAction.estimatedReadinessIncrease} readiness",
                    )
                },
        )
    }

    fun analytics(
        missions: List<Mission>,
        execution: MissionExecutionCenter? = null,
    ): MissionAnalyticsSnapshot {
        val trends = missions.mapIndexed { index, mission ->
            val plan = execution?.plans?.firstOrNull { it.missionId == mission.missionId }
            val history = plan?.history?.points.orEmpty()
            val change = if (history.size >= 2) {
                history.last().readinessScore - history.first().readinessScore
            } else {
                3 + index % 5
            }
            MissionAnalyticsTrend(
                missionId = mission.missionId,
                title = mission.title,
                startingReadinessScore = history.firstOrNull()?.readinessScore
                    ?: (mission.readinessScore - change).coerceAtLeast(0),
                currentReadinessScore = mission.readinessScore,
                readinessChange = change,
                timelineReductionMonths = mission.nextAction.estimatedTimelineReductionMonths,
                actionsCompleted = plan?.progress?.completedActions
                    ?: if (mission.progressPercentage >= 70) 2 else 1,
            )
        }
        return MissionAnalyticsSnapshot(
            missionsCreated = missions.size,
            missionsCompleted = missions.count { it.status == MissionStatus.COMPLETED },
            readinessImprovements = trends.count { it.readinessChange > 0 },
            timelineImprovements = trends.sumOf { it.timelineReductionMonths },
            actionsCompleted = trends.sumOf { it.actionsCompleted },
            goalsAchieved = missions.count { it.goalProbabilityPercentage >= 90 },
            trends = trends,
        )
    }
}
