package com.moneybooth.app.ui.shifts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.data.database.entities.CashMovementEntity
import com.moneybooth.app.core.data.database.entities.ShiftEntity
import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.reconciliation.CashMovementDirection
import com.moneybooth.app.core.domain.reconciliation.CashMovementReason
import com.moneybooth.app.ui.common.AppViewModelFactory

@Composable
fun ShiftActiveScreen(shift: ShiftEntity, container: AppContainer, onEndShift: () -> Unit) {
    val viewModel: ShiftViewModel = viewModel(factory = AppViewModelFactory(container))
    val movements by viewModel.observeCashMovements(shift.id).collectAsStateWithLifecycle(initialValue = emptyList())
    var showAddMovement by remember { mutableStateOf(false) }

    if (showAddMovement) {
        AddCashMovementDialog(
            shift = shift,
            viewModel = viewModel,
            onDismiss = { showAddMovement = false },
        )
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Shift in progress", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Opening cash: ${Money.formatWithCurrency(shift.openingCashMinor)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Button(onClick = { showAddMovement = true }, modifier = Modifier.weight(1f).padding(end = 6.dp)) {
                Text("Log cash movement")
            }
            OutlinedButton(onClick = onEndShift, modifier = Modifier.weight(1f).padding(start = 6.dp)) {
                Text("End shift")
            }
        }

        Text(
            "Cash movements this shift",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 20.dp, bottom = 8.dp),
        )

        LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
            items(movements, key = { it.id }) { movement -> CashMovementRow(movement) }
        }
    }
}

@Composable
private fun CashMovementRow(movement: CashMovementEntity) {
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text(movement.reason.name.replace('_', ' '), style = MaterialTheme.typography.bodyMedium)
                if (movement.note != null) {
                    Text(
                        movement.note,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                    )
                }
            }
            Text(
                (if (movement.direction == CashMovementDirection.IN) "+ " else "− ") +
                    Money.formatWithCurrency(movement.amountMinor),
            )
        }
    }
}

@Composable
private fun AddCashMovementDialog(shift: ShiftEntity, viewModel: ShiftViewModel, onDismiss: () -> Unit) {
    var direction by remember { mutableStateOf(CashMovementDirection.IN) }
    var reason by remember { mutableStateOf(CashMovementReason.CUSTOMER_DEPOSIT_RECEIPT) }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var reasonMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log cash movement") },
        text = {
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { direction = CashMovementDirection.IN },
                        modifier = Modifier.weight(1f).padding(end = 6.dp),
                    ) { Text(if (direction == CashMovementDirection.IN) "✓ Cash In" else "Cash In") }
                    Button(
                        onClick = { direction = CashMovementDirection.OUT },
                        modifier = Modifier.weight(1f).padding(start = 6.dp),
                    ) { Text(if (direction == CashMovementDirection.OUT) "✓ Cash Out" else "Cash Out") }
                }
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
                ExposedDropdownMenuBox(
                    expanded = reasonMenuExpanded,
                    onExpandedChange = { reasonMenuExpanded = it },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                ) {
                    OutlinedTextField(
                        value = reason.name.replace('_', ' '),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Reason") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reasonMenuExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                    )
                    DropdownMenu(expanded = reasonMenuExpanded, onDismissRequest = { reasonMenuExpanded = false }) {
                        CashMovementReason.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name.replace('_', ' ')) },
                                onClick = { reason = option; reasonMenuExpanded = false },
                            )
                        }
                    }
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amountMinor = Money.parseToMinorUnits(amountText)
                if (amountMinor != null && amountMinor > 0L) {
                    viewModel.logCashMovement(
                        shiftId = shift.id,
                        boothId = shift.boothId,
                        direction = direction,
                        amountMinor = amountMinor,
                        reason = reason,
                        note = note.ifBlank { null },
                        createdBy = shift.employeeId,
                    )
                    onDismiss()
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
