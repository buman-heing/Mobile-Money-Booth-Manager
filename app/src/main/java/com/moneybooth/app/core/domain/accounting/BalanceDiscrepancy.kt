package com.moneybooth.app.core.domain.accounting

import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionType

/**
 * Every agent SMS states the float balance after the transaction, so each one lets us check the
 * ledger's arithmetic: last known balance ± everything since should equal the balance the
 * provider just reported. A non-zero result means a transaction happened that this phone never
 * saw (a lost SMS on a bad-signal day, or activity outside the app) — exactly what an owner
 * wants flagged. Commission is paid separately and never moves the float balance.
 */
object BalanceDiscrepancy {

    /** A transaction that happened after the last known balance and did not itself report one. */
    data class UnbalancedMovement(val direction: TransactionDirection, val amountMinor: Long?)

    fun expectedBalanceMinor(
        lastKnownBalanceMinor: Long,
        movementsSince: List<UnbalancedMovement>,
        thisDirection: TransactionDirection,
        thisAmountMinor: Long?,
        thisType: TransactionType,
    ): Long {
        var expected = lastKnownBalanceMinor
        for (m in movementsSince) expected += signed(m.direction, m.amountMinor)
        if (thisType != TransactionType.BALANCE_CHECK) expected += signed(thisDirection, thisAmountMinor)
        return expected
    }

    /** Reported minus expected; positive means more money than the ledger can account for. */
    fun discrepancyMinor(reportedBalanceMinor: Long, expectedBalanceMinor: Long): Long =
        reportedBalanceMinor - expectedBalanceMinor

    private fun signed(direction: TransactionDirection, amountMinor: Long?): Long = when (direction) {
        TransactionDirection.IN -> amountMinor ?: 0L
        TransactionDirection.OUT -> -(amountMinor ?: 0L)
        TransactionDirection.UNKNOWN -> 0L
    }
}
