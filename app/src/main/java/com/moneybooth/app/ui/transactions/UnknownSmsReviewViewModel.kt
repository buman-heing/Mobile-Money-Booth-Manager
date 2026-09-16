package com.moneybooth.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneybooth.app.core.data.database.entities.BoothEntity
import com.moneybooth.app.core.data.database.entities.RawSmsEntity
import com.moneybooth.app.core.data.repository.AuditLogRepository
import com.moneybooth.app.core.data.repository.BoothRepository
import com.moneybooth.app.core.data.repository.RawSmsRepository
import com.moneybooth.app.core.data.repository.TransactionRepository
import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.audit.AuditActionType
import com.moneybooth.app.core.domain.transactions.BusinessClassification
import com.moneybooth.app.core.domain.transactions.IdempotencyKeyGenerator
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionSource
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class UnknownSmsReviewViewModel(
    private val rawSmsRepository: RawSmsRepository,
    private val transactionRepository: TransactionRepository,
    private val boothRepository: BoothRepository,
    private val auditLogRepository: AuditLogRepository,
) : ViewModel() {

    fun observeNeedsReview(): Flow<List<RawSmsEntity>> = rawSmsRepository.observeNeedsReview()

    fun observeBooths(businessId: Long): Flow<List<BoothEntity>> = boothRepository.observeByBusiness(businessId)

    /** Resolves a needs-review raw SMS into a confirmed ledger transaction with the owner's manual interpretation. */
    fun resolve(
        rawSmsId: Long,
        boothId: Long,
        type: TransactionType,
        direction: TransactionDirection,
        amountText: String,
        party: String?,
        classification: BusinessClassification,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            val rawSms = rawSmsRepository.getByIdOnce(rawSmsId) ?: return@launch
            val providerId = rawSms.detectedProviderId ?: "MANUAL_REVIEW"
            val dedupKey = IdempotencyKeyGenerator.generate(providerId, null, rawSms.fingerprint)

            val parsed = ParsedTransactionResult(
                transactionType = type,
                status = TransactionStatus.CONFIRMED,
                direction = direction,
                amountMinor = amountText.ifBlank { null }?.let { Money.parseToMinorUnits(it) },
                currency = Money.DEFAULT_CURRENCY,
                senderName = party?.ifBlank { null },
                externalTransactionId = null,
                matcherId = "manual_review",
            )

            val insertOutcome = transactionRepository.insertFromParsedResult(
                parsed = parsed,
                providerId = providerId,
                dedupKey = dedupKey,
                rawSmsId = rawSmsId,
                smsReceivedTimestamp = rawSms.receivedTimestamp,
                source = TransactionSource.MANUAL_ENTRY,
                boothId = boothId,
                shiftId = null,
                employeeId = null,
                parserVersion = "manual_review",
            )

            if (classification != BusinessClassification.UNKNOWN) {
                transactionRepository.updateClassification(
                    insertOutcome.transactionId,
                    classification,
                    performedBy = "owner",
                    reason = "Resolved from needs-review queue",
                )
            }

            rawSmsRepository.markParsed(
                rawSmsId,
                providerId,
                "manual_review",
                insertOutcome.transactionId,
                insertOutcome.wasDuplicate,
            )

            auditLogRepository.record(
                entityType = "raw_sms",
                entityId = rawSmsId,
                actionType = AuditActionType.MANUAL_TRANSACTION_CREATED,
                performedBy = "owner",
                reason = "Resolved from needs-review queue",
            )

            onDone()
        }
    }
}
