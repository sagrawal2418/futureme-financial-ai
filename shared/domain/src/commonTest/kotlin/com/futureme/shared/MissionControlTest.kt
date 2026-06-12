package com.futureme.shared

import com.futureme.shared.domain.FutureMeProduct
import com.futureme.shared.models.AssistantPrompt
import com.futureme.shared.models.MissionReadinessCategory
import com.futureme.shared.models.MissionTimelineHorizon
import com.futureme.shared.models.MissionType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MissionControlTest {
    private val product = FutureMeProduct()
    private val missions = product.missions()

    @Test
    fun missionEngineBuildsTheEightPrimaryMissions() {
        assertEquals(8, missions.size)
        assertEquals(MissionType.entries.toSet(), missions.map { it.missionType }.toSet())
        assertTrue(missions.all { it.readinessScore in 0..100 })
        assertTrue(missions.all { it.readinessFactors.size == MissionReadinessCategory.entries.size })
        assertTrue(missions.all { it.nextAction.estimatedReadinessIncrease > 0 })
    }

    @Test
    fun everyMissionHasTheRequiredTimeline() {
        val expected = MissionTimelineHorizon.entries.toList()
        missions.forEach { mission ->
            assertEquals(expected, mission.timeline.map { it.horizon })
            assertTrue(mission.timeline.zipWithNext().all { it.first.readinessScore <= it.second.readinessScore })
        }
    }

    @Test
    fun missionControlSurfacesPriorityRiskAndOpportunity() {
        val control = product.missionControl()
        assertEquals(missions.size, control.activeMissions.size)
        assertTrue(control.nextBestAction.impactScore > 0)
        assertTrue(control.risks.isNotEmpty())
        assertEquals(3, control.opportunities.size)
        assertTrue(control.missionProgressPercentage in 0..100)
    }

    @Test
    fun missionAnalyticsTracksImprovementTrends() {
        val analytics = product.missionAnalytics()
        assertEquals(8, analytics.missionsCreated)
        assertEquals(8, analytics.trends.size)
        assertTrue(analytics.readinessImprovements > 0)
        assertTrue(analytics.timelineImprovements > 0)
        assertTrue(analytics.actionsCompleted > 0)
    }

    @Test
    fun coachAnswersMissionFirstQuestions() {
        val response = product.ask(AssistantPrompt("Which mission should I prioritize?"))
        assertTrue("prioritize" in response.answer.lowercase())
        assertTrue(response.suggestedActions.isNotEmpty())

        val faster = product.ask(AssistantPrompt("How can I become ready faster?"))
        assertTrue("shorten the timeline" in faster.answer.lowercase())

        val child = product.ask(
            AssistantPrompt("How can I become ready faster for my child mission?"),
        )
        assertTrue("have a child" in child.answer.lowercase())
    }
}
