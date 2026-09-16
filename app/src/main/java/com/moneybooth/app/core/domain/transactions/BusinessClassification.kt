package com.moneybooth.app.core.domain.transactions

/**
 * The business's own interpretation of a transaction, kept strictly separate from
 * [TransactionType]. Never auto-derived from SMS content — always an explicit owner action.
 * Defaults to [UNKNOWN] until classified.
 */
enum class BusinessClassification {
    CUSTOMER_CASH_IN,
    CUSTOMER_CASH_OUT,
    OWNER_TRANSACTION,
    EMPLOYEE_TRANSACTION,
    BUSINESS_EXPENSE,
    PERSONAL_TRANSACTION,
    OTHER,
    UNKNOWN,
}
