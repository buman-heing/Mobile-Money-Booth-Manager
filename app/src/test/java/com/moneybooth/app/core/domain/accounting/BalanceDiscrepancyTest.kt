package com.moneybooth.app.core.domain.accounting

import com.moneybooth.app.core.domain.accounting.BalanceDiscrepancy.UnbalancedMovement
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Test

/** Figures come from the agent's real SMS run of 3–4 Sept 2026. */
class BalanceDiscrepancyTest {

    @Test
    fun `cash-in after cash-out reconciles exactly, commission ignored`() {
        // CO 800.00 -> Bal 803.12, then CI 10.00 -> Bal 793.12 (Com 0.05 is not deducted from float)
        val expected = BalanceDiscrepancy.expectedBalanceMinor(
            lastKnownBalanceMinor = 80312L,
            movementsSince = emptyList(),
            thisDirection = TransactionDirection.OUT,
            thisAmountMinor = 1000L,
            thisType = TransactionType.DEPOSIT,
        )
        assertEquals(79312L, expected)
        assertEquals(0L, BalanceDiscrepancy.discrepancyMinor(79312L, expected))
    }

    @Test
    fun `a missed cash-out shows up as unaccounted money`() {
        // Ledger last saw Bal 793.12. Provider now says 1823.12 after a CO of 1030.00: fine.
        // But if the CO SMS had been lost and the next CI 10.00 reported Bal 1813.12, we'd expect 783.12.
        val expected = BalanceDiscrepancy.expectedBalanceMinor(
            lastKnownBalanceMinor = 79312L,
            movementsSince = emptyList(),
            thisDirection = TransactionDirection.OUT,
            thisAmountMinor = 1000L,
            thisType = TransactionType.DEPOSIT,
        )
        assertEquals(78312L, expected)
        assertEquals(103000L, BalanceDiscrepancy.discrepancyMinor(181312L, expected))
    }

    @Test
    fun `movements without a balance of their own are counted`() {
        // Bal 500.00, then a P2P receive of 50.00 (no balance in SMS), then CO 100.00 -> Bal 650.00
        val expected = BalanceDiscrepancy.expectedBalanceMinor(
            lastKnownBalanceMinor = 50000L,
            movementsSince = listOf(UnbalancedMovement(TransactionDirection.IN, 5000L)),
            thisDirection = TransactionDirection.IN,
            thisAmountMinor = 10000L,
            thisType = TransactionType.WITHDRAWAL,
        )
        assertEquals(65000L, expected)
    }

    @Test
    fun `a balance enquiry moves nothing`() {
        val expected = BalanceDiscrepancy.expectedBalanceMinor(
            lastKnownBalanceMinor = 63200L,
            movementsSince = emptyList(),
            thisDirection = TransactionDirection.UNKNOWN,
            thisAmountMinor = null,
            thisType = TransactionType.BALANCE_CHECK,
        )
        assertEquals(63200L, expected)
    }
}
