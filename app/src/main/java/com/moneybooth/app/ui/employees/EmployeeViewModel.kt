package com.moneybooth.app.ui.employees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneybooth.app.core.data.database.entities.BoothEntity
import com.moneybooth.app.core.data.database.entities.EmployeeEntity
import com.moneybooth.app.core.data.repository.BoothRepository
import com.moneybooth.app.core.data.repository.EmployeeRepository
import com.moneybooth.app.core.domain.employees.EmployeeRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class EmployeeViewModel(
    private val employeeRepository: EmployeeRepository,
    private val boothRepository: BoothRepository,
) : ViewModel() {

    fun observeEmployees(businessId: Long): Flow<List<EmployeeEntity>> =
        employeeRepository.observeByBusiness(businessId)

    fun observeBooths(businessId: Long): Flow<List<BoothEntity>> = boothRepository.observeByBusiness(businessId)

    fun createEmployee(
        businessId: Long,
        name: String,
        phone: String?,
        role: EmployeeRole,
        assignedBoothId: Long?,
        onDone: () -> Unit,
    ) {
        viewModelScope.launch {
            employeeRepository.createEmployee(businessId, name, phone?.ifBlank { null }, role, assignedBoothId)
            onDone()
        }
    }
}
