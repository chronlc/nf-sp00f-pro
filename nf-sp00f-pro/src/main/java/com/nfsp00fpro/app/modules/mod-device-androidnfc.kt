package com.nfsp00fpro.app.modules

import android.content.Context
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.nfc.tech.NfcA
import android.app.Activity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Android NFC Device Module
 *
 * Provides interface to Android's internal NFC adapter
 *
 * Features:
 * - NFC adapter access and management
 * - Tag discovery and reading
 * - NDEF message handling
 * - NFC Type A communication
 */
class ModDeviceAndroidNfc(private val context: Context) {

    // NFC components
    private var nfcAdapter: NfcAdapter? = null
    private var currentTag: Tag? = null
    private var currentNdefTag: Ndef? = null

    // NFC state
    private var isInitialized = false
    private var isNfcAvailable = false
    private var isNfcEnabled = false
    private var isReaderModeActive = false

    // Reader callback
    private var readerCallback: NfcAdapter.ReaderCallback? = null

    // Coroutine scope
    private val moduleScope = CoroutineScope(Dispatchers.Main)

    /**
     * Initialize NFC module
     *
     * - Obtains NFC adapter reference
     * - Checks NFC availability
     * - Checks NFC enabled state
     * - Sets initialized flag
     */
    fun initialize() {
        try {
            // Get NFC adapter
            nfcAdapter = NfcAdapter.getDefaultAdapter(context)
            
            if (nfcAdapter == null) {
                logStatus("⚠ NFC adapter not available on device")
                isNfcAvailable = false
                isInitialized = true
                return
            }

            isNfcAvailable = true

            // Check if NFC is enabled
            isNfcEnabled = nfcAdapter?.isEnabled() ?: false
            if (!isNfcEnabled) {
                logStatus("⚠ NFC is disabled")
            } else {
                logStatus("✓ NFC is enabled")
            }

            // Initialize reader callback
            readerCallback = NfcAdapter.ReaderCallback { tag ->
                handleTagDiscovered(tag)
            }

            isInitialized = true
            logStatus("✓ Android NFC module initialized")
        } catch (e: Exception) {
            logStatus("✗ Initialization failed: ${e.message}")
        }
    }

    /**
     * Enable reader mode
     *
     * Parameters:
     * - activity: Activity - Activity to enable reader mode for
     *
     * Process:
     * 1. Verify NFC adapter exists
     * 2. Enable reader mode with flags for NFC-A, NFC-B, NFC-F, NFC-V
     * 3. Update reader mode state
     */
    fun enableReaderMode(activity: Activity) {
        try {
            if (nfcAdapter == null) {
                logStatus("✗ NFC adapter not available")
                return
            }

            if (!isNfcEnabled) {
                logStatus("✗ NFC is not enabled")
                return
            }

            val flags = NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

            nfcAdapter?.enableReaderMode(activity, readerCallback, flags, null)
            isReaderModeActive = true
            logStatus("✓ NFC reader mode enabled")
        } catch (e: Exception) {
            logStatus("✗ Failed to enable reader mode: ${e.message}")
        }
    }

    /**
     * Disable reader mode
     *
     * Parameters:
     * - activity: Activity - Activity to disable reader mode for
     */
    fun disableReaderMode(activity: Activity) {
        try {
            if (nfcAdapter == null) {
                logStatus("✗ NFC adapter not available")
                return
            }

            nfcAdapter?.disableReaderMode(activity)
            isReaderModeActive = false
            logStatus("✓ NFC reader mode disabled")
        } catch (e: Exception) {
            logStatus("✗ Failed to disable reader mode: ${e.message}")
        }
    }

    /**
     * Handle discovered NFC tag
     *
     * Parameters:
     * - tag: Tag - Discovered tag
     *
     * Process:
     * 1. Store tag reference
     * 2. Attempt to read NDEF message
     * 3. Log tag information
     */
    private fun handleTagDiscovered(tag: Tag) {
        try {
            currentTag = tag

            // Get tag ID
            val tagId = tag.getId()
            val tagIdHex = tagId.joinToString("") { "%02x".format(it) }
            logStatus("✓ NFC tag discovered: ID=$tagIdHex")

            // Get available technologies
            val technologies = tag.getTechList()
            logStatus("  Technologies: ${technologies.joinToString(", ")}")

            // Try to read as NDEF
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                try {
                    ndef.connect()
                    val message = ndef.getNdefMessage()
                    if (message != null) {
                        val recordCount = message.getRecords().size
                        logStatus("  NDEF Message: $recordCount records")
                    }
                    ndef.close()
                    currentNdefTag = null
                } catch (e: Exception) {
                    logStatus("  NDEF read error: ${e.message}")
                }
            }

            // Try to read as NFC-A
            val nfcA = NfcA.get(tag)
            if (nfcA != null) {
                try {
                    nfcA.connect()
                    val atqa = nfcA.getAtqa()
                    val sak = nfcA.getSak()
                    logStatus("  NFC-A: ATQA=${atqa.joinToString("") { "%02x".format(it) }}, SAK=$sak")
                    nfcA.close()
                } catch (e: Exception) {
                    logStatus("  NFC-A read error: ${e.message}")
                }
            }
        } catch (e: Exception) {
            logStatus("✗ Tag discovery error: ${e.message}")
        }
    }

    /**
     * Read current tag as NDEF
     *
     * Returns: ByteArray? - NDEF message bytes or null if not available
     */
    fun readCurrentTagAsNdef(): ByteArray? {
        if (currentTag == null) {
            logStatus("⚠ No current tag")
            return null
        }

        return try {
            val ndef = Ndef.get(currentTag)
            if (ndef == null) {
                logStatus("⚠ Tag is not NDEF formatted")
                return null
            }

            ndef.connect()
            val message = ndef.getNdefMessage()
            ndef.close()

            if (message != null) {
                logStatus("✓ Read NDEF message (${message.getByteArrayLength()} bytes)")
                message.toByteArray()
            } else {
                logStatus("⚠ No NDEF message on tag")
                null
            }
        } catch (e: Exception) {
            logStatus("✗ NDEF read failed: ${e.message}")
            null
        }
    }

    /**
     * Transceive raw command via NFC-A
     *
     * Parameters:
     * - command: ByteArray - Command to send
     *
     * Returns: ByteArray? - Response data or null if error
     */
    fun transceiveNfcA(command: ByteArray): ByteArray? {
        if (currentTag == null) {
            logStatus("⚠ No current tag")
            return null
        }

        return try {
            val nfcA = NfcA.get(currentTag)
            if (nfcA == null) {
                logStatus("⚠ Tag does not support NFC-A")
                return null
            }

            nfcA.connect()
            val response = nfcA.transceive(command)
            nfcA.close()

            logStatus("✓ NFC-A transceive sent ${command.size} bytes, received ${response.size} bytes")
            response
        } catch (e: Exception) {
            logStatus("✗ NFC-A transceive failed: ${e.message}")
            null
        }
    }

    /**
     * Get current tag ID
     *
     * Returns: String - Tag ID as hex string or "N/A" if no tag
     */
    fun getCurrentTagId(): String {
        if (currentTag == null) {
            return "N/A"
        }

        val tagId = currentTag?.getId() ?: return "N/A"
        return tagId.joinToString("") { "%02x".format(it) }
    }

    /**
     * Get health status
     *
     * Returns: ModuleHealth - Module health information
     */
    fun getHealthStatus(): ModuleHealth {
        val isHealthy = isNfcAvailable && isNfcEnabled && isReaderModeActive
        val message = when {
            !isNfcAvailable -> "NFC not available on device"
            !isNfcEnabled -> "NFC disabled"
            isReaderModeActive -> "Reader mode active"
            isInitialized -> "Initialized, reader mode disabled"
            else -> "Not initialized"
        }
        return ModuleHealth("AndroidNFC", isHealthy, message)
    }

    /**
     * Shutdown module
     *
     * - Closes current tag
     * - Disables reader mode if active
     */
    fun shutdown() {
        try {
            currentTag = null
            currentNdefTag = null
            isInitialized = false
            logStatus("✓ Android NFC module shutdown")
        } catch (e: Exception) {
            logStatus("✗ Shutdown error: ${e.message}")
        }
    }

    /**
     * Internal logging
     */
    private fun logStatus(message: String) {
        println("[ModDeviceAndroidNfc] $message")
    }
}
