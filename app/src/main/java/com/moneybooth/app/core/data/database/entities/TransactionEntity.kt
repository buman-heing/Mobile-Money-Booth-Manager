package com.moneybooth.app.core.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.moneybooth.app.core.sync.newUid
import com.moneybooth.app.core.domain.transactions.BusinessClassification
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionSource
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType

/**
 * The normalized ledger entry. No foreign-key constraints on employeeId/boothId/shiftId/rawSmsId:
 * attribution is often unknown at insert time and is corrected later (see AttributionService),
 * and enforcing FKs here would fight that workflow. Referential integrity for these is a
 * repository-level responsibility.
 */
@Entity(
    tableName = "transactions",
    indices = [
        Index("uid", unique = true),
        Index("dedupKey", unique = true),
        Index("rawSmsId"),
        Index("boothId"),
        Index("employeeId"),
        Index("shiftId"),
        Index("status"),
        Index("businessClassification"),
        Index("transactionTimestamp"),
        Index("externalTransactionId"),
    ],
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(defaultValue = "") val uid: String = newUid(),
    val providerId: String,
    val externalTransactionId: String? = null,
    val transactionType: TransactionType,
    val status: TransactionStatus,
    val direction: TransactionDirection,
    val amountMinor: Long? = null,
    val currency: String = "ZMW",
    val feeMinor: Long? = null,
    val commissionMinor: Long? = null,
    val senderName: String? = null,
    val senderPhone: String? = null,
    val recipientName: String? = null,
    val recipientPhone: String? = null,
    val merchantTillNumber: String? = null,
    val merchantName: String? = null,
    val serviceName: String? = null,
    val balanceBeforeMinor: Long? = null,
    val balanceAfterMinor: Long? = null,
    /** Reported balance minus what the ledger expected; non-zero means activity this phone never saw. */
    val discrepancyMinor: Long? = null,
    val transactionTimestamp: Long? = null,
    val smsReceivedTimestamp: Long,
    val employeeId: Long? = null,
    val boothId: Long? = null,
    val shiftId: Long? = null,
    val businessClassification: BusinessClassification = BusinessClassification.UNKNOWN,
    val rawSmsId: Long? = null,
    val dedupKey: String,
    val parserVersion: String,
    val source: TransactionSource,
    val createdAt: Long,
    val updatedAt: Long,
)
