package com.moneybooth.app.core.domain.transactions

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class IdempotencyKeyGeneratorTest {

    @Test
    fun `uses provider transaction id when present`() {
        val key = IdempotencyKeyGenerator.generate("AIRTEL", "CO260916.1609.H17346", "irrelevant-fingerprint")
        assertEquals("TID:AIRTEL:CO260916.1609.H17346", key)
    }

    @Test
    fun `same provider and transaction id always produce the same key`() {
        val keyA = IdempotencyKeyGenerator.generate("AIRTEL", "PP260916.1603.N90253", "fp1")
        val keyB = IdempotencyKeyGenerator.generate("AIRTEL", "PP260916.1603.N90253", "fp2")
        assertEquals(keyA, keyB)
    }

    @Test
    fun `falls back to fingerprint when no transaction id is present`() {
        val key = IdempotencyKeyGenerator.generate("AIRTEL", null, "abc123")
        assertEquals("FP:abc123", key)
    }

    @Test
    fun `falls back to fingerprint when transaction id is blank`() {
        val key = IdempotencyKeyGenerator.generate("AIRTEL", "   ", "abc123")
        assertEquals("FP:abc123", key)
    }

    @Test
    fun `different providers with the same transaction id do not collide`() {
        val keyA = IdempotencyKeyGenerator.generate("AIRTEL", "12345", "fp")
        val keyB = IdempotencyKeyGenerator.generate("MTN", "12345", "fp")
        assertNotEquals(keyA, keyB)
    }
}
