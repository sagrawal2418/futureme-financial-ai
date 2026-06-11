package com.futureme.shared

import com.futureme.shared.domain.FutureMeProduct
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProductBootstrapV2Test {
    private val bootstrap = FutureMeProduct().bootstrap()

    @Test
    fun bootstrapSynchronizesEveryV2Feature() {
        assertEquals(90, bootstrap.transactions.size)
        assertTrue(bootstrap.insights.size >= 4)
        assertEquals(6, bootstrap.goals.size)
        assertEquals(6, bootstrap.lifeEvents.size)
        assertEquals(6, bootstrap.moneyLeaks.size)
        assertTrue(bootstrap.financialGps.difference > 0.0)
    }

    @Test
    fun noRealProviderCredentialsExistInProductPayload() {
        val serializedFields = bootstrap.toString().lowercase()
        assertTrue("access_token" !in serializedFields)
        assertTrue("api_key" !in serializedFields)
    }
}
