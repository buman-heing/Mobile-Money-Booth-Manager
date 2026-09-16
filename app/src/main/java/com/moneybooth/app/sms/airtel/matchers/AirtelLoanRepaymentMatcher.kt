package com.moneybooth.app.sms.airtel.matchers

import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.sms.common.SmsShapeMatcher

/** "You have repaid  13.75 ZMW towards your FIKILIZA loan. Your new available balance is 6.26 ZMW. Txn ID.LR260911.1647.I10047" */
class AirtelLoanRepaymentMatcher : SmsShapeMatcher {
    override val id = "airtel.loan_repayment.v1"

    private val pattern = Regex(
        """(?i)you\s+have\s+repaid\s+([\d,.]+)\s*ZMW\s+towards\s+your\s+(\S+)\s+loan\.\s*Your\s+new\s+available\s+balance\s+is\s+([\d,.]+)\s*ZMW\.[\s\S]*?Txn\s*ID\W*(\S+)""",
    )

    override fun quickCheck(body: String): Boolean = body.contains("repaid", ignoreCase = true) &&
        body.contains("loan", ignoreCase = true)

    override fun tryParse(input: RawSmsInput): ParsedTransactionResult? {
        val match = pattern.find(input.body) ?: return null
        val amount = Money.parseToMinorUnits(match.groupValues[1]) ?: return null
        val service = match.groupValues[2].trim()
        val balance = Money.parseToMinorUnits(match.groupValues[3])
        val tid = match.groupValues[4].trim().removeSuffix(".")

        return ParsedTransactionResult(
            transactionType = TransactionType.LOAN_REPAYMENT,
            status = TransactionStatus.PARSED,
            direction = TransactionDirection.OUT,
            amountMinor = amount,
            serviceName = service,
            balanceAfterMinor = balance,
            externalTransactionId = tid,
            matcherId = id,
        )
    }
}
