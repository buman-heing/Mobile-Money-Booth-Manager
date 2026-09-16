package com.moneybooth.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.moneybooth.app.core.domain.audit.AuditActionType

@Entity(
    tableName = "audit_log",
    indices = [Index("entityType"), Index("entityId"), Index("timestamp")],
)
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val entityType: String,
    val entityId: Long,
    val actionType: AuditActionType,
    val fieldName: String? = null,
    val oldValue: String? = null,
    val newValue: String? = null,
    val reason: String? = null,
    val performedBy: String,
    val timestamp: Long,
)
