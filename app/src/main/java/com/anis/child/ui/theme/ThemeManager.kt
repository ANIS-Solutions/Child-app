package com.anis.child.ui.theme

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.anis.child.data.PreferenceManager

object ThemeManager {
    var isDarkMode by mutableStateOf(false)
        private set

    private var preferenceManager: PreferenceManager? = null

    fun init(prefs: PreferenceManager) {
        preferenceManager = prefs
        isDarkMode = prefs.isDarkTheme
    }

    fun toggle() {
        isDarkMode = !isDarkMode
        preferenceManager?.isDarkTheme = isDarkMode
    }
}
