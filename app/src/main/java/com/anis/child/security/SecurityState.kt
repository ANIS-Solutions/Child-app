package com.anis.child.security

sealed class SecurityState {
    data object Ok : SecurityState()
    data object Emulator : SecurityState()
    data object Rooted : SecurityState()
    data class IntegrityFailed(val reason: String) : SecurityState()
    data object Tampered : SecurityState()
    data object Debuggable : SecurityState()
}
