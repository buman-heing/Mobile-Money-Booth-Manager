package com.moneybooth.app.core.domain.employees

import com.moneybooth.app.core.data.repository.ShiftRepository

data class Attribution(val shiftId: Long, val employeeId: Long, val boothId: Long)

/**
 * Resolves which employee/shift a newly-ingested transaction should be attributed to.
 * Deliberately never reads this from the SMS itself (several employees may share one
 * mobile-money device) — it always comes from which shift is currently open for the booth,
 * and remains manually correctable afterward (see TransactionRepository.updateAttribution).
 */
class AttributionService(private val shiftRepository: ShiftRepository) {
    suspend fun currentAttribution(boothId: Long): Attribution? {
        val openShift = shiftRepository.getOpenShiftForBooth(boothId) ?: return null
        return Attribution(shiftId = openShift.id, employeeId = openShift.employeeId, boothId = boothId)
    }
}
