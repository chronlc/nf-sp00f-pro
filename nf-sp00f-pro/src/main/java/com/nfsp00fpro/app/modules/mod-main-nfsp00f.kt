package com.nfsp00fpro.app.modules

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbManager
import android.nfc.NfcAdapter
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Main Module Manager for nf-sp00f-pro
 *
 * Responsibilities:
 * - Initialize and manage all device modules
 * - Check health status of all connected devices
 * - Coordinate communication between modules
 * - Handle device discovery and auto-connection
 *
 * Supported Devices:
 * 1. PN532 NFC Reader (Bluetooth + USB)
 * 2. Android Internal NFC Adapter
 * 3. EMV Parser (BER-TLV parsing)
 * 4. EMV Reader (APDU-based card scanning)
 */
class ModMainNfsp00f(private val context: Context) {

    // Device modules
    private var pn532Module: ModDevicePn532? = null
    private var androidNfcModule: ModDeviceAndroidNfc? = null

    // EMV modules
    private var emvParserModule: EmvParser? = null
    private var emvReaderModule: EmvReader? = null
    private var emvDatabaseModule: EmvDatabase? = null

    // Module status
    private var isInitialized = false
    private var healthCheckInterval = 5000L // 5 seconds
    
    // Permission status tracking
    private var permissionStatus: Map<String, Boolean> = emptyMap()
    private var permissionsChecked = false

    // Coroutine scope for async operations
    private val moduleScope = CoroutineScope(Dispatchers.IO)

    /**
     * Initialize all modules
     *
     * This method:
     * 1. Checks all required permissions at startup
     * 2. Initializes PN532 module (with Bluetooth auto-connect)
     * 3. Initializes Android NFC module
     * 4. Initializes EMV Parser module
     * 5. Initializes EMV Reader module
     * 6. Starts health check monitoring
     * 7. Sets initialized flag
     */
    fun initialize() {
        try {
            ModMainDebug.debugLog("ModMainNfsp00f", "initialize_start", mapOf(
                "timestamp" to System.currentTimeMillis()
            ))

            // CHECK PERMISSIONS FIRST
            checkAllPermissions()

            // Initialize PN532 Module
            pn532Module = ModDevicePn532(context)
            pn532Module?.initialize()
            ModMainDebug.debugLog("ModMainNfsp00f", "pn532_initialized", null)

            // Attempt auto-connect to PN532 Bluetooth
            moduleScope.launch {
                attemptPn532BluetoothAutoConnect()
            }

            // Initialize Android NFC Module
            androidNfcModule = ModDeviceAndroidNfc(context)
            androidNfcModule?.initialize()
            ModMainDebug.debugLog("ModMainNfsp00f", "androidnfc_initialized", null)

            // Initialize EMV Parser Module
            emvParserModule = EmvParser()
            logStatus("✓ EMV Parser module initialized")
            ModMainDebug.debugLog("ModMainNfsp00f", "emvparser_initialized", null)

            // Initialize EMV Reader Module
            emvReaderModule = EmvReader(context)
            emvReaderModule?.initialize()
            ModMainDebug.debugLog("ModMainNfsp00f", "emvreader_initialized", null)

            // Initialize EMV Database Module
            emvDatabaseModule = EmvDatabase(context)
            logStatus("✓ EMV Database module initialized")
            ModMainDebug.debugLog("ModMainNfsp00f", "emvdatabase_initialized", null)

            // Start health monitoring
            startHealthMonitoring()

            isInitialized = true
            logStatus("✓ All modules initialized successfully")
            ModMainDebug.debugLog("ModMainNfsp00f", "initialize_complete", mapOf(
                "all_modules_ready" to true,
                "timestamp" to System.currentTimeMillis()
            ))
        } catch (e: Exception) {
            logStatus("✗ Module initialization failed: ${e.message}")
            ModMainDebug.debugLog("ModMainNfsp00f", "initialize_error", mapOf(
                "error" to (e.message ?: "Unknown error") as Any,
                "timestamp" to System.currentTimeMillis()
            ))
        }
    }

    /**
     * Attempt to auto-connect to PN532 via Bluetooth
     *
     * Connection Parameters:
     * - SSID: 'PN532'
     * - MAC: '00:14:03:05:5C:CB'
     * - Protocol: RFCOMM
     */
    private fun attemptPn532BluetoothAutoConnect() {
        try {
            ModMainDebug.debugLog("ModMainNfsp00f", "pn532_autoconnect_start", null)
            
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                logStatus("✗ Bluetooth adapter not available")
                ModMainDebug.debugLog("ModMainNfsp00f", "pn532_autoconnect_error", mapOf(
                    "reason" to "bluetooth_adapter_not_available"
                ))
                return
            }

            if (!bluetoothAdapter.isEnabled()) {
                logStatus("⊘ Bluetooth is disabled, cannot auto-connect")
                ModMainDebug.debugLog("ModMainNfsp00f", "pn532_autoconnect_error", mapOf(
                    "reason" to "bluetooth_disabled"
                ))
                return
            }

            // Get PN532 device by MAC address
            val pn532Device = bluetoothAdapter.getRemoteDevice("00:14:03:05:5C:CB")
            if (pn532Device == null) {
                logStatus("✗ PN532 device not found (MAC: 00:14:03:05:5C:CB)")
                ModMainDebug.debugLog("ModMainNfsp00f", "pn532_autoconnect_error", mapOf(
                    "reason" to "device_not_found",
                    "mac_address" to "00:14:03:05:5C:CB"
                ))
                return
            }

            // Connect via PN532 module
            pn532Module?.connectBluetoothDevice(pn532Device)
            logStatus("✓ PN532 Bluetooth auto-connect initiated")
            ModMainDebug.debugLog("ModMainNfsp00f", "pn532_autoconnect_success", mapOf(
                "device_name" to (pn532Device.name ?: "Unknown") as Any,
                "mac_address" to "00:14:03:05:5C:CB"
            ))
        } catch (e: IllegalArgumentException) {
            logStatus("✗ Invalid MAC address format: ${e.message}")
            ModMainDebug.debugLog("ModMainNfsp00f", "pn532_autoconnect_error", mapOf(
                "reason" to "invalid_mac_address",
                "error" to (e.message ?: "Unknown error") as Any
            ))
        } catch (e: Exception) {
            logStatus("✗ PN532 auto-connect failed: ${e.message}")
            ModMainDebug.debugLog("ModMainNfsp00f", "pn532_autoconnect_error", mapOf(
                "reason" to "connection_failed",
                "error" to (e.message ?: "Unknown error") as Any
            ))
        }
    }

    /**
     * Check health status of all modules
     *
     * Returns map of module health status
     */
    fun checkHealth(): Map<String, ModuleHealth> {
        val healthMap = mutableMapOf<String, ModuleHealth>()

        // Check PN532 health
        val pn532Health = pn532Module?.getHealthStatus()
            ?: ModuleHealth("PN532", false, "Not initialized")
        healthMap["PN532"] = pn532Health

        // Check Android NFC health
        val androidNfcHealth = androidNfcModule?.getHealthStatus()
            ?: ModuleHealth("AndroidNFC", false, "Not initialized")
        healthMap["AndroidNFC"] = androidNfcHealth

        // Check EMV Reader health
        val emvReaderHealth = emvReaderModule?.getHealthStatus()
            ?: ModuleHealth("EMVReader", false, "Not initialized")
        healthMap["EMVReader"] = emvReaderHealth

        return healthMap
    }

    /**
     * Start periodic health monitoring
     */
    private fun startHealthMonitoring() {
        moduleScope.launch {
            ModMainDebug.debugLog("ModMainNfsp00f", "health_monitoring_start", null)
            while (isInitialized) {
                val health = checkHealth()
                health.forEach { (moduleName, status) ->
                    if (status.isHealthy) {
                        logStatus("✓ $moduleName: ${status.message}")
                        ModMainDebug.debugLog("ModMainNfsp00f", "health_check_pass", mapOf(
                            "module" to moduleName,
                            "message" to status.message
                        ))
                    } else {
                        logStatus("⚠ $moduleName: ${status.message}")
                        ModMainDebug.debugLog("ModMainNfsp00f", "health_check_fail", mapOf(
                            "module" to moduleName,
                            "message" to status.message
                        ))
                    }
                }
                kotlinx.coroutines.delay(healthCheckInterval)
            }
        }
    }

    /**
     * Get PN532 module
     */
    fun getPn532Module(): ModDevicePn532? = pn532Module

    /**
     * Get Android NFC module
     */
    fun getAndroidNfcModule(): ModDeviceAndroidNfc? = androidNfcModule

    /**
     * Get EMV Parser module
     */
    fun getEmvParserModule(): EmvParser? = emvParserModule

    /**
     * Get EMV Reader module
     */
    fun getEmvReaderModule(): EmvReader? = emvReaderModule

    /**
     * Get EMV Database module
     */
    fun getEmvDatabaseModule(): EmvDatabase? = emvDatabaseModule

    /**
     * Check if all modules are initialized
     */
    fun isInitialized(): Boolean = isInitialized

    /**
     * Enable NFC reader mode for tag discovery
     * 
     * Called when user initiates card read operation
     * Enables the NFC adapter callback to listen for card taps
     */
    fun enableNfcReaderMode(activity: android.app.Activity) {
        try {
            if (androidNfcModule == null) {
                logStatus("⚠ Android NFC module not initialized")
                return
            }
            
            androidNfcModule?.enableReaderMode(activity)
            logStatus("✓ NFC reader mode enabled")
        } catch (e: Exception) {
            logStatus("✗ Failed to enable NFC reader mode: ${e.message}")
        }
    }

    /**
     * Disable NFC reader mode to conserve power
     * 
     * Called when user navigates away from card read screen
     */
    fun disableNfcReaderMode(activity: android.app.Activity) {
        try {
            if (androidNfcModule == null) {
                return
            }
            
            androidNfcModule?.disableReaderMode(activity)
            logStatus("✓ NFC reader mode disabled")
        } catch (e: Exception) {
            logStatus("✗ Failed to disable NFC reader mode: ${e.message}")
        }
    }

    /**
     * Shutdown all modules
     */
    fun shutdown() {
        try {
            pn532Module?.shutdown()
            androidNfcModule?.shutdown()
            emvReaderModule?.shutdown()
            emvDatabaseModule?.close()
            emvParserModule = null
            emvDatabaseModule = null
            isInitialized = false
            logStatus("✓ All modules shut down")
        } catch (e: Exception) {
            logStatus("✗ Shutdown error: ${e.message}")
        }
    }

    /**
     * Internal logging - delegated to ModMainDebug for unified output
     */
    private fun logStatus(message: String) {
        ModMainDebug.debugLog("ModMainNfsp00f", "status", mapOf("message" to message))
    }

    // ============================================================================
    // PERMISSIONS MANAGEMENT - Check and validate all required permissions
    // ============================================================================

    /**
     * Check all required permissions at app startup
     * 
     * Required Permissions:
     * - Bluetooth (SCAN, CONNECT, ADMIN)
     * - NFC
     * - Location (for Bluetooth scanning)
     * - File Storage (for logging and data persistence)
     */
    private fun checkAllPermissions() {
        permissionStatus = mutableMapOf()
        
        // Runtime permissions required on Android 6.0+ (API 23+)
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                "BLUETOOTH_SCAN",
                "BLUETOOTH_CONNECT",
                "NFC",
                "ACCESS_FINE_LOCATION",
                "ACCESS_COARSE_LOCATION",
                "READ_EXTERNAL_STORAGE",
                "WRITE_EXTERNAL_STORAGE"
            )
        } else {
            listOf(
                "BLUETOOTH",
                "BLUETOOTH_ADMIN",
                "NFC",
                "ACCESS_FINE_LOCATION",
                "ACCESS_COARSE_LOCATION",
                "READ_EXTERNAL_STORAGE",
                "WRITE_EXTERNAL_STORAGE"
            )
        }

        // Check each permission
        requiredPermissions.forEach { permissionName ->
            val permission = when (permissionName) {
                "BLUETOOTH_SCAN" -> Manifest.permission.BLUETOOTH_SCAN
                "BLUETOOTH_CONNECT" -> Manifest.permission.BLUETOOTH_CONNECT
                "BLUETOOTH" -> Manifest.permission.BLUETOOTH
                "BLUETOOTH_ADMIN" -> Manifest.permission.BLUETOOTH_ADMIN
                "NFC" -> Manifest.permission.NFC
                "ACCESS_FINE_LOCATION" -> Manifest.permission.ACCESS_FINE_LOCATION
                "ACCESS_COARSE_LOCATION" -> Manifest.permission.ACCESS_COARSE_LOCATION
                "READ_EXTERNAL_STORAGE" -> Manifest.permission.READ_EXTERNAL_STORAGE
                "WRITE_EXTERNAL_STORAGE" -> Manifest.permission.WRITE_EXTERNAL_STORAGE
                else -> null
            }

            if (permission != null) {
                val isGranted = ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
                
                (permissionStatus as MutableMap)[permissionName] = isGranted
                
                ModMainDebug.debugLog(
                    "ModMainNfsp00f",
                    "permission_check",
                    mapOf(
                        "permission" to permissionName,
                        "granted" to isGranted.toString()
                    )
                )

                logStatus(if (isGranted) "✓ $permissionName granted" else "✗ $permissionName denied")
            }
        }

        permissionsChecked = true
        
        // Log summary
        val grantedCount = permissionStatus.values.count { it }
        val totalCount = permissionStatus.size
        val allGranted = grantedCount == totalCount
        
        ModMainDebug.debugLog(
            "ModMainNfsp00f",
            "permissions_check_complete",
            mapOf(
                "granted" to grantedCount.toString(),
                "total" to totalCount.toString(),
                "all_granted" to allGranted.toString(),
                "status" to if (allGranted) "OK" else "INCOMPLETE"
            )
        )

        logStatus(
            if (allGranted) 
                "✓ All permissions granted ($grantedCount/$totalCount)" 
            else 
                "⚠ Some permissions denied ($grantedCount/$totalCount)"
        )
    }

    /**
     * Get current permission status
     * 
     * @param permissionName Name of permission to check (e.g., "BLUETOOTH_SCAN", "NFC")
     * @return Boolean indicating if permission is granted, or null if not checked
     */
    fun isPermissionGranted(permissionName: String): Boolean? {
        return permissionStatus[permissionName]
    }

    /**
     * Check if all critical permissions are granted
     * 
     * Critical permissions are those required for basic app functionality:
     * - Bluetooth connectivity
     * - NFC communication
     * - File storage
     * 
     * @return Boolean indicating if all critical permissions are available
     */
    fun areAllCriticalPermissionsGranted(): Boolean {
        val criticalPermissions = listOf(
            "BLUETOOTH_SCAN",
            "BLUETOOTH_CONNECT",
            "NFC",
            "WRITE_EXTERNAL_STORAGE"
        )
        
        return criticalPermissions.all { 
            permissionStatus[it] == true || permissionStatus[it.replace("_", "")] == true
        }
    }

    /**
     * Get all permission statuses as a formatted string
     * 
     * @return Formatted string with all permission statuses for logging/display
     */
    fun getPermissionStatusString(): String {
        if (!permissionsChecked) {
            return "Permissions not yet checked"
        }

        val granted = permissionStatus.filter { it.value }
        val denied = permissionStatus.filter { !it.value }

        val result = StringBuilder()
        result.append("GRANTED (${granted.size}):\n")
        granted.keys.forEach { result.append("  ✓ $it\n") }
        
        if (denied.isNotEmpty()) {
            result.append("\nDENIED (${denied.size}):\n")
            denied.keys.forEach { result.append("  ✗ $it\n") }
        }

        return result.toString()
    }

    /**
     * Get all permission statuses as a map
     * 
     * @return Map of permission names to granted status
     */
    fun getPermissionStatus(): Map<String, Boolean> {
        return permissionStatus.toMap()
    }
}

/**
 * Module health status data class
 */
data class ModuleHealth(
    val moduleName: String,
    val isHealthy: Boolean,
    val message: String
)
