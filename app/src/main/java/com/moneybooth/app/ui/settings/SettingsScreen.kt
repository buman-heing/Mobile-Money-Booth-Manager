package com.moneybooth.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.data.DeviceRole
import com.moneybooth.app.core.sync.JoinCode
import com.moneybooth.app.sms.android.SmsPermissionManager
import com.moneybooth.app.ui.common.AppViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(businessId: Long, container: AppContainer, onResetPin: () -> Unit) {
    val viewModel: SettingsViewModel = viewModel(factory = AppViewModelFactory(container))
    val booths by viewModel.observeBooths(businessId).collectAsStateWithLifecycle(initialValue = emptyList())
    val business by container.businessRepository.observeFirst().collectAsStateWithLifecycle(initialValue = null)
    val deviceRole by container.deviceSettingsStore.deviceRoleFlow.collectAsStateWithLifecycle()
    val syncStatus by container.deviceSettingsStore.syncStatusFlow.collectAsStateWithLifecycle()
    val pendingUploads by container.syncOutbox.observePendingCount().collectAsStateWithLifecycle(initialValue = 0)
    var activeBoothId by remember { mutableStateOf(container.deviceSettingsStore.activeBoothId) }
    var smsIngestionEnabled by remember { mutableStateOf(container.deviceSettingsStore.smsIngestionEnabled) }
    var hasSmsPermission by remember { mutableStateOf(viewModel.hasSmsPermission()) }
    var boothMenuExpanded by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { hasSmsPermission = viewModel.hasSmsPermission() }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        SettingsCard(
            title = "What is this phone?",
            subtitle = "The booth phone holds the Airtel Money SIM and records SMS. The owner phone only views.",
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = deviceRole == DeviceRole.BOOTH,
                    onClick = { container.deviceSettingsStore.deviceRole = DeviceRole.BOOTH },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) { Text("Booth phone") }
                SegmentedButton(
                    selected = deviceRole == DeviceRole.OWNER,
                    onClick = { container.deviceSettingsStore.deviceRole = DeviceRole.OWNER },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) { Text("Owner phone") }
            }
        }

        SettingsCard(
            title = "Cloud backup & sharing",
            subtitle = "Changes are saved on this phone first and uploaded whenever there is signal.",
        ) {
            if (deviceRole == DeviceRole.BOOTH) {
                Text("Share code", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    business?.let { JoinCode.forBusiness(it.uid) } ?: "—",
                    style = MaterialTheme.typography.displaySmall,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 6.sp,
                )
                Text(
                    "Read this out to the owner. They type it on their phone to follow this booth.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp),
                )
            }
            SettingsRow("Waiting to upload", if (pendingUploads == 0) "Nothing" else "$pendingUploads record${if (pendingUploads == 1) "" else "s"}")
            SettingsRow("Last upload", syncStatus.lastSyncAt?.let { timeLabel(it) } ?: "Never")
            SettingsRow("Last download", syncStatus.lastPullAt?.let { timeLabel(it) } ?: "Never")
            SettingsRow(
                "Status",
                when {
                    syncStatus.lastError != null -> "Retrying: ${syncStatus.lastError}"
                    syncStatus.lastSyncAt != null -> "Connected"
                    else -> "Waiting for first upload"
                },
            )
        }

        if (deviceRole != DeviceRole.OWNER) {
            SettingsCard(
                title = "Active booth for this device",
                subtitle = "Used to attribute incoming SMS to the right booth.",
            ) {
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
                        shape = MaterialTheme.shapes.small,
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

            SettingsCard(
                title = "Live SMS capture",
                subtitle = if (hasSmsPermission) {
                    "SMS permission granted. The app also works without it via Parser or manual entry."
                } else {
                    "SMS permission not granted. Everything still works via Parser and manual entry."
                },
            ) {
                if (!hasSmsPermission) {
                    Button(
                        onClick = { permissionLauncher.launch(SmsPermissionManager.REQUIRED_PERMISSIONS) },
                        shape = MaterialTheme.shapes.small,
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
                    Text("Capture SMS automatically")
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

        OutlinedButton(
            onClick = onResetPin,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
        ) {
            Text("Reset PIN")
        }
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SettingsCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
            )
            content()
        }
    }
}

@Composable
private fun SettingsRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium)
    }
}

private fun timeLabel(epochMillis: Long): String =
    SimpleDateFormat("d MMM, HH:mm", Locale.getDefault()).format(Date(epochMillis))
