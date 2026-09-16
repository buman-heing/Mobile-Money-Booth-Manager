package com.moneybooth.app.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Tracks whether the current in-memory session is authenticated, and forces re-authentication
 * if the app was backgrounded for longer than [timeoutMillis]. Registered against
 * ProcessLifecycleOwner so it reacts to the whole app going to background, not just one Activity.
 */
class SessionManager(private val timeoutMillis: Long = DEFAULT_TIMEOUT_MILLIS) : DefaultLifecycleObserver {
    private var backgroundedAt: Long? = null

    private val _isAuthenticated = MutableStateFlow(false)
    val isAuthenticated: StateFlow<Boolean> = _isAuthenticated

    fun markAuthenticated() {
        _isAuthenticated.value = true
        backgroundedAt = null
    }

    fun signOut() {
        _isAuthenticated.value = false
    }

    override fun onStop(owner: LifecycleOwner) {
        backgroundedAt = System.currentTimeMillis()
    }

    override fun onStart(owner: LifecycleOwner) {
        val backgroundedAtSnapshot = backgroundedAt
        if (_isAuthenticated.value && backgroundedAtSnapshot != null) {
            val elapsed = System.currentTimeMillis() - backgroundedAtSnapshot
            if (elapsed > timeoutMillis) {
                _isAuthenticated.value = false
            }
        }
        backgroundedAt = null
    }

    companion object {
        const val DEFAULT_TIMEOUT_MILLIS = 5 * 60 * 1000L
    }
}
