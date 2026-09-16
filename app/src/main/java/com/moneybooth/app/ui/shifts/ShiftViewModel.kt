package com.moneybooth.app.ui.shifts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneybooth.app.core.data.database.entities.BoothEntity
import com.moneybooth.app.core.data.database.entities.CashMovementEntity
import com.moneybooth.app.core.data.database.entities.EmployeeEntity
import com.moneybooth.app.core.data.database.entities.ShiftEntity
import com.moneybooth.app.core.data.repository.BoothRepository
import com.moneybooth.app.core.data.repository.CashMovementRepository
import com.moneybooth.app.core.data.repository.EmployeeRepository
import com.moneybooth.app.core.data.repository.OpenShiftResult
import com.moneybooth.app.core.data.repository.ShiftRepository
import com.moneybooth.app.core.domain.reconciliation.CashMovementDirection
import com.moneybooth.app.core.domain.reconciliation.CashMovementReason
import com.moneybooth.app.core.domain.reconciliation.ReconciliationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ShiftViewModel(
    private val shiftRepository: ShiftRepository,
    private val cashMovementRepository: CashMovementRepository,
    private val reconciliationService: ReconciliationService,
    private val employeeRepository: EmployeeRepository,
    private val boothRepository: BoothRepository,
) : ViewModel() {

    fun observeBooths(businessId: Long): Flow<List<BoothEntity>> = boothRepository.observeByBusiness(businessId)

    fun observeEmployees(businessId: Long): Flow<List<EmployeeEntity>> =
        employeeRepository.observeActiveByBusiness(businessId)

    fun observeOpenShift(boothId: Long): Flow<ShiftEntity?> = shiftRepository.observeOpenShiftForBooth(boothId)

    fun observeCashMovements(shiftId: Long): Flow<List<CashMovementEntity>> =
        cashMovementRepository.observeByShift(shiftId)

    fun openShift(
        boothId: Long,
        employeeId: Long,
        openingCashMinor: Long,
        openingMobileMoneyBalanceMinor: Long?,
        onResult: (OpenShiftResult) -> Unit,
    ) {
        viewModelScope.launch {
            val result = shiftRepository.openShift(
                boothId = boothId,
                employeeId = employeeId,
                mobileMoneyAccountId = null,
                openingCashMinor = openingCashMinor,
                openingMobileMoneyBalanceMinor = openingMobileMoneyBalanceMinor,
            )
            onResult(result)
        }
    }

    fun logCashMovement(
        shiftId: Long,
        boothId: Long,
        direction: CashMovementDirection,
        amountMinor: Long,
        reason: CashMovementReason,
        note: String?,
        createdBy: Long,
    ) {
        viewModelScope.launch {
            cashMovementRepository.recordMovement(shiftId, boothId, direction, amountMinor, reason, null, note, createdBy)
        }
    }

    fun closeShift(shiftId: Long, actualCashMinor: Long, notes: String?, onDone: (Long?) -> Unit) {
        viewModelScope.launch {
            val reconciliationId = reconciliationService.closeShift(shiftId, actualCashMinor, closedBy = "owner", notes = notes)
            onDone(reconciliationId)
        }
    }
}
