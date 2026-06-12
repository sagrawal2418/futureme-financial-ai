package com.futureme.shared.mission

import com.futureme.shared.models.GoalType
import com.futureme.shared.models.LifeEventType
import com.futureme.shared.models.MissionType
import com.futureme.shared.models.ReadinessCategory

data class MissionDefinition(
    val missionId: String,
    val missionType: MissionType,
    val title: String,
    val description: String,
    val targetDate: String,
    val estimatedCost: Double,
    val readinessCategory: ReadinessCategory?,
    val goalType: GoalType?,
    val lifeEventType: LifeEventType?,
    val scenarioId: String?,
    val createdDate: String = "2026-06-01",
)

interface MissionRepository {
    fun definitions(): List<MissionDefinition>
}

class DefaultMissionRepository : MissionRepository {
    override fun definitions(): List<MissionDefinition> = listOf(
        MissionDefinition(
            "mission-home",
            MissionType.BUY_HOME,
            "Buy a Home",
            "Build the cash, debt capacity, and resilience needed for the next home.",
            "2027-09-01",
            155_000.0,
            ReadinessCategory.HOME_PURCHASE,
            GoalType.BUY_HOME,
            LifeEventType.HOME_PURCHASE,
            "wait-to-buy",
        ),
        MissionDefinition(
            "mission-child",
            MissionType.HAVE_CHILD,
            "Have a Child",
            "Prepare the household for leave, healthcare, childcare, and a larger reserve.",
            "2028-03-01",
            18_000.0,
            ReadinessCategory.CHILD,
            GoalType.HAVE_CHILD,
            LifeEventType.NEW_BABY,
            "have-a-child",
        ),
        MissionDefinition(
            "mission-relocate",
            MissionType.RELOCATE,
            "Relocate",
            "Confirm the income, housing, benefits, and transition costs of a move.",
            "2027-04-01",
            22_000.0,
            ReadinessCategory.RELOCATION,
            GoalType.MOVE_STATE,
            LifeEventType.RELOCATION,
            "move-to-texas",
        ),
        MissionDefinition(
            "mission-retire",
            MissionType.RETIRE_EARLY,
            "Retire Early",
            "Increase the savings pace and reduce fixed obligations before leaving work.",
            "2043-06-01",
            1_800_000.0,
            ReadinessCategory.RETIREMENT,
            GoalType.RETIRE_EARLY,
            null,
            "invest-more",
        ),
        MissionDefinition(
            "mission-debt-free",
            MissionType.BECOME_DEBT_FREE,
            "Become Debt Free",
            "Eliminate high-interest balances and redirect payments toward future goals.",
            "2027-11-01",
            18_400.0,
            null,
            GoalType.PAY_OFF_DEBT,
            null,
            "pay-off-cards",
        ),
        MissionDefinition(
            "mission-emergency-fund",
            MissionType.BUILD_EMERGENCY_FUND,
            "Build Emergency Fund",
            "Grow liquid reserves to a full twelve months of essential expenses.",
            "2027-12-01",
            120_000.0,
            null,
            GoalType.BUILD_EMERGENCY_FUND,
            LifeEventType.JOB_LOSS,
            "job-loss",
        ),
        MissionDefinition(
            "mission-parent-support",
            MissionType.SUPPORT_PARENTS,
            "Support Parents",
            "Create a durable care reserve without weakening the household plan.",
            "2028-06-01",
            36_000.0,
            ReadinessCategory.PARENT_SUPPORT,
            null,
            LifeEventType.PARENT_SUPPORT,
            null,
        ),
        MissionDefinition(
            "mission-business",
            MissionType.START_BUSINESS,
            "Start a Business",
            "Separate startup capital from an eighteen-month household runway.",
            "2029-01-01",
            150_000.0,
            ReadinessCategory.BUSINESS_STARTUP,
            null,
            null,
            "start-business",
        ),
    )
}
