package com.moneybooth.app.ui.employees

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.domain.employees.EmployeeRole
import com.moneybooth.app.ui.common.AppViewModelFactory

@Composable
fun EmployeeFormScreen(businessId: Long, container: AppContainer, onDone: () -> Unit) {
    val viewModel: EmployeeViewModel = viewModel(factory = AppViewModelFactory(container))

    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(EmployeeRole.ATTENDANT) }
    var submitting by remember { mutableStateOf(false) }
    val roles = listOf(EmployeeRole.ATTENDANT, EmployeeRole.MANAGER, EmployeeRole.OWNER)

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Add employee", style = MaterialTheme.typography.headlineMedium)
        Text(
            "Employees are chosen when a shift opens, so each day's transactions are tied to who was working.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Full name") },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone (optional)") },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        Text(
            "Role",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 16.dp, bottom = 6.dp),
        )
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            roles.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = role == option,
                    onClick = { role = option },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = roles.size),
                ) { Text(option.name.lowercase().replaceFirstChar { it.uppercase() }) }
            }
        }
        Button(
            onClick = {
                if (name.isNotBlank() && !submitting) {
                    submitting = true
                    viewModel.createEmployee(businessId, name.trim(), phone, role, onDone)
                }
            },
            enabled = name.isNotBlank() && !submitting,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp).height(54.dp),
        ) {
            Text("Save", style = MaterialTheme.typography.titleMedium)
        }
    }
}
