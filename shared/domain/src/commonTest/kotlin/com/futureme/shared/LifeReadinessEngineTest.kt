package com.futureme.shared

import com.futureme.shared.domain.FutureMeProduct
import com.futureme.shared.mock.MockFinancialData
import com.futureme.shared.models.ReadinessCategory
import com.futureme.shared.models.ReadinessLevel
import com.futureme.shared.readiness.LifeReadinessEngine
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LifeReadinessEngineTest {
    private val engine = LifeReadinessEngine()
    private val readiness = engine.evaluateAll(MockFinancialData.profile)

    @Test
    fun supportsEveryLifeReadinessCategory() {
        assertEquals(
            ReadinessCategory.entries.toSet(),
            readiness.map { it.category }.toSet(),
        )
    }

    @Test
    fun scoresAreBoundedExplainedAndActionable() {
        readiness.forEach { result ->
            assertTrue(result.readinessScore in 0..100)
            assertTrue(result.strengths.isNotEmpty())
            assertTrue(result.weaknesses.isNotEmpty())
            assertTrue(result.blockers.isNotEmpty())
            assertTrue(result.recommendedActions.isNotEmpty())
            assertTrue(result.projectedReadyDate.isNotBlank())
        }
    }

    @Test
    fun readinessLevelsUseTheVersionThreeThresholds() {
        readiness.forEach { result ->
            val expected = when (result.readinessScore) {
                in 0..39 -> ReadinessLevel.NOT_READY
                in 40..59 -> ReadinessLevel.NEEDS_PREPARATION
                in 60..79 -> ReadinessLevel.ALMOST_READY
                else -> ReadinessLevel.READY
            }
            assertEquals(expected, result.readinessLevel)
        }
    }

    @Test
    fun improvementPlansCloseAVisibleScoreGap() {
        engine.improvementPlans(MockFinancialData.profile).forEach { plan ->
            assertEquals(plan.targetScore - plan.currentScore, plan.scoreGap)
            assertTrue(plan.monthlyCommitment > 0.0)
            assertTrue(plan.recommendations.isNotEmpty())
            assertTrue(plan.estimatedTimelineMonths >= 0)
        }
    }

    @Test
    fun decisionSimulatorReturnsReadinessFinancialRiskAndTimelineImpact() {
        val simulations = FutureMeProduct().bootstrap().decisionSimulations

        assertTrue(simulations.any { it.scenarioId == "spouse-stops-working" })
        assertTrue(simulations.any { it.scenarioId == "start-business" })
        simulations.forEach { simulation ->
            assertEquals(
                simulation.readinessScoreAfter - simulation.readinessScoreBefore,
                simulation.readinessImpact,
            )
            assertEquals(
                simulation.riskScoreAfter - simulation.riskScoreBefore,
                simulation.riskChange,
            )
            assertTrue(simulation.summary.isNotBlank())
        }
    }

    @Test
    fun timelineContainsAllRequiredHorizons() {
        val timeline = FutureMeProduct().lifeTimeline()

        assertEquals(listOf(0, 6, 12, 36, 60), timeline.map { it.monthsFromNow })
        assertTrue(timeline.last().netWorth > timeline.first().netWorth)
        assertTrue(timeline.last().investmentBalance > timeline.first().investmentBalance)
        assertTrue(timeline.last().debtBalance <= timeline.first().debtBalance)
        assertTrue(
            timeline.all {
                it.readinessScores.map { score -> score.category }.toSet() ==
                    ReadinessCategory.entries.toSet()
            },
        )
    }
}
