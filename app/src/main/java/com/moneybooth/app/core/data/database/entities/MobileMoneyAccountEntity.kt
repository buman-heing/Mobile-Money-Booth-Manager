package com.moneybooth.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A provider account (e.g. an Airtel Money line) tied to a booth. Modeled now, even though
 * M1-M4 UI only ever surfaces one active account per booth, so multi-provider / multi-account
 * support needs no schema rework later.
 */
@Entity(
    tableName = "mobile_money_accounts",
    foreignKeys = [
        ForeignKey(
            entity = BoothEntity::class,
            parentColumns = ["id"],
            childColumns = ["boothId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("boothId")],
)
data class MobileMoneyAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val boothId: Long,
    val providerId: String,
    val label: String,
    val phoneNumber: String? = null,
    val active: Boolean = true,
    val createdAt: Long,
)
