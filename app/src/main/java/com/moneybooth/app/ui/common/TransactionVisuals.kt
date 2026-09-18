package com.moneybooth.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.ui.theme.MoneyIn
import com.moneybooth.app.ui.theme.MoneyOut

/** Human labels for the provider-reported shape, written from the booth's point of view. */
fun TransactionType.displayLabel(): String = when (this) {
    TransactionType.WITHDRAWAL -> "Cash-out"
    TransactionType.DEPOSIT -> "Cash-in"
    TransactionType.P2P_SEND -> "Sent"
    TransactionType.P2P_RECEIVE -> "Received"
    TransactionType.AIRTIME_TOPUP -> "Airtime"
    TransactionType.TILL_PAYMENT -> "Till payment"
    TransactionType.LOAN_REPAYMENT -> "Loan repayment"
    TransactionType.LOAN_DISBURSEMENT -> "Loan disbursement"
    TransactionType.BILL_PAYMENT -> "Bill payment"
    TransactionType.AGENT_FLOAT -> "Float"
    TransactionType.UNKNOWN -> "Unknown"
    TransactionType.OTHER -> "Other"
}

private fun iconFor(type: TransactionType, direction: TransactionDirection, status: TransactionStatus): ImageVector =
    when {
        status == TransactionStatus.FAILED -> Icons.Rounded.ErrorOutline
        type == TransactionType.AIRTIME_TOPUP -> Icons.Rounded.PhoneAndroid
        type == TransactionType.TILL_PAYMENT -> Icons.Rounded.Storefront
        type == TransactionType.BILL_PAYMENT -> Icons.Rounded.ReceiptLong
        type == TransactionType.AGENT_FLOAT -> Icons.Rounded.AccountBalanceWallet
        direction == TransactionDirection.IN -> Icons.Rounded.ArrowDownward
        direction == TransactionDirection.OUT -> Icons.Rounded.ArrowUpward
        else -> Icons.Rounded.SwapHoriz
    }

@Composable
fun TransactionIcon(
    type: TransactionType,
    direction: TransactionDirection,
    status: TransactionStatus,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
) {
    val tint: Color = when {
        status == TransactionStatus.FAILED -> MoneyOut
        direction == TransactionDirection.IN -> MoneyIn
        direction == TransactionDirection.OUT -> MoneyOut
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = modifier.size(size).background(tint.copy(alpha = 0.14f), CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(iconFor(type, direction, status), contentDescription = null, tint = tint, modifier = Modifier.size(size * 0.5f))
    }
}
