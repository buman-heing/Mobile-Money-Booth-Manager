package com.moneybooth.app.sms.airtel

import com.moneybooth.app.sms.airtel.matchers.AirtelAirtimeTopupMatcher
import com.moneybooth.app.sms.airtel.matchers.AirtelFailedTransactionMatcher
import com.moneybooth.app.sms.airtel.matchers.AirtelLoanRepaymentMatcher
import com.moneybooth.app.sms.airtel.matchers.AirtelP2pReceiveMatcher
import com.moneybooth.app.sms.airtel.matchers.AirtelP2pSendMatcher
import com.moneybooth.app.sms.airtel.matchers.AirtelTillPaymentMatcher
import com.moneybooth.app.sms.airtel.matchers.AirtelWithdrawalMatcher
import com.moneybooth.app.sms.common.SmsParser
import com.moneybooth.app.sms.common.SmsShapeMatcher

/**
 * Tried in order; each matcher's quickCheck is a cheap keyword filter so unrelated shapes are
 * skipped fast. Adding a new Airtel SMS format later means adding one matcher to this list.
 */
class AirtelSmsParser : SmsParser {
    override val matchers: List<SmsShapeMatcher> = listOf(
        AirtelFailedTransactionMatcher(),
        AirtelWithdrawalMatcher(),
        AirtelP2pReceiveMatcher(),
        AirtelP2pSendMatcher(),
        AirtelAirtimeTopupMatcher(),
        AirtelTillPaymentMatcher(),
        AirtelLoanRepaymentMatcher(),
    )
}
