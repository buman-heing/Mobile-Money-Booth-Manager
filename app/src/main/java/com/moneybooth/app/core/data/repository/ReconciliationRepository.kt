package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.ReconciliationDao
import com.moneybooth.app.core.data.database.entities.ReconciliationEntity
import com.moneybooth.app.core.sync.SyncEntityType
import com.moneybooth.app.core.sync.SyncOutbox
import kotlinx.coroutines.flow.Flow

class ReconciliationRepository(private val reconciliationDao: ReconciliationDao, private val outbox: SyncOutbox) {
    fun observeByShift(shiftId: Long): Flow<ReconciliationEntity?> = reconciliationDao.observeByShift(shiftId)

    fun observeByBooth(boothId: Long): Flow<List<ReconciliationEntity>> = reconciliationDao.observeByBooth(boothId)

    suspend fun insert(reconciliation: ReconciliationEntity): Long {
        val id = reconciliationDao.insert(reconciliation)
        outbox.changed(SyncEntityType.RECONCILIATION, reconciliation.uid)
        return id
    }
}
