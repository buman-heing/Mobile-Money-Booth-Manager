package com.moneybooth.app.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.moneybooth.app.core.data.database.entities.EmployeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Insert
    suspend fun insert(employee: EmployeeEntity): Long

    @Update
    suspend fun update(employee: EmployeeEntity)

    @Query("SELECT * FROM employees WHERE id = :id")
    fun observeById(id: Long): Flow<EmployeeEntity?>

    @Query("SELECT * FROM employees WHERE id = :id")
    suspend fun getByIdOnce(id: Long): EmployeeEntity?

    @Query("SELECT * FROM employees WHERE businessId = :businessId ORDER BY name")
    fun observeByBusiness(businessId: Long): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE businessId = :businessId AND active = 1 ORDER BY name")
    fun observeActiveByBusiness(businessId: Long): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM employees WHERE assignedBoothId = :boothId AND active = 1 ORDER BY name")
    fun observeActiveByBooth(boothId: Long): Flow<List<EmployeeEntity>>
}
