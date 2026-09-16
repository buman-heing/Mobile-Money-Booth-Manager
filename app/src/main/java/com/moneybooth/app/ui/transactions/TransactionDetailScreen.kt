package com.moneybooth.app.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.BusinessClassification
import com.moneybooth.app.core.domain.transactions.TransactionStatus
import com.moneybooth.app.ui.common.AmountText
import com.moneybooth.app.ui.common.AppViewModelFactory
import com.moneybooth.app.ui.common.ConfirmDialog
import com.moneybooth.app.ui.common.StatusBadge

@Composable
fun TransactionDetailScreen(transactionId: Long, container: AppContainer) {
    val viewModel: TransactionDetailViewModel = viewModel(factory = AppViewModelFactory(container))
    val transaction by viewModel.observeTransaction(transactionId).collectAsStateWithLifecycle(initialValue = null)
    val tx = transaction

    if (tx == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading...")
        }
        return
    }

    val rawSms by viewModel.observeRawSms(tx.rawSmsId ?: -1L).collectAsStateWithLifecycle(initialValue = null)
    val auditTrail by viewModel.observeAuditTrail(tx.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var classificationMenuExpanded by remember { mutableStateOf(false) }
    var showRejectConfirm by remember { mutableStateOf(false) }

    if (showRejectConfirm) {
        ConfirmDialog(
            title = "Reject transaction",
            message = "This marks the transaction as rejected and excludes it from the ledger. This can be reviewed later in the audit trail.",
            confirmLabel = "Reject",
            onConfirm = {
                viewModel.rejectTransaction(tx.id, reason = null)
                showRejectConfirm = false
            },
            onDismiss = { showRejectConfirm = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text(tx.transactionType.name.replace('_', ' '), style = MaterialTheme.typography.headlineMedium)
        StatusBadge(tx.status, modifier = Modifier.padding(top = 8.dp))
        AmountText(
            tx.amountMinor,
            direction = tx.direction,
            currency = tx.currency,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(top = 12.dp),
        )

        DetailSection("Parsed data") {
            DetailRow("Transaction ID", tx.externalTransactionId ?: "—")
            DetailRow("Direction", tx.direction.name)
            DetailRow(
                "Balance after",
                tx.balanceAfterMinor?.let { Money.formatWithCurrency(it, tx.currency) } ?: "Not supplied",
            )
            DetailRow("Sender", tx.senderName ?: "—")
            DetailRow("Recipient", tx.recipientName ?: "—")
            DetailRow("Merchant", tx.merchantName ?: "—")
            DetailRow("Service", tx.serviceName ?: "—")
        }

        DetailSection("Business") {
            ExposedDropdownMenuBox(
                expanded = classificationMenuExpanded,
                onExpandedChange = { classificationMenuExpanded = it },
                modifier = Modifier.fillMaxWidth(),
            ) {
                OutlinedTextField(
                    value = tx.businessClassification.name.replace('_', ' '),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Classification") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classificationMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                DropdownMenu(expanded = classificationMenuExpanded, onDismissRequest = { classificationMenuExpanded = false }) {
                    BusinessClassification.entries.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name.replace('_', ' ')) },
                            onClick = {
                                viewModel.updateClassification(tx.id, option)
                                classificationMenuExpanded = false
                            },
                        )
                    }
                }
            }
            DetailRow("Source", tx.source.name)
            DetailRow("Provider", tx.providerId)
            if (tx.employeeId == null) {
                DetailRow("Attributed employee", "Unassigned — correct on the shift screen")
            }
        }

        if (auditTrail.isNotEmpty()) {
            DetailSection("Audit history") {
                auditTrail.forEach { entry ->
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text(
                            entry.actionType.name.replace('_', ' '),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (entry.oldValue != null || entry.newValue != null) {
                            Text(
                                "${entry.oldValue ?: "—"} → ${entry.newValue ?: "—"}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.outline,
                            )
                        }
                    }
                }
            }
        }

        if (tx.status != TransactionStatus.REJECTED) {
            OutlinedButton(
                onClick = { showRejectConfirm = true },
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            ) {
                Text("Reject transaction")
            }
        }

        if (rawSms != null) {
            DetailSection("Original SMS") {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(rawSms!!.rawBody, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "From ${rawSms!!.sender}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
        Text(title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 8.dp))
        content()
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.outline)
        Text(value)
    }
}
