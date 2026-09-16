package com.moneybooth.app.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.moneybooth.app.core.data.database.entities.CashMovementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CashMovementDao {
    @Insert
    suspend fun insert(movement: CashMovementEntity): Long

    @Query("SELECT * FROM cash_movements WHERE shiftId = :shiftId ORDER BY recordedAt DESC")
    fun observeByShift(shiftId: Long): Flow<List<CashMovementEntity>>

    @Query("SELECT * FROM cash_movements WHERE shiftId = :shiftId ORDER BY recordedAt")
    suspend fun getByShiftOnce(shiftId: Long): List<CashMovementEntity>
}
