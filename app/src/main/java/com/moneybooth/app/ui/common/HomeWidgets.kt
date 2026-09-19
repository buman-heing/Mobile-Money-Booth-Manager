package com.moneybooth.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.moneybooth.app.core.data.database.entities.TransactionEntity
import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/** "2 min ago", "3 h ago", "yesterday 18:40", or a date — for last-seen and last-upload labels. */
fun relativeTime(epochMillis: Long, now: Long = System.currentTimeMillis()): String {
    val diff = now - epochMillis
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    return when {
        minutes < 1 -> "just now"
        minutes < 60 -> "$minutes min ago"
        hours < 24 -> "$hours h ago"
        hours < 48 -> "yesterday " + SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(epochMillis))
        else -> SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(epochMillis))
    }
}

fun timeLabel(epochMillis: Long): String =
    SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(epochMillis))

@Composable
fun SectionCard(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.fillMaxWidth(),
        content = content,
    )
}

@Composable
fun StatusLine(icon: ImageVector, tint: Color, title: String, detail: String, onClick: (() -> Unit)? = null) {
    Row(
        modifier = Modifier.fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(tint.copy(alpha = 0.14f), CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (onClick != null) Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** Transactions whose balance didn't add up. Shown on both phones; the owner also gets a notification. */
@Composable
fun UnusualTransactionsCard(transactions: List<TransactionEntity>, onTransactionClick: (Long) -> Unit) {
    if (transactions.isEmpty()) return
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = WarningAmber.copy(alpha = 0.12f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(bottom = 6.dp)) {
            Row(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.WarningAmber, contentDescription = null, tint = WarningAmber)
                Text(
                    "Unusual transactions — worth a follow-up",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(start = 10.dp),
                )
            }
            transactions.forEach { tx ->
                Row(
                    modifier = Modifier.fillMaxWidth().clickable { onTransactionClick(tx.id) }.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "${tx.transactionType.displayLabel()} · ${tx.senderName ?: tx.recipientName ?: tx.merchantName ?: "—"}",
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            "Balance off by ${Money.formatWithCurrency(abs(tx.discrepancyMinor ?: 0L))} · ${timeLabel(tx.smsReceivedTimestamp)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = WarningAmber)
                }
            }
            Spacer(Modifier.height(2.dp))
        }
    }
}
