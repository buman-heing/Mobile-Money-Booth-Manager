package com.moneybooth.app.cloud

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.moneybooth.app.core.data.DeviceSettingsStore
import com.moneybooth.app.core.sync.ApplyResult
import com.moneybooth.app.core.sync.RemoteApplier
import com.moneybooth.app.core.sync.SyncEntityType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await

/**
 * Brings cloud records down into this phone. Two modes share one apply path:
 *  - [pullOnce]: catch up on everything since the last cursor (used by the background worker
 *    and right after connecting with a join code).
 *  - [listen]: live updates while the app is open, so the owner sees a transaction seconds
 *    after the booth phone uploads it.
 * Parents are pulled before children so uid references can be resolved.
 */
class CloudPuller(
    private val firestore: FirebaseFirestore,
    private val auth: CloudAuth,
    private val applier: RemoteApplier,
    private val settings: DeviceSettingsStore,
) {
    private val applyLock = Mutex()

    suspend fun pullOnce(businessUid: String) {
        auth.ensureSignedIn()
        val businessDoc = firestore.collection("businesses").document(businessUid).get().await()
        if (businessDoc.exists()) {
            applyLock.withLock { applier.apply(SyncEntityType.BUSINESS, businessUid, businessDoc.data.orEmpty()) }
        }
        for (type in PULL_ORDER) {
            var cursor = settings.pullCursor(type)
            while (true) {
                val page = baseQuery(businessUid, type, cursor).limit(PAGE_SIZE).get().await().documents
                if (page.isEmpty()) break
                val lastApplied = applyDocs(type, page) ?: break
                cursor = lastApplied
                settings.setPullCursor(type, cursor)
                if (page.size < PAGE_SIZE) break
            }
        }
    }

    fun listen(scope: CoroutineScope, businessUid: String): List<ListenerRegistration> = PULL_ORDER.map { type ->
        baseQuery(businessUid, type, settings.pullCursor(type)).addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val changed = snapshot.documentChanges.map { it.document }
            if (changed.isEmpty()) return@addSnapshotListener
            scope.launch {
                applyDocs(type, changed)?.let { if (it > settings.pullCursor(type)) settings.setPullCursor(type, it) }
            }
        }
    }

    private fun baseQuery(businessUid: String, type: SyncEntityType, afterMillis: Long): Query =
        firestore.collection("businesses").document(businessUid).collection(type.collection)
            .whereGreaterThan("syncedAt", Timestamp(java.util.Date(afterMillis)))
            .orderBy("syncedAt", Query.Direction.ASCENDING)

    /** Applies in order; returns the syncedAt of the last record applied, or null if nothing could be. */
    private suspend fun applyDocs(type: SyncEntityType, docs: List<DocumentSnapshot>): Long? {
        var last: Long? = null
        applyLock.withLock {
            for (doc in docs) {
                val syncedAt = doc.getTimestamp("syncedAt")?.toDate()?.time ?: continue
                when (applier.apply(type, doc.id, doc.data.orEmpty())) {
                    ApplyResult.Applied, ApplyResult.SkippedLocalNewer -> last = syncedAt
                    is ApplyResult.MissingParent -> return last
                }
            }
        }
        return last
    }

    companion object {
        private const val PAGE_SIZE = 200L
        val PULL_ORDER = listOf(
            SyncEntityType.BOOTH,
            SyncEntityType.EMPLOYEE,
            SyncEntityType.MOBILE_MONEY_ACCOUNT,
            SyncEntityType.SHIFT,
            SyncEntityType.RAW_SMS,
            SyncEntityType.TRANSACTION,
            SyncEntityType.CASH_MOVEMENT,
            SyncEntityType.RECONCILIATION,
            SyncEntityType.AUDIT_LOG,
        )
    }
}
