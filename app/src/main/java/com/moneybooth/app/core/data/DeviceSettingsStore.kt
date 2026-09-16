package com.moneybooth.app.core.data

import android.content.Context

/**
 * Small local, non-sensitive device settings (which booth this device's SMS belong to, whether
 * live SMS ingestion is enabled). Deliberately separate from [com.moneybooth.app.security.PinCredentialStore]
 * (secrets) and the Room database (business data).
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

    companion object {
        private const val PREFS_NAME = "moneybooth_device_settings"
        private const val KEY_ACTIVE_BOOTH_ID = "active_booth_id"
        private const val KEY_SMS_INGESTION_ENABLED = "sms_ingestion_enabled"
    }
}
