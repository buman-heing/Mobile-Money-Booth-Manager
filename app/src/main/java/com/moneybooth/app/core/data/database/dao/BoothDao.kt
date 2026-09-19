package com.moneybooth.app.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.moneybooth.app.core.data.database.entities.BoothEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BoothDao {
    @Insert
    suspend fun insert(booth: BoothEntity): Long

    @Update
    suspend fun update(booth: BoothEntity)

    @Query("SELECT * FROM booths WHERE id = :id")
    fun observeById(id: Long): Flow<BoothEntity?>

    @Query("SELECT * FROM booths WHERE id = :id")
    suspend fun getByIdOnce(id: Long): BoothEntity?

    @Query("SELECT * FROM booths WHERE businessId = :businessId ORDER BY name")
    fun observeByBusiness(businessId: Long): Flow<List<BoothEntity>>

    @Query("SELECT * FROM booths WHERE businessId = :businessId AND active = 1 ORDER BY name")
    fun observeActiveByBusiness(businessId: Long): Flow<List<BoothEntity>>

    @Query("SELECT * FROM booths ORDER BY id LIMIT 1")
    suspend fun getFirstOnce(): BoothEntity?

    @Query("SELECT * FROM booths WHERE uid = :uid LIMIT 1")
    suspend fun getByUid(uid: String): BoothEntity?
}
