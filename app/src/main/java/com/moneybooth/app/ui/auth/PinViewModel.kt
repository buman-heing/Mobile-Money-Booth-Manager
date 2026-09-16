package com.moneybooth.app.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.moneybooth.app.security.PinCredentialStore
import com.moneybooth.app.security.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PinViewModel(
    private val pinStore: PinCredentialStore,
    private val sessionManager: SessionManager,
) : ViewModel() {

    fun isPinSet(): Boolean = pinStore.isPinSet()

    fun setPin(pin: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.Default) { pinStore.setPin(pin) }
            sessionManager.markAuthenticated()
            onResult(true)
        }
    }

    fun verifyPin(pin: String, onResult: (success: Boolean, failedAttempts: Int) -> Unit) {
        viewModelScope.launch {
            val ok = withContext(Dispatchers.Default) { pinStore.verifyPin(pin) }
            if (ok) sessionManager.markAuthenticated()
            onResult(ok, pinStore.failedAttempts())
        }
    }
}
