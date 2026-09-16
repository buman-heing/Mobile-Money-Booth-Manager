package com.moneybooth.app.sms.airtel.matchers

import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.sms.common.SmsShapeMatcher

/** "Payment of ZMW 3.00 Till Number SOCHESCARE AIRTEL NETWORKS SELF CARE SOCHE. Airtel Money bal is ZMW 2.56. TID : MP260915.1931.M41519." */
class AirtelTillPaymentMatcher : SmsShapeMatcher {
    override val id = "airtel.till_payment.v1"

    private val pattern = Regex(
        """(?i)payment\s+of\s+ZMW\s*([\d,.]+)\s+Till\s+Number\s+(\S+)\s+([^.]+?)\.\s*Airtel\s+Money\s+bal\s+is\s+ZMW\s*([\d,.]+)\.[\s\S]*?TID\W*(\S+)""",
    )

    override fun quickCheck(body: String): Boolean = body.contains("till number", ignoreCase = true)

    override fun tryParse(input: RawSmsInput): ParsedTransactionResult? {
        val match = pattern.find(input.body) ?: return null
        val amount = Money.parseToMinorUnits(match.groupValues[1]) ?: return null
        val tillNumber = match.groupValues[2].trim()
        val merchantName = match.groupValues[3].trim()
        val balance = Money.parseToMinorUnits(match.groupValues[4])
        val tid = match.groupValues[5].trim().removeSuffix(".")

        return ParsedTransactionResult(
            transactionType = TransactionType.TILL_PAYMENT,
            status = TransactionStatus.PARSED,
            direction = TransactionDirection.OUT,
            amountMinor = amount,
            merchantTillNumber = tillNumber,
            merchantName = merchantName,
            balanceAfterMinor = balance,
            externalTransactionId = tid,
            matcherId = id,
        )
    }
}
