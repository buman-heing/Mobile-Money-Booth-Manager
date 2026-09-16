package com.moneybooth.app.core.domain.employees

import com.moneybooth.app.core.data.database.entities.ShiftEntity

sealed interface ShiftOpenCheck {
    data object Allowed : ShiftOpenCheck
    data class BoothAlreadyHasOpenShift(val existing: ShiftEntity) : ShiftOpenCheck
    data class EmployeeAlreadyHasOpenShift(val existing: ShiftEntity) : ShiftOpenCheck
}

/** At most one OPEN shift per booth and per employee at any time. */
object ShiftRules {
    fun canOpenShift(existingOpenForBooth: ShiftEntity?, existingOpenForEmployee: ShiftEntity?): ShiftOpenCheck =
        when {
            existingOpenForBooth != null -> ShiftOpenCheck.BoothAlreadyHasOpenShift(existingOpenForBooth)
            existingOpenForEmployee != null -> ShiftOpenCheck.EmployeeAlreadyHasOpenShift(existingOpenForEmployee)
            else -> ShiftOpenCheck.Allowed
        }
}
