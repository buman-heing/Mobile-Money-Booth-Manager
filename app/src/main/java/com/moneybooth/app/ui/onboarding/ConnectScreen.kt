package com.moneybooth.app.ui.onboarding

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneybooth.app.core.data.AppContainer
import com.moneybooth.app.core.sync.JoinCode
import com.moneybooth.app.ui.auth.AuthScaffold
import kotlinx.coroutines.launch

/** Owner phone: type the booth phone's share code, then pull that business down. */
@Composable
fun ConnectScreen(container: AppContainer, onCreateInstead: () -> Unit) {
    val scope = rememberCoroutineScope()
    var code by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }

    AuthScaffold(
        title = "Join your business",
        subtitle = "Open Settings on the phone that already has the business and read out its 6-letter share code.",
    ) {
        OutlinedTextField(
            value = code,
            onValueChange = { code = JoinCode.normalize(it).take(JoinCode.LENGTH) },
            label = { Text("Share code") },
            singleLine = true,
            enabled = !busy,
            shape = MaterialTheme.shapes.small,
            textStyle = TextStyle(fontSize = 24.sp, letterSpacing = 6.sp, textAlign = TextAlign.Center),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
            modifier = Modifier.fillMaxWidth(),
        )
        if (error != null) {
            Text(
                error!!,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp),
            )
        }
        Button(
            onClick = {
                busy = true
                error = null
                scope.launch {
                    try {
                        val businessUid = container.remoteStore.lookupJoinCode(code)
                        if (businessUid == null) {
                            error = "No business found for that code. Check it and try again."
                        } else {
                            container.deviceSettingsStore.cloudBusinessUid = businessUid
                            container.cloudPuller.pullOnce(businessUid)
                            container.cloudSyncController.start()
                        }
                    } catch (e: Exception) {
                        error = "Couldn't reach the cloud. Check your connection and try again."
                    } finally {
                        busy = false
                    }
                }
            },
            enabled = code.length == JoinCode.LENGTH && !busy,
            shape = MaterialTheme.shapes.small,
            modifier = Modifier.fillMaxWidth().padding(top = 20.dp).height(54.dp),
        ) {
            if (busy) CircularProgressIndicator(modifier = Modifier.height(22.dp), strokeWidth = 2.dp)
            else Text("Connect", style = MaterialTheme.typography.titleMedium)
        }
        TextButton(onClick = onCreateInstead, enabled = !busy, modifier = Modifier.padding(top = 8.dp)) {
            Text("Set up a new business instead")
        }
    }
}
