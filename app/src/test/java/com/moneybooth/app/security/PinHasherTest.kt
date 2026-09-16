package com.moneybooth.app.security

import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PinHasherTest {

    @Test
    fun `correct pin verifies against its own hash`() {
        val salt = PinHasher.generateSalt()
        val hash = PinHasher.hash("1234", salt)

        assertTrue(PinHasher.verify("1234", salt, hash))
    }

    @Test
    fun `incorrect pin does not verify`() {
        val salt = PinHasher.generateSalt()
        val hash = PinHasher.hash("1234", salt)

        assertFalse(PinHasher.verify("9999", salt, hash))
    }

    @Test
    fun `same pin with different salts produces different hashes`() {
        val saltA = PinHasher.generateSalt()
        val saltB = PinHasher.generateSalt()

        val hashA = PinHasher.hash("1234", saltA)
        val hashB = PinHasher.hash("1234", saltB)

        assertNotEquals(hashA.toList(), hashB.toList())
    }

    @Test
    fun `generated salts are not trivially predictable`() {
        val saltA = PinHasher.generateSalt()
        val saltB = PinHasher.generateSalt()

        assertNotEquals(saltA.toList(), saltB.toList())
    }
}
