package com.moneybooth.app.core.sync

import com.moneybooth.app.core.data.database.AppDatabase
import com.moneybooth.app.core.data.database.entities.SyncOutboxEntity

/**
 * Turns a queued (type, uid) into the record the cloud should hold. Local foreign keys are
 * replaced with the target row's uid, because auto-increment ids differ from phone to phone.
 * Returns null when the local row no longer exists, in which case the queue entry is dropped.
 */
class RecordAssembler(private val db: AppDatabase) {

    suspend fun assemble(entry: SyncOutboxEntity): RemoteRecord? {
        val businessUid = db.businessDao().getFirstOnce()?.uid ?: return null
        val fields: Map<String, Any?> = when (entry.entityType) {
            SyncEntityType.BUSINESS -> db.businessDao().getByUid(entry.entityUid)?.let {
                mapOf("name" to it.name, "defaultCurrency" to it.defaultCurrency, "createdAt" to it.createdAt)
            }

            SyncEntityType.BOOTH -> db.boothDao().getByUid(entry.entityUid)?.let {
                mapOf(
                    "name" to it.name,
                    "location" to it.location,
                    "active" to it.active,
                    "createdAt" to it.createdAt,
                    "updatedAt" to it.updatedAt,
                )
            }

            SyncEntityType.EMPLOYEE -> db.employeeDao().getByUid(entry.entityUid)?.let {
                mapOf(
                    "name" to it.name,
                    "phone" to it.phone,
                    "role" to it.role.name,
                    "active" to it.active,
                    "assignedBoothUid" to boothUid(it.assignedBoothId),
                    "createdAt" to it.createdAt,
                    "updatedAt" to it.updatedAt,
                )
            }

            SyncEntityType.MOBILE_MONEY_ACCOUNT -> db.mobileMoneyAccountDao().getByUid(entry.entityUid)?.let {
                mapOf(
                    "boothUid" to boothUid(it.boothId),
                    "providerId" to it.providerId,
                    "label" to it.label,
                    "phoneNumber" to it.phoneNumber,
                    "active" to it.active,
                    "createdAt" to it.createdAt,
                )
            }

            SyncEntityType.SHIFT -> db.shiftDao().getByUid(entry.entityUid)?.let {
                mapOf(
                    "boothUid" to boothUid(it.boothId),
                    "employeeUid" to employeeUid(it.employeeId),
                    "mobileMoneyAccountUid" to it.mobileMoneyAccountId?.let { id -> db.mobileMoneyAccountDao().getByIdOnce(id)?.uid },
                    "status" to it.status.name,
                    "openedAt" to it.openedAt,
                    "openingCashMinor" to it.openingCashMinor,
                    "openingMobileMoneyBalanceMinor" to it.openingMobileMoneyBalanceMinor,
                    "closedAt" to it.closedAt,
                    "notes" to it.notes,
                    "confirmed" to it.confirmed,
                    "createdAt" to it.createdAt,
                    "updatedAt" to it.updatedAt,
                )
            }

            SyncEntityType.RAW_SMS -> db.rawSmsDao().getByUid(entry.entityUid)?.let {
                mapOf(
                    "sender" to it.sender,
                    "receivedTimestamp" to it.receivedTimestamp,
                    "rawBody" to it.rawBody,
                    "fingerprint" to it.fingerprint,
                    "detectedProviderId" to it.detectedProviderId,
                    "parserVersion" to it.parserVersion,
                    "parsingStatus" to it.parsingStatus.name,
                    "parsedTransactionUid" to transactionUid(it.parsedTransactionId),
                    "createdAt" to it.createdAt,
                )
            }

            SyncEntityType.TRANSACTION -> db.transactionDao().getByUid(entry.entityUid)?.let {
                mapOf(
                    "providerId" to it.providerId,
                    "externalTransactionId" to it.externalTransactionId,
                    "transactionType" to it.transactionType.name,
                    "status" to it.status.name,
                    "direction" to it.direction.name,
                    "amountMinor" to it.amountMinor,
                    "currency" to it.currency,
                    "feeMinor" to it.feeMinor,
                    "commissionMinor" to it.commissionMinor,
                    "senderName" to it.senderName,
                    "senderPhone" to it.senderPhone,
                    "recipientName" to it.recipientName,
                    "recipientPhone" to it.recipientPhone,
                    "merchantTillNumber" to it.merchantTillNumber,
                    "merchantName" to it.merchantName,
                    "serviceName" to it.serviceName,
                    "balanceBeforeMinor" to it.balanceBeforeMinor,
                    "balanceAfterMinor" to it.balanceAfterMinor,
                    "discrepancyMinor" to it.discrepancyMinor,
                    "transactionTimestamp" to it.transactionTimestamp,
                    "smsReceivedTimestamp" to it.smsReceivedTimestamp,
                    "employeeUid" to employeeUid(it.employeeId),
                    "boothUid" to boothUid(it.boothId),
                    "shiftUid" to shiftUid(it.shiftId),
                    "businessClassification" to it.businessClassification.name,
                    "rawSmsUid" to it.rawSmsId?.let { id -> db.rawSmsDao().getByIdOnce(id)?.uid },
                    "dedupKey" to it.dedupKey,
                    "parserVersion" to it.parserVersion,
                    "source" to it.source.name,
                    "createdAt" to it.createdAt,
                    "updatedAt" to it.updatedAt,
                )
            }

            SyncEntityType.CASH_MOVEMENT -> db.cashMovementDao().getByUid(entry.entityUid)?.let {
                mapOf(
                    "shiftUid" to shiftUid(it.shiftId),
                    "boothUid" to boothUid(it.boothId),
                    "direction" to it.direction.name,
                    "amountMinor" to it.amountMinor,
                    "reason" to it.reason.name,
                    "linkedTransactionUid" to transactionUid(it.linkedTransactionId),
                    "note" to it.note,
                    "recordedAt" to it.recordedAt,
                    "createdByEmployeeUid" to employeeUid(it.createdBy),
                )
            }

            SyncEntityType.RECONCILIATION -> db.reconciliationDao().getByUid(entry.entityUid)?.let {
                mapOf(
                    "shiftUid" to shiftUid(it.shiftId),
                    "boothUid" to boothUid(it.boothId),
                    "employeeUid" to employeeUid(it.employeeId),
                    "date" to it.date,
                    "openingCashMinor" to it.openingCashMinor,
                    "cashInMinor" to it.cashInMinor,
                    "cashOutMinor" to it.cashOutMinor,
                    "cashAdjustmentsMinor" to it.cashAdjustmentsMinor,
                    "expectedCashMinor" to it.expectedCashMinor,
                    "actualCashMinor" to it.actualCashMinor,
                    "cashDifferenceMinor" to it.cashDifferenceMinor,
                    "openingMobileMoneyBalanceMinor" to it.openingMobileMoneyBalanceMinor,
                    "mobileMoneyInMinor" to it.mobileMoneyInMinor,
                    "mobileMoneyOutMinor" to it.mobileMoneyOutMinor,
                    "expectedMobileMoneyBalanceMinor" to it.expectedMobileMoneyBalanceMinor,
                    "latestProviderReportedBalanceMinor" to it.latestProviderReportedBalanceMinor,
                    "mobileMoneyDifferenceMinor" to it.mobileMoneyDifferenceMinor,
                    "unclassifiedTransactionCount" to it.unclassifiedTransactionCount,
                    "closedAt" to it.closedAt,
                    "closedBy" to it.closedBy,
                    "notes" to it.notes,
                    "createdAt" to it.createdAt,
                )
            }

            SyncEntityType.AUDIT_LOG -> db.auditLogDao().getByUid(entry.entityUid)?.let {
                mapOf(
                    "entityType" to it.entityType,
                    "entityUid" to if (it.entityType == "transaction") transactionUid(it.entityId) else null,
                    "actionType" to it.actionType.name,
                    "fieldName" to it.fieldName,
                    "oldValue" to it.oldValue,
                    "newValue" to it.newValue,
                    "reason" to it.reason,
                    "performedBy" to it.performedBy,
                    "timestamp" to it.timestamp,
                )
            }
        } ?: return null

        return RemoteRecord(entry.entityType, entry.entityUid, businessUid, fields)
    }

    private suspend fun boothUid(id: Long?): String? = id?.let { db.boothDao().getByIdOnce(it)?.uid }
    private suspend fun employeeUid(id: Long?): String? = id?.let { db.employeeDao().getByIdOnce(it)?.uid }
    private suspend fun shiftUid(id: Long?): String? = id?.let { db.shiftDao().getByIdOnce(it)?.uid }
    private suspend fun transactionUid(id: Long?): String? = id?.let { db.transactionDao().getByIdOnce(it)?.uid }
}
