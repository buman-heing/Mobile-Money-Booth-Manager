package com.moneybooth.app.core.domain.transactions

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransactionValidatorTest {

    @Test
    fun `failed transaction with no amount or balance is valid`() {
        assertTrue(TransactionValidator.failedHasNoAmounts(TransactionStatus.FAILED, null, null))
    }

    @Test
    fun `failed transaction with an amount is invalid`() {
        assertFalse(TransactionValidator.failedHasNoAmounts(TransactionStatus.FAILED, 4800L, null))
    }

    @Test
    fun `failed transaction with a balance is invalid`() {
        assertFalse(TransactionValidator.failedHasNoAmounts(TransactionStatus.FAILED, null, 600L))
    }

    @Test
    fun `non-failed transaction with an amount is valid`() {
        assertTrue(TransactionValidator.failedHasNoAmounts(TransactionStatus.PARSED, 4800L, 600L))
    }
}
