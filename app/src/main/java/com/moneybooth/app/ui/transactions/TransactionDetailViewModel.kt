package com.moneybooth.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneybooth.app.core.data.database.entities.AuditLogEntity
import com.moneybooth.app.core.data.database.entities.RawSmsEntity
import com.moneybooth.app.core.data.database.entities.TransactionEntity
import com.moneybooth.app.core.data.repository.AuditLogRepository
import com.moneybooth.app.core.data.repository.RawSmsRepository
import com.moneybooth.app.core.data.repository.TransactionRepository
import com.moneybooth.app.core.domain.transactions.BusinessClassification
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class TransactionDetailViewModel(
    private val transactionRepository: TransactionRepository,
    private val rawSmsRepository: RawSmsRepository,
    private val auditLogRepository: AuditLogRepository,
) : ViewModel() {

    fun observeTransaction(id: Long): Flow<TransactionEntity?> = transactionRepository.observeById(id)

    fun observeRawSms(id: Long): Flow<RawSmsEntity?> = rawSmsRepository.observeById(id)

    fun observeAuditTrail(transactionId: Long): Flow<List<AuditLogEntity>> =
        auditLogRepository.observeByEntity("transaction", transactionId)

    fun updateClassification(transactionId: Long, classification: BusinessClassification) {
        viewModelScope.launch {
            transactionRepository.updateClassification(
                transactionId = transactionId,
                newClassification = classification,
                performedBy = "owner",
                reason = null,
            )
        }
    }

    fun rejectTransaction(transactionId: Long, reason: String?) {
        viewModelScope.launch {
            transactionRepository.rejectTransaction(transactionId, performedBy = "owner", reason = reason)
        }
    }
}
