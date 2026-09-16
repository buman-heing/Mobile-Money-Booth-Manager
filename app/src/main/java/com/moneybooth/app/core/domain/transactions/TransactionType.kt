package com.moneybooth.app.core.domain.transactions

/**
 * Provider-reported transaction shape. Deliberately open-ended: new provider SMS formats
 * add new values here without any change to accounting/reconciliation logic, which only
 * ever branches on [TransactionStatus], [TransactionDirection] and business classification.
 */
enum class TransactionType {
    WITHDRAWAL,
    DEPOSIT,
    P2P_SEND,
    P2P_RECEIVE,
    AIRTIME_TOPUP,
    TILL_PAYMENT,
    LOAN_REPAYMENT,
    LOAN_DISBURSEMENT,
    BILL_PAYMENT,
    AGENT_FLOAT,
    UNKNOWN,
    OTHER,
}
