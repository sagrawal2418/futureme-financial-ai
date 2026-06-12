package com.futureme.shared.models

import kotlinx.serialization.Serializable

@Serializable
enum class ProductPriority {
    MUST_HAVE,
    NICE_TO_HAVE,
    FUTURE_RELEASE,
    POTENTIALLY_REMOVE,
}

@Serializable
data class ProductNavigationTab(
    val id: String,
    val label: String,
    val customerQuestion: String,
    val contents: List<String>,
    val rationale: String,
)

@Serializable
data class FeatureRecommendation(
    val name: String,
    val priority: ProductPriority,
    val rationale: String,
)

@Serializable
data class ProductStrategy(
    val positioningStatement: String,
    val productPromise: String,
    val navigation: List<ProductNavigationTab>,
    val featureRecommendations: List<FeatureRecommendation>,
)

@Serializable
data class AiEvaluationCategoryScore(
    val category: String,
    val label: String,
    val promptCount: Int,
    val responseQualityScore: Int,
    val reasoningQualityScore: Int,
    val consistencyScore: Int,
    val hallucinationRiskScore: Int,
    val explanationUsefulnessScore: Int,
    val passRate: Int,
)

@Serializable
data class AiEvaluationDashboard(
    val totalPrompts: Int,
    val lastRunDate: String,
    val overallQualityScore: Int,
    val reasoningQualityScore: Int,
    val consistencyScore: Int,
    val hallucinationRiskScore: Int,
    val explanationUsefulnessScore: Int,
    val categories: List<AiEvaluationCategoryScore>,
    val statusLabel: String,
    val methodologyNote: String,
)

@Serializable
data class PersonaReadinessScore(
    val category: String,
    val score: Int,
)

@Serializable
data class CustomerPersona(
    val id: String,
    val title: String,
    val summary: String,
    val profile: FinancialProfile,
    val goals: List<String>,
    val challenges: List<String>,
    val expectedRecommendations: List<String>,
    val expectedReadinessScores: List<PersonaReadinessScore>,
    val expectedMissionPlan: List<String>,
)

@Serializable
data class ExecutiveDemoStory(
    val title: String,
    val audience: List<String>,
    val personaId: String,
    val opening: String,
    val steps: List<String>,
    val closing: String,
)
