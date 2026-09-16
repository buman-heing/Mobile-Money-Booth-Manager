package com.moneybooth.app.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.moneybooth.app.core.data.database.entities.MobileMoneyAccountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MobileMoneyAccountDao {
    @Insert
    suspend fun insert(account: MobileMoneyAccountEntity): Long

    @Update
    suspend fun update(account: MobileMoneyAccountEntity)

    @Query("SELECT * FROM mobile_money_accounts WHERE id = :id")
    fun observeById(id: Long): Flow<MobileMoneyAccountEntity?>

    @Query("SELECT * FROM mobile_money_accounts WHERE boothId = :boothId ORDER BY label")
    fun observeByBooth(boothId: Long): Flow<List<MobileMoneyAccountEntity>>

    @Query("SELECT * FROM mobile_money_accounts WHERE boothId = :boothId AND active = 1 ORDER BY label")
    suspend fun getActiveByBoothOnce(boothId: Long): List<MobileMoneyAccountEntity>
}
