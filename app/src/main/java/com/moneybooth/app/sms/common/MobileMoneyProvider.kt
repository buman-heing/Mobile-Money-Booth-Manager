package com.moneybooth.app.sms.common

import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.core.domain.transactions.ValidationResult

/**
 * A pluggable mobile-money provider (Airtel Money now; MTN and others register the same way
 * later). The core accounting/ledger/reconciliation code never references a provider by name.
 */
interface MobileMoneyProvider {
    val providerId: String
    val providerName: String
    val parser: SmsParser
    val detector: ProviderDetector
    val supportedTransactionTypes: Set<TransactionType>

    fun validate(result: ParsedTransactionResult): ValidationResult
}
