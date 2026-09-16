package com.moneybooth.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reconciliations",
    foreignKeys = [
        ForeignKey(
            entity = ShiftEntity::class,
            parentColumns = ["id"],
            childColumns = ["shiftId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("shiftId", unique = true), Index("boothId"), Index("employeeId")],
)
data class ReconciliationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val shiftId: Long,
    val boothId: Long,
    val employeeId: Long,
    val date: Long,
    val openingCashMinor: Long,
    val cashInMinor: Long,
    val cashOutMinor: Long,
    val cashAdjustmentsMinor: Long,
    val expectedCashMinor: Long,
    val actualCashMinor: Long,
    val cashDifferenceMinor: Long,
    val openingMobileMoneyBalanceMinor: Long? = null,
    val mobileMoneyInMinor: Long,
    val mobileMoneyOutMinor: Long,
    val expectedMobileMoneyBalanceMinor: Long? = null,
    val latestProviderReportedBalanceMinor: Long? = null,
    val mobileMoneyDifferenceMinor: Long? = null,
    val unclassifiedTransactionCount: Int,
    val closedAt: Long,
    val closedBy: String,
    val notes: String? = null,
    val createdAt: Long,
)
