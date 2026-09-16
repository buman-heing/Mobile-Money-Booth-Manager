package com.moneybooth.app.ui.navigation

object Destinations {
    const val HOME = "home"
    const val BOOTH_ADD = "booths/add"
    const val EMPLOYEE_ADD = "employees/add"
    const val TRANSACTION_ADD = "transactions/add"
    const val TRANSACTION_DETAIL = "transactions/{transactionId}"
    const val REVIEW = "review"

    fun transactionDetail(transactionId: Long) = "transactions/$transactionId"
}
