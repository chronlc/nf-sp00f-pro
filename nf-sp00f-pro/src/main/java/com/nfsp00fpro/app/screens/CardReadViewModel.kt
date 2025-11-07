package com.nfsp00fpro.app.screens

import android.app.Activity
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.nfsp00fpro.app.modules.CardSession
import com.nfsp00fpro.app.modules.EmvDatabase
import com.nfsp00fpro.app.modules.EmvReader
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

    // EMV Reader module for actual card data extraction from hardware
    private val emvReader = EmvReader(context)

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

    // Card Read Statistics for RoGuE TeRMiNaL
    data class CardReadStats(
        val cardsScanned: Int = 0,
        val apduCount: Int = 0,
        val tagsCount: Int = 0
    )
    
    private val _cardReadStats = MutableStateFlow(CardReadStats())
    val cardReadStats: StateFlow<CardReadStats> = _cardReadStats.asStateFlow()

    init {
        // Initialize EMV reader module with real device access
        emvReader.initialize()
        ModMainDebug.debugLog(
            module = "CardReadViewModel",
            operation = "emv_reader_initialized",
            data = mapOf("timestamp" to System.currentTimeMillis().toString())
        )
        
        // Load real data from database on initialization
        loadRecentReads()
        
        // Start refreshing APDU logs from ModMainDebug periodically (every 500ms)
        viewModelScope.launch {
            while (true) {
                refreshApduLogs()
                kotlinx.coroutines.delay(500) // Refresh every 500ms for real-time display
            }
        }
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
                // When device detects card, onCardDetected() callback will be triggered
                // to call emvReader() and process the card data
                
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "waiting_for_device_read",
                    data = mapOf("ready" to "true")
                )
                
                // Update hardware status to show device is ready
                _hardwareStatus.value = "Ready for card - waiting for tap"
                
                // Keep _isReading = true to show Stop button while listening
                // It will be set to false when stopCardReading() is called or card read completes
                
            } catch (e: Exception) {
                e.printStackTrace()
                _hardwareStatus.value = "Read Error"
                _isReading.value = false
                
                // Log real error from device interaction
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "card_read_error",
                    data = mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }
    }

    /**
     * Called when device detects a card tap
     * This is where we actually read the card data using EmvReader module
     * 
     * Data source: Real NFC device card detection callback
     * Processing: emvReader() extracts all card data and saves to database
     * Update: _cardData updated with real CardSession from database
     */
    fun onCardDetected() {
        viewModelScope.launch {
            try {
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "card_detected",
                    data = mapOf("timestamp" to System.currentTimeMillis().toString())
                )
                
                _hardwareStatus.value = "Reading card..."
                _readingProgress.value = 25
                
                // Call EMV reader module to extract all card data (AIDs, Track 2, PAN, etc.)
                // This is production code that reads from actual hardware, not simulation
                emvReader.emvReader(isContactless = true)
                
                _readingProgress.value = 50
                
                // Get the session that was created and populated by emvReader
                // emvReader creates session, reads all AIDs, saves APDU logs, stores TLV tags
                val sessions = emvDatabase.getRecentSessions(limit = 1)
                if (sessions.isNotEmpty()) {
                    _cardData.value = sessions[0]
                    _readingProgress.value = 100
                    _hardwareStatus.value = "Card read successfully"
                    
                    ModMainDebug.debugLog(
                        module = "CardReadViewModel",
                        operation = "card_data_loaded",
                        data = mapOf(
                            "session_id" to (sessions[0].sessionId),
                            "status" to (sessions[0].status)
                        )
                    )
                } else {
                    _hardwareStatus.value = "Card read completed but data not found"
                    ModMainDebug.debugLog(
                        module = "CardReadViewModel",
                        operation = "card_read_no_data",
                        data = mapOf("timestamp" to System.currentTimeMillis().toString())
                    )
                }
                
                // Keep reading state true so Stop button visible
                // User can review data or click Stop to exit
                
            } catch (e: Exception) {
                e.printStackTrace()
                _hardwareStatus.value = "Card read error: ${e.message}"
                
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "card_detection_error",
                    data = mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }
    }

    /**
     * Stop real NFC card reading from actual device
     * Disables NFC reader mode to conserve power and stop detecting cards
     * Called when user clicks Stop Reading button
     */
    fun stopCardReading() {
        viewModelScope.launch {
            try {
                // Disable NFC reader mode to stop listening for card taps
                if (moduleManager != null && activity != null) {
                    moduleManager.disableNfcReaderMode(activity)
                    ModMainDebug.debugLog(
                        module = "CardReadViewModel",
                        operation = "nfc_reader_mode_disabled",
                        data = mapOf("timestamp" to System.currentTimeMillis().toString())
                    )
                } else {
                    ModMainDebug.debugLog(
                        module = "CardReadViewModel",
                        operation = "reader_mode_disable_error",
                        data = mapOf("reason" to "moduleManager or activity null")
                    )
                }

                // Update hardware status to show device is ready but not reading
                _hardwareStatus.value = "Ready - Idle (click Start to read)"
                
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "card_read_stop",
                    data = mapOf("timestamp" to System.currentTimeMillis().toString())
                )

            } catch (e: Exception) {
                e.printStackTrace()
                _hardwareStatus.value = "Stop Error"
                
                // Log real error from device interaction
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "card_read_stop_error",
                    data = mapOf("error" to (e.message ?: "Unknown error"))
                )
            } finally {
                _isReading.value = false
                _readingProgress.value = 0
            }
        }
    }
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
     * Refresh APDU logs from ModMainDebug
     * Called periodically or when user navigates to Card Read screen
     * Pulls real logs that were generated by device modules
     * 
     * Data source: ModMainDebug circular buffer (real module logs)
     */
    fun refreshApduLogs() {
        viewModelScope.launch {
            try {
                // Get latest 50 APDU entries from ModMainDebug for detailed terminal display
                val entries = ModMainDebug.getLatestEntries(50)
                
                // Format entries for display: "[MODULE] operation | data"
                val formattedLogs = entries.map { entry ->
                    val dataStr = if (entry.data.isNotEmpty()) {
                        entry.data.entries.joinToString(", ") { (k, v) -> "$k=$v" }
                    } else {
                        ""
                    }
                    
                    val logLine = "[${entry.module}] ${entry.operation}"
                    if (dataStr.isNotEmpty()) "$logLine | $dataStr" else logLine
                }
                
                _apduLog.value = formattedLogs
                
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "apdu_logs_refreshed",
                    data = mapOf("count" to formattedLogs.size.toString())
                )
            } catch (e: Exception) {
                e.printStackTrace()
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "apdu_logs_error",
                    data = mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }
    }

    /**
     * Get card read statistics from database
     * Data source: Direct counts from CardSession, ApduLog, and TlvTag tables
     * Called periodically or on demand for RoGuE TeRMiNaL stats display
     */
    fun refreshCardReadStats() {
        viewModelScope.launch {
            try {
                val cardsCount = emvDatabase.getSessionCount() // Count total CardSession records
                val apduCount = emvDatabase.getApduLogCount()  // Count total ApduLog records
                val tagsCount = emvDatabase.getTlvTagCount()   // Count total TlvTag records
                
                _cardReadStats.value = CardReadStats(
                    cardsScanned = cardsCount,
                    apduCount = apduCount,
                    tagsCount = tagsCount
                )
                
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "stats_refreshed",
                    data = mapOf(
                        "cards" to cardsCount.toString(),
                        "apdus" to apduCount.toString(),
                        "tags" to tagsCount.toString()
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                ModMainDebug.debugLog(
                    module = "CardReadViewModel",
                    operation = "stats_error",
                    data = mapOf("error" to (e.message ?: "Unknown error"))
                )
            }
        }
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
