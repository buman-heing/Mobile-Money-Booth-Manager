package com.moneybooth.app.ui.employees

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.data.database.entities.EmployeeEntity
import com.moneybooth.app.ui.common.AppViewModelFactory
import com.moneybooth.app.ui.common.EmptyState

@Composable
fun EmployeeListScreen(businessId: Long, container: AppContainer, onAddEmployee: () -> Unit) {
    val viewModel: EmployeeViewModel = viewModel(factory = AppViewModelFactory(container))
    val employees by viewModel.observeEmployees(businessId).collectAsStateWithLifecycle(initialValue = emptyList())

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = onAddEmployee) {
                Icon(Icons.Filled.Add, contentDescription = "Add employee")
            }
        },
    ) { padding ->
        if (employees.isEmpty()) {
            EmptyState(
                title = "No employees yet",
                subtitle = "Tap + to add your first employee.",
                modifier = Modifier.fillMaxSize().padding(padding),
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(employees, key = { it.id }) { employee -> EmployeeRow(employee) }
            }
        }
    }
}

@Composable
private fun EmployeeRow(employee: EmployeeEntity) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(employee.name, style = MaterialTheme.typography.titleMedium)
            Text(
                employee.role.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
            )
        }
    }
}
