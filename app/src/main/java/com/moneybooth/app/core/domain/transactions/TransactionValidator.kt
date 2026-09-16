package com.moneybooth.app.core.domain.transactions

/**
 * Defense-in-depth invariant checks, independent of any single parser's correctness.
 * Deliberately takes primitive fields rather than a sms-layer type, so core never depends on sms.
 */
object TransactionValidator {
    fun failedHasNoAmounts(status: TransactionStatus, amountMinor: Long?, balanceAfterMinor: Long?): Boolean =
        !(status == TransactionStatus.FAILED && (amountMinor != null || balanceAfterMinor != null))
}
