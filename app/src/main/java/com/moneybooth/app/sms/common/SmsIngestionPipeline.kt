package com.moneybooth.app.sms.common

import com.moneybooth.app.core.data.repository.RawSmsRepository
import com.moneybooth.app.core.data.repository.TransactionRepository
import com.moneybooth.app.core.domain.transactions.IdempotencyKeyGenerator
import com.moneybooth.app.core.domain.transactions.ParserOutcome
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.SmsFingerprint
import com.moneybooth.app.core.domain.transactions.TransactionSource
import com.moneybooth.app.core.domain.transactions.ValidationResult
import com.moneybooth.app.sms.providers.ProviderRegistry

sealed interface IngestionResult {
    data class Ledgered(val rawSmsId: Long, val transactionId: Long) : IngestionResult
    data class Duplicate(val rawSmsId: Long, val transactionId: Long) : IngestionResult
    data class NeedsReview(val rawSmsId: Long, val reason: String) : IngestionResult
}

/**
 * The single implementation of raw SMS -> detect -> parse -> validate -> dedup -> attribute ->
 * ledger. Used identically by the live SMS receiver (M4) and the developer paste-SMS screen, so
 * dev-mode testing exercises the exact same code path as production ingestion.
 */
class SmsIngestionPipeline(
    private val providerRegistry: ProviderRegistry,
    private val rawSmsRepository: RawSmsRepository,
    private val transactionRepository: TransactionRepository,
) {
    suspend fun ingest(
        input: RawSmsInput,
        source: TransactionSource,
        boothId: Long? = null,
        shiftId: Long? = null,
        employeeId: Long? = null,
    ): IngestionResult {
        val rawSmsId = rawSmsRepository.insertRaw(input)

        val provider = providerRegistry.detect(input)
        if (provider == null) {
            rawSmsRepository.markPendingReview(rawSmsId, detectedProviderId = null, parserVersion = null)
            return IngestionResult.NeedsReview(rawSmsId, "No provider recognized this message format.")
        }

        return when (val outcome = provider.parser.parse(input)) {
            is ParserOutcome.NoMatch -> {
                rawSmsRepository.markPendingReview(rawSmsId, provider.providerId, PARSER_VERSION)
                IngestionResult.NeedsReview(rawSmsId, "Message format not recognized by the ${provider.providerName} parser.")
            }

            is ParserOutcome.Matched -> {
                val validation = provider.validate(outcome.result)
                if (validation is ValidationResult.Invalid) {
                    rawSmsRepository.markPendingReview(rawSmsId, provider.providerId, PARSER_VERSION)
                    IngestionResult.NeedsReview(rawSmsId, validation.reason)
                } else {
                    val fallbackFingerprint = SmsFingerprint.compute(input.sender, input.body)
                    val dedupKey = IdempotencyKeyGenerator.generate(
                        provider.providerId,
                        outcome.result.externalTransactionId,
                        fallbackFingerprint,
                    )
                    val insertOutcome = transactionRepository.insertFromParsedResult(
                        parsed = outcome.result,
                        providerId = provider.providerId,
                        dedupKey = dedupKey,
                        rawSmsId = rawSmsId,
                        smsReceivedTimestamp = input.receivedTimestamp,
                        source = source,
                        boothId = boothId,
                        shiftId = shiftId,
                        employeeId = employeeId,
                        parserVersion = PARSER_VERSION,
                    )
                    rawSmsRepository.markParsed(
                        rawSmsId,
                        provider.providerId,
                        PARSER_VERSION,
                        insertOutcome.transactionId,
                        insertOutcome.wasDuplicate,
                    )
                    if (insertOutcome.wasDuplicate) {
                        IngestionResult.Duplicate(rawSmsId, insertOutcome.transactionId)
                    } else {
                        IngestionResult.Ledgered(rawSmsId, insertOutcome.transactionId)
                    }
                }
            }
        }
    }

    companion object {
        const val PARSER_VERSION = "1"
    }
}
