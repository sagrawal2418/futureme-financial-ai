@file:OptIn(ExperimentalJsExport::class)

package com.futureme.shared.web

import com.futureme.shared.domain.FutureMeProduct
import com.futureme.shared.models.AssistantPrompt
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@JsExport
object FutureMeWebApi {
    private val product = FutureMeProduct()
    private val json = Json {
        encodeDefaults = true
        explicitNulls = true
    }

    fun bootstrapJson(): String = json.encodeToString(product.bootstrap())

    fun simulateJson(scenarioId: String): String =
        json.encodeToString(product.simulate(scenarioId))

    fun compareJson(leftScenarioId: String, rightScenarioId: String): String =
        json.encodeToString(product.compare(leftScenarioId, rightScenarioId))

    fun askJson(question: String, latestScenarioId: String?): String =
        json.encodeToString(
            product.ask(
                AssistantPrompt(
                    question = question,
                    latestScenarioId = latestScenarioId,
                ),
            ),
        )

    fun recordAnalyticsEventJson(typeCode: String, subjectId: String): String =
        json.encodeToString(product.recordAnalyticsEvent(typeCode, subjectId))

    fun saveDecisionJson(scenarioId: String): String =
        json.encodeToString(product.saveDecision(scenarioId))

    fun decisionJournalJson(): String =
        json.encodeToString(product.decisionJournal())
}
