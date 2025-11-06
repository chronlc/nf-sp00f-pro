package com.nfsp00fpro.app.modules

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

/**
 * PN532 NFC Device Module
 *
 * Supports communication with PN532 via:
 * 1. Bluetooth (SSID: 'PN532', MAC: '00:14:03:05:5C:CB')
 * 2. USB
 *
 * Features:
 * - Device discovery and connection
 * - Bluetooth socket management
 * - USB device enumeration
 * - Data transmission and reception
 */
class ModDevicePn532(private val context: Context) {

    // Bluetooth components
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var bluetoothInputStream: InputStream? = null
    private var bluetoothOutputStream: OutputStream? = null
    private var connectedBluetoothDevice: BluetoothDevice? = null

    // USB components
    private var usbManager: UsbManager? = null
    private var connectedUsbDevice: UsbDevice? = null

    // Connection state
    private var isInitialized = false
    private var isBluetoothConnected = false
    private var isUsbConnected = false

    // PN532 Configuration
    companion object {
        private const val PN532_BLUETOOTH_SSID = "PN532"
        private const val PN532_BLUETOOTH_MAC = "00:14:03:05:5C:CB"
        private const val PN532_RFCOMM_UUID = "00001101-0000-1000-8000-00805F9B34FB"
    }

    // Coroutine scope
    private val moduleScope = CoroutineScope(Dispatchers.IO)

    /**
     * Initialize PN532 module
     *
     * - Obtains Bluetooth adapter reference
     * - Obtains USB manager reference
     * - Sets initialized flag
     */
    fun initialize() {
        try {
            // Initialize Bluetooth
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (bluetoothAdapter == null) {
                logStatus("⚠ Bluetooth adapter not available")
                ModMainDebug.debugLog("ModDevicePn532", "initialize_warning", mapOf(
                    "reason" to "bluetooth_adapter_not_available"
                ))
            } else {
                ModMainDebug.debugLog("ModDevicePn532", "bluetooth_adapter_found", null)
            }

            // Initialize USB
            usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
            if (usbManager == null) {
                logStatus("⚠ USB manager not available")
                ModMainDebug.debugLog("ModDevicePn532", "initialize_warning", mapOf(
                    "reason" to "usb_manager_not_available"
                ))
            } else {
                ModMainDebug.debugLog("ModDevicePn532", "usb_manager_found", null)
            }

            isInitialized = true
            logStatus("✓ PN532 module initialized")
            ModMainDebug.debugLog("ModDevicePn532", "initialize_complete", mapOf(
                "bluetooth_available" to (bluetoothAdapter != null),
                "usb_available" to (usbManager != null)
            ))
        } catch (e: Exception) {
            logStatus("✗ Initialization failed: ${e.message}")
            ModMainDebug.debugLog("ModDevicePn532", "initialize_error", mapOf(
                "error" to (e.message ?: "Unknown error") as Any
            ))
        }
    }

    /**
     * Connect to PN532 via Bluetooth device
     *
     * Parameters:
     * - bluetoothDevice: BluetoothDevice - Device to connect to
     *
     * Process:
     * 1. Create RFCOMM socket to service UUID
     * 2. Establish connection
     * 3. Get input/output streams
     * 4. Update connection state
     */
    fun connectBluetoothDevice(bluetoothDevice: BluetoothDevice) {
        moduleScope.launch {
            try {
                ModMainDebug.debugLog("ModDevicePn532", "bt_connect_start", mapOf(
                    "device_name" to (bluetoothDevice.name ?: "Unknown") as Any,
                    "device_address" to bluetoothDevice.address
                ))

                // Verify Bluetooth adapter exists
                if (bluetoothAdapter == null) {
                    logStatus("✗ Bluetooth adapter not available")
                    ModMainDebug.debugLog("ModDevicePn532", "bt_connect_error", mapOf(
                        "reason" to "adapter_not_available"
                    ))
                    return@launch
                }

                // Create RFCOMM socket to service record
                try {
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(
                        UUID.fromString(PN532_RFCOMM_UUID)
                    )
                    ModMainDebug.debugLog("ModDevicePn532", "bt_socket_created", null)
                } catch (e: IOException) {
                    logStatus("✗ RFCOMM socket creation failed: ${e.message}")
                    ModMainDebug.debugLog("ModDevicePn532", "bt_connect_error", mapOf(
                        "reason" to "socket_creation_failed",
                        "error" to (e.message ?: "Unknown error") as Any
                    ))
                    return@launch
                }

                // Connect socket
                try {
                    bluetoothSocket?.connect()
                    ModMainDebug.debugLog("ModDevicePn532", "bt_socket_connected", null)
                } catch (e: IOException) {
                    logStatus("✗ Bluetooth connection failed: ${e.message}")
                    try {
                        bluetoothSocket?.close()
                    } catch (closeException: IOException) {
                        logStatus("✗ Socket close failed: ${closeException.message}")
                    }
                    bluetoothSocket = null
                    ModMainDebug.debugLog("ModDevicePn532", "bt_connect_error", mapOf(
                        "reason" to "socket_connect_failed",
                        "error" to (e.message ?: "Unknown error") as Any
                    ))
                    return@launch
                }

                // Get I/O streams
                try {
                    bluetoothInputStream = bluetoothSocket?.getInputStream()
                    bluetoothOutputStream = bluetoothSocket?.getOutputStream()
                    ModMainDebug.debugLog("ModDevicePn532", "bt_streams_acquired", null)
                } catch (e: IOException) {
                    logStatus("✗ Failed to get streams: ${e.message}")
                    try {
                        bluetoothSocket?.close()
                    } catch (closeException: IOException) {
                        logStatus("✗ Socket close failed: ${closeException.message}")
                    }
                    bluetoothSocket = null
                    ModMainDebug.debugLog("ModDevicePn532", "bt_connect_error", mapOf(
                        "reason" to "streams_acquisition_failed",
                        "error" to (e.message ?: "Unknown error") as Any
                    ))
                    return@launch
                }

                // Update state
                connectedBluetoothDevice = bluetoothDevice
                isBluetoothConnected = true
                logStatus("✓ Bluetooth connected to ${bluetoothDevice.getName()} (${bluetoothDevice.getAddress()})")
                ModMainDebug.debugLog("ModDevicePn532", "bt_connected", mapOf(
                    "device_name" to (bluetoothDevice.name ?: "Unknown") as Any,
                    "device_address" to bluetoothDevice.address,
                    "timestamp" to System.currentTimeMillis()
                ))
            } catch (e: Exception) {
                logStatus("✗ Bluetooth connection error: ${e.message}")
                ModMainDebug.debugLog("ModDevicePn532", "bt_connect_error", mapOf(
                    "reason" to "general_exception",
                    "error" to (e.message ?: "Unknown error") as Any
                ))
            }
        }
    }

    /**
     * Disconnect Bluetooth
     *
     * - Closes I/O streams
     * - Closes socket
     * - Updates connection state
     */
    fun disconnectBluetooth() {
        try {
            bluetoothInputStream?.close()
            bluetoothOutputStream?.close()
            bluetoothSocket?.close()
            isBluetoothConnected = false
            connectedBluetoothDevice = null
            logStatus("✓ Bluetooth disconnected")
            ModMainDebug.debugLog("ModDevicePn532", "bt_disconnected", mapOf(
                "timestamp" to System.currentTimeMillis()
            ))
        } catch (e: IOException) {
            logStatus("✗ Disconnect error: ${e.message}")
            ModMainDebug.debugLog("ModDevicePn532", "bt_disconnect_error", mapOf(
                "error" to (e.message ?: "Unknown error") as Any
            ))
        }
    }

    /**
     * Send data via Bluetooth
     *
     * Parameters:
     * - data: ByteArray - Data to send
     *
     * Returns: Boolean - true if sent successfully, false otherwise
     */
    fun sendBluetoothData(data: ByteArray): Boolean {
        if (!isBluetoothConnected) {
            logStatus("⚠ Bluetooth not connected")
            ModMainDebug.debugLog("ModDevicePn532", "bt_send_error", mapOf(
                "reason" to "not_connected",
                "data_length" to data.size
            ))
            return false
        }

        return try {
            bluetoothOutputStream?.write(data)
            bluetoothOutputStream?.flush()
            logStatus("✓ Sent ${data.size} bytes via Bluetooth")
            ModMainDebug.debugLog("ModDevicePn532", "bt_data_sent", mapOf(
                "data_length" to data.size,
                "timestamp" to System.currentTimeMillis()
            ))
            true
        } catch (e: IOException) {
            logStatus("✗ Send failed: ${e.message}")
            ModMainDebug.debugLog("ModDevicePn532", "bt_send_error", mapOf(
                "reason" to "io_error",
                "error" to (e.message ?: "Unknown error") as Any,
                "data_length" to data.size
            ))
            false
        }
    }

    /**
     * Receive data via Bluetooth
     *
     * Parameters:
     * - bufferSize: Int - Buffer size for reading
     *
     * Returns: ByteArray? - Data received or null if error
     */
    fun receiveBluetoothData(bufferSize: Int = 1024): ByteArray? {
        if (!isBluetoothConnected) {
            logStatus("⚠ Bluetooth not connected")
            ModMainDebug.debugLog("ModDevicePn532", "bt_receive_error", mapOf(
                "reason" to "not_connected",
                "buffer_size" to bufferSize
            ))
            return null
        }

        return try {
            val buffer = ByteArray(bufferSize)
            val bytesRead = bluetoothInputStream?.read(buffer) ?: 0
            if (bytesRead > 0) {
                logStatus("✓ Received $bytesRead bytes via Bluetooth")
                ModMainDebug.debugLog("ModDevicePn532", "bt_data_received", mapOf(
                    "bytes_read" to bytesRead,
                    "buffer_size" to bufferSize,
                    "timestamp" to System.currentTimeMillis()
                ))
                buffer.copyOf(bytesRead)
            } else {
                ModMainDebug.debugLog("ModDevicePn532", "bt_receive_empty", mapOf(
                    "buffer_size" to bufferSize
                ))
                null
            }
        } catch (e: IOException) {
            logStatus("✗ Receive failed: ${e.message}")
            ModMainDebug.debugLog("ModDevicePn532", "bt_receive_error", mapOf(
                "reason" to "io_error",
                "error" to (e.message ?: "Unknown error") as Any,
                "buffer_size" to bufferSize
            ))
            null
        }
    }

    /**
     * Discover USB PN532 devices
     *
     * Returns: List<UsbDevice> - Connected PN532 USB devices
     */
    fun discoverUsbDevices(): List<UsbDevice> {
        if (usbManager == null) {
            logStatus("⚠ USB manager not available")
            return emptyList()
        }

        val devices = mutableListOf<UsbDevice>()
        val deviceList = usbManager?.getDeviceList() ?: emptyMap()

        for ((_, device) in deviceList) {
            // Check if device is PN532 (vendor ID and product ID may vary)
            // You may need to adjust these based on your specific PN532 device
            if (isPn532Device(device)) {
                devices.add(device)
                logStatus("✓ Found PN532 USB device: ${device.getDeviceName()}")
            }
        }

        return devices
    }

    /**
     * Check if USB device is PN532
     *
     * Parameters:
     * - device: UsbDevice - Device to check
     *
     * Returns: Boolean - true if device is PN532, false otherwise
     *
     * Note: Adjust vendor/product IDs based on your PN532 device configuration
     */
    private fun isPn532Device(device: UsbDevice): Boolean {
        // PN532 common vendor IDs: 0x04CC (Philips), others may vary
        // Product IDs vary by PN532 variant
        // This is a placeholder - update with actual PN532 IDs
        return device.getVendorId() == 0x04CC || device.getProductId() == 0xABCD
    }

    /**
     * Get health status
     *
     * Returns: ModuleHealth - Module health information
     */
    fun getHealthStatus(): ModuleHealth {
        val isHealthy = isBluetoothConnected || isUsbConnected
        val message = when {
            isBluetoothConnected -> "Bluetooth: Connected to ${connectedBluetoothDevice?.getName()}"
            isUsbConnected -> "USB: Connected"
            !isInitialized -> "Not initialized"
            else -> "No connections"
        }
        return ModuleHealth("PN532", isHealthy, message)
    }

    /**
     * Shutdown module
     *
     * - Closes Bluetooth connection
     * - Releases USB resources
     */
    fun shutdown() {
        try {
            disconnectBluetooth()
            connectedUsbDevice = null
            isUsbConnected = false
            isInitialized = false
            logStatus("✓ PN532 module shutdown")
        } catch (e: Exception) {
            logStatus("✗ Shutdown error: ${e.message}")
        }
    }

    /**
     * Internal logging
     */
    private fun logStatus(message: String) {
        ModMainDebug.debugLog("ModDevicePn532", "operation", mapOf("message" to message))
    }
}
