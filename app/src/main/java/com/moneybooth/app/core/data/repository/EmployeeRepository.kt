package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.EmployeeDao
import com.moneybooth.app.core.data.database.entities.EmployeeEntity
import com.moneybooth.app.core.domain.employees.EmployeeRole
import kotlinx.coroutines.flow.Flow

class EmployeeRepository(private val employeeDao: EmployeeDao) {
    fun observeById(id: Long): Flow<EmployeeEntity?> = employeeDao.observeById(id)

    suspend fun getByIdOnce(id: Long): EmployeeEntity? = employeeDao.getByIdOnce(id)

    fun observeByBusiness(businessId: Long): Flow<List<EmployeeEntity>> = employeeDao.observeByBusiness(businessId)

    fun observeActiveByBusiness(businessId: Long): Flow<List<EmployeeEntity>> =
        employeeDao.observeActiveByBusiness(businessId)

    fun observeActiveByBooth(boothId: Long): Flow<List<EmployeeEntity>> = employeeDao.observeActiveByBooth(boothId)

    suspend fun createEmployee(
        businessId: Long,
        name: String,
        phone: String?,
        role: EmployeeRole,
        assignedBoothId: Long?,
    ): Long {
        val now = System.currentTimeMillis()
        return employeeDao.insert(
            EmployeeEntity(
                businessId = businessId,
                name = name,
                phone = phone,
                role = role,
                assignedBoothId = assignedBoothId,
                createdAt = now,
                updatedAt = now,
            ),
        )
    }

    suspend fun updateEmployee(employee: EmployeeEntity) =
        employeeDao.update(employee.copy(updatedAt = System.currentTimeMillis()))
}
