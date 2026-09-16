package com.moneybooth.app.sms.airtel.matchers

import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.sms.common.SmsShapeMatcher

/** "Money sent to Sara ngala on 971430077.Amount ZMW 17.00. Your bal is ZMW 5.56.TID: PP260915.1930.D02882." */
class AirtelP2pSendMatcher : SmsShapeMatcher {
    override val id = "airtel.p2p_send.v1"

    private val pattern = Regex(
        """(?i)money\s+sent\s+to\s+(.+?)\s+on\s+(\S+)\.\s*Amount\s+ZMW\s*([\d,.]+)\.\s*Your\s+bal\s+is\s+ZMW\s*([\d,.]+)\.[\s\S]*?TID:?\s*(\S+)""",
    )

    override fun quickCheck(body: String): Boolean = body.contains("money sent to", ignoreCase = true)

    override fun tryParse(input: RawSmsInput): ParsedTransactionResult? {
        val match = pattern.find(input.body) ?: return null
        val name = match.groupValues[1].trim()
        val phone = match.groupValues[2].trim()
        val amount = Money.parseToMinorUnits(match.groupValues[3]) ?: return null
        val balance = Money.parseToMinorUnits(match.groupValues[4])
        val tid = match.groupValues[5].trim().removeSuffix(".")

        return ParsedTransactionResult(
            transactionType = TransactionType.P2P_SEND,
            status = TransactionStatus.PARSED,
            direction = TransactionDirection.OUT,
            recipientName = name,
            recipientPhone = phone,
            amountMinor = amount,
            balanceAfterMinor = balance,
            externalTransactionId = tid,
            matcherId = id,
        )
    }
}
