package com.futureme.shared

import com.futureme.shared.domain.FutureMeProduct
import com.futureme.shared.mission.MissionActionEngine
import com.futureme.shared.mission.MissionDependencyEngine
import com.futureme.shared.mission.MissionHealthEngine
import com.futureme.shared.mission.MissionHistoryService
import com.futureme.shared.mission.MissionNotificationService
import com.futureme.shared.mission.MissionProgressEngine
import com.futureme.shared.mission.MissionScenarioEvaluator
import com.futureme.shared.models.MissionActionStatus
import com.futureme.shared.models.MissionHealthStatus
import com.futureme.shared.models.MissionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MissionExecutionLayerTest {
    private val product = FutureMeProduct()
    private val bootstrap = product.bootstrap()
    private val home = bootstrap.missions.first { it.missionType == MissionType.BUY_HOME }
    private val homeExecution = bootstrap.missionExecution.plans.first {
        it.missionId == home.missionId
    }

    @Test
    fun missionActionEngineBuildsMeasurableStepsForEveryMission() {
        val engine = MissionActionEngine()
        bootstrap.missions.forEach { mission ->
            val actions = engine.generate(mission, bootstrap.profile, bootstrap.dashboard)
            assertEquals(4, actions.size)
            assertTrue(actions.all { it.readinessGain > 0 })
            assertTrue(actions.all { it.targetDate.isNotBlank() })
            assertTrue(actions.all { it.metricProgressPercentage in 0..100 })
        }
    }

    @Test
    fun missionDependencyEngineSeparatesBlockedAndUnlockedActions() {
        val actions = MissionActionEngine().generate(home, bootstrap.profile, bootstrap.dashboard)
        val plan = MissionDependencyEngine().resolve(actions)

        assertTrue(plan.blockedActions.isNotEmpty())
        assertTrue(plan.unlockedActions.isNotEmpty())
        assertNotNull(plan.nextAction)
        assertTrue(plan.blockedActions.all { it.completionStatus == MissionActionStatus.LOCKED })
    }

    @Test
    fun missionProgressEngineDerivesProgressFromActionsMetricsAndReadiness() {
        val progress = MissionProgressEngine().calculate(home, homeExecution.actionPlan)

        assertTrue(progress.progressPercentage in 0..100)
        assertEquals(homeExecution.actionPlan.actions.size, progress.totalActions)
        assertTrue(progress.metricProgressPercentage > 0)
        assertTrue(progress.summary.contains("actions are complete"))
    }

    @Test
    fun missionHealthEngineRespondsToFallingReadinessAndRisk() {
        val result = MissionHealthEngine().evaluate(
            mission = home,
            actionPlan = homeExecution.actionPlan,
            previousReadinessScore = home.readinessScore + 8,
            previousRiskScore = 0,
        )

        assertTrue(result.factors.first { it.id == "falling-readiness" }.triggered)
        assertTrue(result.factors.first { it.id == "increasing-risk" }.triggered)
        assertTrue(result.status != MissionHealthStatus.GREEN)
    }

    @Test
    fun missionNotificationServiceGeneratesLocalMissionUpdates() {
        val notifications = MissionNotificationService().generate(
            mission = home,
            actionPlan = homeExecution.actionPlan,
            health = homeExecution.health,
            history = homeExecution.history,
            scenarios = homeExecution.scenarioImpacts,
        )

        assertTrue(notifications.isNotEmpty())
        assertTrue(notifications.all { !it.isRead })
        assertTrue(notifications.all { it.missionId == home.missionId })
    }

    @Test
    fun missionHistoryServiceStoresMetricsAndEvents() {
        val history = MissionHistoryService().build(
            mission = home,
            progress = homeExecution.progress,
            health = homeExecution.health,
            actionPlan = homeExecution.actionPlan,
            scenarios = homeExecution.scenarioImpacts,
        )

        assertEquals(4, history.points.size)
        assertTrue(history.events.isNotEmpty())
        assertTrue(history.points.zipWithNext().all {
            it.first.readinessScore <= it.second.readinessScore
        })
    }

    @Test
    fun missionScenarioEvaluatorReturnsMissionSpecificImpacts() {
        val scenarios = MissionScenarioEvaluator().evaluate(
            home,
            bootstrap.decisionSimulations,
        )

        assertEquals(4, scenarios.size)
        assertTrue(scenarios.any { it.title == "Buy now" })
        assertTrue(scenarios.any { it.title == "Wait 12 months" })
        assertTrue(scenarios.any { it.title == "Increase down payment" })
        assertTrue(scenarios.any { it.title == "Pay debt first" })
    }

    @Test
    fun completingAnUnlockedActionRecalculatesTheMission() {
        val before = product.bootstrap()
        val plan = before.missionExecution.plans.first { it.missionId == home.missionId }
        val action = requireNotNull(plan.actionPlan.nextAction)

        val after = product.completeMissionAction(action.actionId)
        val updated = after.missionExecution.plans.first { it.missionId == home.missionId }
        val completed = updated.actionPlan.actions.first { it.actionId == action.actionId }

        assertEquals(MissionActionStatus.COMPLETED, completed.completionStatus)
        assertTrue(updated.progress.progressPercentage >= plan.progress.progressPercentage)
        assertTrue(after.analyticsEvents.first().type.name == "MISSION_ACTION_COMPLETED")
    }
}
