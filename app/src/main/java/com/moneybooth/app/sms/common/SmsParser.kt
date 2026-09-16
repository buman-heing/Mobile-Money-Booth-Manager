package com.moneybooth.app.sms.common

import com.moneybooth.app.core.domain.transactions.ParserOutcome
import com.moneybooth.app.core.domain.transactions.RawSmsInput

interface SmsParser {
    val matchers: List<SmsShapeMatcher>

    fun parse(input: RawSmsInput): ParserOutcome {
        for (matcher in matchers) {
            if (!matcher.quickCheck(input.body)) continue
            val result = matcher.tryParse(input) ?: continue
            return ParserOutcome.Matched(result)
        }
        return ParserOutcome.NoMatch
    }
}
