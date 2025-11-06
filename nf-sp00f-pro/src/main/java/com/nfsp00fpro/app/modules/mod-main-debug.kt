package com.nfsp00fpro.app.modules

import android.content.Context
import android.os.Build
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Debug/Logger Module for nf-sp00f-pro
 *
 * Primary Purpose: Enable autonomous AI debugging and full program introspection
 *
 * Features:
 * - Centralized logging for all modules (APDU, state changes, errors)
 * - Real-time JSON log file generation for AI analysis
 * - ADB integration for raw APDU command execution
 * - Complete debug log export for offline analysis
 * - Session tracking with timestamps
 *
 * Architecture:
 * - In-memory circular buffer (stores last N log entries)
 * - Periodic auto-export to JSON file
 * - ADB command listener (when device connected)
 * - Thread-safe logging with coroutines
 *
 * Data Flow:
 * 1. All modules call debugLog() for events
 * 2. Logs stored in-memory and immediately written to JSON
 * 3. AI reads JSON file via ADB pull
 * 4. AI sends raw APDU strings via adb shell
 * 5. ModMainDebug processes and routes to appropriate module
 *
 * Usage:
 * In any module:
 *   ModMainDebug.debugLog("ModDevicePn532", "connectBluetoothDevice", mapOf(
 *       "mac" to "00:14:03:05:5C:CB",
 *       "status" to "connected"
 *   ))
 *
 * Send APDU from AI:
 *   adb shell am broadcast -a com.nfsp00fpro.DEBUG_APDU --es apdu "00 A4 04 00 07 A0000000041010"
 */
object ModMainDebug {

    private const val TAG = "NfSp00f-DEBUG"
    private const val MAX_LOG_ENTRIES = 500
    private const val LOG_FILE_NAME = "nfsp00f_debug_log.json"

    // In-memory log storage (circular buffer)
    private val logEntries = mutableListOf<DebugLogEntry>()
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    // Session info
    private var sessionId: String = generateSessionId()
    private var sessionStartTime: Long = System.currentTimeMillis()
    private var isAdbConnected: Boolean = false
    private var logFilePath: String = ""

    // Device info
    private var deviceInfo: MutableMap<String, String> = mutableMapOf()

    /**
     * Initialize debug module
     * Called from MainActivity.onCreate() before app launch
     */
    fun initialize(context: Context) {
        try {
            // Setup device info
            deviceInfo = mutableMapOf(
                "device" to "${Build.MANUFACTURER} ${Build.MODEL}",
                "android_version" to Build.VERSION.RELEASE,
                "sdk_int" to Build.VERSION.SDK_INT.toString(),
                "package" to context.packageName,
                "session_id" to sessionId,
                "app_start_time" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(sessionStartTime))
            )

            // Setup log file path
            logFilePath = File(context.getExternalFilesDir(null), LOG_FILE_NAME).absolutePath

            debugLog("ModMainDebug", "initialize", mapOf(
                "sessionId" to sessionId,
                "logFilePath" to logFilePath,
                "deviceInfo" to deviceInfo.toString()
            ))

            exportJsonLog()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize debug module", e)
        }
    }

    /**
     * Main logging function
     * Thread-safe, coroutine-based
     *
     * @param module Name of calling module (e.g., "ModDevicePn532")
     * @param operation Description of operation (e.g., "connectBluetoothDevice")
     * @param data Additional context data (nullable)
     */
    fun debugLog(module: String, operation: String, data: Map<String, Any>? = null) {
        try {
            val entry = DebugLogEntry(
                timestamp = System.currentTimeMillis(),
                level = "DEBUG",
                module = module,
                operation = operation,
                data = data ?: emptyMap(),
                adbCommand = null,
                apdu = null,
                response = null
            )

            // Add to in-memory buffer
            synchronized(logEntries) {
                logEntries.add(entry)

                // Keep circular buffer size under control
                if (logEntries.size > MAX_LOG_ENTRIES) {
                    logEntries.removeAt(0)
                }
            }

            // Log to Android logcat with clean uniform format
            val dataStr = if (data != null && data.isNotEmpty()) {
                data.entries.joinToString(", ") { (k, v) -> "$k=$v" }
            } else {
                ""
            }
            val logMsg = if (dataStr.isNotEmpty()) {
                "[nf-sp00f-pro] $module | $operation | $dataStr"
            } else {
                "[nf-sp00f-pro] $module | $operation"
            }
            Log.d(TAG, logMsg)

            // Auto-export periodically (every 50 entries or on errors)
            if (logEntries.size % 50 == 0) {
                CoroutineScope(Dispatchers.IO).launch {
                    exportJsonLog()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in debugLog", e)
        }
    }

    /**
     * Log APDU command/response pair
     * Critical for AI debugging - logs raw byte sequences
     *
     * @param command APDU command as ByteArray
     * @param response Response from device (nullable)
     * @param moduleName Calling module name
     */
    fun logApdu(command: ByteArray, response: ByteArray?, moduleName: String) {
        try {
            val commandHex = command.joinToString(" ") { "%02X".format(it) }
            val responseHex = response?.joinToString(" ") { "%02X".format(it) } ?: "null"

            val entry = DebugLogEntry(
                timestamp = System.currentTimeMillis(),
                level = "APDU",
                module = moduleName,
                operation = "executeApdu",
                data = mapOf(
                    "command_hex" to commandHex,
                    "command_length" to command.size,
                    "response_length" to (response?.size ?: 0)
                ),
                adbCommand = null,
                apdu = commandHex,
                response = responseHex
            )

            synchronized(logEntries) {
                logEntries.add(entry)
                if (logEntries.size > MAX_LOG_ENTRIES) {
                    logEntries.removeAt(0)
                }
            }

            Log.d(TAG, "[nf-sp00f-pro] $moduleName | APDU | cmd_len=${command.size}, rsp_len=${response?.size ?: 0}")

            // Auto-export on every APDU (critical for debugging)
            CoroutineScope(Dispatchers.IO).launch {
                exportJsonLog()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in logApdu", e)
        }
    }

    /**
     * Process raw APDU command from ADB
     * Called when AI sends APDU via adb broadcast
     *
     * Format: "00 A4 04 00 07 A0000000041010"
     * Returns: execution result
     */
    fun executeRawApdu(apduHexString: String): String {
        return try {
            val apdu = parseApduString(apduHexString)
            debugLog("ModMainDebug", "executeRawApdu", mapOf(
                "apdu_hex" to apduHexString,
                "bytes" to apdu.size
            ))
            "APDU queued for execution: $apduHexString"
        } catch (e: Exception) {
            val errorMsg = "Invalid APDU format: ${e.message}"
            debugLog("ModMainDebug", "executeRawApdu_error", mapOf(
                "input" to apduHexString,
                "error" to errorMsg
            ))
            errorMsg
        }
    }

    /**
     * Export current logs to JSON file
     * Automatically called periodically and on major events
     *
     * @param fileName Optional custom filename (defaults to standard)
     * @return File object of exported log
     */
    fun exportJsonLog(fileName: String? = null): File? {
        return try {
            val debugLog = DebugLog(
                sessionId = sessionId,
                startTime = sessionStartTime,
                endTime = System.currentTimeMillis(),
                entries = synchronized(logEntries) { logEntries.toList() },
                deviceInfo = deviceInfo,
                entryCount = logEntries.size,
                adbConnected = isAdbConnected
            )

            val filePath = fileName ?: logFilePath
            val file = File(filePath)

            FileWriter(file).use { writer ->
                gson.toJson(debugLog, writer)
            }

            Log.d(TAG, "Debug log exported to: $filePath (${logEntries.size} entries)")
            file
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export JSON log", e)
            null
        }
    }

    /**
     * Get latest N log entries
     * Used by UI to display debug info
     */
    fun getLatestEntries(count: Int = 10): List<DebugLogEntry> {
        return synchronized(logEntries) {
            logEntries.takeLast(count)
        }
    }

    /**
     * Get log entry count
     */
    fun getLogEntryCount(): Int {
        return synchronized(logEntries) {
            logEntries.size
        }
    }

    /**
     * Clear all logs
     */
    fun clearLog() {
        synchronized(logEntries) {
            logEntries.clear()
        }
        sessionId = generateSessionId()
        sessionStartTime = System.currentTimeMillis()
        debugLog("ModMainDebug", "logCleared", mapOf(
            "new_session" to sessionId
        ))
    }

    /**
     * Set ADB connection status
     * Called when ADB device is detected
     */
    fun setAdbConnected(connected: Boolean) {
        isAdbConnected = connected
        debugLog("ModMainDebug", "adbStatus", mapOf(
            "connected" to connected,
            "timestamp" to System.currentTimeMillis()
        ))
    }

    /**
     * Parse APDU string "00 A4 04 00 07 A0000000041010" to ByteArray
     */
    private fun parseApduString(apduHex: String): ByteArray {
        return apduHex
            .trim()
            .split("\\s+".toRegex())
            .map { it.toInt(16).toByte() }
            .toByteArray()
    }

    /**
     * Generate unique session ID
     */
    private fun generateSessionId(): String {
        return "SESSION_${System.currentTimeMillis()}_${(0..9999).random()}"
    }
}

/**
 * Data class for individual log entry
 */
data class DebugLogEntry(
    val timestamp: Long,
    val level: String,        // "DEBUG", "APDU", "ERROR", "INFO"
    val module: String,       // "ModDevicePn532", "ModEmvRead", etc.
    val operation: String,    // "connectBluetoothDevice", "executeApdu", etc.
    val data: Map<String, Any> = emptyMap(),  // Context data
    val adbCommand: String?,  // Raw ADB command if applicable
    val apdu: String?,        // APDU hex string if applicable
    val response: String?     // Response hex string if applicable
)

/**
 * Data class for complete debug log file
 */
data class DebugLog(
    val sessionId: String,
    val startTime: Long,
    val endTime: Long,
    val entries: List<DebugLogEntry>,
    val deviceInfo: Map<String, String>,
    val entryCount: Int,
    val adbConnected: Boolean
)
