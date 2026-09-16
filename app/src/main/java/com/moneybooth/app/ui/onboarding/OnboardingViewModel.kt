package com.moneybooth.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneybooth.app.core.data.repository.BoothRepository
import com.moneybooth.app.core.data.repository.BusinessRepository
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val businessRepository: BusinessRepository,
    private val boothRepository: BoothRepository,
) : ViewModel() {

    fun createBusinessAndFirstBooth(businessName: String, boothName: String, onDone: () -> Unit) {
        viewModelScope.launch {
            val businessId = businessRepository.createBusiness(businessName)
            boothRepository.createBooth(businessId, boothName)
            onDone()
        }
    }
}
