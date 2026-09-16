package com.moneybooth.app.sms.common

import com.moneybooth.app.core.domain.transactions.ParsedTransactionResult
import com.moneybooth.app.core.domain.transactions.RawSmsInput

/**
 * One SMS shape (e.g. "Airtel withdrawal"). Adding a newly-supplied SMS format later means
 * adding a new implementation of this interface plus one line registering it in a parser's
 * matcher list — no existing matcher or accounting code is touched.
 */
interface SmsShapeMatcher {
    val id: String

    /** Cheap keyword pre-filter, tried before the full regex. */
    fun quickCheck(body: String): Boolean

    /** Returns null when this shape does not actually match (quickCheck can false-positive). */
    fun tryParse(input: RawSmsInput): ParsedTransactionResult?
}
