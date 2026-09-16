package com.moneybooth.app.sms.android

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

/**
 * The SMS layer is optional and isolated: every other part of the app (manual entry, the
 * developer paste screen) works with zero SMS permission granted and regardless of how the
 * APK was installed. This class only ever gates the live [SmsReceivedReceiver] path.
 */
class SmsPermissionManager(private val context: Context) {
    fun hasSmsPermission(): Boolean = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.RECEIVE_SMS, Manifest.permission.READ_SMS)
    }
}
