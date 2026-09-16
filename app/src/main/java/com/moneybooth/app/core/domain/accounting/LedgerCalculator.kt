package com.moneybooth.app.core.domain.accounting

import com.moneybooth.app.core.data.database.entities.TransactionEntity
import com.moneybooth.app.core.domain.transactions.TransactionDirection

/** Pure derived stats over a set of ledger transactions (e.g. for a shift). */
object LedgerCalculator {
    fun sumByDirection(transactions: List<TransactionEntity>, direction: TransactionDirection): Long =
        transactions
            .filter { LedgerInclusionPolicy.isIncluded(it.status) && it.direction == direction }
            .sumOf { it.amountMinor ?: 0L }

    fun latestKnownBalanceMinor(transactions: List<TransactionEntity>): Long? =
        transactions
            .filter { LedgerInclusionPolicy.isIncluded(it.status) && it.balanceAfterMinor != null }
            .maxByOrNull { it.transactionTimestamp ?: it.smsReceivedTimestamp }
            ?.balanceAfterMinor
}
