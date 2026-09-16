package com.moneybooth.app.ui.shifts

import androidx.compose.foundation.layout.Column
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
import com.moneybooth.app.core.data.repository.OpenShiftResult
import com.moneybooth.app.core.domain.accounting.Money
import com.moneybooth.app.ui.common.AppViewModelFactory
import com.moneybooth.app.ui.common.EmptyState

@Composable
fun ShiftStartScreen(businessId: Long, boothId: Long, container: AppContainer, onStarted: () -> Unit) {
    val viewModel: ShiftViewModel = viewModel(factory = AppViewModelFactory(container))
    val employees by viewModel.observeEmployees(businessId).collectAsStateWithLifecycle(initialValue = emptyList())

    if (employees.isEmpty()) {
        EmptyState(
            title = "No employees yet",
            subtitle = "Add an employee before starting a shift.",
            modifier = Modifier.fillMaxSize(),
        )
        return
    }

    var employeeId by remember { mutableStateOf<Long?>(null) }
    var employeeMenuExpanded by remember { mutableStateOf(false) }
    var openingCashText by remember { mutableStateOf("") }
    var openingMobileMoneyText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("Start shift", style = MaterialTheme.typography.headlineMedium)

        ExposedDropdownMenuBox(
            expanded = employeeMenuExpanded,
            onExpandedChange = { employeeMenuExpanded = it },
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) {
            OutlinedTextField(
                value = employees.firstOrNull { it.id == employeeId }?.name ?: "Select employee",
                onValueChange = {},
                readOnly = true,
                label = { Text("Employee") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = employeeMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
            )
            DropdownMenu(expanded = employeeMenuExpanded, onDismissRequest = { employeeMenuExpanded = false }) {
                employees.forEach { employee ->
                    DropdownMenuItem(
                        text = { Text(employee.name) },
                        onClick = { employeeId = employee.id; employeeMenuExpanded = false },
                    )
                }
            }
        }

        OutlinedTextField(
            value = openingCashText,
            onValueChange = { openingCashText = it },
            label = { Text("Opening cash (${Money.DEFAULT_CURRENCY})") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        OutlinedTextField(
            value = openingMobileMoneyText,
            onValueChange = { openingMobileMoneyText = it },
            label = { Text("Opening mobile money balance (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )

        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }

        Button(
            onClick = {
                val selectedEmployeeId = employeeId
                val openingCash = Money.parseToMinorUnits(openingCashText)
                val openingMm = openingMobileMoneyText.ifBlank { null }?.let { Money.parseToMinorUnits(it) }
                when {
                    selectedEmployeeId == null -> error = "Select an employee."
                    openingCash == null -> error = "Enter a valid opening cash amount."
                    else -> {
                        error = null
                        viewModel.openShift(boothId, selectedEmployeeId, openingCash, openingMm) { result ->
                            when (result) {
                                is OpenShiftResult.Success -> onStarted()
                                is OpenShiftResult.AlreadyOpenForBooth -> error = "This booth already has an open shift."
                                is OpenShiftResult.AlreadyOpenForEmployee -> error = "This employee already has an open shift."
                            }
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) {
            Text("Start shift")
        }
    }
}
