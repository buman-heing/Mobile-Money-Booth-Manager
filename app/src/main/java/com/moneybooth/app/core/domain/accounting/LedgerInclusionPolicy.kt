package com.moneybooth.app.core.domain.accounting

import com.moneybooth.app.core.domain.transactions.TransactionStatus

/** Which transaction statuses count toward ledger totals (dashboard, reconciliation sums). */
object LedgerInclusionPolicy {
    private val includedStatuses = setOf(TransactionStatus.PARSED, TransactionStatus.CONFIRMED)

    fun isIncluded(status: TransactionStatus): Boolean = status in includedStatuses
}
