package com.moneybooth.app.core.data

import android.content.Context
import com.moneybooth.app.core.sync.SyncEntityType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** What this particular phone is for. Decides which screens show and whether SMS capture runs. */
enum class DeviceRole { UNSET, BOOTH, OWNER }

/**
 * Small local, non-sensitive device settings (which booth this device's SMS belong to, whether
 * live SMS ingestion is enabled, sync bookkeeping). Deliberately separate from
 * [com.moneybooth.app.security.PinCredentialStore] (secrets) and the Room database (business data).
 */
class DeviceSettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var activeBoothId: Long?
        get() = prefs.getLong(KEY_ACTIVE_BOOTH_ID, -1L).takeIf { it != -1L }
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_ACTIVE_BOOTH_ID) else putLong(KEY_ACTIVE_BOOTH_ID, value)
            }.apply()
        }

    var smsIngestionEnabled: Boolean
        get() = prefs.getBoolean(KEY_SMS_INGESTION_ENABLED, false)
        set(value) = prefs.edit().putBoolean(KEY_SMS_INGESTION_ENABLED, value).apply()

    var deviceRole: DeviceRole
        get() = prefs.getString(KEY_DEVICE_ROLE, null)?.let { runCatching { DeviceRole.valueOf(it) }.getOrNull() } ?: DeviceRole.UNSET
        set(value) {
            prefs.edit().putString(KEY_DEVICE_ROLE, value.name).apply()
            _deviceRoleFlow.value = value
        }

    private val _deviceRoleFlow = MutableStateFlow(deviceRole)
    val deviceRoleFlow: StateFlow<DeviceRole> = _deviceRoleFlow

    var lastSyncAt: Long?
        get() = prefs.getLong(KEY_LAST_SYNC_AT, -1L).takeIf { it != -1L }
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_LAST_SYNC_AT) else putLong(KEY_LAST_SYNC_AT, value)
            }.apply()
            _syncStatusFlow.value = syncStatus()
        }

    var lastSyncError: String?
        get() = prefs.getString(KEY_LAST_SYNC_ERROR, null)
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_LAST_SYNC_ERROR) else putString(KEY_LAST_SYNC_ERROR, value)
            }.apply()
            _syncStatusFlow.value = syncStatus()
        }

    var lastPullAt: Long?
        get() = prefs.getLong(KEY_LAST_PULL_AT, -1L).takeIf { it != -1L }
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_LAST_PULL_AT) else putLong(KEY_LAST_PULL_AT, value)
            }.apply()
            _syncStatusFlow.value = syncStatus()
        }

    /** Stable id for this installation, so the owner can tell one employee phone from another. */
    val deviceId: String
        get() = prefs.getString(KEY_DEVICE_ID, null) ?: java.util.UUID.randomUUID().toString().also {
            prefs.edit().putString(KEY_DEVICE_ID, it).apply()
        }

    /** The business this phone follows in the cloud (set when an owner phone joins with a code). */
    var cloudBusinessUid: String?
        get() = prefs.getString(KEY_CLOUD_BUSINESS_UID, null)
        set(value) {
            prefs.edit().apply {
                if (value == null) remove(KEY_CLOUD_BUSINESS_UID) else putString(KEY_CLOUD_BUSINESS_UID, value)
            }.apply()
        }

    fun pullCursor(type: SyncEntityType): Long = prefs.getLong(KEY_PULL_CURSOR_PREFIX + type.name, 0L)

    fun setPullCursor(type: SyncEntityType, syncedAtMillis: Long) {
        prefs.edit().putLong(KEY_PULL_CURSOR_PREFIX + type.name, syncedAtMillis).apply()
    }

    private fun syncStatus() = SyncStatus(lastSyncAt, lastSyncError, lastPullAt)
    private val _syncStatusFlow = MutableStateFlow(syncStatus())
    val syncStatusFlow: StateFlow<SyncStatus> = _syncStatusFlow

    companion object {
        private const val PREFS_NAME = "moneybooth_device_settings"
        private const val KEY_ACTIVE_BOOTH_ID = "active_booth_id"
        private const val KEY_SMS_INGESTION_ENABLED = "sms_ingestion_enabled"
        private const val KEY_DEVICE_ROLE = "device_role"
        private const val KEY_LAST_SYNC_AT = "last_sync_at"
        private const val KEY_LAST_SYNC_ERROR = "last_sync_error"
        private const val KEY_LAST_PULL_AT = "last_pull_at"
        private const val KEY_CLOUD_BUSINESS_UID = "cloud_business_uid"
        private const val KEY_DEVICE_ID = "device_id"
        private const val KEY_PULL_CURSOR_PREFIX = "pull_cursor_"
    }
}

data class SyncStatus(val lastSyncAt: Long?, val lastError: String?, val lastPullAt: Long?)
