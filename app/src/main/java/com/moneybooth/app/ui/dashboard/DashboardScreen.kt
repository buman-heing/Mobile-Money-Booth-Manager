package com.moneybooth.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.data.database.entities.TransactionEntity
import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.ui.common.AmountText
import com.moneybooth.app.ui.common.AppViewModelFactory
import com.moneybooth.app.ui.common.BrandMark
import com.moneybooth.app.ui.common.TransactionIcon
import com.moneybooth.app.ui.common.displayLabel
import com.moneybooth.app.ui.theme.BrandGradient
import com.moneybooth.app.ui.theme.MoneyIn
import com.moneybooth.app.ui.theme.MoneyOut
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    container: AppContainer,
    businessName: String,
    onTransactionClick: (Long) -> Unit,
    onSeeAllTransactions: () -> Unit,
) {
    val viewModel: DashboardViewModel = viewModel(factory = AppViewModelFactory(container))
    val count by viewModel.observeTodayCount(null).collectAsStateWithLifecycle(initialValue = 0)
    val moneyIn by viewModel.observeTodayMoneyIn(null).collectAsStateWithLifecycle(initialValue = 0L)
    val moneyOut by viewModel.observeTodayMoneyOut(null).collectAsStateWithLifecycle(initialValue = 0L)
    val commission by viewModel.observeTodayCommission(null).collectAsStateWithLifecycle(initialValue = 0L)
    val balance by viewModel.observeMobileMoneyBalance(null).collectAsStateWithLifecycle(initialValue = null)
    val recent by viewModel.observeRecent().collectAsStateWithLifecycle(initialValue = emptyList())

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(greeting(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    businessName,
                    style = MaterialTheme.typography.headlineMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            BrandMark(size = 40.dp)
        }

        Spacer(Modifier.height(20.dp))
        BalanceHero(balance = balance, commission = commission, count = count)

        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Money in", Money.formatWithCurrency(moneyIn), Icons.Rounded.ArrowDownward, MoneyIn)
            StatTile("Money out", Money.formatWithCurrency(moneyOut), Icons.Rounded.ArrowUpward, MoneyOut)
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Commission", Money.formatWithCurrency(commission), Icons.Rounded.Stars, MaterialTheme.colorScheme.tertiary)
            StatTile("Transactions", count.toString(), Icons.Rounded.ReceiptLong, MaterialTheme.colorScheme.primary)
        }

        Spacer(Modifier.height(28.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recent activity", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.clickable(onClick = onSeeAllTransactions),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("See all", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
        }
        Spacer(Modifier.height(8.dp))

        if (recent.isEmpty()) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    "No activity yet. Incoming SMS will show up here automatically.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp),
                )
            }
        } else {
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerLow,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column {
                    recent.forEachIndexed { index, tx ->
                        RecentRow(tx, onClick = { onTransactionClick(tx.id) })
                        if (index < recent.lastIndex) {
                            Box(
                                Modifier.fillMaxWidth().padding(start = 72.dp).height(1.dp)
                                    .background(MaterialTheme.colorScheme.outlineVariant),
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun BalanceHero(balance: Long?, commission: Long, count: Int) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(MaterialTheme.shapes.large).background(BrandGradient),
    ) {
        Box(
            Modifier.size(200.dp).align(Alignment.TopEnd).offset(x = 60.dp, y = (-70).dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape),
        )
        Box(
            Modifier.size(120.dp).align(Alignment.BottomStart).offset(x = (-40).dp, y = 50.dp)
                .background(Color.Black.copy(alpha = 0.10f), CircleShape),
        )
        Column(modifier = Modifier.padding(22.dp)) {
            Text(
                "MOBILE MONEY BALANCE",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.75f),
            )
            Text(
                balance?.let { Money.formatWithCurrency(it) } ?: "—",
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
                modifier = Modifier.padding(top = 6.dp),
            )
            Spacer(Modifier.height(18.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HeroChip(Icons.Rounded.Stars, "${Money.formatWithCurrency(commission)} earned")
                HeroChip(Icons.Rounded.ReceiptLong, "$count today")
            }
        }
    }
}

@Composable
private fun HeroChip(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier.background(Color.White.copy(alpha = 0.16f), CircleShape).padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelMedium, color = Color.White)
    }
}

@Composable
private fun RowScope.StatTile(label: String, value: String, icon: ImageVector, tint: Color) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.weight(1f),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier.size(34.dp).background(tint.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.height(14.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                value,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun RecentRow(tx: TransactionEntity, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TransactionIcon(tx.transactionType, tx.direction, tx.status, size = 40.dp)
        Column(modifier = Modifier.weight(1f).padding(horizontal = 14.dp)) {
            Text(tx.transactionType.displayLabel(), style = MaterialTheme.typography.titleSmall)
            Text(
                listOfNotNull(tx.senderName ?: tx.recipientName ?: tx.merchantName, timeLabel(tx.smsReceivedTimestamp))
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AmountText(tx.amountMinor, direction = tx.direction, currency = tx.currency, style = MaterialTheme.typography.titleSmall)
    }
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Good morning"
    in 12..16 -> "Good afternoon"
    else -> "Good evening"
}

private fun timeLabel(epochMillis: Long): String =
    SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(epochMillis))
