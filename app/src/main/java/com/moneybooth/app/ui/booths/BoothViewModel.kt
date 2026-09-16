package com.moneybooth.app.ui.booths

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneybooth.app.core.data.database.entities.BoothEntity
import com.moneybooth.app.core.data.repository.BoothRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class BoothViewModel(private val boothRepository: BoothRepository) : ViewModel() {

    fun observeBooths(businessId: Long): Flow<List<BoothEntity>> = boothRepository.observeByBusiness(businessId)

    fun createBooth(businessId: Long, name: String, location: String?, onDone: () -> Unit) {
        viewModelScope.launch {
            boothRepository.createBooth(businessId, name, location?.ifBlank { null })
            onDone()
        }
    }
}
