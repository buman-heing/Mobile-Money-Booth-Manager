package com.moneybooth.app.core.sync

import java.security.MessageDigest

/**
 * A short code the owner types on their phone to follow a booth phone's business. Derived from
 * the business uid so both phones agree on it without talking to each other first.
 * Alphabet omits 0/O/1/I to survive being read out over a phone call.
 */
object JoinCode {
    private const val ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    const val LENGTH = 6

    fun forBusiness(businessUid: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(businessUid.toByteArray())
        return buildString {
            for (i in 0 until LENGTH) append(ALPHABET[(digest[i].toInt() and 0xFF) % ALPHABET.length])
        }
    }

    /** Codes never contain 0/O/1/I, so anything outside the alphabet is simply dropped. */
    fun normalize(input: String): String = input.uppercase().filter { it in ALPHABET }
}
