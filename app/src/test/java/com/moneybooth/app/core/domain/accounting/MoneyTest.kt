package com.moneybooth.app.core.domain.accounting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class MoneyTest {

    @Test
    fun `parses a plain decimal amount to minor units`() {
        assertEquals(4800L, Money.parseToMinorUnits("48.00"))
        assertEquals(6L, Money.parseToMinorUnits("0.06"))
        assertEquals(1375L, Money.parseToMinorUnits("13.75"))
    }

    @Test
    fun `returns null for unparseable text`() {
        assertNull(Money.parseToMinorUnits("not a number"))
    }

    @Test
    fun `formats minor units back to a two-decimal string`() {
        assertEquals("48.00", Money.formatMajor(4800L))
        assertEquals("0.06", Money.formatMajor(6L))
    }

    @Test
    fun `formats with currency prefix`() {
        assertEquals("ZMW 48.00", Money.formatWithCurrency(4800L))
    }
}
