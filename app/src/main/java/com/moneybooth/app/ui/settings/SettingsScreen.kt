package com.moneybooth.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.sms.android.SmsPermissionManager
import com.moneybooth.app.ui.common.AppViewModelFactory

@Composable
fun SettingsScreen(businessId: Long, container: AppContainer, onResetPin: () -> Unit) {
    val viewModel: SettingsViewModel = viewModel(factory = AppViewModelFactory(container))
    val booths by viewModel.observeBooths(businessId).collectAsStateWithLifecycle(initialValue = emptyList())
    var activeBoothId by remember { mutableStateOf(container.deviceSettingsStore.activeBoothId) }
    var smsIngestionEnabled by remember { mutableStateOf(container.deviceSettingsStore.smsIngestionEnabled) }
    var hasSmsPermission by remember { mutableStateOf(viewModel.hasSmsPermission()) }
    var boothMenuExpanded by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { hasSmsPermission = viewModel.hasSmsPermission() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Active booth for this device", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Used to attribute incoming SMS to the right booth.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                )
                ExposedDropdownMenuBox(
                    expanded = boothMenuExpanded,
                    onExpandedChange = { boothMenuExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = booths.firstOrNull { it.id == activeBoothId }?.name ?: "Not set",
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
                                onClick = {
                                    activeBoothId = booth.id
                                    container.deviceSettingsStore.activeBoothId = booth.id
                                    boothMenuExpanded = false
                                },
                            )
                        }
                    }
                }
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(top = 16.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Live SMS ingestion", style = MaterialTheme.typography.titleMedium)
                Text(
                    if (hasSmsPermission) {
                        "SMS permission granted. The app can also work entirely without this — use Dev Tools or manual entry."
                    } else {
                        "SMS permission not granted. Everything still works via Dev Tools and manual entry."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
                )
                if (!hasSmsPermission) {
                    Button(
                        onClick = { permissionLauncher.launch(SmsPermissionManager.REQUIRED_PERMISSIONS) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    ) {
                        Text("Grant SMS permission")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Enable live SMS ingestion")
                    Switch(
                        checked = smsIngestionEnabled && hasSmsPermission,
                        enabled = hasSmsPermission,
                        onCheckedChange = {
                            smsIngestionEnabled = it
                            container.deviceSettingsStore.smsIngestionEnabled = it
                        },
                    )
                }
            }
        }

        OutlinedButton(onClick = onResetPin, modifier = Modifier.fillMaxWidth().padding(top = 20.dp)) {
            Text("Reset PIN")
        }
    }
}
