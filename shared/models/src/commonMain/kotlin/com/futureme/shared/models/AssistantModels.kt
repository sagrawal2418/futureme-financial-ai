package com.futureme.shared.models

import kotlinx.serialization.Serializable

@Serializable
data class AssistantPrompt(
    val question: String,
    val latestScenarioId: String? = null,
)

@Serializable
data class SuggestedQuestion(
    val id: String,
    val title: String,
    val prompt: String,
    val category: String,
)

@Serializable
data class AssistantResponse(
    val answer: String,
    val relatedScenarioId: String? = null,
    val suggestedActions: List<String> = emptyList(),
)
