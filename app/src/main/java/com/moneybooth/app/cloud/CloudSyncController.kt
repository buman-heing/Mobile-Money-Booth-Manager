package com.moneybooth.app.cloud

import com.google.firebase.firestore.ListenerRegistration
import com.moneybooth.app.core.data.DeviceSettingsStore
import com.moneybooth.app.core.data.repository.BusinessRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Owns the live cloud listeners for the time the app is on screen. Off screen, the periodic
 * workers keep both directions moving without holding a connection open.
 */
class CloudSyncController(
    private val puller: CloudPuller,
    private val settings: DeviceSettingsStore,
    private val businessRepository: BusinessRepository,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var registrations: List<ListenerRegistration> = emptyList()

    /** The business this phone follows: an explicitly joined one, else the one created locally. */
    suspend fun businessUid(): String? = settings.cloudBusinessUid ?: businessRepository.getFirstOnce()?.uid

    fun start() {
        scope.launch {
            val uid = businessUid() ?: return@launch
            stop()
            runCatching { puller.pullOnce(uid) }
            registrations = puller.listen(scope, uid)
        }
    }

    fun stop() {
        registrations.forEach { it.remove() }
        registrations = emptyList()
    }
}
