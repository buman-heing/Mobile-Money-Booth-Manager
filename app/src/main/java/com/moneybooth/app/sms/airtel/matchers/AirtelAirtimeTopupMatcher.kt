package com.moneybooth.app.sms.airtel.matchers

import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.sms.common.SmsShapeMatcher

/** "Your ZMW 2.00 airtime top-up is successful. Your new Airtel Money balance is ZMW 0.56.Txn ID : RC260916.1117.Q13311" */
class AirtelAirtimeTopupMatcher : SmsShapeMatcher {
    override val id = "airtel.airtime_topup.v1"

    private val pattern = Regex(
        """(?i)your\s+ZMW\s*([\d,.]+)\s+airtime\s+top-?up\s+is\s+successful\.[\s\S]*?balance\s+is\s+ZMW\s*([\d,.]+)\.[\s\S]*?(?:Txn\s*ID|TID)\W*(\S+)""",
    )

    override fun quickCheck(body: String): Boolean = body.contains("airtime top", ignoreCase = true)

    override fun tryParse(input: RawSmsInput): ParsedTransactionResult? {
        val match = pattern.find(input.body) ?: return null
        val amount = Money.parseToMinorUnits(match.groupValues[1]) ?: return null
        val balance = Money.parseToMinorUnits(match.groupValues[2])
        val tid = match.groupValues[3].trim().removeSuffix(".")

        return ParsedTransactionResult(
            transactionType = TransactionType.AIRTIME_TOPUP,
            status = TransactionStatus.PARSED,
            direction = TransactionDirection.OUT,
            amountMinor = amount,
            balanceAfterMinor = balance,
            externalTransactionId = tid,
            matcherId = id,
        )
    }
}
