package com.moneybooth.app.sms.airtel

import com.moneybooth.app.core.domain.transactions.ParserOutcome
import com.moneybooth.app.core.domain.transactions.RawSmsInput
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * One permanent regression test per real Airtel Money SMS example supplied during development.
 * Never change an existing case's expectations to make a new format pass — add a new case instead.
 */
class AirtelParserRegressionTest {
    private val provider = AirtelMoneyProvider()

    private fun parse(body: String, sender: String = "AIRTEL") =
        provider.parser.parse(RawSmsInput(sender, body, receivedTimestamp = 0L))

    @Test
    fun `withdrawal sms`() {
        val outcome = parse(
            "You have withdrawn ZMW 48.00 from 1824209 Joseph Lungu. Bal is ZMW 0.06. TID: CO260916.1609.H17346.",
        )
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals(TransactionType.WITHDRAWAL, result.transactionType)
        assertEquals(TransactionStatus.PARSED, result.status)
        assertEquals(TransactionDirection.OUT, result.direction)
        assertEquals(4800L, result.amountMinor)
        assertEquals("ZMW", result.currency)
        assertEquals("1824209", result.merchantTillNumber)
        assertEquals("Joseph Lungu", result.merchantName)
        assertEquals(6L, result.balanceAfterMinor)
        assertEquals("CO260916.1609.H17346", result.externalTransactionId)
    }

    @Test
    fun `money received sms with no balance supplied`() {
        val outcome = parse(
            "You have received ZMW 50.00 from 977429540 David Phiri.Dial *115# to check your new Bal. TID: PP260916.1603.N90253.",
        )
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals(TransactionType.P2P_RECEIVE, result.transactionType)
        assertEquals(TransactionDirection.IN, result.direction)
        assertEquals(5000L, result.amountMinor)
        assertEquals("977429540", result.senderPhone)
        assertEquals("David Phiri", result.senderName)
        assertNull("balance must be null, the SMS never supplies it", result.balanceAfterMinor)
        assertEquals("PP260916.1603.N90253", result.externalTransactionId)
    }

    @Test
    fun `airtime top-up sms`() {
        val outcome = parse(
            "Your ZMW 2.00 airtime top-up is successful. Your new Airtel Money balance is ZMW 0.56.Txn ID : RC260916.1117.Q13311",
        )
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals(TransactionType.AIRTIME_TOPUP, result.transactionType)
        assertEquals(TransactionDirection.OUT, result.direction)
        assertEquals(200L, result.amountMinor)
        assertEquals(56L, result.balanceAfterMinor)
        assertEquals("RC260916.1117.Q13311", result.externalTransactionId)
    }

    @Test
    fun `till payment sms`() {
        val outcome = parse(
            "Payment of ZMW 3.00 Till Number SOCHESCARE AIRTEL NETWORKS SELF CARE SOCHE. Airtel Money bal is ZMW 2.56. TID : MP260915.1931.M41519.",
        )
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals(TransactionType.TILL_PAYMENT, result.transactionType)
        assertEquals(TransactionDirection.OUT, result.direction)
        assertEquals(300L, result.amountMinor)
        assertEquals("SOCHESCARE", result.merchantTillNumber)
        assertEquals("AIRTEL NETWORKS SELF CARE SOCHE", result.merchantName)
        assertEquals(256L, result.balanceAfterMinor)
        assertEquals("MP260915.1931.M41519", result.externalTransactionId)
    }

    @Test
    fun `p2p send sms`() {
        val outcome = parse(
            "Money sent to Sara ngala on 971430077.Amount ZMW 17.00. Your bal is ZMW 5.56.TID: PP260915.1930.D02882.",
        )
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals(TransactionType.P2P_SEND, result.transactionType)
        assertEquals(TransactionDirection.OUT, result.direction)
        assertEquals("971430077", result.recipientPhone)
        assertEquals("Sara ngala", result.recipientName)
        assertEquals(1700L, result.amountMinor)
        assertEquals(556L, result.balanceAfterMinor)
        assertEquals("PP260915.1930.D02882", result.externalTransactionId)
    }

    @Test
    fun `failed transaction sms never carries an amount or balance`() {
        val outcome = parse(
            "FAILED.TID: LP260908.2107.L58987, Dear Customer,you have insufficient funds to complete this transaction.Kindly top up and try again..",
        )
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals(TransactionStatus.FAILED, result.status)
        assertEquals("LP260908.2107.L58987", result.externalTransactionId)
        assertNull(result.amountMinor)
        assertNull(result.balanceAfterMinor)

        val validation = provider.validate(result)
        assertTrue(validation is com.moneybooth.app.core.domain.transactions.ValidationResult.Valid)
    }

    @Test
    fun `loan repayment sms`() {
        val outcome = parse(
            "You have repaid  13.75 ZMW towards your FIKILIZA loan. Your new available balance is 6.26 ZMW. Txn ID.LR260911.1647.I10047",
        )
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals(TransactionType.LOAN_REPAYMENT, result.transactionType)
        assertEquals(TransactionDirection.OUT, result.direction)
        assertEquals(1375L, result.amountMinor)
        assertEquals("FIKILIZA", result.serviceName)
        assertEquals(626L, result.balanceAfterMinor)
        assertEquals("LR260911.1647.I10047", result.externalTransactionId)
    }

    @Test
    fun `unrelated text is not matched by any Airtel shape`() {
        val outcome = parse("Hey, are we still meeting for lunch today at 1pm?")
        assertEquals(ParserOutcome.NoMatch, outcome)
    }

    @Test
    fun `detector recognizes airtel by sender id`() {
        assertTrue(provider.detector.matches(RawSmsInput("AIRTEL", "anything at all", 0L)))
    }

    @Test
    fun `detector recognizes airtel by distinctive content when sender is unknown`() {
        assertTrue(
            provider.detector.matches(
                RawSmsInput(
                    "12345",
                    "You have received ZMW 50.00 from 977429540 David Phiri.Dial *115# to check your new Bal. TID: PP260916.1603.N90253.",
                    0L,
                ),
            ),
        )
    }
}
