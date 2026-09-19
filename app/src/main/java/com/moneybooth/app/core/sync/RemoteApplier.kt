package com.moneybooth.app.core.sync

import com.moneybooth.app.core.data.database.AppDatabase
import com.moneybooth.app.core.data.database.entities.AuditLogEntity
import com.moneybooth.app.core.data.database.entities.BoothEntity
import com.moneybooth.app.core.data.database.entities.BusinessEntity
import com.moneybooth.app.core.data.database.entities.CashMovementEntity
import com.moneybooth.app.core.data.database.entities.EmployeeEntity
import com.moneybooth.app.core.data.database.entities.MobileMoneyAccountEntity
import com.moneybooth.app.core.data.database.entities.RawSmsEntity
import com.moneybooth.app.core.data.database.entities.ReconciliationEntity
import com.moneybooth.app.core.data.database.entities.ShiftEntity
import com.moneybooth.app.core.data.database.entities.TransactionEntity
import com.moneybooth.app.core.domain.audit.AuditActionType
import com.moneybooth.app.core.domain.employees.EmployeeRole
import com.moneybooth.app.core.domain.employees.ShiftStatus
import com.moneybooth.app.core.domain.reconciliation.CashMovementDirection
import com.moneybooth.app.core.domain.reconciliation.CashMovementReason
import com.moneybooth.app.core.domain.transactions.BusinessClassification
import com.moneybooth.app.core.domain.transactions.ParsingStatus
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionSource
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType

sealed interface ApplyResult {
    data object Applied : ApplyResult
    data object SkippedLocalNewer : ApplyResult
    /** A referenced parent row hasn't arrived yet; the caller should retry this record later. */
    data class MissingParent(val what: String) : ApplyResult
}

/**
 * Writes a cloud record into the local database, matching rows by uid and translating uid
 * references back into this phone's local ids. Goes through the DAOs directly (not the
 * repositories) so applying a record never re-queues it for upload.
 */
class RemoteApplier(private val db: AppDatabase) {
    /** Fires once per flagged transaction the first time this phone learns of it. */
    var onDiscrepancyArrived: ((TransactionEntity) -> Unit)? = null

    suspend fun apply(type: SyncEntityType, uid: String, f: Map<String, Any?>): ApplyResult {
        return when (type) {
        SyncEntityType.BUSINESS -> {
            val existing = db.businessDao().getByUid(uid)
            val entity = BusinessEntity(
                id = existing?.id ?: 0L,
                uid = uid,
                name = f.str("name") ?: existing?.name ?: "Business",
                defaultCurrency = f.str("defaultCurrency") ?: "ZMW",
                createdAt = f.lng("createdAt") ?: existing?.createdAt ?: 0L,
            )
            if (existing == null) db.businessDao().insert(entity) else db.businessDao().update(entity)
            ApplyResult.Applied
        }

        SyncEntityType.BOOTH -> {
            val businessId = db.businessDao().getByUid(f.str("businessUid") ?: "")?.id
                ?: return ApplyResult.MissingParent("business")
            val existing = db.boothDao().getByUid(uid)
            if (existing != null && existing.updatedAt > (f.lng("updatedAt") ?: 0L)) return ApplyResult.SkippedLocalNewer
            val entity = BoothEntity(
                id = existing?.id ?: 0L,
                uid = uid,
                businessId = businessId,
                name = f.str("name") ?: "Booth",
                location = f.str("location"),
                active = f.bool("active") ?: true,
                createdAt = f.lng("createdAt") ?: 0L,
                updatedAt = f.lng("updatedAt") ?: 0L,
            )
            if (existing == null) db.boothDao().insert(entity) else db.boothDao().update(entity)
            ApplyResult.Applied
        }

        SyncEntityType.EMPLOYEE -> {
            val businessId = db.businessDao().getByUid(f.str("businessUid") ?: "")?.id
                ?: return ApplyResult.MissingParent("business")
            val assignedBoothId = f.str("assignedBoothUid")?.let { db.boothDao().getByUid(it)?.id }
            val existing = db.employeeDao().getByUid(uid)
            if (existing != null && existing.updatedAt > (f.lng("updatedAt") ?: 0L)) return ApplyResult.SkippedLocalNewer
            val entity = EmployeeEntity(
                id = existing?.id ?: 0L,
                uid = uid,
                businessId = businessId,
                name = f.str("name") ?: "Employee",
                phone = f.str("phone"),
                role = f.enum("role", EmployeeRole.ATTENDANT),
                active = f.bool("active") ?: true,
                assignedBoothId = assignedBoothId,
                createdAt = f.lng("createdAt") ?: 0L,
                updatedAt = f.lng("updatedAt") ?: 0L,
            )
            if (existing == null) db.employeeDao().insert(entity) else db.employeeDao().update(entity)
            ApplyResult.Applied
        }

        SyncEntityType.MOBILE_MONEY_ACCOUNT -> {
            val boothId = db.boothDao().getByUid(f.str("boothUid") ?: "")?.id ?: return ApplyResult.MissingParent("booth")
            val existing = db.mobileMoneyAccountDao().getByUid(uid)
            val entity = MobileMoneyAccountEntity(
                id = existing?.id ?: 0L,
                uid = uid,
                boothId = boothId,
                providerId = f.str("providerId") ?: "AIRTEL",
                label = f.str("label") ?: "",
                phoneNumber = f.str("phoneNumber"),
                active = f.bool("active") ?: true,
                createdAt = f.lng("createdAt") ?: 0L,
            )
            if (existing == null) db.mobileMoneyAccountDao().insert(entity) else db.mobileMoneyAccountDao().update(entity)
            ApplyResult.Applied
        }

        SyncEntityType.SHIFT -> {
            val boothId = db.boothDao().getByUid(f.str("boothUid") ?: "")?.id ?: return ApplyResult.MissingParent("booth")
            val employeeId = db.employeeDao().getByUid(f.str("employeeUid") ?: "")?.id
                ?: return ApplyResult.MissingParent("employee")
            val existing = db.shiftDao().getByUid(uid)
            if (existing != null && existing.updatedAt > (f.lng("updatedAt") ?: 0L)) return ApplyResult.SkippedLocalNewer
            val entity = ShiftEntity(
                id = existing?.id ?: 0L,
                uid = uid,
                boothId = boothId,
                employeeId = employeeId,
                mobileMoneyAccountId = f.str("mobileMoneyAccountUid")?.let { db.mobileMoneyAccountDao().getByUid(it)?.id },
                status = f.enum("status", ShiftStatus.OPEN),
                openedAt = f.lng("openedAt") ?: 0L,
                openingCashMinor = f.lng("openingCashMinor") ?: 0L,
                openingMobileMoneyBalanceMinor = f.lng("openingMobileMoneyBalanceMinor"),
                closedAt = f.lng("closedAt"),
                notes = f.str("notes"),
                confirmed = f.bool("confirmed") ?: false,
                createdAt = f.lng("createdAt") ?: 0L,
                updatedAt = f.lng("updatedAt") ?: 0L,
            )
            if (existing == null) db.shiftDao().insert(entity) else db.shiftDao().update(entity)
            ApplyResult.Applied
        }

        SyncEntityType.RAW_SMS -> {
            val existing = db.rawSmsDao().getByUid(uid)
            val entity = RawSmsEntity(
                id = existing?.id ?: 0L,
                uid = uid,
                sender = f.str("sender") ?: "",
                receivedTimestamp = f.lng("receivedTimestamp") ?: 0L,
                rawBody = f.str("rawBody") ?: "",
                fingerprint = f.str("fingerprint") ?: "",
                detectedProviderId = f.str("detectedProviderId"),
                parserVersion = f.str("parserVersion"),
                parsingStatus = f.enum("parsingStatus", ParsingStatus.UNPARSED),
                parsedTransactionId = f.str("parsedTransactionUid")?.let { db.transactionDao().getByUid(it)?.id },
                createdAt = f.lng("createdAt") ?: 0L,
            )
            if (existing == null) db.rawSmsDao().insert(entity) else db.rawSmsDao().update(entity)
            ApplyResult.Applied
        }

        SyncEntityType.TRANSACTION -> {
            val existing = db.transactionDao().getByUid(uid)
                ?: f.str("dedupKey")?.let { db.transactionDao().getByDedupKey(it) }
            if (existing != null && existing.updatedAt > (f.lng("updatedAt") ?: 0L)) return ApplyResult.SkippedLocalNewer
            val entity = TransactionEntity(
                id = existing?.id ?: 0L,
                uid = uid,
                providerId = f.str("providerId") ?: "AIRTEL",
                externalTransactionId = f.str("externalTransactionId"),
                transactionType = f.enum("transactionType", TransactionType.UNKNOWN),
                status = f.enum("status", TransactionStatus.PARSED),
                direction = f.enum("direction", TransactionDirection.UNKNOWN),
                amountMinor = f.lng("amountMinor"),
                currency = f.str("currency") ?: "ZMW",
                feeMinor = f.lng("feeMinor"),
                commissionMinor = f.lng("commissionMinor"),
                senderName = f.str("senderName"),
                senderPhone = f.str("senderPhone"),
                recipientName = f.str("recipientName"),
                recipientPhone = f.str("recipientPhone"),
                merchantTillNumber = f.str("merchantTillNumber"),
                merchantName = f.str("merchantName"),
                serviceName = f.str("serviceName"),
                balanceBeforeMinor = f.lng("balanceBeforeMinor"),
                balanceAfterMinor = f.lng("balanceAfterMinor"),
                discrepancyMinor = f.lng("discrepancyMinor"),
                transactionTimestamp = f.lng("transactionTimestamp"),
                smsReceivedTimestamp = f.lng("smsReceivedTimestamp") ?: 0L,
                employeeId = f.str("employeeUid")?.let { db.employeeDao().getByUid(it)?.id },
                boothId = f.str("boothUid")?.let { db.boothDao().getByUid(it)?.id },
                shiftId = f.str("shiftUid")?.let { db.shiftDao().getByUid(it)?.id },
                businessClassification = f.enum("businessClassification", BusinessClassification.UNKNOWN),
                rawSmsId = f.str("rawSmsUid")?.let { db.rawSmsDao().getByUid(it)?.id },
                dedupKey = f.str("dedupKey") ?: uid,
                parserVersion = f.str("parserVersion") ?: "",
                source = f.enum("source", TransactionSource.SMS_AUTO),
                createdAt = f.lng("createdAt") ?: 0L,
                updatedAt = f.lng("updatedAt") ?: 0L,
            )
            if (existing == null) db.transactionDao().insert(entity) else db.transactionDao().update(entity)
            val flagged = (entity.discrepancyMinor ?: 0L) != 0L
            val alreadyKnown = (existing?.discrepancyMinor ?: 0L) != 0L
            if (flagged && !alreadyKnown) onDiscrepancyArrived?.invoke(entity)
            ApplyResult.Applied
        }

        SyncEntityType.CASH_MOVEMENT -> {
            val shiftId = db.shiftDao().getByUid(f.str("shiftUid") ?: "")?.id ?: return ApplyResult.MissingParent("shift")
            val boothId = db.boothDao().getByUid(f.str("boothUid") ?: "")?.id ?: return ApplyResult.MissingParent("booth")
            val createdBy = f.str("createdByEmployeeUid")?.let { db.employeeDao().getByUid(it)?.id } ?: 0L
            val existing = db.cashMovementDao().getByUid(uid)
            if (existing != null) return ApplyResult.Applied
            db.cashMovementDao().insert(
                CashMovementEntity(
                    uid = uid,
                    shiftId = shiftId,
                    boothId = boothId,
                    direction = f.enum("direction", CashMovementDirection.IN),
                    amountMinor = f.lng("amountMinor") ?: 0L,
                    reason = f.enum("reason", CashMovementReason.OTHER),
                    linkedTransactionId = f.str("linkedTransactionUid")?.let { db.transactionDao().getByUid(it)?.id },
                    note = f.str("note"),
                    recordedAt = f.lng("recordedAt") ?: 0L,
                    createdBy = createdBy,
                ),
            )
            ApplyResult.Applied
        }

        SyncEntityType.RECONCILIATION -> {
            val shiftId = db.shiftDao().getByUid(f.str("shiftUid") ?: "")?.id ?: return ApplyResult.MissingParent("shift")
            val boothId = db.boothDao().getByUid(f.str("boothUid") ?: "")?.id ?: return ApplyResult.MissingParent("booth")
            val employeeId = db.employeeDao().getByUid(f.str("employeeUid") ?: "")?.id
                ?: return ApplyResult.MissingParent("employee")
            if (db.reconciliationDao().getByUid(uid) != null) return ApplyResult.Applied
            db.reconciliationDao().insert(
                ReconciliationEntity(
                    uid = uid,
                    shiftId = shiftId,
                    boothId = boothId,
                    employeeId = employeeId,
                    date = f.lng("date") ?: 0L,
                    openingCashMinor = f.lng("openingCashMinor") ?: 0L,
                    cashInMinor = f.lng("cashInMinor") ?: 0L,
                    cashOutMinor = f.lng("cashOutMinor") ?: 0L,
                    cashAdjustmentsMinor = f.lng("cashAdjustmentsMinor") ?: 0L,
                    expectedCashMinor = f.lng("expectedCashMinor") ?: 0L,
                    actualCashMinor = f.lng("actualCashMinor") ?: 0L,
                    cashDifferenceMinor = f.lng("cashDifferenceMinor") ?: 0L,
                    openingMobileMoneyBalanceMinor = f.lng("openingMobileMoneyBalanceMinor"),
                    mobileMoneyInMinor = f.lng("mobileMoneyInMinor") ?: 0L,
                    mobileMoneyOutMinor = f.lng("mobileMoneyOutMinor") ?: 0L,
                    expectedMobileMoneyBalanceMinor = f.lng("expectedMobileMoneyBalanceMinor"),
                    latestProviderReportedBalanceMinor = f.lng("latestProviderReportedBalanceMinor"),
                    mobileMoneyDifferenceMinor = f.lng("mobileMoneyDifferenceMinor"),
                    unclassifiedTransactionCount = f.lng("unclassifiedTransactionCount")?.toInt() ?: 0,
                    closedAt = f.lng("closedAt") ?: 0L,
                    closedBy = f.str("closedBy") ?: "",
                    notes = f.str("notes"),
                    createdAt = f.lng("createdAt") ?: 0L,
                ),
            )
            ApplyResult.Applied
        }

        SyncEntityType.AUDIT_LOG -> {
            if (db.auditLogDao().getByUid(uid) != null) return ApplyResult.Applied
            val entityType = f.str("entityType") ?: "transaction"
            val entityId = f.str("entityUid")?.let { db.transactionDao().getByUid(it)?.id }
                ?: return ApplyResult.MissingParent(entityType)
            db.auditLogDao().insert(
                AuditLogEntity(
                    uid = uid,
                    entityType = entityType,
                    entityId = entityId,
                    actionType = f.enum("actionType", AuditActionType.CLASSIFICATION_CHANGE),
                    fieldName = f.str("fieldName"),
                    oldValue = f.str("oldValue"),
                    newValue = f.str("newValue"),
                    reason = f.str("reason"),
                    performedBy = f.str("performedBy") ?: "",
                    timestamp = f.lng("timestamp") ?: 0L,
                ),
            )
            ApplyResult.Applied
        }
        }
    }

    private fun Map<String, Any?>.str(key: String): String? = this[key] as? String
    private fun Map<String, Any?>.bool(key: String): Boolean? = this[key] as? Boolean
    private fun Map<String, Any?>.lng(key: String): Long? = (this[key] as? Number)?.toLong()
    private inline fun <reified E : Enum<E>> Map<String, Any?>.enum(key: String, default: E): E =
        str(key)?.let { name -> enumValues<E>().firstOrNull { it.name == name } } ?: default
}
