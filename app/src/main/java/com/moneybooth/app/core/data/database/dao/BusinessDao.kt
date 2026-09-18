package com.moneybooth.app.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.moneybooth.app.core.data.database.entities.BusinessEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BusinessDao {
    @Insert
    suspend fun insert(business: BusinessEntity): Long

    @Update
    suspend fun update(business: BusinessEntity)

    @Query("SELECT * FROM businesses WHERE id = :id")
    fun observeById(id: Long): Flow<BusinessEntity?>

    @Query("SELECT * FROM businesses ORDER BY id LIMIT 1")
    fun observeFirst(): Flow<BusinessEntity?>

    @Query("SELECT * FROM businesses ORDER BY id LIMIT 1")
    suspend fun getFirstOnce(): BusinessEntity?

    @Query("SELECT * FROM businesses ORDER BY name")
    fun observeAll(): Flow<List<BusinessEntity>>

    @Query("SELECT * FROM businesses WHERE uid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): BusinessEntity?
}
