package com.moneybooth.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.moneybooth.app.core.domain.employees.ShiftStatus

@Entity(
    tableName = "shifts",
    foreignKeys = [
        ForeignKey(
            entity = BoothEntity::class,
            parentColumns = ["id"],
            childColumns = ["boothId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = EmployeeEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("boothId"), Index("employeeId"), Index("status")],
)
data class ShiftEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val boothId: Long,
    val employeeId: Long,
    val mobileMoneyAccountId: Long? = null,
    val status: ShiftStatus = ShiftStatus.OPEN,
    val openedAt: Long,
    val openingCashMinor: Long,
    val openingMobileMoneyBalanceMinor: Long? = null,
    val closedAt: Long? = null,
    val notes: String? = null,
    val confirmed: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long,
)
