package com.example.teamnest

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = AppDatabase.getDatabase(application).userPreferencesDao()
    private val _isDarkTheme = mutableStateOf(false)
    val isDarkTheme: State<Boolean> = _isDarkTheme

    init {
        viewModelScope.launch {
            dao.getPreferences().collectLatest { prefs ->
                _isDarkTheme.value = prefs?.isDarkTheme ?: false
            }
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            val current = _isDarkTheme.value
            dao.savePreferences(
                UserPreferences(
                    id = 1, 
                    isDarkTheme = !current,
                    // Keep existing user info if any
                    userId = null, 
                    userName = null, 
                    userEmail = null
                )
            )
        }
    }
}
