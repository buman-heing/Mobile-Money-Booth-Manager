package com.moneybooth.app.core.domain.transactions

import java.security.MessageDigest

object SmsFingerprint {
    fun compute(sender: String, body: String): String {
        val normalized = normalize(sender) + "" + normalize(body)
        val digest = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun normalize(value: String): String = value.trim().replace(Regex("\\s+"), " ")
}
