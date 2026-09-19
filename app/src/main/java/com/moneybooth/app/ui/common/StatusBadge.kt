package com.moneybooth.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.ui.theme.MoneyIn
import com.moneybooth.app.ui.theme.MoneyOut
import com.moneybooth.app.ui.theme.WarningAmber

/** "The balance didn't add up here" — shown instead of the status pill. */
@Composable
fun UnusualBadge(modifier: Modifier = Modifier) {
    Text(
        text = "UNUSUAL",
        modifier = modifier
            .background(WarningAmber.copy(alpha = 0.18f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = WarningAmber,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
    )
}

@Composable
fun StatusBadge(status: TransactionStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        TransactionStatus.PARSED -> "Parsed" to MaterialTheme.colorScheme.primary
        TransactionStatus.CONFIRMED -> "Confirmed" to MoneyIn
        TransactionStatus.PENDING_REVIEW -> "Needs review" to WarningAmber
        TransactionStatus.FAILED -> "Failed" to MoneyOut
        TransactionStatus.REVERSED -> "Reversed" to MoneyOut
        TransactionStatus.REJECTED -> "Rejected" to MaterialTheme.colorScheme.outline
    }
    Text(
        text = label.uppercase(),
        modifier = modifier
            .background(color.copy(alpha = 0.14f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        color = color,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
    )
}
