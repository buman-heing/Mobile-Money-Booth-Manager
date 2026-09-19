package com.moneybooth.app.ui.dashboard

import androidx.lifecycle.ViewModel
import com.moneybooth.app.core.data.database.entities.ShiftEntity
import com.moneybooth.app.core.data.database.entities.TransactionEntity
import com.moneybooth.app.core.data.repository.BoothRepository
import com.moneybooth.app.core.data.repository.EmployeeRepository
import com.moneybooth.app.core.data.repository.ShiftRepository
import com.moneybooth.app.core.data.repository.TransactionRepository
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import java.util.Calendar

/** Backs both home screens; each picks the pieces it needs. */
class DashboardViewModel(
    private val transactionRepository: TransactionRepository,
    private val shiftRepository: ShiftRepository,
    private val employeeRepository: EmployeeRepository,
    private val boothRepository: BoothRepository,
) : ViewModel() {

    private fun todayRange(): Pair<Long, Long> {
        val start = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        val end = start + 24L * 60 * 60 * 1000 - 1
        return start to end
    }

    fun observeTodayCount(): Flow<Int> {
        val (start, end) = todayRange()
        return transactionRepository.observeCountForDay(null, start, end)
    }

    fun observeTodayCount(direction: TransactionDirection): Flow<Int> {
        val (start, end) = todayRange()
        return transactionRepository.observeCountForDayByDirection(null, direction, start, end)
    }

    fun observeTodaySum(direction: TransactionDirection): Flow<Long> {
        val (start, end) = todayRange()
        return transactionRepository.observeSumForDay(null, direction, start, end)
    }

    fun observeTodayCommission(): Flow<Long> {
        val (start, end) = todayRange()
        return transactionRepository.observeCommissionSumForDay(null, start, end)
    }

    fun observeMobileMoneyBalance(): Flow<Long?> = transactionRepository.observeLatestKnownBalance(null)

    fun observeRecent(limit: Int = 5): Flow<List<TransactionEntity>> =
        transactionRepository.filter().map { it.take(limit) }

    /** Transactions whose reported balance did not match the ledger, newest first. */
    fun observeUnusual(limit: Int = 3): Flow<List<TransactionEntity>> =
        transactionRepository.filter().map { list -> list.filter { (it.discrepancyMinor ?: 0L) != 0L }.take(limit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun observeOpenShift(): Flow<ShiftEntity?> = flow { emit(boothRepository.getDefaultOnce()?.id) }
        .flatMapLatest { boothId -> if (boothId == null) flowOf(null) else shiftRepository.observeOpenShiftForBooth(boothId) }

    fun observeEmployeeName(employeeId: Long): Flow<String?> = employeeRepository.observeById(employeeId).map { it?.name }
}
