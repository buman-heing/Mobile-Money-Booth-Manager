package com.moneybooth.app.core.data.database

import androidx.room.TypeConverter
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

class Converters {
    @TypeConverter
    fun fromEmployeeRole(value: EmployeeRole): String = value.name

    @TypeConverter
    fun toEmployeeRole(value: String): EmployeeRole = EmployeeRole.valueOf(value)

    @TypeConverter
    fun fromShiftStatus(value: ShiftStatus): String = value.name

    @TypeConverter
    fun toShiftStatus(value: String): ShiftStatus = ShiftStatus.valueOf(value)

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String = value.name

    @TypeConverter
    fun toTransactionType(value: String): TransactionType =
        runCatching { TransactionType.valueOf(value) }.getOrDefault(TransactionType.UNKNOWN)

    @TypeConverter
    fun fromTransactionStatus(value: TransactionStatus): String = value.name

    @TypeConverter
    fun toTransactionStatus(value: String): TransactionStatus = TransactionStatus.valueOf(value)

    @TypeConverter
    fun fromTransactionDirection(value: TransactionDirection): String = value.name

    @TypeConverter
    fun toTransactionDirection(value: String): TransactionDirection =
        runCatching { TransactionDirection.valueOf(value) }.getOrDefault(TransactionDirection.UNKNOWN)

    @TypeConverter
    fun fromBusinessClassification(value: BusinessClassification): String = value.name

    @TypeConverter
    fun toBusinessClassification(value: String): BusinessClassification =
        runCatching { BusinessClassification.valueOf(value) }.getOrDefault(BusinessClassification.UNKNOWN)

    @TypeConverter
    fun fromTransactionSource(value: TransactionSource): String = value.name

    @TypeConverter
    fun toTransactionSource(value: String): TransactionSource = TransactionSource.valueOf(value)

    @TypeConverter
    fun fromParsingStatus(value: ParsingStatus): String = value.name

    @TypeConverter
    fun toParsingStatus(value: String): ParsingStatus = ParsingStatus.valueOf(value)

    @TypeConverter
    fun fromCashMovementDirection(value: CashMovementDirection): String = value.name

    @TypeConverter
    fun toCashMovementDirection(value: String): CashMovementDirection = CashMovementDirection.valueOf(value)

    @TypeConverter
    fun fromCashMovementReason(value: CashMovementReason): String = value.name

    @TypeConverter
    fun toCashMovementReason(value: String): CashMovementReason = CashMovementReason.valueOf(value)

    @TypeConverter
    fun fromAuditActionType(value: AuditActionType): String = value.name

    @TypeConverter
    fun toAuditActionType(value: String): AuditActionType = AuditActionType.valueOf(value)
}
