package com.moneybooth.app.core.data.database.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.moneybooth.app.core.domain.transactions.ParsingStatus

/**
 * The original SMS, preserved verbatim forever. Only [detectedProviderId], [parserVersion],
 * [parsingStatus] and [parsedTransactionId] are ever updated after insert — [sender] and
 * [rawBody] are never modified.
 */
@Entity(
    tableName = "raw_sms",
    indices = [Index("fingerprint"), Index("parsingStatus")],
)
data class RawSmsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sender: String,
    val receivedTimestamp: Long,
    val rawBody: String,
    val fingerprint: String,
    val detectedProviderId: String? = null,
    val parserVersion: String? = null,
    val parsingStatus: ParsingStatus = ParsingStatus.UNPARSED,
    val parsedTransactionId: Long? = null,
    val createdAt: Long,
)
