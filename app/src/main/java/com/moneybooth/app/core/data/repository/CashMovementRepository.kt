package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.CashMovementDao
import com.moneybooth.app.core.data.database.entities.CashMovementEntity
import com.moneybooth.app.core.domain.reconciliation.CashMovementDirection
import com.moneybooth.app.core.domain.reconciliation.CashMovementReason
import kotlinx.coroutines.flow.Flow

class CashMovementRepository(private val cashMovementDao: CashMovementDao) {
    fun observeByShift(shiftId: Long): Flow<List<CashMovementEntity>> = cashMovementDao.observeByShift(shiftId)

    suspend fun getByShiftOnce(shiftId: Long): List<CashMovementEntity> = cashMovementDao.getByShiftOnce(shiftId)

    suspend fun recordMovement(
        shiftId: Long,
        boothId: Long,
        direction: CashMovementDirection,
        amountMinor: Long,
        reason: CashMovementReason,
        linkedTransactionId: Long?,
        note: String?,
        createdBy: Long,
    ): Long = cashMovementDao.insert(
        CashMovementEntity(
            shiftId = shiftId,
            boothId = boothId,
            direction = direction,
            amountMinor = amountMinor,
            reason = reason,
            linkedTransactionId = linkedTransactionId,
            note = note,
            recordedAt = System.currentTimeMillis(),
            createdBy = createdBy,
        ),
    )
}
