package com.moneybooth.app.sms.airtel

import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.core.domain.transactions.TransactionValidator
import com.moneybooth.app.core.domain.transactions.ValidationResult
import com.moneybooth.app.sms.common.MobileMoneyProvider
import com.moneybooth.app.sms.common.ProviderDetector
import com.moneybooth.app.sms.common.SmsParser

class AirtelMoneyProvider : MobileMoneyProvider {
    override val providerId: String = "AIRTEL"
    override val providerName: String = "Airtel Money"
    override val parser: SmsParser = AirtelSmsParser()
    override val detector: ProviderDetector = AirtelProviderDetector()
    override val supportedTransactionTypes: Set<TransactionType> = setOf(
        TransactionType.WITHDRAWAL,
        TransactionType.DEPOSIT,
        TransactionType.P2P_RECEIVE,
        TransactionType.P2P_SEND,
        TransactionType.AIRTIME_TOPUP,
        TransactionType.TILL_PAYMENT,
        TransactionType.LOAN_REPAYMENT,
        TransactionType.BALANCE_CHECK,
    )

    override fun validate(result: ParsedTransactionResult): ValidationResult {
        val ok = TransactionValidator.failedHasNoAmounts(result.status, result.amountMinor, result.balanceAfterMinor)
        return if (ok) ValidationResult.Valid else ValidationResult.Invalid("FAILED transactions must not include amount or balance.")
    }
}
