package com.moneybooth.app.core.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.moneybooth.app.core.data.database.entities.TransactionEntity
import com.moneybooth.app.core.domain.transactions.BusinessClassification
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert
    suspend fun insert(transaction: TransactionEntity): Long

    @Update
    suspend fun update(transaction: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE dedupKey = :dedupKey LIMIT 1")
    suspend fun getByDedupKey(dedupKey: String): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE id = :id")
    fun observeById(id: Long): Flow<TransactionEntity?>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getByIdOnce(id: Long): TransactionEntity?

    @Query("SELECT * FROM transactions WHERE shiftId = :shiftId ORDER BY smsReceivedTimestamp DESC")
    fun observeByShift(shiftId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE shiftId = :shiftId ORDER BY smsReceivedTimestamp")
    suspend fun getByShiftOnce(shiftId: Long): List<TransactionEntity>

    @Query(
        """
        SELECT * FROM transactions
        WHERE (:boothId IS NULL OR boothId = :boothId)
          AND (:employeeId IS NULL OR employeeId = :employeeId)
          AND (:providerId IS NULL OR providerId = :providerId)
          AND (:transactionType IS NULL OR transactionType = :transactionType)
          AND (:status IS NULL OR status = :status)
          AND (:classification IS NULL OR businessClassification = :classification)
          AND (:startTime IS NULL OR smsReceivedTimestamp >= :startTime)
          AND (:endTime IS NULL OR smsReceivedTimestamp <= :endTime)
          AND (
                :search IS NULL
                OR externalTransactionId LIKE '%' || :search || '%'
                OR senderPhone LIKE '%' || :search || '%'
                OR recipientPhone LIKE '%' || :search || '%'
                OR senderName LIKE '%' || :search || '%'
                OR recipientName LIKE '%' || :search || '%'
                OR merchantName LIKE '%' || :search || '%'
              )
        ORDER BY smsReceivedTimestamp DESC
        """,
    )
    fun filter(
        boothId: Long?,
        employeeId: Long?,
        providerId: String?,
        transactionType: TransactionType?,
        status: TransactionStatus?,
        classification: BusinessClassification?,
        startTime: Long?,
        endTime: Long?,
        search: String?,
    ): Flow<List<TransactionEntity>>

    @Query(
        """
        SELECT COUNT(*) FROM transactions
        WHERE (:boothId IS NULL OR boothId = :boothId)
          AND smsReceivedTimestamp BETWEEN :startTime AND :endTime
          AND status IN ('PARSED', 'CONFIRMED')
        """,
    )
    fun observeCountForDay(boothId: Long?, startTime: Long, endTime: Long): Flow<Int>

    @Query(
        """
        SELECT COALESCE(SUM(amountMinor), 0) FROM transactions
        WHERE (:boothId IS NULL OR boothId = :boothId)
          AND direction = :direction
          AND smsReceivedTimestamp BETWEEN :startTime AND :endTime
          AND status IN ('PARSED', 'CONFIRMED')
        """,
    )
    fun observeSumForDay(boothId: Long?, direction: TransactionDirection, startTime: Long, endTime: Long): Flow<Long>

    @Query(
        """
        SELECT balanceAfterMinor FROM transactions
        WHERE (:boothId IS NULL OR boothId = :boothId)
          AND balanceAfterMinor IS NOT NULL
          AND status IN ('PARSED', 'CONFIRMED')
        ORDER BY COALESCE(transactionTimestamp, smsReceivedTimestamp) DESC
        LIMIT 1
        """,
    )
    fun observeLatestKnownBalance(boothId: Long?): Flow<Long?>
}
