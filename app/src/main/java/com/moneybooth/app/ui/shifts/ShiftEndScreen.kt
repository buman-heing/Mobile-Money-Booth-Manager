package com.moneybooth.app.ui.shifts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.data.database.entities.ShiftEntity
import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.ui.common.AppViewModelFactory

@Composable
fun ShiftEndScreen(shift: ShiftEntity, container: AppContainer, onClosed: (reconciliationId: Long?) -> Unit, onCancel: () -> Unit) {
    val viewModel: ShiftViewModel = viewModel(factory = AppViewModelFactory(container))
    var actualCashText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Close shift", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Count the physical cash in the drawer and enter it below.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
        )

        OutlinedTextField(
            value = actualCashText,
            onValueChange = { actualCashText = it },
            label = { Text("Actual cash counted (${Money.DEFAULT_CURRENCY})") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes (optional)") },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            onClick = {
                val actualCash = Money.parseToMinorUnits(actualCashText)
                if (actualCash == null) {
                    error = "Enter the counted cash amount."
                } else {
                    error = null
                    submitting = true
                    viewModel.closeShift(shift.id, actualCash, notes.ifBlank { null }, onClosed)
                }
            },
            enabled = !submitting,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) {
            Text("Close shift and reconcile")
        }
        OutlinedButton(onClick = onCancel, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            Text("Cancel")
        }
    }
}
