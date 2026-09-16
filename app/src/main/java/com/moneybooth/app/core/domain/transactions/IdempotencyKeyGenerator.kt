package com.moneybooth.app.core.domain.transactions

/**
 * Dedup key for a transaction: the provider transaction ID when present (strongest signal),
 * else a fingerprint of the originating SMS. Re-receiving the same SMS must never create a
 * second ledger row.
 */
object IdempotencyKeyGenerator {
    fun generate(providerId: String, externalTransactionId: String?, fallbackFingerprint: String): String =
        if (!externalTransactionId.isNullOrBlank()) {
            "TID:$providerId:$externalTransactionId"
        } else {
            "FP:$fallbackFingerprint"
        }
}
