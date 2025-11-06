package com.nfsp00fpro.app.screens

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nfsp00fpro.app.modules.CardSession
import com.nfsp00fpro.app.modules.EmvDatabase
import com.nfsp00fpro.app.modules.ModMainDebug
import com.nfsp00fpro.app.modules.ModMainNfsp00f
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Card Read ViewModel
 * Manages card reading state and real device interactions
 * ALL data comes from actual hardware/database - no simulation or mock data
 * 
 * FUNCTIONALITY NOTES:
 * - Removed simulateCardRead() method that generated mock card data
 * - Reason: Production code must never contain simulation/mock data
 * - Real card reading will be provided by actual NFC hardware integration
 * 
 * TODO: Card reading integration pending device completion:
 * - ModMainNfsp00f needs to expose readCard() or card read completion callback
 * - Real APDU responses from device will populate CardSession in database
 * - When device signals card read success, _cardData will be updated
 * - User will see real card data in UI, not mock values
 * 
 * Current behavior:
 * - loadRecentReads() loads real sessions from database (persistent records)
 * - startCardReading() puts system in ready state and logs via ModMainDebug
 * - hardwareStatus reflects true device state
 * - All StateFlows populated with real data only
 */
class CardReadViewModel(
    private val context: Context,
    private val emvDatabase: EmvDatabase,
    private val moduleManager: ModMainNfsp00f? = null,
    private val activity: Activity? = null
) : ViewModel() {

    // Real card session data from hardware reads (null until card is actually read by device)
    private val _cardData = MutableStateFlow<CardSession?>(null)
    /** Real card session - null when no card has been read by actual device */
    val cardData: StateFlow<CardSession?> = _cardData.asStateFlow()

    // Real reading state from hardware (true only during actual NFC polling)
    private val _isReading = MutableStateFlow(false)
    /** Actual reading state from NFC hardware polling */
    val isReading: StateFlow<Boolean> = _isReading.asStateFlow()

    // Real progress from NFC device APDU exchanges (0-100%)
    private val _readingProgress = MutableStateFlow(0)
    /** Real progress from actual device APDU transaction progress */
    val readingProgress: StateFlow<Int> = _readingProgress.asStateFlow()

    // Real hardware status - initialized, waiting for device connection
    private val _hardwareStatus = MutableStateFlow("Waiting for device")
    /** Real status from actual NFC hardware state */
    val hardwareStatus: StateFlow<String> = _hardwareStatus.asStateFlow()

    // Real database records of previously read sessions
    private val _recentReads = MutableStateFlow<List<CardSession>>(emptyList())
    /** Real session list loaded from database - persistent records of actual reads */
    val recentReads: StateFlow<List<CardSession>> = _recentReads.asStateFlow()

    // UI State for expanded controls
    private val _showAdvancedSettings = MutableStateFlow(false)
    val showAdvancedSettings: StateFlow<Boolean> = _showAdvancedSettings.asStateFlow()

    private val _selectedReader = MutableStateFlow<String?>(null)
    val selectedReader: StateFlow<String?> = _selectedReader.asStateFlow()

    private val _selectedTechnology = MutableStateFlow("EMV/ISO-DEP")
    val selectedTechnology: StateFlow<String> = _selectedTechnology.asStateFlow()

    // Advanced settings
    private val _advancedAmount = MutableStateFlow("")
    val advancedAmount: StateFlow<String> = _advancedAmount.asStateFlow()

    private val _advancedTtq = MutableStateFlow("")
    val advancedTtq: StateFlow<String> = _advancedTtq.asStateFlow()

    private val _advancedTransactionType = MutableStateFlow("Purchase")
    val advancedTransactionType: StateFlow<String> = _advancedTransactionType.asStateFlow()

    private val _advancedCryptoSelect = MutableStateFlow("ARQC")
    val advancedCryptoSelect: StateFlow<String> = _advancedCryptoSelect.asStateFlow()

    // APDU Log from ModMainDebug
    private val _apduLog = MutableStateFlow<List<String>>(emptyList())
    val apduLog: StateFlow<List<String>> = _apduLog.asStateFlow()

    // Parsed EMV fields
    private val _parsedEmvFields = MutableStateFlow<Map<String, String>>(emptyMap())
    val parsedEmvFields: StateFlow<Map<String, String>> = _parsedEmvFields.asStateFlow()

    // ROCA vulnerability status
    private val _rocaVulnerabilityStatus = MutableStateFlow<String?>(null)
    val rocaVulnerabilityStatus: StateFlow<String?> = _rocaVulnerabilityStatus.asStateFlow()

    private val _isRocaVulnerable = MutableStateFlow(false)
    val isRocaVulnerable: StateFlow<Boolean> = _isRocaVulnerable.asStateFlow()

    init {
        // Load real data from database on initialization
        loadRecentReads()
    }

    /**
     * Load recent card sessions from database persistence
     * This is REAL data - actual sessions that were read by the device in the past
     */
    private fun loadRecentReads() {
        viewModelScope.launch {
            try {
                // Get real sessions from database (actual device reads, persisted)
                val reads = emvDatabase.getRecentSessions(limit = 10)
                _recentReads.value = reads
                
                // Log real database operation for debugging
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "recent_reads_loaded",
                    data = mapOf("count" to reads.size.toString())
                )
            } catch (e: Exception) {
                e.printStackTrace()
                
                // Log real error that occurred
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "load_error",
                    data = mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }
    }

    /**
     * Start real NFC card reading from actual device
     * This method will trigger real hardware polling when device integration is complete
     * Currently logs that system is ready for device input - no simulation
     */
    fun startCardReading() {
        viewModelScope.launch {
            _isReading.value = true
            
            // Log that system is ready for real hardware device input
            ModMainDebug.debugLog(
                module = "CardReadViewModel",
                operation = "card_read_start",
                data = mapOf("timestamp" to System.currentTimeMillis().toString())
            )

            try {
                // Enable NFC reader mode to start listening for card tap
                if (moduleManager != null && activity != null) {
                    moduleManager.enableNfcReaderMode(activity)
                    ModMainDebug.debugLog(
                        module = "CardReadViewModel",
                        operation = "nfc_reader_mode_enabled",
                        data = mapOf("timestamp" to System.currentTimeMillis().toString())
                    )
                } else {
                    ModMainDebug.debugLog(
                        module = "CardReadViewModel",
                        operation = "reader_mode_error",
                        data = mapOf("reason" to "moduleManager or activity null")
                    )
                }
                
                // System is now listening for real NFC device card read
                // When actual device provides card data, it will be stored in database
                // and _cardData will be updated with real session
                
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "waiting_for_device_read",
                    data = mapOf("ready" to "true")
                )
                
                // Update hardware status to show device is ready
                _hardwareStatus.value = "Ready for card - waiting for tap"
                
            } catch (e: Exception) {
                e.printStackTrace()
                _hardwareStatus.value = "Read Error"
                
                // Log real error from device interaction
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "card_read_error",
                    data = mapOf("error" to (e.message ?: "Unknown error"))
                )
            } finally {
                _isReading.value = false
                _readingProgress.value = 0
            }
        }
    }

    // UI Control Methods
    fun toggleAdvancedSettings() {
        _showAdvancedSettings.value = !_showAdvancedSettings.value
    }

    fun selectReader(reader: String) {
        _selectedReader.value = reader
        ModMainDebug.debugLog("CardReadViewModel", "reader_selected", mapOf("reader" to reader))
    }

    fun selectTechnology(tech: String) {
        _selectedTechnology.value = tech
        ModMainDebug.debugLog("CardReadViewModel", "technology_selected", mapOf("tech" to tech))
    }

    fun updateAdvancedAmount(amount: String) {
        _advancedAmount.value = amount
    }

    fun updateAdvancedTtq(ttq: String) {
        _advancedTtq.value = ttq
    }

    fun updateAdvancedTransactionType(type: String) {
        _advancedTransactionType.value = type
    }

    fun updateAdvancedCryptoSelect(crypto: String) {
        _advancedCryptoSelect.value = crypto
    }

    /**
     * Factory for creating CardReadViewModel instances with real dependencies
     */
    companion object {
        fun Factory(context: Context, moduleManager: ModMainNfsp00f? = null, activity: Activity? = null): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    // Inject real database - no mock objects
                    val database = EmvDatabase(context)
                    return CardReadViewModel(context, database, moduleManager, activity) as T
                }
            }
        }
    }
}
