package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.AuditLogDao
import com.moneybooth.app.core.data.database.dao.TransactionDao
import com.moneybooth.app.core.data.database.entities.AuditLogEntity
import com.moneybooth.app.core.data.database.entities.TransactionEntity
import com.moneybooth.app.core.domain.audit.AuditActionType
import com.moneybooth.app.core.domain.transactions.BusinessClassification
import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionSource
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.core.sync.SyncEntityType
import com.moneybooth.app.core.sync.SyncOutbox
import kotlinx.coroutines.flow.Flow

data class InsertOutcome(val transactionId: Long, val wasDuplicate: Boolean)

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val auditLogDao: AuditLogDao,
    private val outbox: SyncOutbox,
) {
    fun observeById(id: Long): Flow<TransactionEntity?> = transactionDao.observeById(id)

    suspend fun getByIdOnce(id: Long): TransactionEntity? = transactionDao.getByIdOnce(id)

    fun observeByShift(shiftId: Long): Flow<List<TransactionEntity>> = transactionDao.observeByShift(shiftId)

    suspend fun getByShiftOnce(shiftId: Long): List<TransactionEntity> = transactionDao.getByShiftOnce(shiftId)

    fun filter(
        boothId: Long? = null,
        employeeId: Long? = null,
        providerId: String? = null,
        transactionType: TransactionType? = null,
        status: TransactionStatus? = null,
        classification: BusinessClassification? = null,
        startTime: Long? = null,
        endTime: Long? = null,
        search: String? = null,
    ): Flow<List<TransactionEntity>> = transactionDao.filter(
        boothId, employeeId, providerId, transactionType, status, classification, startTime, endTime,
        search?.ifBlank { null },
    )

    fun observeCountForDay(boothId: Long?, startTime: Long, endTime: Long): Flow<Int> =
        transactionDao.observeCountForDay(boothId, startTime, endTime)

    fun observeSumForDay(boothId: Long?, direction: TransactionDirection, startTime: Long, endTime: Long): Flow<Long> =
        transactionDao.observeSumForDay(boothId, direction, startTime, endTime)

    fun observeCommissionSumForDay(boothId: Long?, startTime: Long, endTime: Long): Flow<Long> =
        transactionDao.observeCommissionSumForDay(boothId, startTime, endTime)

    fun observeLatestKnownBalance(boothId: Long?): Flow<Long?> = transactionDao.observeLatestKnownBalance(boothId)

    /** Inserts a ledger row from a parsed SMS result, or returns the existing row if [dedupKey] was already seen. */
    suspend fun insertFromParsedResult(
        parsed: ParsedTransactionResult,
        providerId: String,
        dedupKey: String,
        rawSmsId: Long?,
        smsReceivedTimestamp: Long,
        source: TransactionSource,
        boothId: Long?,
        shiftId: Long?,
        employeeId: Long?,
        parserVersion: String,
    ): InsertOutcome {
        transactionDao.getByDedupKey(dedupKey)?.let { return InsertOutcome(it.id, wasDuplicate = true) }

        val now = System.currentTimeMillis()
        val entity = TransactionEntity(
            providerId = providerId,
            externalTransactionId = parsed.externalTransactionId,
            transactionType = parsed.transactionType,
            status = parsed.status,
            direction = parsed.direction,
            amountMinor = parsed.amountMinor,
            currency = parsed.currency,
            feeMinor = parsed.feeMinor,
            commissionMinor = parsed.commissionMinor,
            senderName = parsed.senderName,
            senderPhone = parsed.senderPhone,
            recipientName = parsed.recipientName,
            recipientPhone = parsed.recipientPhone,
            merchantTillNumber = parsed.merchantTillNumber,
            merchantName = parsed.merchantName,
            serviceName = parsed.serviceName,
            balanceBeforeMinor = parsed.balanceBeforeMinor,
            balanceAfterMinor = parsed.balanceAfterMinor,
            transactionTimestamp = parsed.transactionTimestamp,
            smsReceivedTimestamp = smsReceivedTimestamp,
            employeeId = employeeId,
            boothId = boothId,
            shiftId = shiftId,
            businessClassification = BusinessClassification.UNKNOWN,
            rawSmsId = rawSmsId,
            dedupKey = dedupKey,
            parserVersion = parserVersion,
            source = source,
            createdAt = now,
            updatedAt = now,
        )
        val id = transactionDao.insert(entity)
        outbox.changed(SyncEntityType.TRANSACTION, entity.uid)
        return InsertOutcome(id, wasDuplicate = false)
    }

    suspend fun createManualTransaction(
        providerId: String,
        transactionType: TransactionType,
        direction: TransactionDirection,
        amountMinor: Long,
        currency: String,
        boothId: Long,
        employeeId: Long?,
        shiftId: Long?,
        classification: BusinessClassification,
        note: String?,
        dedupKey: String,
        performedBy: String,
    ): Long {
        val now = System.currentTimeMillis()
        val entity = TransactionEntity(
            providerId = providerId,
            transactionType = transactionType,
            status = TransactionStatus.CONFIRMED,
            direction = direction,
            amountMinor = amountMinor,
            currency = currency,
            senderName = null,
            merchantName = note,
            smsReceivedTimestamp = now,
            employeeId = employeeId,
            boothId = boothId,
            shiftId = shiftId,
            businessClassification = classification,
            rawSmsId = null,
            dedupKey = dedupKey,
            parserVersion = "manual",
            source = TransactionSource.MANUAL_ENTRY,
            createdAt = now,
            updatedAt = now,
        )
        val id = transactionDao.insert(entity)
        outbox.changed(SyncEntityType.TRANSACTION, entity.uid)
        audit(
            AuditLogEntity(
                entityType = "transaction",
                entityId = id,
                actionType = AuditActionType.MANUAL_TRANSACTION_CREATED,
                newValue = "$transactionType $direction $amountMinor",
                performedBy = performedBy,
                timestamp = now,
            ),
        )
        return id
    }

    suspend fun updateClassification(
        transactionId: Long,
        newClassification: BusinessClassification,
        performedBy: String,
        reason: String?,
    ) {
        val existing = transactionDao.getByIdOnce(transactionId) ?: return
        if (existing.businessClassification == newClassification) return
        val now = System.currentTimeMillis()
        transactionDao.update(existing.copy(businessClassification = newClassification, updatedAt = now))
        outbox.changed(SyncEntityType.TRANSACTION, existing.uid)
        audit(
            AuditLogEntity(
                entityType = "transaction",
                entityId = transactionId,
                actionType = AuditActionType.CLASSIFICATION_CHANGE,
                fieldName = "businessClassification",
                oldValue = existing.businessClassification.name,
                newValue = newClassification.name,
                reason = reason,
                performedBy = performedBy,
                timestamp = now,
            ),
        )
    }

    suspend fun updateAttribution(
        transactionId: Long,
        newEmployeeId: Long?,
        newShiftId: Long?,
        performedBy: String,
        reason: String?,
    ) {
        val existing = transactionDao.getByIdOnce(transactionId) ?: return
        val now = System.currentTimeMillis()
        transactionDao.update(existing.copy(employeeId = newEmployeeId, shiftId = newShiftId, updatedAt = now))
        outbox.changed(SyncEntityType.TRANSACTION, existing.uid)
        audit(
            AuditLogEntity(
                entityType = "transaction",
                entityId = transactionId,
                actionType = AuditActionType.EMPLOYEE_ATTRIBUTION_CHANGE,
                fieldName = "employeeId",
                oldValue = existing.employeeId?.toString(),
                newValue = newEmployeeId?.toString(),
                reason = reason,
                performedBy = performedBy,
                timestamp = now,
            ),
        )
    }

    suspend fun rejectTransaction(transactionId: Long, performedBy: String, reason: String?) {
        val existing = transactionDao.getByIdOnce(transactionId) ?: return
        val now = System.currentTimeMillis()
        transactionDao.update(existing.copy(status = TransactionStatus.REJECTED, updatedAt = now))
        outbox.changed(SyncEntityType.TRANSACTION, existing.uid)
        audit(
            AuditLogEntity(
                entityType = "transaction",
                entityId = transactionId,
                actionType = AuditActionType.TRANSACTION_REJECTED,
                oldValue = existing.status.name,
                newValue = TransactionStatus.REJECTED.name,
                reason = reason,
                performedBy = performedBy,
                timestamp = now,
            ),
        )
    }

    private suspend fun audit(entry: AuditLogEntity) {
        auditLogDao.insert(entry)
        outbox.changed(SyncEntityType.AUDIT_LOG, entry.uid)
    }
}
