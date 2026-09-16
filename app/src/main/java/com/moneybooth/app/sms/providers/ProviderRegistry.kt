package com.moneybooth.app.sms.providers

import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.sms.common.MobileMoneyProvider

class ProviderRegistry(private val providers: List<MobileMoneyProvider>) {
    fun detect(input: RawSmsInput): MobileMoneyProvider? = providers.firstOrNull { it.detector.matches(input) }

    fun byId(providerId: String): MobileMoneyProvider? = providers.firstOrNull { it.providerId == providerId }

    fun all(): List<MobileMoneyProvider> = providers
}
