package com.moneybooth.app.ui.settings

import androidx.lifecycle.ViewModel
import com.moneybooth.app.sms.android.SmsPermissionManager

class SettingsViewModel(private val smsPermissionManager: SmsPermissionManager) : ViewModel() {
    fun hasSmsPermission(): Boolean = smsPermissionManager.hasSmsPermission()
}
