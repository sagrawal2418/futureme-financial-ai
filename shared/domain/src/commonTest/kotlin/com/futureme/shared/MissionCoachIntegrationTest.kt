package com.futureme.shared

import com.futureme.shared.domain.FutureMeProduct
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MissionCoachIntegrationTest {
    @Test
    fun everyMissionHasGroundedExplanationsAndQuestions() {
        val bootstrap = FutureMeProduct().bootstrap()

        assertEquals(bootstrap.missions.size, bootstrap.missionCoachBriefings.size)
        bootstrap.missionCoachBriefings.forEach { briefing ->
            assertTrue(briefing.coachingSummary.isNotBlank())
            assertTrue(briefing.whyNotReady.isNotBlank())
            assertTrue(briefing.whatImprovedRecently.isNotBlank())
            assertTrue(briefing.whatIsHurtingProgress.isNotBlank())
            assertTrue(briefing.whatShouldIFocusOn.isNotBlank())
            assertTrue(briefing.howCanIAccelerateTimeline.isNotBlank())
            assertTrue(briefing.whatHappensIfIDoNothing.isNotBlank())
            assertEquals(3, briefing.suggestedQuestions.size)
            assertTrue(briefing.isFallback)
        }
    }

    @Test
    fun completingAnActionRefreshesTheMissionBriefing() {
        val product = FutureMeProduct()
        val before = product.bootstrap()
        val action = before.missionExecution.plans
            .first { it.actionPlan.nextAction != null }
            .actionPlan.nextAction!!
        val beforeBriefing = before.missionCoachBriefings.first {
            it.missionId == action.missionId
        }

        val after = product.completeMissionAction(action.actionId)
        val afterBriefing = after.missionCoachBriefings.first {
            it.missionId == action.missionId
        }

        assertNotEquals(
            beforeBriefing.recommendedFocusArea,
            afterBriefing.recommendedFocusArea,
        )
        assertTrue(afterBriefing.whatChanged.isNotEmpty())
    }
}
