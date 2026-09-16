package com.moneybooth.app.ui.shifts

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
import com.moneybooth.app.ui.common.AppViewModelFactory
import com.moneybooth.app.ui.common.EmptyState
import com.moneybooth.app.ui.reconciliation.ReconciliationSummaryScreen

@Composable
fun ShiftScreen(businessId: Long, container: AppContainer) {
    val viewModel: ShiftViewModel = viewModel(factory = AppViewModelFactory(container))
    val booths by viewModel.observeBooths(businessId).collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedBoothId by remember { mutableStateOf<Long?>(null) }
    var boothMenuExpanded by remember { mutableStateOf(false) }
    LaunchedEffect(booths) { if (selectedBoothId == null) selectedBoothId = booths.firstOrNull()?.id }

    val boothId = selectedBoothId
    if (boothId == null) {
        EmptyState(title = "No booth yet", subtitle = "Create a booth first.", modifier = Modifier.fillMaxSize())
        return
    }

    val openShift by viewModel.observeOpenShift(boothId).collectAsStateWithLifecycle(initialValue = null)
    var showEndForm by remember { mutableStateOf(false) }
    var closedShiftIdForSummary by remember { mutableStateOf<Long?>(null) }

    val summaryShiftId = closedShiftIdForSummary
    if (summaryShiftId != null) {
        ReconciliationSummaryScreen(
            shiftId = summaryShiftId,
            container = container,
            onDone = { closedShiftIdForSummary = null; showEndForm = false },
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        if (booths.size > 1) {
            ExposedDropdownMenuBox(
                expanded = boothMenuExpanded,
                onExpandedChange = { boothMenuExpanded = it },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
            ) {
                OutlinedTextField(
                    value = booths.firstOrNull { it.id == boothId }?.name ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Booth") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = boothMenuExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                )
                DropdownMenu(expanded = boothMenuExpanded, onDismissRequest = { boothMenuExpanded = false }) {
                    booths.forEach { booth ->
                        DropdownMenuItem(
                            text = { Text(booth.name) },
                            onClick = { selectedBoothId = booth.id; boothMenuExpanded = false },
                        )
                    }
                }
            }
        }

        val currentShift = openShift
        when {
            currentShift == null -> ShiftStartScreen(
                businessId = businessId,
                boothId = boothId,
                container = container,
                onStarted = {},
            )
            showEndForm -> ShiftEndScreen(
                shift = currentShift,
                container = container,
                onClosed = { reconciliationId ->
                    if (reconciliationId != null) closedShiftIdForSummary = currentShift.id
                    showEndForm = false
                },
                onCancel = { showEndForm = false },
            )
            else -> ShiftActiveScreen(
                shift = currentShift,
                container = container,
                onEndShift = { showEndForm = true },
            )
        }
    }
}
