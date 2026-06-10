package com.futureme.shared.models

import com.futureme.shared.design.DesignTokens
import kotlinx.serialization.Serializable

@Serializable
data class ProductBootstrap(
    val identity: UserIdentity,
    val profile: FinancialProfile,
    val dashboard: DashboardSnapshot,
    val scenarios: List<Scenario>,
    val recentScenarioResults: List<ScenarioResult>,
    val suggestedQuestions: List<SuggestedQuestion>,
    val designTokens: DesignTokens,
    val disclaimer: String,
)
