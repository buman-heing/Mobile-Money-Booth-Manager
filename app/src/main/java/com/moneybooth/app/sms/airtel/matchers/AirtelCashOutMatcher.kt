package com.moneybooth.app.sms.airtel.matchers

import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.sms.common.SmsShapeMatcher

/**
 * Agent-side cash-out: a customer withdrew cash at the booth, so the booth's float went UP.
 *
 * "ZMW 900.00  received from 977992879 Angela Chazura. Bal ZMW 902.03. Comm ZMW 9.00 TID: CO260914.1556.V49915"
 *
 * Distinguished from a plain P2P receive ("Money received ZMW ...") by the leading amount and the
 * agent commission field, which never appears on a customer-side SMS.
 */
class AirtelCashOutMatcher : SmsShapeMatcher {
    override val id = "airtel.cash_out.v1"

    private val pattern = Regex(
        """(?i)ZMW\s*$AMOUNT\s+received\s+from\s+(\S+)\s+(.+?)\.\s*Bal\s+ZMW\s*$AMOUNT(?:\.?\s*Comm?\s+ZMW\s*$AMOUNT)?[\s\S]*?TID\W*(\S+)""",
    )

    override fun quickCheck(body: String): Boolean = body.contains("received from", ignoreCase = true)

    override fun tryParse(input: RawSmsInput): ParsedTransactionResult? {
        val match = pattern.find(input.body) ?: return null
        val amount = Money.parseToMinorUnits(match.groupValues[1]) ?: return null
        val phone = match.groupValues[2].trim()
        val name = match.groupValues[3].trim()
        val balance = Money.parseToMinorUnits(match.groupValues[4])
        val commission = match.groupValues[5].takeIf { it.isNotBlank() }?.let(Money::parseToMinorUnits)
        val tid = match.groupValues[6].trim().removeSuffix(".")

        return ParsedTransactionResult(
            transactionType = TransactionType.WITHDRAWAL,
            status = TransactionStatus.PARSED,
            direction = TransactionDirection.IN,
            amountMinor = amount,
            commissionMinor = commission,
            senderPhone = phone,
            senderName = name,
            balanceAfterMinor = balance,
            externalTransactionId = tid,
            matcherId = id,
        )
    }
}
