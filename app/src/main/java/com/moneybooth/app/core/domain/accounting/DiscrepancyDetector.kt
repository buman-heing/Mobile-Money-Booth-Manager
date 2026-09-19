package com.moneybooth.app.core.domain.accounting

import com.moneybooth.app.core.data.repository.TransactionRepository
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult

/** Runs at ingestion on the phone that receives the SMS; the result travels with the transaction. */
class DiscrepancyDetector(private val transactionRepository: TransactionRepository) {

    /** Null when this SMS carries no balance or nothing earlier is known to compare against. */
    suspend fun detect(parsed: ParsedTransactionResult, smsReceivedTimestamp: Long): Long? {
        val reported = parsed.balanceAfterMinor ?: return null
        val previous = transactionRepository.getLatestWithBalanceBefore(smsReceivedTimestamp) ?: return null
        val since = transactionRepository.getWithoutBalanceBetween(previous.smsReceivedTimestamp, smsReceivedTimestamp)
            .map { BalanceDiscrepancy.UnbalancedMovement(it.direction, it.amountMinor) }
        val expected = BalanceDiscrepancy.expectedBalanceMinor(
            lastKnownBalanceMinor = previous.balanceAfterMinor ?: return null,
            movementsSince = since,
            thisDirection = parsed.direction,
            thisAmountMinor = parsed.amountMinor,
            thisType = parsed.transactionType,
        )
        return BalanceDiscrepancy.discrepancyMinor(reported, expected)
    }
}
