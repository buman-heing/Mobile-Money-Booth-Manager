package com.moneybooth.app.sms.airtel

import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.sms.common.ProviderDetector

/**
 * Detects Airtel Money messages by sender ID when available, falling back to distinctive
 * content markers (useful for the developer paste screen, which has no real telecom sender).
 * Refine this once real device sender IDs are confirmed from further examples.
 */
class AirtelProviderDetector : ProviderDetector {
    private val tidPattern = Regex("""(?i)\b[A-Z]{2}\d{6}\.\d{4}\.[A-Z0-9]+""")

    override fun matches(input: RawSmsInput): Boolean {
        val senderMatches = input.sender.contains("airtel", ignoreCase = true)
        val bodyMatches = input.body.contains("airtel money", ignoreCase = true) ||
            input.body.contains("txn id", ignoreCase = true) ||
            tidPattern.containsMatchIn(input.body)
        return senderMatches || bodyMatches
    }
}
