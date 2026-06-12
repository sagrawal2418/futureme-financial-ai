package com.futureme.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class MissionCoachQuestion(
    val id: String,
    val title: String,
    val prompt: String,
)

@Serializable
data class MissionExplanationHistoryEntry(
    val explanationId: String,
    val generatedAt: String,
    val readinessScore: Int,
    val coachingSummary: String,
    val recommendedFocusArea: String,
    val topRisk: String,
)

@Serializable
data class MissionCoachBriefing(
    val missionId: String,
    val coachingSummary: String,
    val recommendedFocusArea: String,
    val topRisk: String,
    val topOpportunity: String,
    val suggestedActions: List<String>,
    val whyNotReady: String,
    val whatImprovedRecently: String,
    val whatIsHurtingProgress: String,
    val whatShouldIFocusOn: String,
    val howCanIAccelerateTimeline: String,
    val whatHappensIfIDoNothing: String,
    val suggestedQuestions: List<MissionCoachQuestion>,
    val latestExplanation: MissionExplanationHistoryEntry,
    val previousExplanation: MissionExplanationHistoryEntry,
    val whatChanged: List<String>,
    val providerLabel: String,
    val modelLabel: String,
    val isFallback: Boolean,
)
