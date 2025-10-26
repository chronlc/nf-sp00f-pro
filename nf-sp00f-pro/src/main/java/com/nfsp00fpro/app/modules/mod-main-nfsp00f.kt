package com.nfsp00fpro.app.modules

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.hardware.usb.UsbManager
import android.nfc.NfcAdapter
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

    // Coroutine scope for async operations
    private val moduleScope = CoroutineScope(Dispatchers.IO)

    /**
     * Initialize all modules
     *
     * This method:
     * 1. Initializes PN532 module (with Bluetooth auto-connect)
     * 2. Initializes Android NFC module
     * 3. Initializes EMV Parser module
     * 4. Initializes EMV Reader module
     * 5. Starts health check monitoring
     * 6. Sets initialized flag
     */
    fun initialize() {
        try {
            // Initialize PN532 Module
            pn532Module = ModDevicePn532(context)
            pn532Module?.initialize()

            // Attempt auto-connect to PN532 Bluetooth
            moduleScope.launch {
                attemptPn532BluetoothAutoConnect()
            }

            // Initialize Android NFC Module
            androidNfcModule = ModDeviceAndroidNfc(context)
            androidNfcModule?.initialize()

            // Initialize EMV Parser Module
            emvParserModule = EmvParser()
            logStatus("✓ EMV Parser module initialized")

            // Initialize EMV Reader Module
            emvReaderModule = EmvReader(context)
            emvReaderModule?.initialize()

            // Initialize EMV Database Module
            emvDatabaseModule = EmvDatabase(context)
            logStatus("✓ EMV Database module initialized")

            // Start health monitoring
            startHealthMonitoring()

            isInitialized = true
            logStatus("✓ All modules initialized successfully")
        } catch (e: Exception) {
            logStatus("✗ Module initialization failed: ${e.message}")
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
            val bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                logStatus("✗ Bluetooth adapter not available")
                return
            }

            if (!bluetoothAdapter.isEnabled()) {
                logStatus("⊘ Bluetooth is disabled, cannot auto-connect")
                return
            }

            // Get PN532 device by MAC address
            val pn532Device = bluetoothAdapter.getRemoteDevice("00:14:03:05:5C:CB")
            if (pn532Device == null) {
                logStatus("✗ PN532 device not found (MAC: 00:14:03:05:5C:CB)")
                return
            }

            // Connect via PN532 module
            pn532Module?.connectBluetoothDevice(pn532Device)
            logStatus("✓ PN532 Bluetooth auto-connect initiated")
        } catch (e: IllegalArgumentException) {
            logStatus("✗ Invalid MAC address format: ${e.message}")
        } catch (e: Exception) {
            logStatus("✗ PN532 auto-connect failed: ${e.message}")
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
            while (isInitialized) {
                val health = checkHealth()
                health.forEach { (moduleName, status) ->
                    if (status.isHealthy) {
                        logStatus("✓ $moduleName: ${status.message}")
                    } else {
                        logStatus("⚠ $moduleName: ${status.message}")
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
     * Internal logging
     */
    private fun logStatus(message: String) {
        println("[ModMainNfsp00f] $message")
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
