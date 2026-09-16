package com.moneybooth.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.moneybooth.app.core.domain.reconciliation.CashMovementDirection
import com.moneybooth.app.core.domain.reconciliation.CashMovementReason

/**
 * An explicit, staff-logged physical-cash movement. Never auto-derived from SMS: mobile-money
 * SMS only ever reports mobile-money balance movements, and guessing which of those also
 * imply a cash exchange would be an unverified business-rule assumption.
 */
@Entity(
    tableName = "cash_movements",
    foreignKeys = [
        ForeignKey(
            entity = ShiftEntity::class,
            parentColumns = ["id"],
            childColumns = ["shiftId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("shiftId"), Index("boothId")],
)
data class CashMovementEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val shiftId: Long,
    val boothId: Long,
    val direction: CashMovementDirection,
    val amountMinor: Long,
    val reason: CashMovementReason,
    val linkedTransactionId: Long? = null,
    val note: String? = null,
    val recordedAt: Long,
    val createdBy: Long,
)
