package com.moneybooth.app.ui.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.ui.common.AppViewModelFactory

/**
 * First-run wizard: business name, then its first booth. Adding employees/starting a shift
 * happens afterward from the Employees/Shift tabs, matching the app's step-by-step setup flow.
 */
@Composable
fun BusinessSetupScreen(container: AppContainer) {
    val viewModel: OnboardingViewModel = viewModel(factory = AppViewModelFactory(container))
    var step by remember { mutableIntStateOf(0) }
    var businessName by remember { mutableStateOf("") }
    var boothName by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (step == 0) {
            Text("What's your business called?", style = MaterialTheme.typography.headlineMedium)
            Text(
                "This is the business that owns your booths.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
            OutlinedTextField(
                value = businessName,
                onValueChange = { businessName = it },
                label = { Text("Business name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = { if (businessName.isNotBlank()) step = 1 },
                enabled = businessName.isNotBlank(),
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            ) {
                Text("Next")
            }
        } else {
            Text("Name your first booth", style = MaterialTheme.typography.headlineMedium)
            Text(
                "You can add more booths later.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
            OutlinedTextField(
                value = boothName,
                onValueChange = { boothName = it },
                label = { Text("Booth name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    if (boothName.isNotBlank() && !submitting) {
                        submitting = true
                        viewModel.createBusinessAndFirstBooth(businessName, boothName) {}
                    }
                },
                enabled = boothName.isNotBlank() && !submitting,
                modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
            ) {
                Text(if (submitting) "Setting up..." else "Finish setup")
            }
        }
    }
}
