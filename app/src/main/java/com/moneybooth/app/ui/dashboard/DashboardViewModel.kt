package com.moneybooth.app.ui.dashboard

import androidx.lifecycle.ViewModel
import com.moneybooth.app.core.data.repository.TransactionRepository
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class DashboardViewModel(private val transactionRepository: TransactionRepository) : ViewModel() {

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

    fun observeTodayCount(boothId: Long?): Flow<Int> {
        val (start, end) = todayRange()
        return transactionRepository.observeCountForDay(boothId, start, end)
    }

    fun observeTodayMoneyIn(boothId: Long?): Flow<Long> {
        val (start, end) = todayRange()
        return transactionRepository.observeSumForDay(boothId, TransactionDirection.IN, start, end)
    }

    fun observeTodayMoneyOut(boothId: Long?): Flow<Long> {
        val (start, end) = todayRange()
        return transactionRepository.observeSumForDay(boothId, TransactionDirection.OUT, start, end)
    }

    fun observeMobileMoneyBalance(boothId: Long?): Flow<Long?> = transactionRepository.observeLatestKnownBalance(boothId)
}
