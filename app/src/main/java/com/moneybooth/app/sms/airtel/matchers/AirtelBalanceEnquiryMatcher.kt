package com.moneybooth.app.sms.airtel.matchers

import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.sms.common.SmsShapeMatcher

/**
 * "Your Current Balance is ZMW 6.32" — the reply to a balance enquiry. No money moved; the
 * balance is recorded so the ledger can be checked against it. The SMS has no transaction id,
 * so the receive time stands in for one (two enquiries a minute apart are two records).
 */
class AirtelBalanceEnquiryMatcher : SmsShapeMatcher {
    override val id = "airtel.balance_enquiry.v1"

    private val pattern = Regex("""(?i)current\s+balance\s+is\s+ZMW\s*$AMOUNT""")

    override fun quickCheck(body: String): Boolean = body.contains("current balance", ignoreCase = true)

    override fun tryParse(input: RawSmsInput): ParsedTransactionResult? {
        val match = pattern.find(input.body) ?: return null
        val balance = Money.parseToMinorUnits(match.groupValues[1]) ?: return null
        return ParsedTransactionResult(
            transactionType = TransactionType.BALANCE_CHECK,
            status = TransactionStatus.PARSED,
            direction = TransactionDirection.UNKNOWN,
            amountMinor = null,
            balanceAfterMinor = balance,
            externalTransactionId = "BAL${input.receivedTimestamp}",
            matcherId = id,
        )
    }
}
