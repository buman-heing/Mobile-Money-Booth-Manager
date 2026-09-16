package com.moneybooth.app.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.moneybooth.app.core.data.database.entities.RawSmsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RawSmsDao {
    @Insert
    suspend fun insert(rawSms: RawSmsEntity): Long

    @Update
    suspend fun update(rawSms: RawSmsEntity)

    @Query("SELECT * FROM raw_sms WHERE fingerprint = :fingerprint LIMIT 1")
    suspend fun getByFingerprint(fingerprint: String): RawSmsEntity?

    @Query("SELECT * FROM raw_sms WHERE id = :id")
    fun observeById(id: Long): Flow<RawSmsEntity?>

    @Query("SELECT * FROM raw_sms WHERE id = :id")
    suspend fun getByIdOnce(id: Long): RawSmsEntity?

    @Query("SELECT * FROM raw_sms WHERE parsingStatus IN ('PENDING_REVIEW', 'FAILED_TO_PARSE') ORDER BY receivedTimestamp DESC")
    fun observeNeedsReview(): Flow<List<RawSmsEntity>>

    @Query("SELECT * FROM raw_sms ORDER BY receivedTimestamp DESC")
    fun observeAll(): Flow<List<RawSmsEntity>>
}
