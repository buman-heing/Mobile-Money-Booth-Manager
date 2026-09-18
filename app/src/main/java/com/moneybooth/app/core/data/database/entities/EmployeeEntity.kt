package com.moneybooth.app.core.data.database.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.moneybooth.app.core.sync.newUid
import com.moneybooth.app.core.domain.employees.EmployeeRole

@Entity(
    tableName = "employees",
    foreignKeys = [
        ForeignKey(
            entity = BusinessEntity::class,
            parentColumns = ["id"],
            childColumns = ["businessId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = BoothEntity::class,
            parentColumns = ["id"],
            childColumns = ["assignedBoothId"],
            onDelete = ForeignKey.SET_NULL,
        ),
    ],
    indices = [Index("uid", unique = true), Index("businessId"), Index("assignedBoothId")],
)
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(defaultValue = "") val uid: String = newUid(),
    val businessId: Long,
    val name: String,
    val phone: String? = null,
    val role: EmployeeRole = EmployeeRole.ATTENDANT,
    val active: Boolean = true,
    val assignedBoothId: Long? = null,
    val createdAt: Long,
    val updatedAt: Long,
)
