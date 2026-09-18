package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.EmployeeDao
import com.moneybooth.app.core.data.database.entities.EmployeeEntity
import com.moneybooth.app.core.domain.employees.EmployeeRole
import com.moneybooth.app.core.sync.SyncEntityType
import com.moneybooth.app.core.sync.SyncOutbox
import kotlinx.coroutines.flow.Flow

class EmployeeRepository(private val employeeDao: EmployeeDao, private val outbox: SyncOutbox) {
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
        val entity = EmployeeEntity(
            businessId = businessId,
            name = name,
            phone = phone,
            role = role,
            assignedBoothId = assignedBoothId,
            createdAt = now,
            updatedAt = now,
        )
        val id = employeeDao.insert(entity)
        outbox.changed(SyncEntityType.EMPLOYEE, entity.uid)
        return id
    }

    suspend fun updateEmployee(employee: EmployeeEntity) {
        employeeDao.update(employee.copy(updatedAt = System.currentTimeMillis()))
        outbox.changed(SyncEntityType.EMPLOYEE, employee.uid)
    }
}
