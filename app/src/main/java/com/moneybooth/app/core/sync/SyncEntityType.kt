package com.moneybooth.app.core.sync

/** The kinds of local rows that get copied to the cloud. Each maps to one remote collection. */
enum class SyncEntityType(val collection: String) {
    BUSINESS("businesses"),
    BOOTH("booths"),
    EMPLOYEE("employees"),
    MOBILE_MONEY_ACCOUNT("mobileMoneyAccounts"),
    SHIFT("shifts"),
    RAW_SMS("rawSms"),
    TRANSACTION("transactions"),
    CASH_MOVEMENT("cashMovements"),
    RECONCILIATION("reconciliations"),
    AUDIT_LOG("auditLog"),
}
