package com.moneybooth.app.core.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.moneybooth.app.core.sync.newUid

@Entity(
    tableName = "booths",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("uid", unique = true), Index("businessId")],
)
data class BoothEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(defaultValue = "") val uid: String = newUid(),
    val businessId: Long,
    val name: String,
    val location: String? = null,
    val active: Boolean = true,
    val createdAt: Long,
    val updatedAt: Long,
)
