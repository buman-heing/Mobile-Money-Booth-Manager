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

    // ---- Agent-side shapes (real booth SIM samples, Sept 2026) ----

    @Test
    fun `agent cash-out sms - customer withdrew, booth float went up, commission earned`() {
        val outcome = parse(
            "ZMW 900.00  received from 977992879 Angela Chazura. Bal ZMW 902.03. Comm ZMW 9.00 TID: CO260914.1556.V49915",
        )
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals("airtel.cash_out.v1", result.matcherId)
        assertEquals(TransactionType.WITHDRAWAL, result.transactionType)
        assertEquals(TransactionStatus.PARSED, result.status)
        assertEquals(TransactionDirection.IN, result.direction)
        assertEquals(90000L, result.amountMinor)
        assertEquals("ZMW", result.currency)
        assertEquals("977992879", result.senderPhone)
        assertEquals("Angela Chazura", result.senderName)
        assertEquals(90203L, result.balanceAfterMinor)
        assertEquals(900L, result.commissionMinor)
        assertEquals("CO260914.1556.V49915", result.externalTransactionId)
    }

    @Test
    fun `agent cash-out sms - larger amount and multi-word name`() {
        val outcome = parse(
            "ZMW 1030.00  received from 975921874 Churchil Hakazeene. Bal ZMW 1823.12. Comm ZMW 10.00 TID: CO260904.1226.L86643",
        )
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals(TransactionType.WITHDRAWAL, result.transactionType)
        assertEquals(103000L, result.amountMinor)
        assertEquals("Churchil Hakazeene", result.senderName)
        assertEquals(182312L, result.balanceAfterMinor)
        assertEquals(1000L, result.commissionMinor)
        assertEquals("CO260904.1226.L86643", result.externalTransactionId)
    }

    @Test
    fun `agent cash-in sms - customer deposited, booth float went down, commission earned`() {
        val outcome = parse(
            "You have sent ZMW 5.00 to 970532065 GRACE CHANDA. Bal ZMW 51.36.Com ZMW 0.03 TID: CI260816.2154.L14464",
        )
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals("airtel.cash_in.v1", result.matcherId)
        assertEquals(TransactionType.DEPOSIT, result.transactionType)
        assertEquals(TransactionStatus.PARSED, result.status)
        assertEquals(TransactionDirection.OUT, result.direction)
        assertEquals(500L, result.amountMinor)
        assertEquals("970532065", result.recipientPhone)
        assertEquals("GRACE CHANDA", result.recipientName)
        assertEquals("balance must stop before the '.Com' that follows it", 5136L, result.balanceAfterMinor)
        assertEquals(3L, result.commissionMinor)
        assertEquals("CI260816.2154.L14464", result.externalTransactionId)
    }

    @Test
    fun `agent cash-in sms - large amount and lowercase name`() {
        val outcome = parse(
            "You have sent ZMW 3000.00 to 976095648 estery changwa. Bal ZMW 24.12.Com ZMW 15.00 TID: CI260915.0930.B14366",
        )
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals(TransactionType.DEPOSIT, result.transactionType)
        assertEquals(300000L, result.amountMinor)
        assertEquals("estery changwa", result.recipientName)
        assertEquals(2412L, result.balanceAfterMinor)
        assertEquals(1500L, result.commissionMinor)
        assertEquals("CI260915.0930.B14366", result.externalTransactionId)
    }

    @Test
    fun `agent sms still parses when the phone prepends a spam-alert label`() {
        val outcome = parse(
            "Spam Alert :You have sent ZMW 1500.00 to 777427400 Shadreck Mwale. Bal ZMW 2.03.Com ZMW 7.50 TID: CI260914.1414.H41591",
        )
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals(TransactionType.DEPOSIT, result.transactionType)
        assertEquals(150000L, result.amountMinor)
        assertEquals("Shadreck Mwale", result.recipientName)
        assertEquals(203L, result.balanceAfterMinor)
        assertEquals(750L, result.commissionMinor)
        assertEquals("CI260914.1414.H41591", result.externalTransactionId)
    }

    /**
     * Only the prefix up to the sender name is confirmed from a real screenshot (the rest was cut
     * off); the tail here is synthetic. The matcher deliberately relies on nothing after the name
     * except the TID, so replace the tail with a real one when a full sample is available.
     */
    @Test
    fun `plain p2p receive sms has no commission and no balance`() {
        val outcome = parse(
            "Money received ZMW 10.00 from 20317390 Grace Chanda. Dial *115# to check balance. TID: PP260904.2016.K11111",
        )
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals("airtel.p2p_receive.v2", result.matcherId)
        assertEquals(TransactionType.P2P_RECEIVE, result.transactionType)
        assertEquals(TransactionDirection.IN, result.direction)
        assertEquals(1000L, result.amountMinor)
        assertEquals("20317390", result.senderPhone)
        assertEquals("Grace Chanda", result.senderName)
        assertNull("balance must be null, the SMS never supplies it", result.balanceAfterMinor)
        assertNull("a plain P2P receive earns no agent commission", result.commissionMinor)
        assertEquals("PP260904.2016.K11111", result.externalTransactionId)
    }

    @Test
    fun `balance enquiry reply records the balance but moves no money`() {
        val outcome = parse("Your Current Balance is ZMW 6.32")
        assertTrue(outcome is ParserOutcome.Matched)
        val result = (outcome as ParserOutcome.Matched).result

        assertEquals(TransactionType.BALANCE_CHECK, result.transactionType)
        assertEquals(TransactionDirection.UNKNOWN, result.direction)
        assertNull(result.amountMinor)
        assertEquals(632L, result.balanceAfterMinor)
        assertNull(result.commissionMinor)
        assertTrue(result.externalTransactionId!!.startsWith("BAL"))
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
        assertTrue(provider.detector.matches(RawSmsInput("AirtelMoney", "anything at all", 0L)))
    }

    @Test
    fun `detector recognizes airtel by distinctive content when sender is unknown`() {
        assertTrue(
            provider.detector.matches(
                RawSmsInput(
                    "12345",
                    "ZMW 900.00  received from 977992879 Angela Chazura. Bal ZMW 902.03. Comm ZMW 9.00 TID: CO260914.1556.V49915",
                    0L,
                ),
            ),
        )
    }
}
