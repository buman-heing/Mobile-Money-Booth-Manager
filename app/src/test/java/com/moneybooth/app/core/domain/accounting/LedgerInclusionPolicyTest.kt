package com.moneybooth.app.core.domain.accounting

import com.moneybooth.app.core.domain.transactions.TransactionStatus
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LedgerInclusionPolicyTest {

    @Test
    fun `parsed and confirmed transactions are included`() {
        assertTrue(LedgerInclusionPolicy.isIncluded(TransactionStatus.PARSED))
        assertTrue(LedgerInclusionPolicy.isIncluded(TransactionStatus.CONFIRMED))
    }

    @Test
    fun `failed transactions are never included`() {
        assertFalse(LedgerInclusionPolicy.isIncluded(TransactionStatus.FAILED))
    }

    @Test
    fun `pending review, reversed and rejected transactions are excluded`() {
        assertFalse(LedgerInclusionPolicy.isIncluded(TransactionStatus.PENDING_REVIEW))
        assertFalse(LedgerInclusionPolicy.isIncluded(TransactionStatus.REVERSED))
        assertFalse(LedgerInclusionPolicy.isIncluded(TransactionStatus.REJECTED))
    }
}
