package com.moneybooth.app.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.moneybooth.app.core.data.database.entities.ShiftEntity
import com.moneybooth.app.core.domain.employees.ShiftStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Insert
    suspend fun insert(shift: ShiftEntity): Long

    @Update
    suspend fun update(shift: ShiftEntity)

    @Query("SELECT * FROM shifts WHERE id = :id")
    fun observeById(id: Long): Flow<ShiftEntity?>

    @Query("SELECT * FROM shifts WHERE id = :id")
    suspend fun getByIdOnce(id: Long): ShiftEntity?

    @Query("SELECT * FROM shifts WHERE boothId = :boothId AND status = :status LIMIT 1")
    suspend fun getOpenShiftForBooth(boothId: Long, status: ShiftStatus = ShiftStatus.OPEN): ShiftEntity?

    @Query("SELECT * FROM shifts WHERE boothId = :boothId AND status = :status LIMIT 1")
    fun observeOpenShiftForBooth(boothId: Long, status: ShiftStatus = ShiftStatus.OPEN): Flow<ShiftEntity?>

    @Query("SELECT * FROM shifts WHERE employeeId = :employeeId AND status = :status LIMIT 1")
    suspend fun getOpenShiftForEmployee(employeeId: Long, status: ShiftStatus = ShiftStatus.OPEN): ShiftEntity?

    @Query("SELECT * FROM shifts WHERE boothId = :boothId ORDER BY openedAt DESC")
    fun observeByBooth(boothId: Long): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE employeeId = :employeeId ORDER BY openedAt DESC")
    fun observeByEmployee(employeeId: Long): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM shifts WHERE uid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): ShiftEntity?
}
