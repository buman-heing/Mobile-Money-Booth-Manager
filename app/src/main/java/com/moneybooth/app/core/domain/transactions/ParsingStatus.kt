package com.moneybooth.app.core.domain.transactions

enum class ParsingStatus {
    UNPARSED,
    PARSED,
    DUPLICATE,
    FAILED_TO_PARSE,
    PENDING_REVIEW,
}
