package com.moneybooth.app.sms.airtel.matchers

import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.sms.common.SmsShapeMatcher

/**
 * "FAILED.TID: LP260908.2107.L58987, Dear Customer,you have insufficient funds to complete this
 * transaction.Kindly top up and try again.."
 *
 * A failed transaction must never populate amount/balance, and must never affect the ledger totals
 * (enforced by [com.moneybooth.app.core.domain.accounting.LedgerInclusionPolicy], which excludes
 * FAILED status from all sums).
 */
class AirtelFailedTransactionMatcher : SmsShapeMatcher {
    override val id = "airtel.failed.v1"

    private val pattern = Regex("""(?i)^\s*FAILED\.?\s*TID:?\s*([^,\s]+)""")

    override fun quickCheck(body: String): Boolean = body.trim().startsWith("FAILED", ignoreCase = true)

    override fun tryParse(input: RawSmsInput): ParsedTransactionResult? {
        val match = pattern.find(input.body) ?: return null
        val tid = match.groupValues[1].trim().removeSuffix(",")

        return ParsedTransactionResult(
            transactionType = TransactionType.OTHER,
            status = TransactionStatus.FAILED,
            direction = TransactionDirection.UNKNOWN,
            amountMinor = null,
            balanceAfterMinor = null,
            externalTransactionId = tid,
            matcherId = id,
        )
    }
}
