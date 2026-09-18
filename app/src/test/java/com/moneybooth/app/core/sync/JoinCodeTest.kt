package com.moneybooth.app.core.sync

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class JoinCodeTest {
    @Test
    fun `same business always gets the same code`() {
        assertEquals(JoinCode.forBusiness("abc-123"), JoinCode.forBusiness("abc-123"))
    }

    @Test
    fun `different businesses get different codes`() {
        assertNotEquals(JoinCode.forBusiness("abc-123"), JoinCode.forBusiness("abc-124"))
    }

    @Test
    fun `codes are six characters from the safe alphabet`() {
        val code = JoinCode.forBusiness(newUid())
        assertEquals(6, code.length)
        assertTrue(code.none { it in "0O1I" })
    }

    @Test
    fun `normalize forgives lowercase and spaces and drops characters codes never use`() {
        assertEquals("ABCDEF", JoinCode.normalize(" ab cd ef "))
        assertEquals("AB", JoinCode.normalize("a0o1ib"))
    }
}
