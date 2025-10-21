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
            }

            // Initialize USB
            usbManager = context.getSystemService(Context.USB_SERVICE) as? UsbManager
            if (usbManager == null) {
                logStatus("⚠ USB manager not available")
            }

            isInitialized = true
            logStatus("✓ PN532 module initialized")
        } catch (e: Exception) {
            logStatus("✗ Initialization failed: ${e.message}")
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
                // Verify Bluetooth adapter exists
                if (bluetoothAdapter == null) {
                    logStatus("✗ Bluetooth adapter not available")
                    return@launch
                }

                // Create RFCOMM socket to service record
                try {
                    bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(
                        UUID.fromString(PN532_RFCOMM_UUID)
                    )
                } catch (e: IOException) {
                    logStatus("✗ RFCOMM socket creation failed: ${e.message}")
                    return@launch
                }

                // Connect socket
                try {
                    bluetoothSocket?.connect()
                } catch (e: IOException) {
                    logStatus("✗ Bluetooth connection failed: ${e.message}")
                    try {
                        bluetoothSocket?.close()
                    } catch (closeException: IOException) {
                        logStatus("✗ Socket close failed: ${closeException.message}")
                    }
                    bluetoothSocket = null
                    return@launch
                }

                // Get I/O streams
                try {
                    bluetoothInputStream = bluetoothSocket?.getInputStream()
                    bluetoothOutputStream = bluetoothSocket?.getOutputStream()
                } catch (e: IOException) {
                    logStatus("✗ Failed to get streams: ${e.message}")
                    try {
                        bluetoothSocket?.close()
                    } catch (closeException: IOException) {
                        logStatus("✗ Socket close failed: ${closeException.message}")
                    }
                    bluetoothSocket = null
                    return@launch
                }

                // Update state
                connectedBluetoothDevice = bluetoothDevice
                isBluetoothConnected = true
                logStatus("✓ Bluetooth connected to ${bluetoothDevice.getName()} (${bluetoothDevice.getAddress()})")
            } catch (e: Exception) {
                logStatus("✗ Bluetooth connection error: ${e.message}")
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
        } catch (e: IOException) {
            logStatus("✗ Disconnect error: ${e.message}")
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
            return false
        }

        return try {
            bluetoothOutputStream?.write(data)
            bluetoothOutputStream?.flush()
            logStatus("✓ Sent ${data.size} bytes via Bluetooth")
            true
        } catch (e: IOException) {
            logStatus("✗ Send failed: ${e.message}")
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
            return null
        }

        return try {
            val buffer = ByteArray(bufferSize)
            val bytesRead = bluetoothInputStream?.read(buffer) ?: 0
            if (bytesRead > 0) {
                logStatus("✓ Received $bytesRead bytes via Bluetooth")
                buffer.copyOf(bytesRead)
            } else {
                null
            }
        } catch (e: IOException) {
            logStatus("✗ Receive failed: ${e.message}")
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
        println("[ModDevicePn532] $message")
    }
}
