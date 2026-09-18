package com.moneybooth.app.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.MarkEmailUnread
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.data.database.entities.TransactionEntity
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.ui.common.AmountText
import com.moneybooth.app.ui.common.AppViewModelFactory
import com.moneybooth.app.ui.common.EmptyState
import com.moneybooth.app.ui.common.FilterChipRow
import com.moneybooth.app.ui.common.FilterOption
import com.moneybooth.app.ui.common.StatusBadge
import com.moneybooth.app.ui.common.TransactionIcon
import com.moneybooth.app.ui.common.displayLabel
import com.moneybooth.app.ui.theme.WarningAmber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionListScreen(
    container: AppContainer,
    onTransactionClick: (Long) -> Unit,
    onAddTransaction: () -> Unit,
    onReviewClick: () -> Unit,
) {
    val viewModel: TransactionsViewModel = viewModel(factory = AppViewModelFactory(container))
    val reviewViewModel: UnknownSmsReviewViewModel = viewModel(factory = AppViewModelFactory(container))
    val statusFilter by viewModel.currentStatusFilter.collectAsStateWithLifecycle()
    val transactions by viewModel.transactions.collectAsStateWithLifecycle(initialValue = emptyList())
    val needsReview by reviewViewModel.observeNeedsReview().collectAsStateWithLifecycle(initialValue = emptyList())
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTransaction,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape,
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add transaction")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Activity",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 12.dp),
            )

            if (needsReview.isNotEmpty()) {
                Surface(
                    shape = MaterialTheme.shapes.medium,
                    color = WarningAmber.copy(alpha = 0.14f),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).clickable(onClick = onReviewClick),
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.MarkEmailUnread, contentDescription = null, tint = WarningAmber)
                        Text(
                            "${needsReview.size} message${if (needsReview.size == 1) "" else "s"} need review",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                        )
                        Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = WarningAmber)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    viewModel.setSearch(it)
                },
                placeholder = { Text("Search TID, phone, name…") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                singleLine = true,
                shape = CircleShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
            )
            Spacer(Modifier.height(10.dp))
            FilterChipRow(
                options = listOf(
                    FilterOption(TransactionStatus.PARSED, "Parsed"),
                    FilterOption(TransactionStatus.CONFIRMED, "Confirmed"),
                    FilterOption(TransactionStatus.PENDING_REVIEW, "Needs review"),
                    FilterOption(TransactionStatus.FAILED, "Failed"),
                ),
                selected = statusFilter,
                onSelect = { viewModel.setStatusFilter(it) },
                modifier = Modifier.padding(horizontal = 20.dp),
            )
            if (transactions.isEmpty()) {
                EmptyState(
                    title = "No transactions yet",
                    subtitle = "Incoming Airtel Money SMS will land here. You can also paste one in Parser or add one manually.",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(transactions, key = { it.id }) { tx ->
                        TransactionRow(tx, onClick = { onTransactionClick(tx.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: TransactionEntity, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TransactionIcon(tx.transactionType, tx.direction, tx.status)
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
            Column(horizontalAlignment = Alignment.End) {
                AmountText(tx.amountMinor, direction = tx.direction, currency = tx.currency, style = MaterialTheme.typography.titleSmall)
                if (tx.status != TransactionStatus.PARSED) {
                    StatusBadge(tx.status, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
    }
}

private fun timeLabel(epochMillis: Long): String =
    SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(epochMillis))
