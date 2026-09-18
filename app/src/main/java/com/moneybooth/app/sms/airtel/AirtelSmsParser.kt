package com.moneybooth.app.sms.airtel

import com.moneybooth.app.sms.airtel.matchers.AirtelAirtimeTopupMatcher
import com.moneybooth.app.sms.airtel.matchers.AirtelCashInMatcher
import com.moneybooth.app.sms.airtel.matchers.AirtelCashOutMatcher
import com.moneybooth.app.sms.airtel.matchers.AirtelFailedTransactionMatcher
import com.moneybooth.app.sms.airtel.matchers.AirtelLoanRepaymentMatcher
import com.moneybooth.app.sms.airtel.matchers.AirtelP2pReceiveMatcher
import com.moneybooth.app.sms.airtel.matchers.AirtelP2pSendMatcher
import com.moneybooth.app.sms.airtel.matchers.AirtelTillPaymentMatcher
import com.moneybooth.app.sms.common.SmsParser
import com.moneybooth.app.sms.common.SmsShapeMatcher

/**
 * Tried in order; each matcher's quickCheck is a cheap keyword filter so unrelated shapes are
 * skipped fast. Agent-side shapes (cash-out / cash-in, which carry commission) are tried before
 * the plain P2P shapes so the commission-bearing variants always win.
 * Adding a new Airtel SMS format later means adding one matcher to this list.
 */
class AirtelSmsParser : SmsParser {
    override val matchers: List<SmsShapeMatcher> = listOf(
        AirtelFailedTransactionMatcher(),
        AirtelCashOutMatcher(),
        AirtelCashInMatcher(),
        AirtelP2pReceiveMatcher(),
        AirtelP2pSendMatcher(),
        AirtelAirtimeTopupMatcher(),
        AirtelTillPaymentMatcher(),
        AirtelLoanRepaymentMatcher(),
    )
}
