package com.moneybooth.app.core.domain.transactions

/**
 * Neutral input to the SMS ingestion pipeline. Used identically for a real incoming SMS and
 * for text pasted into the developer parser screen, so both paths exercise the same pipeline.
 */
data class RawSmsInput(
    val sender: String,
    val body: String,
    val receivedTimestamp: Long,
)
