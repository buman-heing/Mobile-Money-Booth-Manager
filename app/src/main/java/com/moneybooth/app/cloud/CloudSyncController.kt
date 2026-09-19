package com.moneybooth.app.cloud

import com.google.firebase.firestore.ListenerRegistration
import com.moneybooth.app.core.data.DeviceRole
import com.moneybooth.app.core.data.DeviceSettingsStore
import com.moneybooth.app.core.data.database.dao.SyncOutboxDao
import com.moneybooth.app.core.data.repository.BusinessRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Owns the live cloud listeners for the time the app is on screen. Off screen, the periodic
 * workers keep both directions moving without holding a connection open.
 */
class CloudSyncController(
    private val puller: CloudPuller,
    private val remoteStore: FirestoreRemoteStore,
    private val settings: DeviceSettingsStore,
    private val businessRepository: BusinessRepository,
    private val outboxDao: SyncOutboxDao,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var registrations: List<ListenerRegistration> = emptyList()

    private val _devices = MutableStateFlow<List<DeviceStatus>>(emptyList())
    /** Every phone attached to this business and when it last checked in. */
    val devices: StateFlow<List<DeviceStatus>> = _devices

    /** The business this phone follows: an explicitly joined one, else the one created locally. */
    suspend fun businessUid(): String? = settings.cloudBusinessUid ?: businessRepository.getFirstOnce()?.uid

    fun start() {
        scope.launch {
            val uid = businessUid() ?: return@launch
            stop()
            runCatching { puller.pullOnce(uid) }
            registrations = puller.listen(scope, uid) + puller.listenDevices(uid) { _devices.value = it }
            sendHeartbeat()
        }
    }

    fun stop() {
        registrations.forEach { it.remove() }
        registrations = emptyList()
    }

    /** Employee phones announce themselves; owner phones only listen. */
    suspend fun sendHeartbeat() {
        if (settings.deviceRole != DeviceRole.BOOTH) return
        val uid = businessUid() ?: return
        remoteStore.heartbeat(
            uid,
            settings.deviceId,
            mapOf(
                "role" to settings.deviceRole.name,
                "smsCaptureEnabled" to settings.smsIngestionEnabled,
                "pendingUploads" to outboxDao.pendingCount(),
            ),
        )
    }
}
