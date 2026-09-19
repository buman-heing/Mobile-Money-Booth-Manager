package com.moneybooth.app.ui.navigation

object Destinations {
    const val HOME = "home"
    const val EMPLOYEE_ADD = "employees/add"
    const val TRANSACTION_ADD = "transactions/add"
    const val TRANSACTION_DETAIL = "transactions/{transactionId}"
    const val REVIEW = "review"
    const val DEV_PARSER = "dev/parser"

    fun transactionDetail(transactionId: Long) = "transactions/$transactionId"
}
