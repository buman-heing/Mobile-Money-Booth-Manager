package com.moneybooth.app.sms.airtel.matchers

import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.sms.common.SmsShapeMatcher

/** "You have received ZMW 50.00 from 977429540 David Phiri.Dial *115# to check your new Bal. TID: PP260916.1603.N90253." */
class AirtelP2pReceiveMatcher : SmsShapeMatcher {
    override val id = "airtel.p2p_receive.v1"

    private val pattern = Regex(
        """(?i)you\s+have\s+received\s+ZMW\s*([\d,.]+)\s+from\s+(\S+)\s+([^.]+?)\.[\s\S]*?TID:?\s*(\S+)""",
    )

    override fun quickCheck(body: String): Boolean = body.contains("received", ignoreCase = true)

    override fun tryParse(input: RawSmsInput): ParsedTransactionResult? {
        val match = pattern.find(input.body) ?: return null
        val amount = Money.parseToMinorUnits(match.groupValues[1]) ?: return null
        val phone = match.groupValues[2].trim()
        val name = match.groupValues[3].trim()
        val tid = match.groupValues[4].trim().removeSuffix(".")

        // Deliberately no balance capture group: this SMS shape never supplies one.
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
