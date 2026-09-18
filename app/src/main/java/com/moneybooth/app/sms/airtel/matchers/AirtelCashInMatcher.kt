package com.moneybooth.app.sms.airtel.matchers

import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.sms.common.SmsShapeMatcher

/**
 * Agent-side cash-in: a customer deposited cash at the booth, so the booth's float went DOWN.
 *
 * "You have sent ZMW 5.00 to 970532065 GRACE CHANDA. Bal ZMW 51.36.Com ZMW 0.03 TID: CI260816.2154.L14464"
 *
 * Note the real template has no space between the balance and "Com" — the amount pattern stops
 * before that period on purpose.
 */
class AirtelCashInMatcher : SmsShapeMatcher {
    override val id = "airtel.cash_in.v1"

    private val pattern = Regex(
        """(?i)you\s+have\s+sent\s+ZMW\s*$AMOUNT\s+to\s+(\S+)\s+(.+?)\.\s*Bal\s+ZMW\s*$AMOUNT(?:\.?\s*Comm?\s+ZMW\s*$AMOUNT)?[\s\S]*?TID\W*(\S+)""",
    )

    override fun quickCheck(body: String): Boolean = body.contains("you have sent", ignoreCase = true)

    override fun tryParse(input: RawSmsInput): ParsedTransactionResult? {
        val match = pattern.find(input.body) ?: return null
        val amount = Money.parseToMinorUnits(match.groupValues[1]) ?: return null
        val phone = match.groupValues[2].trim()
        val name = match.groupValues[3].trim()
        val balance = Money.parseToMinorUnits(match.groupValues[4])
        val commission = match.groupValues[5].takeIf { it.isNotBlank() }?.let(Money::parseToMinorUnits)
        val tid = match.groupValues[6].trim().removeSuffix(".")

        return ParsedTransactionResult(
            transactionType = TransactionType.DEPOSIT,
            status = TransactionStatus.PARSED,
            direction = TransactionDirection.OUT,
            amountMinor = amount,
            commissionMinor = commission,
            recipientPhone = phone,
            recipientName = name,
            balanceAfterMinor = balance,
            externalTransactionId = tid,
            matcherId = id,
        )
    }
}
