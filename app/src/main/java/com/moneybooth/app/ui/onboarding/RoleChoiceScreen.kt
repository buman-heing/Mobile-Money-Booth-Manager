package com.moneybooth.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.moneybooth.app.core.data.DeviceRole
import com.moneybooth.app.ui.auth.AuthScaffold
import com.moneybooth.app.ui.theme.Magenta

@Composable
fun RoleChoiceScreen(onChosen: (DeviceRole) -> Unit) {
    AuthScaffold(
        title = "What is this phone for?",
        subtitle = "You can change this later in Settings.",
    ) {
        RoleCard(
            title = "Booth phone",
            body = "This phone holds the Airtel Money SIM at the booth. It records every transaction SMS automatically.",
            icon = Icons.Rounded.Storefront,
            tint = MaterialTheme.colorScheme.primary,
            onClick = { onChosen(DeviceRole.BOOTH) },
        )
        Spacer(Modifier.height(12.dp))
        RoleCard(
            title = "Owner phone",
            body = "Follow one or more booths from anywhere. Enter the share code shown on the booth phone.",
            icon = Icons.Rounded.Visibility,
            tint = Magenta,
            onClick = { onChosen(DeviceRole.OWNER) },
        )
    }
}

@Composable
private fun RoleCard(title: String, body: String, icon: ImageVector, tint: Color, onClick: () -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).background(tint.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(icon, contentDescription = null, tint = tint)
            }
            Column(modifier = Modifier.padding(start = 14.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
