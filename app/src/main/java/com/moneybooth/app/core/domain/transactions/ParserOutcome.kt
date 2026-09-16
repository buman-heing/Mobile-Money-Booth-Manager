package com.moneybooth.app.core.domain.transactions

sealed interface ParserOutcome {
    data class Matched(val result: ParsedTransactionResult) : ParserOutcome
    data object NoMatch : ParserOutcome
}
