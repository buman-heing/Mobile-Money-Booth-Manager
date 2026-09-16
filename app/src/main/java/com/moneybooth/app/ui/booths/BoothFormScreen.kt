package com.moneybooth.app.ui.booths

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.ui.common.AppViewModelFactory

@Composable
fun BoothFormScreen(businessId: Long, container: AppContainer, onDone: () -> Unit) {
    val viewModel: BoothViewModel = viewModel(factory = AppViewModelFactory(container))
    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var submitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text("Add booth", style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Booth name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        )
        OutlinedTextField(
            value = location,
            onValueChange = { location = it },
            label = { Text("Location (optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
        )
        Button(
            onClick = {
                if (name.isNotBlank() && !submitting) {
                    submitting = true
                    viewModel.createBooth(businessId, name, location, onDone)
                }
            },
            enabled = name.isNotBlank() && !submitting,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp),
        ) {
            Text("Save")
        }
    }
}
