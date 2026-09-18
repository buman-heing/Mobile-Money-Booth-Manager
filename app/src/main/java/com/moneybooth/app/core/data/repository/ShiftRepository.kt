package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.ShiftDao
import com.moneybooth.app.core.data.database.entities.ShiftEntity
import com.moneybooth.app.core.domain.employees.ShiftOpenCheck
import com.moneybooth.app.core.domain.employees.ShiftRules
import com.moneybooth.app.core.domain.employees.ShiftStatus
import com.moneybooth.app.core.sync.SyncEntityType
import com.moneybooth.app.core.sync.SyncOutbox
import kotlinx.coroutines.flow.Flow

sealed interface OpenShiftResult {
    data class Success(val shiftId: Long) : OpenShiftResult
    data class AlreadyOpenForBooth(val existingShift: ShiftEntity) : OpenShiftResult
    data class AlreadyOpenForEmployee(val existingShift: ShiftEntity) : OpenShiftResult
}

class ShiftRepository(private val shiftDao: ShiftDao, private val outbox: SyncOutbox) {
    fun observeById(id: Long): Flow<ShiftEntity?> = shiftDao.observeById(id)

    suspend fun getByIdOnce(id: Long): ShiftEntity? = shiftDao.getByIdOnce(id)

    fun observeOpenShiftForBooth(boothId: Long): Flow<ShiftEntity?> = shiftDao.observeOpenShiftForBooth(boothId)

    suspend fun getOpenShiftForBooth(boothId: Long): ShiftEntity? = shiftDao.getOpenShiftForBooth(boothId)

    suspend fun getOpenShiftForEmployee(employeeId: Long): ShiftEntity? = shiftDao.getOpenShiftForEmployee(employeeId)

    fun observeByBooth(boothId: Long): Flow<List<ShiftEntity>> = shiftDao.observeByBooth(boothId)

    fun observeByEmployee(employeeId: Long): Flow<List<ShiftEntity>> = shiftDao.observeByEmployee(employeeId)

    /** Enforces at most one OPEN shift per booth and per employee at a time (see [ShiftRules]). */
    suspend fun openShift(
        boothId: Long,
        employeeId: Long,
        mobileMoneyAccountId: Long?,
        openingCashMinor: Long,
        openingMobileMoneyBalanceMinor: Long?,
    ): OpenShiftResult {
        val openForBooth = shiftDao.getOpenShiftForBooth(boothId)
        val openForEmployee = shiftDao.getOpenShiftForEmployee(employeeId)
        when (val check = ShiftRules.canOpenShift(openForBooth, openForEmployee)) {
            is ShiftOpenCheck.BoothAlreadyHasOpenShift -> return OpenShiftResult.AlreadyOpenForBooth(check.existing)
            is ShiftOpenCheck.EmployeeAlreadyHasOpenShift -> return OpenShiftResult.AlreadyOpenForEmployee(check.existing)
            ShiftOpenCheck.Allowed -> Unit
        }

        val now = System.currentTimeMillis()
        val entity = ShiftEntity(
            boothId = boothId,
            employeeId = employeeId,
            mobileMoneyAccountId = mobileMoneyAccountId,
            status = ShiftStatus.OPEN,
            openedAt = now,
            openingCashMinor = openingCashMinor,
            openingMobileMoneyBalanceMinor = openingMobileMoneyBalanceMinor,
            createdAt = now,
            updatedAt = now,
        )
        val id = shiftDao.insert(entity)
        outbox.changed(SyncEntityType.SHIFT, entity.uid)
        return OpenShiftResult.Success(id)
    }

    suspend fun closeShift(shiftId: Long, confirmed: Boolean) {
        val existing = shiftDao.getByIdOnce(shiftId) ?: return
        val now = System.currentTimeMillis()
        shiftDao.update(
            existing.copy(
                status = ShiftStatus.CLOSED,
                closedAt = now,
                confirmed = confirmed,
                updatedAt = now,
            ),
        )
        outbox.changed(SyncEntityType.SHIFT, existing.uid)
    }
}
