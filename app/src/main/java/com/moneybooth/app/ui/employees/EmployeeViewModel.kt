package com.moneybooth.app.ui.employees

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneybooth.app.core.data.database.entities.EmployeeEntity
import com.moneybooth.app.core.data.repository.EmployeeRepository
import com.moneybooth.app.core.domain.employees.EmployeeRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class EmployeeViewModel(private val employeeRepository: EmployeeRepository) : ViewModel() {

    fun observeEmployees(businessId: Long): Flow<List<EmployeeEntity>> =
        employeeRepository.observeByBusiness(businessId)

    fun createEmployee(businessId: Long, name: String, phone: String?, role: EmployeeRole, onDone: () -> Unit) {
        viewModelScope.launch {
            employeeRepository.createEmployee(businessId, name, phone?.ifBlank { null }, role, assignedBoothId = null)
            onDone()
        }
    }

    fun setActive(employee: EmployeeEntity, active: Boolean) {
        viewModelScope.launch { employeeRepository.updateEmployee(employee.copy(active = active)) }
    }
}
