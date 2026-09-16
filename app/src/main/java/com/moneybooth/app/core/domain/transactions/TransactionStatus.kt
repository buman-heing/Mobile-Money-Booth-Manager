package com.moneybooth.app.core.domain.transactions

enum class TransactionStatus {
    /** Parser produced a confident, complete result. Included in the ledger. */
    PARSED,

    /** Owner has reviewed/confirmed the transaction. Included in the ledger. */
    CONFIRMED,

    /** Parser could not confidently interpret the message; needs manual review. Excluded from the ledger. */
    PENDING_REVIEW,

    /** Provider reported the underlying operation failed. Never affects the ledger. */
    FAILED,

    /** A previously ledgered transaction was reversed. Excluded from the ledger going forward. */
    REVERSED,

    /** Owner rejected this as not a real/relevant transaction. Excluded from the ledger. */
    REJECTED,
}
