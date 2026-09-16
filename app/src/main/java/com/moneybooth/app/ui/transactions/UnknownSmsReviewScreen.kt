package com.moneybooth.app.ui.transactions

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.moneybooth.app.core.data.database.entities.RawSmsEntity
import com.moneybooth.app.core.domain.transactions.BusinessClassification
import com.moneybooth.app.core.domain.transactions.TransactionDirection
import com.moneybooth.app.core.domain.transactions.TransactionType
import com.moneybooth.app.ui.common.AppViewModelFactory
import com.moneybooth.app.ui.common.EmptyState

@Composable
fun UnknownSmsReviewScreen(businessId: Long, container: AppContainer) {
    val viewModel: UnknownSmsReviewViewModel = viewModel(factory = AppViewModelFactory(container))
    val pending by viewModel.observeNeedsReview().collectAsStateWithLifecycle(initialValue = emptyList())
    var selected by remember { mutableStateOf<RawSmsEntity?>(null) }

    val current = selected
    if (current != null) {
        ResolveForm(
            businessId = businessId,
            rawSms = current,
            viewModel = viewModel,
            onDone = { selected = null },
            onCancel = { selected = null },
        )
        return
    }

    if (pending.isEmpty()) {
        EmptyState(
            title = "Nothing needs review",
            subtitle = "Unrecognized SMS messages will show up here.",
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        items(pending, key = { it.id }) { sms ->
            Card(modifier = Modifier.fillMaxWidth().clickable { selected = sms }) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("New message requires review", style = MaterialTheme.typography.titleMedium)
                    Text(
                        sms.sender,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                    Text(sms.rawBody, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 6.dp))
                }
            }
        }
    }
}

@Composable
private fun ResolveForm(
    businessId: Long,
    rawSms: RawSmsEntity,
    viewModel: UnknownSmsReviewViewModel,
    onDone: () -> Unit,
    onCancel: () -> Unit,
) {
    val booths by viewModel.observeBooths(businessId).collectAsStateWithLifecycle(initialValue = emptyList())
    var boothId by remember { mutableStateOf(booths.firstOrNull()?.id) }
    var type by remember { mutableStateOf(TransactionType.OTHER) }
    var direction by remember { mutableStateOf(TransactionDirection.UNKNOWN) }
    var amountText by remember { mutableStateOf("") }
    var party by remember { mutableStateOf("") }
    var classification by remember { mutableStateOf(BusinessClassification.UNKNOWN) }
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var directionMenuExpanded by remember { mutableStateOf(false) }
    var classificationMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Review message", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(rawSms.rawBody, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "From ${rawSms.sender}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }

        ExposedDropdownMenuBox(
            expanded = typeMenuExpanded,
            onExpandedChange = { typeMenuExpanded = it },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
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

        ExposedDropdownMenuBox(
            expanded = directionMenuExpanded,
            onExpandedChange = { directionMenuExpanded = it },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            OutlinedTextField(
                value = direction.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Direction") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = directionMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
            )
            DropdownMenu(expanded = directionMenuExpanded, onDismissRequest = { directionMenuExpanded = false }) {
                TransactionDirection.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name) },
                        onClick = { direction = option; directionMenuExpanded = false },
                    )
                }
            }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("Amount (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        OutlinedTextField(
            value = party,
            onValueChange = { party = it },
            label = { Text("Relevant party (optional)") },
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

        Button(
            onClick = {
                val resolvedBoothId = boothId ?: booths.firstOrNull()?.id
                if (resolvedBoothId != null) {
                    viewModel.resolve(rawSms.id, resolvedBoothId, type, direction, amountText, party, classification, onDone)
                }
            },
            enabled = booths.isNotEmpty(),
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) {
            Text("Save interpretation")
        }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Cancel")
        }
    }
}
