package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.AuditLogDao
import com.moneybooth.app.core.data.database.entities.AuditLogEntity
import com.moneybooth.app.core.domain.audit.AuditActionType
import kotlinx.coroutines.flow.Flow

class AuditLogRepository(private val auditLogDao: AuditLogDao) {
    fun observeByEntity(entityType: String, entityId: Long): Flow<List<AuditLogEntity>> =
        auditLogDao.observeByEntity(entityType, entityId)

    suspend fun record(
        entityType: String,
        entityId: Long,
        actionType: AuditActionType,
        performedBy: String,
        fieldName: String? = null,
        oldValue: String? = null,
        newValue: String? = null,
        reason: String? = null,
    ) {
        auditLogDao.insert(
            AuditLogEntity(
                entityType = entityType,
                entityId = entityId,
                actionType = actionType,
                fieldName = fieldName,
                oldValue = oldValue,
                newValue = newValue,
                reason = reason,
                performedBy = performedBy,
                timestamp = System.currentTimeMillis(),
            ),
        )
    }
}
