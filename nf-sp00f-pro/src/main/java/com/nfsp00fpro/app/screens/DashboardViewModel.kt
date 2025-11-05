package com.nfsp00fpro.app.screens

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nfsp00fpro.app.modules.EmvDatabase
import com.nfsp00fpro.app.modules.ModMainNfsp00f
import com.nfsp00fpro.app.modules.CardSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Dashboard ViewModel
 * Manages dashboard state and database interactions
 */
class DashboardViewModel(
    private val context: Context,
    private val emvDatabase: EmvDatabase
) : ViewModel() {

    private val _hardwareReady = MutableStateFlow(false)
    val hardwareReady: StateFlow<Boolean> = _hardwareReady.asStateFlow()

    private val _recentSessions = MutableStateFlow<List<CardSession>>(emptyList())
    val recentSessions: StateFlow<List<CardSession>> = _recentSessions.asStateFlow()

    // Hardware status states
    private val _androidNfcStatus = MutableStateFlow("Available")
    val androidNfcStatus: StateFlow<String> = _androidNfcStatus.asStateFlow()

    private val _bluetoothStatus = MutableStateFlow("Available")
    val bluetoothStatus: StateFlow<String> = _bluetoothStatus.asStateFlow()

    private val _pn532BluetoothStatus = MutableStateFlow("Scanning")
    val pn532BluetoothStatus: StateFlow<String> = _pn532BluetoothStatus.asStateFlow()

    private val _pn532UsbStatus = MutableStateFlow("Not Available")
    val pn532UsbStatus: StateFlow<String> = _pn532UsbStatus.asStateFlow()

    private val _emvParserStatus = MutableStateFlow("Ready")
    val emvParserStatus: StateFlow<String> = _emvParserStatus.asStateFlow()

    init {
        initializeModule()
        loadRecentSessions()
    }

    private fun initializeModule() {
        viewModelScope.launch {
            try {
                // Initialize main module
                val moduleManager = ModMainNfsp00f(context)
                moduleManager.initialize()
                _hardwareReady.value = true
            } catch (e: Exception) {
                e.printStackTrace()
                _hardwareReady.value = false
            }
        }
    }

    private fun loadRecentSessions() {
        viewModelScope.launch {
            try {
                val sessions = emvDatabase.getRecentSessions(limit = 50)
                _recentSessions.value = sessions
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    /**
     * Factory for creating DashboardViewModel instances
     */
    companion object {
        fun Factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val database = EmvDatabase(context)
                    return DashboardViewModel(context, database) as T
                }
            }
        }
    }
}
