package com.moneybooth.app.core.domain.reconciliation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReconciliationCalculatorTest {

    @Test
    fun `expected cash is opening plus in minus out plus adjustments`() {
        val expected = ReconciliationCalculator.expectedCashMinor(
            openingCashMinor = 10_000L,
            cashInMinor = 5_000L,
            cashOutMinor = 3_000L,
            adjustmentsMinor = 100L,
        )
        assertEquals(12_100L, expected)
    }

    @Test
    fun `cash difference is actual minus expected`() {
        assertEquals(600L, ReconciliationCalculator.cashDifferenceMinor(actualCashMinor = 12_700L, expectedCashMinor = 12_100L))
        assertEquals(-200L, ReconciliationCalculator.cashDifferenceMinor(actualCashMinor = 11_900L, expectedCashMinor = 12_100L))
        assertEquals(0L, ReconciliationCalculator.cashDifferenceMinor(actualCashMinor = 12_100L, expectedCashMinor = 12_100L))
    }

    @Test
    fun `expected mobile money is opening plus incoming minus outgoing`() {
        val expected = ReconciliationCalculator.expectedMobileMoneyMinor(
            openingBalanceMinor = 32_000L,
            incomingMinor = 4_835L,
            outgoingMinor = 4_210L,
        )
        assertEquals(32_625L, expected)
    }

    @Test
    fun `mobile money difference is null when no provider balance was ever supplied`() {
        assertNull(ReconciliationCalculator.mobileMoneyDifferenceMinor(providerReportedMinor = null, expectedMinor = 32_625L))
    }

    @Test
    fun `mobile money difference flags a mismatch rather than hiding it`() {
        val difference = ReconciliationCalculator.mobileMoneyDifferenceMinor(
            providerReportedMinor = 32_000L,
            expectedMinor = 32_625L,
        )
        assertEquals(-625L, difference)
    }
}
