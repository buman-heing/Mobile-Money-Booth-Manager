package com.moneybooth.app.core.data.repository

import com.moneybooth.app.core.data.database.dao.RawSmsDao
import com.moneybooth.app.core.data.database.entities.RawSmsEntity
import com.moneybooth.app.core.domain.transactions.ParsingStatus
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.SmsFingerprint
import com.moneybooth.app.core.sync.SyncEntityType
import com.moneybooth.app.core.sync.SyncOutbox
import kotlinx.coroutines.flow.Flow

class RawSmsRepository(private val rawSmsDao: RawSmsDao, private val outbox: SyncOutbox) {
    fun observeNeedsReview(): Flow<List<RawSmsEntity>> = rawSmsDao.observeNeedsReview()

    fun observeAll(): Flow<List<RawSmsEntity>> = rawSmsDao.observeAll()

    fun observeById(id: Long): Flow<RawSmsEntity?> = rawSmsDao.observeById(id)

    suspend fun getByFingerprint(fingerprint: String): RawSmsEntity? = rawSmsDao.getByFingerprint(fingerprint)

    suspend fun getByIdOnce(id: Long): RawSmsEntity? = rawSmsDao.getByIdOnce(id)

    /** Always inserts, even for content identical to a prior message — the raw SMS is never skipped. */
    suspend fun insertRaw(input: RawSmsInput): Long {
        val entity = RawSmsEntity(
            sender = input.sender,
            receivedTimestamp = input.receivedTimestamp,
            rawBody = input.body,
            fingerprint = SmsFingerprint.compute(input.sender, input.body),
            createdAt = System.currentTimeMillis(),
        )
        val id = rawSmsDao.insert(entity)
        outbox.changed(SyncEntityType.RAW_SMS, entity.uid)
        return id
    }

    suspend fun markParsed(id: Long, providerId: String, parserVersion: String, parsedTransactionId: Long, isDuplicate: Boolean) {
        val existing = rawSmsDao.getByIdOnce(id) ?: return
        rawSmsDao.update(
            existing.copy(
                detectedProviderId = providerId,
                parserVersion = parserVersion,
                parsingStatus = if (isDuplicate) ParsingStatus.DUPLICATE else ParsingStatus.PARSED,
                parsedTransactionId = parsedTransactionId,
            ),
        )
        outbox.changed(SyncEntityType.RAW_SMS, existing.uid)
    }

    suspend fun markPendingReview(id: Long, detectedProviderId: String?, parserVersion: String?) {
        val existing = rawSmsDao.getByIdOnce(id) ?: return
        rawSmsDao.update(
            existing.copy(
                detectedProviderId = detectedProviderId,
                parserVersion = parserVersion,
                parsingStatus = ParsingStatus.PENDING_REVIEW,
            ),
        )
        outbox.changed(SyncEntityType.RAW_SMS, existing.uid)
    }
}
