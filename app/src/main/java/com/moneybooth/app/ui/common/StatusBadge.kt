package com.moneybooth.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.ui.theme.WarningAmber
import com.moneybooth.app.ui.theme.MoneyInGreen
import com.moneybooth.app.ui.theme.MoneyOutRed

@Composable
fun StatusBadge(status: TransactionStatus, modifier: Modifier = Modifier) {
    val (label, color) = when (status) {
        TransactionStatus.PARSED -> "Parsed" to MoneyInGreen
        TransactionStatus.CONFIRMED -> "Confirmed" to MoneyInGreen
        TransactionStatus.PENDING_REVIEW -> "Needs review" to WarningAmber
        TransactionStatus.FAILED -> "Failed" to MoneyOutRed
        TransactionStatus.REVERSED -> "Reversed" to MoneyOutRed
        TransactionStatus.REJECTED -> "Rejected" to MaterialTheme.colorScheme.outline
    }
    Text(
        text = label,
        modifier = modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
        color = color,
        style = MaterialTheme.typography.labelMedium,
        textAlign = TextAlign.Center,
    )
}
