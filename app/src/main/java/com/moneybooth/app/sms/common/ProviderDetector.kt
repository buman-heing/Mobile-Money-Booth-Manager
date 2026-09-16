package com.moneybooth.app.sms.common

import com.moneybooth.app.core.domain.transactions.RawSmsInput

interface ProviderDetector {
    fun matches(input: RawSmsInput): Boolean
}
