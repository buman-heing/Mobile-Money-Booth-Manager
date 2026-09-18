package com.moneybooth.app.core.sync

/** One row as the cloud sees it: no local ids, only uids, so any device can read it. */
data class RemoteRecord(
    val type: SyncEntityType,
    val uid: String,
    val businessUid: String,
    val fields: Map<String, Any?>,
)

sealed interface PushResult {
    data object Success : PushResult
    data class Failure(val message: String) : PushResult
}

/** The cloud side of sync. The app never talks to a backend anywhere else. */
interface RemoteStore {
    val isConfigured: Boolean
    suspend fun push(records: List<RemoteRecord>): PushResult
}

/** Stands in until a real backend is wired up: uploads are kept queued, nothing is lost. */
object UnconfiguredRemoteStore : RemoteStore {
    override val isConfigured: Boolean = false
    override suspend fun push(records: List<RemoteRecord>): PushResult = PushResult.Failure("Cloud not set up yet")
}
