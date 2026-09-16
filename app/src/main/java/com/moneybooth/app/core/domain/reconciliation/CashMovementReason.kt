package com.moneybooth.app.core.domain.reconciliation

enum class CashMovementReason {
    CUSTOMER_WITHDRAWAL_PAYOUT,
    CUSTOMER_DEPOSIT_RECEIPT,
    EXPENSE,
    OWNER_DRAWING,
    FLOAT_TOPUP,
    ADJUSTMENT,
    OTHER,
}
