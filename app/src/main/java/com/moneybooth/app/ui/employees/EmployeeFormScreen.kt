package com.moneybooth.app.ui.employees

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
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
import com.moneybooth.app.core.domain.employees.EmployeeRole
import com.moneybooth.app.ui.common.AppViewModelFactory

@Composable
fun EmployeeFormScreen(businessId: Long, container: AppContainer, onDone: () -> Unit) {
    val viewModel: EmployeeViewModel = viewModel(factory = AppViewModelFactory(container))
    val booths by viewModel.observeBooths(businessId).collectAsStateWithLifecycle(initialValue = emptyList())

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(EmployeeRole.ATTENDANT) }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var assignedBoothId by remember { mutableStateOf<Long?>(null) }
    var boothMenuExpanded by remember { mutableStateOf(false) }
    var submitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text("Add employee", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )

        ExposedDropdownMenuBox(
            expanded = roleMenuExpanded,
            onExpandedChange = { roleMenuExpanded = it },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        ) {
            OutlinedTextField(
                value = role.name.lowercase().replaceFirstChar { it.uppercase() },
                onValueChange = {},
                readOnly = true,
                label = { Text("Role") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
            )
            DropdownMenu(expanded = roleMenuExpanded, onDismissRequest = { roleMenuExpanded = false }) {
                EmployeeRole.entries.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) },
                        onClick = { role = option; roleMenuExpanded = false },
                    )
                }
            }
        }

        if (booths.isNotEmpty()) {
            ExposedDropdownMenuBox(
                expanded = boothMenuExpanded,
                onExpandedChange = { boothMenuExpanded = it },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                OutlinedTextField(
                    value = booths.firstOrNull { it.id == assignedBoothId }?.name ?: "Unassigned",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Assigned booth") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = boothMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                DropdownMenu(expanded = boothMenuExpanded, onDismissRequest = { boothMenuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Unassigned") },
                        onClick = { assignedBoothId = null; boothMenuExpanded = false },
                    )
                    booths.forEach { booth ->
                        DropdownMenuItem(
                            text = { Text(booth.name) },
                            onClick = { assignedBoothId = booth.id; boothMenuExpanded = false },
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (name.isNotBlank() && !submitting) {
                    submitting = true
                    viewModel.createEmployee(businessId, name, phone, role, assignedBoothId, onDone)
                }
            },
            enabled = name.isNotBlank() && !submitting,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) {
            Text("Save")
        }
    }
}
