package com.moneybooth.app.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.moneybooth.app.core.data.database.entities.ReconciliationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReconciliationDao {
    @Insert
    suspend fun insert(reconciliation: ReconciliationEntity): Long

    @Query("SELECT * FROM reconciliations WHERE shiftId = :shiftId LIMIT 1")
    fun observeByShift(shiftId: Long): Flow<ReconciliationEntity?>

    @Query("SELECT * FROM reconciliations WHERE boothId = :boothId ORDER BY closedAt DESC")
    fun observeByBooth(boothId: Long): Flow<List<ReconciliationEntity>>
}
