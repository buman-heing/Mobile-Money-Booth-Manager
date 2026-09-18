package com.moneybooth.app.sms.airtel.matchers

import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.sms.common.SmsShapeMatcher

/**
 * Plain person-to-person receive on the booth line (not a cash-out: no commission, no balance).
 *
 * "Money received ZMW 10.00 from 20317390 Grace Chanda. Dial *115# to check balance. TID: PP..."
 *
 * The text between the sender name and the TID is not relied upon, only the TID is.
 */
class AirtelP2pReceiveMatcher : SmsShapeMatcher {
    override val id = "airtel.p2p_receive.v2"

    private val pattern = Regex(
        """(?i)money\s+received\s+ZMW\s*$AMOUNT\s+from\s+(\S+)\s+(.+?)\.[\s\S]*?TID\W*(\S+)""",
    )

    override fun quickCheck(body: String): Boolean = body.contains("money received", ignoreCase = true)

    override fun tryParse(input: RawSmsInput): ParsedTransactionResult? {
        val match = pattern.find(input.body) ?: return null
        val amount = Money.parseToMinorUnits(match.groupValues[1]) ?: return null
        val phone = match.groupValues[2].trim()
        val name = match.groupValues[3].trim()
        val tid = match.groupValues[4].trim().removeSuffix(".")

        return ParsedTransactionResult(
            transactionType = TransactionType.P2P_RECEIVE,
            status = TransactionStatus.PARSED,
            direction = TransactionDirection.IN,
            amountMinor = amount,
            senderPhone = phone,
            senderName = name,
            balanceAfterMinor = null,
            externalTransactionId = tid,
            matcherId = id,
        )
    }
}
