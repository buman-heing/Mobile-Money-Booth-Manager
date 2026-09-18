package com.moneybooth.app.ui.devtools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.core.domain.transactions.ValidationResult
import com.moneybooth.app.sms.common.IngestionResult
import com.moneybooth.app.ui.common.AppViewModelFactory

@Composable
fun DevSmsParserScreen(container: AppContainer) {
    val viewModel: DevSmsParserViewModel = viewModel(factory = AppViewModelFactory(container))
    val state by viewModel.state.collectAsStateWithLifecycle()
    var sender by remember { mutableStateOf("AIRTEL") }
    var body by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Developer SMS parser", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Paste a provider SMS to test parsing without needing a real device or SIM.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        OutlinedTextField(
            value = sender,
            onValueChange = { sender = it },
            label = { Text("Sender ID (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("SMS text") },
            modifier = Modifier.fillMaxWidth().height(140.dp).padding(top = 12.dp),
        )

        Button(
            onClick = { viewModel.parse(sender, body) },
            enabled = body.isNotBlank(),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            Text("Parse")
        }

        when (val s = state) {
            is DevSmsUiState.Idle -> Unit

            is DevSmsUiState.NoMatch -> {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Not recognized", style = MaterialTheme.typography.titleMedium)
                        Text(s.reason, modifier = Modifier.padding(top = 4.dp))
                    }
                }
            }

            is DevSmsUiState.Previewed -> {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Parse result", style = MaterialTheme.typography.titleMedium)
                        PreviewRow("Provider", s.preview.providerName)
                        PreviewRow("Type", s.preview.transactionType.name.replace('_', ' '))
                        PreviewRow("Status", s.preview.status.name)
                        PreviewRow("Direction", s.preview.direction.name)
                        PreviewRow(
                            "Amount",
                            s.preview.amountMinor?.let { Money.formatWithCurrency(it) } ?: "Not supplied",
                        )
                        PreviewRow("Transaction ID", s.preview.transactionId ?: "—")
                        PreviewRow(
                            "Balance after",
                            s.preview.balanceAfterMinor?.let { Money.formatWithCurrency(it) } ?: "Not supplied",
                        )
                        PreviewRow(
                            "Commission",
                            s.preview.commissionMinor?.let { Money.formatWithCurrency(it) } ?: "—",
                        )
                        PreviewRow("Party", s.preview.party ?: "—")
                        PreviewRow("Confidence", s.preview.confidence.name)
                        PreviewRow(
                            "Validation",
                            when (val v = s.preview.validation) {
                                is ValidationResult.Valid -> "Valid"
                                is ValidationResult.Invalid -> "Invalid: ${v.reason}"
                            },
                        )

                        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                            Button(
                                onClick = { viewModel.accept() },
                                modifier = Modifier.weight(1f).padding(end = 6.dp),
                            ) { Text("Accept") }
                            OutlinedButton(
                                onClick = { viewModel.reject() },
                                modifier = Modifier.weight(1f).padding(start = 6.dp),
                            ) { Text("Reject") }
                        }
                    }
                }
            }

            is DevSmsUiState.Accepted -> {
                Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            when (s.outcome) {
                                is IngestionResult.Ledgered -> "Saved to the ledger."
                                is IngestionResult.Duplicate -> "Already in the ledger — duplicate transaction ID was not re-added."
                                is IngestionResult.NeedsReview -> "Saved for review: ${s.outcome.reason}"
                            },
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Button(
                            onClick = { viewModel.reset(); body = "" },
                            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        ) { Text("Parse another") }
                    }
                }
            }
        }
    }
}

@Composable
private fun PreviewRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.outline)
        Text(value)
    }
}
