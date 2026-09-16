package com.moneybooth.app.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        floatingActionButton = {
            FloatingActionButton(onClick = onAddTransaction) {
                Icon(Icons.Filled.Add, contentDescription = "Add transaction")
            }
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (needsReview.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable(onClick = onReviewClick),
                ) {
                    Text(
                        "${needsReview.size} message${if (needsReview.size == 1) "" else "s"} need review →",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    viewModel.setSearch(it)
                },
                label = { Text("Search TID, phone, name...") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            )
            FilterChipRow(
                options = listOf(
                    FilterOption(TransactionStatus.PARSED, "Parsed"),
                    FilterOption(TransactionStatus.CONFIRMED, "Confirmed"),
                    FilterOption(TransactionStatus.PENDING_REVIEW, "Needs review"),
                    FilterOption(TransactionStatus.FAILED, "Failed"),
                ),
                selected = statusFilter,
                onSelect = { viewModel.setStatusFilter(it) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
            if (transactions.isEmpty()) {
                EmptyState(
                    title = "No transactions",
                    subtitle = "Paste an SMS in Dev Tools or add one manually.",
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
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
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(tx.transactionType.name.replace('_', ' '), style = MaterialTheme.typography.titleMedium)
                Text(
                    tx.senderName ?: tx.recipientName ?: tx.merchantName ?: "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                )
                StatusBadge(tx.status, modifier = Modifier.padding(top = 4.dp))
            }
            AmountText(tx.amountMinor, direction = tx.direction, currency = tx.currency)
        }
    }
}
