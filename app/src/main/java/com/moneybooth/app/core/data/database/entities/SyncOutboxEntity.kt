package com.moneybooth.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.moneybooth.app.core.sync.SyncEntityType

/**
 * "This row changed and the cloud hasn't seen it yet." One entry per (type, uid): repeated edits
 * before an upload collapse into a single entry, and the upload always sends the row's latest
 * state, so a booth phone that is offline for days uploads only what matters once signal returns.
 */
@Entity(
    tableName = "sync_outbox",
    indices = [Index(value = ["entityType", "entityUid"], unique = true), Index("createdAt")],
)
data class SyncOutboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val entityType: SyncEntityType,
    val entityUid: String,
    val createdAt: Long,
    val attempts: Int = 0,
    val lastError: String? = null,
)
