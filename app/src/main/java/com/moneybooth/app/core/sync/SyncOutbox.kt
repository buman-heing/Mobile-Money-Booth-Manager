package com.moneybooth.app.core.sync

import com.moneybooth.app.core.data.database.dao.SyncOutboxDao
import com.moneybooth.app.core.data.database.entities.SyncOutboxEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositories call [changed] after every local write. Nothing here touches the network; the
 * queue is drained later by the sync worker whenever the phone has signal.
 */
class SyncOutbox(private val dao: SyncOutboxDao) {
    /** Set by the app container so a change can nudge the sync worker without core depending on WorkManager. */
    var onChanged: (() -> Unit)? = null

    suspend fun changed(type: SyncEntityType, uid: String) {
        dao.enqueue(SyncOutboxEntity(entityType = type, entityUid = uid, createdAt = System.currentTimeMillis()))
        onChanged?.invoke()
    }

    fun observePendingCount(): Flow<Int> = dao.observePendingCount()
}
