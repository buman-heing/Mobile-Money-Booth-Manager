package com.moneybooth.app.ui.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.ui.auth.AuthScaffold
import com.moneybooth.app.ui.common.AppViewModelFactory

@Composable
fun BusinessSetupScreen(container: AppContainer, onJoinInstead: () -> Unit) {
    val viewModel: OnboardingViewModel = viewModel(factory = AppViewModelFactory(container))
    var businessName by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }

    AuthScaffold(
        title = "Name your business",
        subtitle = "This is what the owner and employees will see. You can add employees afterwards.",
    ) {
        OutlinedTextField(
            value = businessName,
            onValueChange = { businessName = it },
            label = { Text("Business name") },
            singleLine = true,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth(),
        )
        Button(
            onClick = {
                if (businessName.isNotBlank() && !submitting) {
                    submitting = true
                    viewModel.createBusiness(businessName.trim()) {}
                }
            },
            enabled = businessName.isNotBlank() && !submitting,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(54.dp),
        ) {
            Text(if (submitting) "Setting up…" else "Get started", style = MaterialTheme.typography.titleMedium)
        }
        TextButton(onClick = onJoinInstead, enabled = !submitting, modifier = Modifier.padding(top = 8.dp)) {
            Text("I have a share code instead")
        }
    }
}
