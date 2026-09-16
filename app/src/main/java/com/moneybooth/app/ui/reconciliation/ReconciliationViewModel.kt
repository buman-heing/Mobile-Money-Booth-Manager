package com.moneybooth.app.ui.reconciliation

import androidx.lifecycle.ViewModel
import com.moneybooth.app.core.data.database.entities.ReconciliationEntity
import com.moneybooth.app.core.data.repository.ReconciliationRepository
import kotlinx.coroutines.flow.Flow

class ReconciliationViewModel(private val reconciliationRepository: ReconciliationRepository) : ViewModel() {
    fun observeByShift(shiftId: Long): Flow<ReconciliationEntity?> = reconciliationRepository.observeByShift(shiftId)
}
