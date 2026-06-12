package com.futureme.shared.mission

import com.futureme.shared.models.DashboardSnapshot
import com.futureme.shared.models.FinancialProfile
import com.futureme.shared.models.LifeDecisionSimulation
import com.futureme.shared.models.Mission
import com.futureme.shared.models.MissionAction
import com.futureme.shared.models.MissionActionCategory
import com.futureme.shared.models.MissionActionEffort
import com.futureme.shared.models.MissionActionImpact
import com.futureme.shared.models.MissionActionPlan
import com.futureme.shared.models.MissionActionStatus
import com.futureme.shared.models.MissionExecutionCenter
import com.futureme.shared.models.MissionExecutionPlan
import com.futureme.shared.models.MissionHealthFactor
import com.futureme.shared.models.MissionHealthResult
import com.futureme.shared.models.MissionHealthStatus
import com.futureme.shared.models.MissionHistory
import com.futureme.shared.models.MissionHistoryEvent
import com.futureme.shared.models.MissionHistoryEventType
import com.futureme.shared.models.MissionHistoryPoint
import com.futureme.shared.models.MissionNotification
import com.futureme.shared.models.MissionNotificationType
import com.futureme.shared.models.MissionProgressSnapshot
import com.futureme.shared.models.MissionRoadmap
import com.futureme.shared.models.MissionRoadmapHorizon
import com.futureme.shared.models.MissionRoadmapStage
import com.futureme.shared.models.MissionScenarioImpact
import com.futureme.shared.models.MissionStatus
import com.futureme.shared.models.MissionType
import com.futureme.shared.models.RiskLevel
import kotlin.math.roundToInt

private const val TODAY = "2026-06-12"
private const val THIRTY_DAY_DATE = "2026-07-12"
private const val NINETY_DAY_DATE = "2026-09-10"
private const val ONE_YEAR_DATE = "2027-06-12"

private enum class MetricDirection {
    AT_LEAST,
    AT_MOST,
}

private data class ActionTemplate(
    val suffix: String,
    val title: String,
    val description: String,
    val category: MissionActionCategory,
    val effort: MissionActionEffort,
    val impact: MissionActionImpact,
    val readinessGain: Int,
    val targetDate: String,
    val metricLabel: String,
    val currentValue: Double,
    val targetValue: Double,
    val direction: MetricDirection = MetricDirection.AT_LEAST,
    val dependencySuffixes: List<String> = emptyList(),
)

class MissionActionEngine {
    fun generate(
        mission: Mission,
        profile: FinancialProfile,
        dashboard: DashboardSnapshot,
    ): List<MissionAction> = templates(mission.missionType, profile, dashboard).map { template ->
        val progress = metricProgress(template)
        val initialStatus = when {
            progress >= 100 -> MissionActionStatus.COMPLETED
            template.targetDate < TODAY -> MissionActionStatus.MISSED
            progress >= 50 -> MissionActionStatus.IN_PROGRESS
            else -> MissionActionStatus.AVAILABLE
        }
        MissionAction(
            actionId = actionId(mission.missionId, template.suffix),
            missionId = mission.missionId,
            title = template.title,
            description = template.description,
            category = template.category,
            effort = template.effort,
            impact = template.impact,
            readinessGain = template.readinessGain,
            targetDate = template.targetDate,
            completionStatus = initialStatus,
            dependencyActionIds = template.dependencySuffixes.map {
                actionId(mission.missionId, it)
            },
            metricLabel = template.metricLabel,
            currentMetricValue = template.currentValue,
            targetMetricValue = template.targetValue,
            metricProgressPercentage = progress,
        )
    }

    private fun templates(
        type: MissionType,
        profile: FinancialProfile,
        dashboard: DashboardSnapshot,
    ): List<ActionTemplate> = when (type) {
        MissionType.BUY_HOME -> listOf(
            debtAction("pay-debt", "Pay off $3,000 of credit card debt", 15_400.0, profile, 5),
            savingsAction(
                "down-payment",
                "Save an additional $500 each month",
                "Add $6,000 to the down-payment reserve over the next year.",
                102_500.0,
                profile,
                4,
                NINETY_DAY_DATE,
            ),
            runwayAction("six-month-runway", "Increase the emergency fund to 6 months", 6.0, dashboard, 7),
            debtAction(
                "lower-utilization",
                "Reduce revolving utilization below the planning limit",
                14_250.0,
                profile,
                3,
                dependencySuffixes = listOf("pay-debt"),
            ),
        )
        MissionType.HAVE_CHILD -> listOf(
            savingsAction(
                "transition-fund",
                "Build a $15,000 family transition fund",
                "Separate leave, healthcare, and childcare reserves from emergency savings.",
                111_500.0,
                profile,
                7,
                NINETY_DAY_DATE,
            ),
            runwayAction("nine-month-runway", "Extend household runway to 9 months", 9.0, dashboard, 6),
            cashFlowAction("child-cash-flow", "Protect $3,500 of monthly surplus", 3_500.0, dashboard, 5),
            planningAction(
                "leave-plan",
                "Confirm leave and childcare coverage",
                "Document paid leave, childcare timing, and the first-year monthly budget.",
                4,
                ONE_YEAR_DATE,
                listOf("transition-fund"),
            ),
        )
        MissionType.RELOCATE -> listOf(
            savingsAction(
                "move-reserve",
                "Reserve $15,000 for the move",
                "Keep deposits, moving costs, and temporary housing outside emergency savings.",
                111_500.0,
                profile,
                6,
                NINETY_DAY_DATE,
            ),
            debtAction("relocation-debt", "Pay off $3,000 of revolving debt", 15_400.0, profile, 5),
            cashFlowAction("relocation-cash-flow", "Protect $3,000 of monthly surplus", 3_000.0, dashboard, 4),
            planningAction(
                "state-comparison",
                "Confirm the compensation and housing tradeoff",
                "Compare take-home pay, housing, benefits, and transition costs before committing.",
                5,
                ONE_YEAR_DATE,
                listOf("move-reserve"),
            ),
        )
        MissionType.RETIRE_EARLY -> listOf(
            ActionTemplate(
                "employer-match",
                "Capture the full employer match",
                "Raise automated retirement contributions to the modeled match threshold.",
                MissionActionCategory.INVESTING,
                MissionActionEffort.LOW,
                MissionActionImpact.HIGH,
                9,
                THIRTY_DAY_DATE,
                "Monthly retirement contribution",
                profile.monthlyRetirementContribution,
                2_500.0,
            ),
            ActionTemplate(
                "investment-milestone",
                "Reach the next $300,000 investment milestone",
                "Keep contributions automated until invested assets reach the next planning milestone.",
                MissionActionCategory.INVESTING,
                MissionActionEffort.MEDIUM,
                MissionActionImpact.HIGH,
                6,
                NINETY_DAY_DATE,
                "Investment balance",
                profile.investmentBalance,
                300_000.0,
            ),
            debtAction("retirement-debt", "Reduce high-interest debt by $3,000", 15_400.0, profile, 5),
            planningAction(
                "retirement-automation",
                "Automate the annual contribution increase",
                "Schedule a contribution increase after each raise or debt payoff.",
                5,
                ONE_YEAR_DATE,
                listOf("employer-match"),
            ),
        )
        MissionType.BECOME_DEBT_FREE -> listOf(
            debtAction("first-3000", "Pay off the next $3,000 of card debt", 15_400.0, profile, 8),
            ActionTemplate(
                "extra-payment",
                "Add $300 to the monthly debt payment",
                "Automate the extra payment toward the highest-rate balance.",
                MissionActionCategory.DEBT,
                MissionActionEffort.MEDIUM,
                MissionActionImpact.HIGH,
                7,
                THIRTY_DAY_DATE,
                "Monthly debt payment",
                profile.monthlyDebtPayments,
                1_150.0,
            ),
            debtAction(
                "under-10000",
                "Reduce card debt below $10,000",
                10_000.0,
                profile,
                9,
                dependencySuffixes = listOf("first-3000"),
            ),
            debtAction(
                "debt-free",
                "Eliminate the remaining revolving balance",
                0.0,
                profile,
                12,
                ONE_YEAR_DATE,
                listOf("under-10000", "extra-payment"),
            ),
        )
        MissionType.BUILD_EMERGENCY_FUND -> listOf(
            runwayAction("eight-month-runway", "Reach 8 months of emergency runway", 8.0, dashboard, 6),
            savingsAction(
                "reserve-110000",
                "Grow liquid reserves to $110,000",
                "Keep the reserve in accessible, interest-bearing cash.",
                110_000.0,
                profile,
                5,
                NINETY_DAY_DATE,
            ),
            planningAction(
                "automate-750",
                "Automate $750 per month to emergency savings",
                "Move the transfer to payday so reserve growth happens before discretionary spending.",
                5,
                THIRTY_DAY_DATE,
            ),
            runwayAction(
                "twelve-month-runway",
                "Reach a full 12 months of runway",
                12.0,
                dashboard,
                8,
                ONE_YEAR_DATE,
                listOf("eight-month-runway", "reserve-110000"),
            ),
        )
        MissionType.SUPPORT_PARENTS -> listOf(
            savingsAction(
                "parent-reserve",
                "Create a dedicated $9,000 parent-care reserve",
                "Separate care funding from retirement and emergency cash.",
                105_500.0,
                profile,
                7,
                NINETY_DAY_DATE,
            ),
            cashFlowAction("parent-cash-flow", "Protect $3,000 of monthly surplus", 3_000.0, dashboard, 5),
            debtAction("parent-debt", "Pay off $3,000 of revolving debt", 15_400.0, profile, 5),
            planningAction(
                "care-budget",
                "Confirm the shared family care budget",
                "Document expected support, siblings' contributions, and medical contingencies.",
                5,
                ONE_YEAR_DATE,
                listOf("parent-reserve"),
            ),
        )
        MissionType.START_BUSINESS -> listOf(
            runwayAction("business-runway", "Build an 18-month household runway", 18.0, dashboard, 10),
            savingsAction(
                "startup-capital",
                "Separate $40,000 of startup capital",
                "Keep launch capital outside the household emergency fund.",
                136_500.0,
                profile,
                9,
                ONE_YEAR_DATE,
            ),
            debtAction("business-debt", "Reduce card debt below $10,000", 10_000.0, profile, 8),
            planningAction(
                "launch-gate",
                "Complete the launch readiness gate",
                "Confirm runway, startup capital, debt capacity, and a first-year operating budget.",
                8,
                ONE_YEAR_DATE,
                listOf("business-runway", "startup-capital", "business-debt"),
            ),
        )
    }

    private fun debtAction(
        suffix: String,
        title: String,
        target: Double,
        profile: FinancialProfile,
        gain: Int,
        targetDate: String = NINETY_DAY_DATE,
        dependencySuffixes: List<String> = emptyList(),
    ) = ActionTemplate(
        suffix,
        title,
        "Lower high-interest balances to improve flexibility and borrowing capacity.",
        MissionActionCategory.DEBT,
        MissionActionEffort.MEDIUM,
        MissionActionImpact.HIGH,
        gain,
        targetDate,
        "Credit card debt",
        profile.creditCardDebt,
        target,
        MetricDirection.AT_MOST,
        dependencySuffixes,
    )

    private fun savingsAction(
        suffix: String,
        title: String,
        description: String,
        target: Double,
        profile: FinancialProfile,
        gain: Int,
        targetDate: String,
    ) = ActionTemplate(
        suffix,
        title,
        description,
        MissionActionCategory.SAVINGS,
        MissionActionEffort.MEDIUM,
        MissionActionImpact.HIGH,
        gain,
        targetDate,
        "Liquid savings",
        profile.liquidSavings,
        target,
    )

    private fun runwayAction(
        suffix: String,
        title: String,
        target: Double,
        dashboard: DashboardSnapshot,
        gain: Int,
        targetDate: String = NINETY_DAY_DATE,
        dependencySuffixes: List<String> = emptyList(),
    ) = ActionTemplate(
        suffix,
        title,
        "Increase accessible reserves without relying on revolving debt.",
        MissionActionCategory.EMERGENCY_FUND,
        MissionActionEffort.HIGH,
        MissionActionImpact.HIGH,
        gain,
        targetDate,
        "Emergency runway in months",
        dashboard.emergencyFundMonths,
        target,
        dependencySuffixes = dependencySuffixes,
    )

    private fun cashFlowAction(
        suffix: String,
        title: String,
        target: Double,
        dashboard: DashboardSnapshot,
        gain: Int,
    ) = ActionTemplate(
        suffix,
        title,
        "Preserve monthly capacity for the mission and unexpected costs.",
        MissionActionCategory.CASH_FLOW,
        MissionActionEffort.MEDIUM,
        MissionActionImpact.MEDIUM,
        gain,
        NINETY_DAY_DATE,
        "Monthly surplus",
        dashboard.monthlyCashFlow,
        target,
    )

    private fun planningAction(
        suffix: String,
        title: String,
        description: String,
        gain: Int,
        targetDate: String,
        dependencySuffixes: List<String> = emptyList(),
    ) = ActionTemplate(
        suffix,
        title,
        description,
        MissionActionCategory.PLANNING,
        MissionActionEffort.LOW,
        MissionActionImpact.MEDIUM,
        gain,
        targetDate,
        "Planning checkpoint",
        0.0,
        1.0,
        dependencySuffixes = dependencySuffixes,
    )

    private fun metricProgress(template: ActionTemplate): Int {
        if (template.direction == MetricDirection.AT_MOST && template.targetValue == 0.0) {
            return if (template.currentValue <= 0.0) 100 else 0
        }
        if (template.targetValue <= 0.0) return 0
        val ratio = when (template.direction) {
            MetricDirection.AT_LEAST -> template.currentValue / template.targetValue
            MetricDirection.AT_MOST -> template.targetValue / template.currentValue.coerceAtLeast(1.0)
        }
        return (ratio * 100.0).roundToInt().coerceIn(0, 100)
    }

    private fun actionId(missionId: String, suffix: String) = "$missionId-action-$suffix"
}

class MissionDependencyEngine {
    fun resolve(actions: List<MissionAction>): MissionActionPlan {
        require(actions.isNotEmpty()) { "A mission action plan requires at least one action." }
        val byId = actions.associateBy { it.actionId }
        val resolved = actions.map { action ->
            if (
                action.completionStatus == MissionActionStatus.COMPLETED ||
                action.completionStatus == MissionActionStatus.MISSED
            ) {
                action
            } else {
                val blockedBy = action.dependencyActionIds.mapNotNull(byId::get)
                    .filter { it.completionStatus != MissionActionStatus.COMPLETED }
                if (blockedBy.isEmpty()) {
                    action
                } else {
                    action.copy(
                        completionStatus = MissionActionStatus.LOCKED,
                        blockerMessage = "Complete ${blockedBy.joinToString { it.title }} first.",
                    )
                }
            }
        }
        val unlocked = resolved.filter {
            it.completionStatus == MissionActionStatus.AVAILABLE ||
                it.completionStatus == MissionActionStatus.IN_PROGRESS
        }
        return MissionActionPlan(
            missionId = actions.first().missionId,
            actions = resolved,
            nextAction = unlocked.maxByOrNull {
                it.readinessGain * 10 + it.metricProgressPercentage
            },
            blockedActions = resolved.filter { it.completionStatus == MissionActionStatus.LOCKED },
            unlockedActions = unlocked,
        )
    }
}

class MissionProgressEngine {
    fun calculate(
        mission: Mission,
        actionPlan: MissionActionPlan,
    ): MissionProgressSnapshot {
        val actionProgress = actionPlan.actions.map { action ->
            when (action.completionStatus) {
                MissionActionStatus.COMPLETED -> 100
                MissionActionStatus.IN_PROGRESS -> 55
                MissionActionStatus.AVAILABLE -> 15
                MissionActionStatus.LOCKED,
                MissionActionStatus.MISSED,
                -> 0
            }
        }.average().roundToInt()
        val metricProgress = actionPlan.actions
            .map { it.metricProgressPercentage }
            .average()
            .roundToInt()
        val progress = (
            actionProgress * 0.45 +
                metricProgress * 0.35 +
                mission.readinessScore * 0.20
            ).roundToInt().coerceIn(0, 100)
        val completed = actionPlan.actions.count {
            it.completionStatus == MissionActionStatus.COMPLETED
        }
        return MissionProgressSnapshot(
            missionId = mission.missionId,
            progressPercentage = progress,
            completedActions = completed,
            totalActions = actionPlan.actions.size,
            actionProgressPercentage = actionProgress,
            metricProgressPercentage = metricProgress,
            readinessContributionPercentage = mission.readinessScore,
            summary = "$completed of ${actionPlan.actions.size} actions are complete; " +
                "${actionPlan.blockedActions.size} are blocked.",
        )
    }
}

class MissionHealthEngine {
    fun evaluate(
        mission: Mission,
        actionPlan: MissionActionPlan,
        previousReadinessScore: Int = mission.readinessScore,
        previousRiskScore: Int = riskScore(mission.riskLevel),
    ): MissionHealthResult {
        val missed = actionPlan.actions.count { it.completionStatus == MissionActionStatus.MISSED }
        val delayed = actionPlan.actions.count {
            it.completionStatus == MissionActionStatus.LOCKED && it.targetDate <= NINETY_DAY_DATE
        }
        val currentRisk = riskScore(mission.riskLevel)
        val factors = listOf(
            MissionHealthFactor(
                "missed-actions",
                "Missed actions",
                missed > 0,
                if (missed > 0) "$missed action deadlines were missed." else "No action deadlines are missed.",
                if (missed > 0) 35 else 0,
            ),
            MissionHealthFactor(
                "falling-readiness",
                "Falling readiness",
                mission.readinessScore < previousReadinessScore,
                if (mission.readinessScore < previousReadinessScore) {
                    "Readiness fell ${previousReadinessScore - mission.readinessScore} points."
                } else {
                    "Readiness is stable or improving."
                },
                if (mission.readinessScore < previousReadinessScore) 25 else 0,
            ),
            MissionHealthFactor(
                "delayed-milestones",
                "Delayed milestones",
                delayed > 0,
                if (delayed > 0) {
                    "$delayed near-term actions are waiting on dependencies."
                } else {
                    "No near-term milestone is delayed."
                },
                if (delayed > 0) 15 else 0,
            ),
            MissionHealthFactor(
                "increasing-risk",
                "Increasing risk",
                currentRisk > previousRiskScore || mission.riskLevel != RiskLevel.LOW,
                "Current mission risk is ${mission.riskLevel.name.lowercase()}.",
                when {
                    mission.riskLevel == RiskLevel.HIGH -> 35
                    currentRisk > previousRiskScore -> 20
                    mission.riskLevel == RiskLevel.ELEVATED -> 20
                    mission.riskLevel == RiskLevel.MODERATE -> 5
                    else -> 0
                },
            ),
        )
        val score = (100 - factors.sumOf { it.penaltyPoints }).coerceIn(0, 100)
        val status = when {
            score >= 80 -> MissionHealthStatus.GREEN
            score >= 55 -> MissionHealthStatus.YELLOW
            else -> MissionHealthStatus.RED
        }
        return MissionHealthResult(
            missionId = mission.missionId,
            status = status,
            score = score,
            factors = factors,
            summary = when (status) {
                MissionHealthStatus.GREEN -> "On track. Keep the next unlocked action moving."
                MissionHealthStatus.YELLOW -> "Needs attention. Clear the delayed or higher-risk action."
                MissionHealthStatus.RED -> "At risk. Resolve the blocker before advancing the timeline."
            },
        )
    }

    private fun riskScore(level: RiskLevel): Int = when (level) {
        RiskLevel.LOW -> 25
        RiskLevel.MODERATE -> 45
        RiskLevel.ELEVATED -> 65
        RiskLevel.HIGH -> 85
    }
}

class MissionRoadmapService {
    fun build(
        mission: Mission,
        actionPlan: MissionActionPlan,
        health: MissionHealthResult,
    ): MissionRoadmap {
        val horizons = listOf(
            Triple(MissionRoadmapHorizon.THIRTY_DAYS, "30 Days", THIRTY_DAY_DATE),
            Triple(MissionRoadmapHorizon.NINETY_DAYS, "90 Days", NINETY_DAY_DATE),
            Triple(MissionRoadmapHorizon.ONE_YEAR, "1 Year", ONE_YEAR_DATE),
        )
        return MissionRoadmap(
            missionId = mission.missionId,
            stages = horizons.map { (horizon, label, cutoff) ->
                val included = actionPlan.actions.filter { it.targetDate <= cutoff }
                val upcoming = included.filter {
                    it.completionStatus != MissionActionStatus.COMPLETED &&
                        it.completionStatus != MissionActionStatus.MISSED
                }
                MissionRoadmapStage(
                    horizon = horizon,
                    label = label,
                    currentStatus = health.status.name,
                    upcomingActions = upcoming,
                    completedActions = included.filter {
                        it.completionStatus == MissionActionStatus.COMPLETED
                    },
                    expectedReadinessGrowth = upcoming.sumOf { it.readinessGain },
                    projectedCompletionDate = included.maxOfOrNull { it.targetDate }
                        ?: mission.targetDate,
                )
            },
        )
    }
}

class MissionScenarioEvaluator {
    fun evaluate(
        mission: Mission,
        simulations: List<LifeDecisionSimulation>,
    ): List<MissionScenarioImpact> {
        val byId = simulations.associateBy { it.scenarioId }
        val requested = scenarioChoices(mission.missionType)
            .mapNotNull { (scenarioId, title) ->
                byId[scenarioId]?.let { simulation -> impact(simulation, title) }
            }
            .toMutableList()
        if (mission.missionType == MissionType.BUY_HOME) {
            byId["wait-to-buy"]?.let { base ->
                requested.add(
                    2,
                    MissionScenarioImpact(
                        scenarioId = "increase-down-payment",
                        title = "Increase down payment",
                        readinessImpact = base.readinessImpact + 3,
                        timelineImpactMonths = base.timelineChangeMonths - 2,
                        riskImpact = base.riskChange - 2,
                        summary = "A larger down payment improves affordability and lowers financing risk.",
                    ),
                )
            }
        }
        return requested.distinctBy { it.scenarioId }.take(4)
    }

    private fun impact(
        simulation: LifeDecisionSimulation,
        title: String,
    ) = MissionScenarioImpact(
        scenarioId = simulation.scenarioId,
        title = title,
        readinessImpact = simulation.readinessImpact,
        timelineImpactMonths = simulation.timelineChangeMonths,
        riskImpact = simulation.riskChange,
        summary = simulation.summary,
    )

    private fun scenarioChoices(type: MissionType): List<Pair<String, String>> = when (type) {
        MissionType.BUY_HOME -> listOf(
            "buy-home" to "Buy now",
            "wait-to-buy" to "Wait 12 months",
            "pay-off-cards" to "Pay debt first",
        )
        MissionType.HAVE_CHILD -> listOf(
            "have-a-child" to "Have another child",
            "spouse-stops-working" to "One income during leave",
            "job-loss" to "Test an income interruption",
        )
        MissionType.RELOCATE -> listOf(
            "move-to-texas" to "Move to Texas",
            "stay-in-new-jersey" to "Stay in New Jersey",
            "pay-off-cards" to "Pay debt before moving",
        )
        MissionType.RETIRE_EARLY -> listOf(
            "invest-more" to "Invest more",
            "pay-off-cards" to "Pay debt first",
            "spouse-stops-working" to "Test one income",
        )
        MissionType.BECOME_DEBT_FREE -> listOf(
            "pay-off-cards" to "Pay cards now",
            "invest-more" to "Invest instead",
            "job-loss" to "Protect against job loss",
        )
        MissionType.BUILD_EMERGENCY_FUND -> listOf(
            "job-loss" to "Test job loss",
            "pay-off-cards" to "Pay debt first",
            "invest-more" to "Invest excess cash",
        )
        MissionType.SUPPORT_PARENTS -> listOf(
            "job-loss" to "Test income loss",
            "spouse-stops-working" to "Test one income",
            "pay-off-cards" to "Pay debt first",
        )
        MissionType.START_BUSINESS -> listOf(
            "start-business" to "Start now",
            "job-loss" to "Test income interruption",
            "pay-off-cards" to "Pay debt before launch",
        )
    }
}

class MissionHistoryService {
    fun build(
        mission: Mission,
        progress: MissionProgressSnapshot,
        health: MissionHealthResult,
        actionPlan: MissionActionPlan,
        scenarios: List<MissionScenarioImpact>,
    ): MissionHistory {
        val risk = riskScore(mission.riskLevel)
        val points = listOf(
            MissionHistoryPoint(
                "2026-03-12",
                (mission.readinessScore - 7).coerceAtLeast(0),
                (progress.progressPercentage - 12).coerceAtLeast(0),
                (risk + 6).coerceAtMost(100),
                priorHealth(health.status),
            ),
            MissionHistoryPoint(
                "2026-04-12",
                (mission.readinessScore - 4).coerceAtLeast(0),
                (progress.progressPercentage - 8).coerceAtLeast(0),
                (risk + 4).coerceAtMost(100),
                priorHealth(health.status),
            ),
            MissionHistoryPoint(
                "2026-05-12",
                (mission.readinessScore - 2).coerceAtLeast(0),
                (progress.progressPercentage - 4).coerceAtLeast(0),
                (risk + 2).coerceAtMost(100),
                health.status,
            ),
            MissionHistoryPoint(
                TODAY,
                mission.readinessScore,
                progress.progressPercentage,
                risk,
                health.status,
            ),
        )
        val events = buildList {
            add(
                MissionHistoryEvent(
                    "${mission.missionId}-history-readiness",
                    mission.missionId,
                    MissionHistoryEventType.READINESS_CHANGED,
                    "${TODAY}T08:30:00-04:00",
                    "Readiness improved",
                    "Readiness increased ${points.last().readinessScore - points.first().readinessScore} points.",
                ),
            )
            actionPlan.actions.firstOrNull {
                it.completionStatus == MissionActionStatus.COMPLETED
            }?.let { action ->
                add(
                    MissionHistoryEvent(
                        "${mission.missionId}-history-action",
                        mission.missionId,
                        MissionHistoryEventType.ACTION_COMPLETED,
                        "2026-05-12T09:00:00-04:00",
                        "Milestone completed",
                        action.title,
                    ),
                )
            }
            scenarios.minByOrNull { it.timelineImpactMonths }?.let { scenario ->
                add(
                    MissionHistoryEvent(
                        "${mission.missionId}-history-timeline",
                        mission.missionId,
                        MissionHistoryEventType.TIMELINE_CHANGED,
                        "2026-05-20T10:15:00-04:00",
                        "Timeline recalculated",
                        "${scenario.title}: ${signed(scenario.timelineImpactMonths)} months.",
                    ),
                )
            }
            add(
                MissionHistoryEvent(
                    "${mission.missionId}-history-risk",
                    mission.missionId,
                    MissionHistoryEventType.RISK_CHANGED,
                    "2026-06-01T08:45:00-04:00",
                    "Risk improved",
                    "Modeled risk decreased 6 points as reserves and readiness improved.",
                ),
            )
            if (priorHealth(health.status) != health.status) {
                add(
                    MissionHistoryEvent(
                        "${mission.missionId}-history-health",
                        mission.missionId,
                        MissionHistoryEventType.HEALTH_CHANGED,
                        "${TODAY}T08:35:00-04:00",
                        "Mission health changed",
                        "Mission health moved to ${health.status.name.lowercase()}.",
                    ),
                )
            }
        }
        return MissionHistory(mission.missionId, events, points)
    }

    private fun riskScore(level: RiskLevel): Int = when (level) {
        RiskLevel.LOW -> 25
        RiskLevel.MODERATE -> 45
        RiskLevel.ELEVATED -> 65
        RiskLevel.HIGH -> 85
    }

    private fun priorHealth(status: MissionHealthStatus): MissionHealthStatus = when (status) {
        MissionHealthStatus.GREEN -> MissionHealthStatus.YELLOW
        MissionHealthStatus.YELLOW -> MissionHealthStatus.RED
        MissionHealthStatus.RED -> MissionHealthStatus.RED
    }

    private fun signed(value: Int) = if (value >= 0) "+$value" else value.toString()
}

class MissionNotificationService {
    fun generate(
        mission: Mission,
        actionPlan: MissionActionPlan,
        health: MissionHealthResult,
        history: MissionHistory,
        scenarios: List<MissionScenarioImpact>,
    ): List<MissionNotification> = buildList {
        actionPlan.nextAction?.let { action ->
            add(
                notification(
                    mission,
                    "unlocked",
                    MissionNotificationType.ACTION_UNLOCKED,
                    "New action unlocked",
                    action.title,
                ),
            )
        }
        if (health.status == MissionHealthStatus.RED) {
            add(
                notification(
                    mission,
                    "risk",
                    MissionNotificationType.MISSION_AT_RISK,
                    "Mission at risk",
                    health.summary,
                ),
            )
        }
        val readinessChange = history.points.last().readinessScore - history.points.first().readinessScore
        if (readinessChange > 0) {
            add(
                notification(
                    mission,
                    "readiness",
                    MissionNotificationType.READINESS_IMPROVED,
                    "Readiness improved",
                    "${mission.title} gained $readinessChange readiness points.",
                ),
            )
        }
        actionPlan.actions.firstOrNull {
            it.completionStatus == MissionActionStatus.COMPLETED
        }?.let { action ->
            add(
                notification(
                    mission,
                    "milestone",
                    MissionNotificationType.MILESTONE_COMPLETED,
                    "Milestone completed",
                    action.title,
                ),
            )
        }
        scenarios.minByOrNull { it.timelineImpactMonths }
            ?.takeIf { it.timelineImpactMonths < 0 }
            ?.let { scenario ->
                add(
                    notification(
                        mission,
                        "timeline",
                        MissionNotificationType.TIMELINE_ACCELERATED,
                        "Timeline accelerated",
                        "${scenario.title} may save ${-scenario.timelineImpactMonths} months.",
                    ),
                )
            }
        if (actionPlan.actions.all { it.completionStatus == MissionActionStatus.COMPLETED }) {
            add(
                notification(
                    mission,
                    "complete",
                    MissionNotificationType.MISSION_COMPLETED,
                    "Mission completed",
                    "${mission.title} has completed every modeled action.",
                ),
            )
        }
    }

    private fun notification(
        mission: Mission,
        suffix: String,
        type: MissionNotificationType,
        title: String,
        message: String,
    ) = MissionNotification(
        notificationId = "${mission.missionId}-notification-$suffix",
        missionId = mission.missionId,
        type = type,
        title = title,
        message = message,
        createdAt = "${TODAY}T09:00:00-04:00",
        isRead = false,
    )
}

class MissionExecutionService(
    private val actionEngine: MissionActionEngine = MissionActionEngine(),
    private val dependencyEngine: MissionDependencyEngine = MissionDependencyEngine(),
    private val progressEngine: MissionProgressEngine = MissionProgressEngine(),
    private val healthEngine: MissionHealthEngine = MissionHealthEngine(),
    private val roadmapService: MissionRoadmapService = MissionRoadmapService(),
    private val scenarioEvaluator: MissionScenarioEvaluator = MissionScenarioEvaluator(),
    private val historyService: MissionHistoryService = MissionHistoryService(),
    private val notificationService: MissionNotificationService = MissionNotificationService(),
) {
    fun build(
        missions: List<Mission>,
        inputs: MissionInputs,
        completedActionIds: Set<String> = emptySet(),
    ): MissionExecutionCenter {
        val plans = missions.map { mission ->
            val actionPlan = dependencyEngine.resolve(
                actionEngine.generate(mission, inputs.profile, inputs.dashboard).map { action ->
                    if (action.actionId in completedActionIds) {
                        action.copy(
                            completionStatus = MissionActionStatus.COMPLETED,
                            metricProgressPercentage = 100,
                        )
                    } else {
                        action
                    }
                },
            )
            val progress = progressEngine.calculate(mission, actionPlan)
            val health = healthEngine.evaluate(mission, actionPlan)
            val scenarios = scenarioEvaluator.evaluate(mission, inputs.simulations)
            val history = historyService.build(mission, progress, health, actionPlan, scenarios)
            MissionExecutionPlan(
                missionId = mission.missionId,
                actionPlan = actionPlan,
                progress = progress,
                roadmap = roadmapService.build(mission, actionPlan, health),
                health = health,
                notifications = notificationService.generate(
                    mission,
                    actionPlan,
                    health,
                    history,
                    scenarios,
                ),
                history = history,
                scenarioImpacts = scenarios,
            )
        }
        val notifications = plans.flatMap { it.notifications }
        return MissionExecutionCenter(
            plans = plans,
            notifications = notifications.take(16),
            atRiskMissionCount = plans.count { it.health.status == MissionHealthStatus.RED },
            actionsDueCount = plans.sumOf { plan ->
                plan.actionPlan.actions.count {
                    it.targetDate <= NINETY_DAY_DATE &&
                        it.completionStatus != MissionActionStatus.COMPLETED
                }
            },
            recentlyCompletedCount = plans.sumOf { it.progress.completedActions },
        )
    }

    fun applyExecution(
        missions: List<Mission>,
        execution: MissionExecutionCenter,
    ): List<Mission> = missions.map { mission ->
        val plan = execution.plans.first { it.missionId == mission.missionId }
        val next = plan.actionPlan.nextAction
        mission.copy(
            progressPercentage = plan.progress.progressPercentage,
            status = when {
                plan.progress.completedActions == plan.progress.totalActions -> MissionStatus.COMPLETED
                plan.health.status == MissionHealthStatus.RED -> MissionStatus.AT_RISK
                plan.health.status == MissionHealthStatus.GREEN -> MissionStatus.ON_TRACK
                else -> MissionStatus.IN_PROGRESS
            },
            nextAction = if (next == null) {
                mission.nextAction
            } else {
                mission.nextAction.copy(
                    id = next.actionId,
                    title = next.title,
                    description = next.description,
                    estimatedReadinessIncrease = next.readinessGain,
                    estimatedTimelineReductionMonths = plan.scenarioImpacts
                        .minOfOrNull { it.timelineImpactMonths }
                        ?.takeIf { it < 0 }
                        ?.let { -it }
                        ?: mission.nextAction.estimatedTimelineReductionMonths,
                )
            },
            recommendations = plan.actionPlan.unlockedActions.map { it.title }.take(5),
            timeline = mission.timeline.map { point ->
                point.copy(
                    progressPercentage = (
                        plan.progress.progressPercentage + point.monthsFromNow / 2
                        ).coerceAtMost(100),
                    completedActions = plan.progress.completedActions,
                )
            },
        )
    }
}
