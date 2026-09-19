package com.moneybooth.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.moneybooth.app.ui.common.timeLabel

@Composable
fun SettingsScreen(businessId: Long, container: AppContainer, onResetPin: () -> Unit, onOpenParser: () -> Unit) {
    val viewModel: SettingsViewModel = viewModel(factory = AppViewModelFactory(container))
    val business by container.businessRepository.observeFirst().collectAsStateWithLifecycle(initialValue = null)
    val deviceRole by container.deviceSettingsStore.deviceRoleFlow.collectAsStateWithLifecycle()
    val syncStatus by container.deviceSettingsStore.syncStatusFlow.collectAsStateWithLifecycle()
    val pendingUploads by container.syncOutbox.observePendingCount().collectAsStateWithLifecycle(initialValue = 0)
    var smsIngestionEnabled by remember { mutableStateOf(container.deviceSettingsStore.smsIngestionEnabled) }
    var hasSmsPermission by remember { mutableStateOf(viewModel.hasSmsPermission()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { hasSmsPermission = viewModel.hasSmsPermission() }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Settings", style = MaterialTheme.typography.headlineMedium)

        SettingsCard(
            title = "Share code",
            subtitle = "Anyone who types this code on their phone joins ${business?.name ?: "this business"}. Read it out to the other phone.",
        ) {
            Text(
                business?.let { JoinCode.forBusiness(it.uid) } ?: "—",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
                letterSpacing = 6.sp,
            )
        }

        if (deviceRole == DeviceRole.BOOTH) {
            SettingsCard(
                title = "SMS recording",
                subtitle = if (hasSmsPermission) {
                    "Airtel Money messages are saved the moment they arrive."
                } else {
                    "The app needs permission to read SMS on this phone."
                },
            ) {
                if (!hasSmsPermission) {
                    Button(
                        onClick = { permissionLauncher.launch(SmsPermissionManager.REQUIRED_PERMISSIONS) },
                        shape = MaterialTheme.shapes.small,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    ) {
                        Text("Allow SMS access")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Record SMS automatically")
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

        SettingsCard(
            title = "Cloud",
            subtitle = "Everything is saved on this phone first and shared whenever there is signal.",
        ) {
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

        SettingsCard(
            title = "This phone is the…",
            subtitle = "Employee phone holds the Airtel Money SIM and records SMS. Owner phone follows from anywhere.",
        ) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = deviceRole == DeviceRole.BOOTH,
                    onClick = { container.deviceSettingsStore.deviceRole = DeviceRole.BOOTH },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                ) { Text("Employee phone") }
                SegmentedButton(
                    selected = deviceRole == DeviceRole.OWNER,
                    onClick = { container.deviceSettingsStore.deviceRole = DeviceRole.OWNER },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                ) { Text("Owner phone") }
            }
        }

        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).clickable(onClick = onOpenParser),
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Test an SMS message", style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Paste a message to see how the app reads it.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
