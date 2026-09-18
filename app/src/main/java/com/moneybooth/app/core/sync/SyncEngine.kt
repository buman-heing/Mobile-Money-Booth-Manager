package com.moneybooth.app.core.sync

import com.moneybooth.app.core.data.database.dao.SyncOutboxDao
import com.moneybooth.app.core.data.database.entities.SyncOutboxEntity

sealed interface SyncRunResult {
    data class Uploaded(val count: Int) : SyncRunResult
    data object NothingToDo : SyncRunResult
    data object CloudNotConfigured : SyncRunResult
    data class Failed(val message: String, val remaining: Int) : SyncRunResult
}

/**
 * Drains the outbox in creation order, in batches, until it is empty or the cloud refuses.
 * Pure Kotlin so it can be exercised without Android or WorkManager.
 */
class SyncEngine(
    private val outboxDao: SyncOutboxDao,
    private val remoteStore: RemoteStore,
    private val batchSize: Int = 100,
    private val assemble: suspend (SyncOutboxEntity) -> RemoteRecord?,
) {
    suspend fun runOnce(): SyncRunResult {
        if (!remoteStore.isConfigured) return SyncRunResult.CloudNotConfigured

        var uploaded = 0
        while (true) {
            val batch = outboxDao.nextBatch(batchSize)
            if (batch.isEmpty()) break

            val stale = mutableListOf<Long>()
            val records = mutableListOf<Pair<Long, RemoteRecord>>()
            for (entry in batch) {
                val record = assemble(entry)
                if (record == null) stale += entry.id else records += entry.id to record
            }
            if (stale.isNotEmpty()) outboxDao.delete(stale)
            if (records.isEmpty()) continue

            when (val result = remoteStore.push(records.map { it.second })) {
                PushResult.Success -> {
                    outboxDao.delete(records.map { it.first })
                    uploaded += records.size
                }
                is PushResult.Failure -> {
                    outboxDao.markFailed(records.map { it.first }, result.message)
                    return SyncRunResult.Failed(result.message, outboxDao.pendingCount())
                }
            }
        }
        return if (uploaded == 0) SyncRunResult.NothingToDo else SyncRunResult.Uploaded(uploaded)
    }
}
