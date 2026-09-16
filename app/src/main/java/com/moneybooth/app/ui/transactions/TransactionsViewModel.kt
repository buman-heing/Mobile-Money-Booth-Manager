package com.moneybooth.app.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneybooth.app.core.data.database.entities.BoothEntity
import com.moneybooth.app.core.data.database.entities.TransactionEntity
import com.moneybooth.app.core.data.repository.BoothRepository
import com.moneybooth.app.core.data.repository.TransactionRepository
import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.BusinessClassification
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionSource
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import java.util.UUID

class TransactionsViewModel(
    private val transactionRepository: TransactionRepository,
    private val boothRepository: BoothRepository,
) : ViewModel() {

    private val statusFilter = MutableStateFlow<TransactionStatus?>(null)
    private val searchQuery = MutableStateFlow<String?>(null)

    val currentStatusFilter: StateFlow<TransactionStatus?> = statusFilter

    @OptIn(ExperimentalCoroutinesApi::class)
    val transactions: Flow<List<TransactionEntity>> =
        combine(statusFilter, searchQuery) { status, search -> status to search }
            .flatMapLatest { (status, search) -> transactionRepository.filter(status = status, search = search) }

    fun setStatusFilter(status: TransactionStatus?) {
        statusFilter.value = status
    }

    fun setSearch(query: String) {
        searchQuery.value = query.ifBlank { null }
    }

    fun observeBooths(businessId: Long): Flow<List<BoothEntity>> = boothRepository.observeByBusiness(businessId)

    fun createManual(
        type: TransactionType,
        direction: TransactionDirection,
        amountMinor: Long,
        boothId: Long,
        classification: BusinessClassification,
        note: String?,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            transactionRepository.createManualTransaction(
                providerId = "MANUAL",
                transactionType = type,
                direction = direction,
                amountMinor = amountMinor,
                currency = Money.DEFAULT_CURRENCY,
                boothId = boothId,
                employeeId = null,
                shiftId = null,
                classification = classification,
                note = note,
                dedupKey = "MANUAL:${UUID.randomUUID()}",
                performedBy = "owner",
            )
            onDone()
        }
    }
}
