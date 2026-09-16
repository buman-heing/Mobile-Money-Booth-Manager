package com.moneybooth.app.ui.transactions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.BusinessClassification
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.ui.common.AppViewModelFactory

@Composable
fun TransactionFormScreen(businessId: Long, container: AppContainer, onDone: () -> Unit) {
    val viewModel: TransactionsViewModel = viewModel(factory = AppViewModelFactory(container))
    val booths by viewModel.observeBooths(businessId).collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedBoothId by remember { mutableStateOf<Long?>(null) }
    LaunchedEffect(booths) { if (selectedBoothId == null) selectedBoothId = booths.firstOrNull()?.id }

    var type by remember { mutableStateOf(TransactionType.OTHER) }
    var direction by remember { mutableStateOf(TransactionDirection.IN) }
    var amountText by remember { mutableStateOf("") }
    var classification by remember { mutableStateOf(BusinessClassification.UNKNOWN) }
    var note by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    var typeMenuExpanded by remember { mutableStateOf(false) }
    var classificationMenuExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
    ) {
        Text("Add transaction", style = MaterialTheme.typography.headlineMedium)

        ExposedDropdownMenuBox(
            expanded = typeMenuExpanded,
            onExpandedChange = { typeMenuExpanded = it },
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) {
            OutlinedTextField(
                value = type.name.replace('_', ' '),
                onValueChange = {},
                readOnly = true,
                label = { Text("Transaction type") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
            )
            DropdownMenu(expanded = typeMenuExpanded, onDismissRequest = { typeMenuExpanded = false }) {
                TransactionType.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name.replace('_', ' ')) },
                        onClick = { type = option; typeMenuExpanded = false },
                    )
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = { direction = TransactionDirection.IN },
                modifier = Modifier.weight(1f).padding(top = 12.dp, end = 6.dp),
            ) { Text(if (direction == TransactionDirection.IN) "✓ Money In" else "Money In") }
            Button(
                onClick = { direction = TransactionDirection.OUT },
                modifier = Modifier.weight(1f).padding(top = 12.dp, start = 6.dp),
            ) { Text(if (direction == TransactionDirection.OUT) "✓ Money Out" else "Money Out") }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount (${Money.DEFAULT_CURRENCY})") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )

        ExposedDropdownMenuBox(
            expanded = classificationMenuExpanded,
            onExpandedChange = { classificationMenuExpanded = it },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            OutlinedTextField(
                value = classification.name.replace('_', ' '),
                onValueChange = {},
                readOnly = true,
                label = { Text("Business classification") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = classificationMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
            )
            DropdownMenu(expanded = classificationMenuExpanded, onDismissRequest = { classificationMenuExpanded = false }) {
                BusinessClassification.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name.replace('_', ' ')) },
                        onClick = { classification = option; classificationMenuExpanded = false },
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

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            onClick = {
                val amountMinor = Money.parseToMinorUnits(amountText)
                val boothId = selectedBoothId
                when {
                    amountMinor == null || amountMinor <= 0L -> error = "Enter a valid amount."
                    boothId == null -> error = "Create a booth first."
                    else -> {
                        error = null
                        submitting = true
                        viewModel.createManual(type, direction, amountMinor, boothId, classification, note.ifBlank { null }, onDone)
                    }
                }
            },
            enabled = !submitting,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) {
            Text("Save")
        }
    }
}
