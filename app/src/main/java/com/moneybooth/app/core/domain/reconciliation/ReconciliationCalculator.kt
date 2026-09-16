package com.moneybooth.app.core.domain.reconciliation

/** Pure arithmetic, fully unit-testable without Room/Android. */
object ReconciliationCalculator {
    fun expectedCashMinor(openingCashMinor: Long, cashInMinor: Long, cashOutMinor: Long, adjustmentsMinor: Long): Long =
        openingCashMinor + cashInMinor - cashOutMinor + adjustmentsMinor

    fun cashDifferenceMinor(actualCashMinor: Long, expectedCashMinor: Long): Long =
        actualCashMinor - expectedCashMinor

    fun expectedMobileMoneyMinor(openingBalanceMinor: Long, incomingMinor: Long, outgoingMinor: Long): Long =
        openingBalanceMinor + incomingMinor - outgoingMinor

    /** Null when no SMS has supplied a provider-reported balance yet — never guessed. */
    fun mobileMoneyDifferenceMinor(providerReportedMinor: Long?, expectedMinor: Long): Long? =
        providerReportedMinor?.let { it - expectedMinor }
}
