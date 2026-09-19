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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.CloudDone
import androidx.compose.material.icons.rounded.CloudOff
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.SmsFailed
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.data.database.entities.TransactionEntity
import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.ui.common.AmountText
import com.moneybooth.app.ui.common.AppViewModelFactory
import com.moneybooth.app.ui.common.BrandMark
import com.moneybooth.app.ui.common.SectionCard
import com.moneybooth.app.ui.common.StatusLine
import com.moneybooth.app.ui.common.TransactionIcon
import com.moneybooth.app.ui.common.UnusualTransactionsCard
import com.moneybooth.app.ui.common.displayLabel
import com.moneybooth.app.ui.common.relativeTime
import com.moneybooth.app.ui.common.timeLabel
import com.moneybooth.app.ui.theme.MoneyIn
import com.moneybooth.app.ui.theme.MoneyOut
import com.moneybooth.app.ui.theme.WarningAmber

/**
 * The employee phone's home: is the phone doing its job (recording SMS, uploading), is a shift
 * open, and how is today going. Deliberately no management, no commission, no history digging.
 */
@Composable
fun BoothHomeScreen(
    container: AppContainer,
    businessName: String,
    onTransactionClick: (Long) -> Unit,
    onOpenShift: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val viewModel: DashboardViewModel = viewModel(factory = AppViewModelFactory(container))
    val balance by viewModel.observeMobileMoneyBalance().collectAsStateWithLifecycle(initialValue = null)
    val inCount by viewModel.observeTodayCount(TransactionDirection.IN).collectAsStateWithLifecycle(initialValue = 0)
    val outCount by viewModel.observeTodayCount(TransactionDirection.OUT).collectAsStateWithLifecycle(initialValue = 0)
    val inSum by viewModel.observeTodaySum(TransactionDirection.IN).collectAsStateWithLifecycle(initialValue = 0L)
    val outSum by viewModel.observeTodaySum(TransactionDirection.OUT).collectAsStateWithLifecycle(initialValue = 0L)
    val recent by viewModel.observeRecent(5).collectAsStateWithLifecycle(initialValue = emptyList())
    val unusual by viewModel.observeUnusual().collectAsStateWithLifecycle(initialValue = emptyList())
    val openShift by viewModel.observeOpenShift().collectAsStateWithLifecycle(initialValue = null)
    val syncStatus by container.deviceSettingsStore.syncStatusFlow.collectAsStateWithLifecycle()
    val pending by container.syncOutbox.observePendingCount().collectAsStateWithLifecycle(initialValue = 0)

    val smsOn = container.deviceSettingsStore.smsIngestionEnabled && container.smsPermissionManager.hasSmsPermission()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Employee phone", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(businessName, style = MaterialTheme.typography.headlineMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            BrandMark(size = 40.dp)
        }

        Spacer(Modifier.height(20.dp))
        SectionCard {
            Column {
                StatusLine(
                    icon = if (smsOn) Icons.Rounded.Sms else Icons.Rounded.SmsFailed,
                    tint = if (smsOn) MoneyIn else MoneyOut,
                    title = if (smsOn) "Recording Airtel Money SMS" else "SMS recording is OFF",
                    detail = if (smsOn) "Every transaction message is saved automatically." else "Tap to turn it on so nothing is missed.",
                    onClick = if (smsOn) null else onOpenSettings,
                )
                StatusLine(
                    icon = if (pending == 0) Icons.Rounded.CloudDone else Icons.Rounded.CloudOff,
                    tint = if (pending == 0) MoneyIn else WarningAmber,
                    title = if (pending == 0) "Everything sent to the owner" else "$pending waiting for signal",
                    detail = syncStatus.lastSyncAt?.let { "Last sent ${relativeTime(it)}" } ?: "Nothing sent yet",
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        val shift = openShift
        if (shift == null) {
            SectionCard {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("No shift open", style = MaterialTheme.typography.titleSmall)
                        Text(
                            "Start a shift so today's transactions are tied to who is working.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Button(onClick = onOpenShift, shape = MaterialTheme.shapes.small, modifier = Modifier.padding(start = 12.dp)) {
                        Text("Start")
                    }
                }
            }
        } else {
            val employeeName by viewModel.observeEmployeeName(shift.employeeId).collectAsStateWithLifecycle(initialValue = null)
            SectionCard {
                StatusLine(
                    icon = Icons.Rounded.Schedule,
                    tint = MaterialTheme.colorScheme.primary,
                    title = "Shift open · ${employeeName ?: "…"}",
                    detail = "Since ${timeLabel(shift.openedAt)} · opening cash ${Money.formatWithCurrency(shift.openingCashMinor)}",
                    onClick = onOpenShift,
                )
            }
        }

        if (unusual.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            UnusualTransactionsCard(unusual, onTransactionClick)
        }

        Spacer(Modifier.height(24.dp))
        Text("Today", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        SectionCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("FLOAT BALANCE", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    balance?.let { Money.formatWithCurrency(it) } ?: "—",
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            CountTile("Cash-outs", inCount, inSum, Icons.Rounded.ArrowDownward, MoneyIn)
            CountTile("Cash-ins", outCount, outSum, Icons.Rounded.ArrowUpward, MoneyOut)
        }

        Spacer(Modifier.height(24.dp))
        Text("Latest", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(10.dp))
        if (recent.isEmpty()) {
            SectionCard {
                Text(
                    "No transactions yet today.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(20.dp),
                )
            }
        } else {
            SectionCard {
                Column {
                    recent.forEachIndexed { index, tx ->
                        RecentRow(tx, onClick = { onTransactionClick(tx.id) })
                        if (index < recent.lastIndex) {
                            Box(Modifier.fillMaxWidth().padding(start = 72.dp).height(1.dp).background(MaterialTheme.colorScheme.outlineVariant))
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}

/**
 * Labels are from the customer's point of view (what the employee says out loud), while the
 * money direction is the float's: a customer cash-out is float IN, a cash-in is float OUT.
 */
@Composable
private fun RowScope.CountTile(label: String, count: Int, totalMinor: Long, icon: ImageVector, tint: Color) {
    Surface(shape = MaterialTheme.shapes.medium, color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.weight(1f)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = tint)
                Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 6.dp))
            }
            Text("$count", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(top = 8.dp))
            Text(Money.formatWithCurrency(totalMinor), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                listOfNotNull(tx.senderName ?: tx.recipientName ?: tx.merchantName, timeLabel(tx.smsReceivedTimestamp)).joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        AmountText(tx.amountMinor, direction = tx.direction, currency = tx.currency, style = MaterialTheme.typography.titleSmall)
    }
}
