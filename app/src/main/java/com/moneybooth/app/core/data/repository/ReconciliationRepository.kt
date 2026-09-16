package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.ReconciliationDao
import com.moneybooth.app.core.data.database.entities.ReconciliationEntity
import kotlinx.coroutines.flow.Flow

class ReconciliationRepository(private val reconciliationDao: ReconciliationDao) {
    fun observeByShift(shiftId: Long): Flow<ReconciliationEntity?> = reconciliationDao.observeByShift(shiftId)

    fun observeByBooth(boothId: Long): Flow<List<ReconciliationEntity>> = reconciliationDao.observeByBooth(boothId)

    suspend fun insert(reconciliation: ReconciliationEntity): Long = reconciliationDao.insert(reconciliation)
}
