package com.moneybooth.app.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.moneybooth.app.core.data.database.entities.SyncOutboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncOutboxDao {
    /** Ignoring conflicts keeps the original createdAt/attempts when a pending row changes again. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enqueue(entry: SyncOutboxEntity): Long

    @Query("SELECT * FROM sync_outbox ORDER BY createdAt, id LIMIT :limit")
    suspend fun nextBatch(limit: Int): List<SyncOutboxEntity>

    @Query("DELETE FROM sync_outbox WHERE id IN (:ids)")
    suspend fun delete(ids: List<Long>)

    @Query("UPDATE sync_outbox SET attempts = attempts + 1, lastError = :error WHERE id IN (:ids)")
    suspend fun markFailed(ids: List<Long>, error: String?)

    @Query("SELECT COUNT(*) FROM sync_outbox")
    fun observePendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM sync_outbox")
    suspend fun pendingCount(): Int
}
