package com.moneybooth.app.sms.airtel.matchers

import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.sms.common.SmsShapeMatcher

/** "You have withdrawn ZMW 48.00 from 1824209 Joseph Lungu. Bal is ZMW 0.06. TID: CO260916.1609.H17346." */
class AirtelWithdrawalMatcher : SmsShapeMatcher {
    override val id = "airtel.withdrawal.v1"

    private val pattern = Regex(
        """(?i)you\s+have\s+withdrawn\s+ZMW\s*([\d,.]+)\s+from\s+(\S+)\s+([^.]+?)\.\s*Bal\s+is\s+ZMW\s*([\d,.]+)\.[\s\S]*?TID:?\s*(\S+)""",
    )

    override fun quickCheck(body: String): Boolean = body.contains("withdraw", ignoreCase = true)

    override fun tryParse(input: RawSmsInput): ParsedTransactionResult? {
        val match = pattern.find(input.body) ?: return null
        val amount = Money.parseToMinorUnits(match.groupValues[1]) ?: return null
        val otherPartyIdentifier = match.groupValues[2].trim()
        val otherPartyName = match.groupValues[3].trim()
        val balance = Money.parseToMinorUnits(match.groupValues[4])
        val tid = match.groupValues[5].trim().removeSuffix(".")

        return ParsedTransactionResult(
            transactionType = TransactionType.WITHDRAWAL,
            status = TransactionStatus.PARSED,
            direction = TransactionDirection.OUT,
            amountMinor = amount,
            merchantTillNumber = otherPartyIdentifier,
            merchantName = otherPartyName,
            balanceAfterMinor = balance,
            externalTransactionId = tid,
            matcherId = id,
        )
    }
}
