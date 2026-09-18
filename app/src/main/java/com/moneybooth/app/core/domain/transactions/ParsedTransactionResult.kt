package com.moneybooth.app.core.domain.transactions

import com.moneybooth.app.core.domain.accounting.Money

/**
 * The deterministic parser's output for one SMS shape. Optional fields are null when the
 * message simply didn't supply them (e.g. no balance in a P2P-receive SMS) — never guessed.
 */
data class ParsedTransactionResult(
    val transactionType: TransactionType,
    val status: TransactionStatus,
    val direction: TransactionDirection,
    val amountMinor: Long? = null,
    val currency: String = Money.DEFAULT_CURRENCY,
    val feeMinor: Long? = null,
    /** Agent commission earned on this transaction (booth revenue). Distinct from [feeMinor], which is a cost. */
    val commissionMinor: Long? = null,
    val senderName: String? = null,
    val senderPhone: String? = null,
    val recipientName: String? = null,
    val recipientPhone: String? = null,
    val merchantTillNumber: String? = null,
    val merchantName: String? = null,
    val serviceName: String? = null,
    val balanceBeforeMinor: Long? = null,
    val balanceAfterMinor: Long? = null,
    val transactionTimestamp: Long? = null,
    val externalTransactionId: String? = null,
    val matcherId: String,
    val confidence: ParseConfidence = ParseConfidence.HIGH,
)
