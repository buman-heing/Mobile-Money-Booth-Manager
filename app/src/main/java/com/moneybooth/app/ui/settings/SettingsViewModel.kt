package com.moneybooth.app.ui.settings

import androidx.lifecycle.ViewModel
import com.moneybooth.app.core.data.database.entities.BoothEntity
import com.moneybooth.app.core.data.repository.BoothRepository
import com.moneybooth.app.sms.android.SmsPermissionManager
import kotlinx.coroutines.flow.Flow

class SettingsViewModel(
    private val boothRepository: BoothRepository,
    private val smsPermissionManager: SmsPermissionManager,
) : ViewModel() {
    fun observeBooths(businessId: Long): Flow<List<BoothEntity>> = boothRepository.observeByBusiness(businessId)

    fun hasSmsPermission(): Boolean = smsPermissionManager.hasSmsPermission()
}
