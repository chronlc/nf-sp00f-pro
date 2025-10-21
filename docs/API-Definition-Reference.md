# STEP 4 & 5: Definition Reading - Complete API Documentation

**Date:** October 20, 2025  
**Status:** Dependencies documented with exact signatures  
**Scope:** Three core modules - Bluetooth, USB, NFC APIs

---

## 📚 BLUETOOTH API DOCUMENTATION

### Core Classes & Methods

#### 1. `android.bluetooth.BluetoothAdapter`
**Package:** `android.bluetooth`  
**Import:** `import android.bluetooth.BluetoothAdapter`

**Key Methods:**
```kotlin
// Get the default Bluetooth adapter
fun getDefaultAdapter(): BluetoothAdapter?
// Returns: BluetoothAdapter? (nullable - returns null if no Bluetooth adapter)

// Check if Bluetooth is enabled
fun isEnabled(): Boolean
// Returns: Boolean (true if Bluetooth is on, false if off)

// Enable Bluetooth
fun enable(): Boolean
// Returns: Boolean (true if enable initiated, false if already enabled or error)
// Note: Shows user confirmation dialog, requires BLUETOOTH_ADMIN permission

// Disable Bluetooth
fun disable(): Boolean
// Returns: Boolean (true if disable initiated, false if already disabled or error)

// Start discovery process
fun startDiscovery(): Boolean
// Returns: Boolean (true if discovery started, false if already discovering or error)
// Broadcasts: ACTION_DISCOVERY_STARTED, ACTION_DISCOVERY_FINISHED
// Requires: BLUETOOTH_ADMIN, BLUETOOTH_SCAN permissions

// Get discovered devices
fun getBondedDevices(): Set<BluetoothDevice>
// Returns: Set<BluetoothDevice> (empty set if no bonded devices)

// Get remote device by address
fun getRemoteDevice(address: String): BluetoothDevice
// Parameters:
//   - address: String - Device MAC address format "00:14:03:05:5C:CB"
// Returns: BluetoothDevice (object representing remote device)
// Throws: IllegalArgumentException if address format is invalid

// Start listening for incoming connections (server socket)
fun listenUsingRfcommWithServiceRecord(name: String, uuid: UUID): BluetoothServerSocket
// Parameters:
//   - name: String - Service name for the socket
//   - uuid: UUID - Unique identifier for service
// Returns: BluetoothServerSocket
// Requires: BLUETOOTH, BLUETOOTH_ADMIN permissions
```

**Permissions Required:**
- `BLUETOOTH` - Basic Bluetooth operations
- `BLUETOOTH_ADMIN` - Enable/disable, discovery control
- `BLUETOOTH_SCAN` (API 31+) - Discover devices
- `BLUETOOTH_CONNECT` (API 31+) - Connect to devices
- `ACCESS_FINE_LOCATION` - Location for discovery (on some devices)

**Constants:**
```kotlin
BluetoothAdapter.ACTION_DISCOVERY_STARTED = "android.bluetooth.adapter.action.DISCOVERY_STARTED"
BluetoothAdapter.ACTION_DISCOVERY_FINISHED = "android.bluetooth.adapter.action.DISCOVERY_FINISHED"
BluetoothAdapter.ACTION_STATE_CHANGED = "android.bluetooth.adapter.action.STATE_CHANGED"
BluetoothAdapter.EXTRA_STATE = "android.bluetooth.adapter.extra.STATE"
BluetoothAdapter.STATE_ON = 12
BluetoothAdapter.STATE_OFF = 10
BluetoothAdapter.STATE_TURNING_ON = 11
BluetoothAdapter.STATE_TURNING_OFF = 13
```

---

#### 2. `android.bluetooth.BluetoothDevice`
**Package:** `android.bluetooth`  
**Import:** `import android.bluetooth.BluetoothDevice`

**Key Methods:**
```kotlin
// Get device address (MAC)
fun getAddress(): String
// Returns: String - Device MAC address format "00:14:03:05:5C:CB"

// Get device name
fun getName(): String?
// Returns: String? - Device name or null if unknown

// Check if device is bonded
fun getBondState(): Int
// Returns: Int - BOND_NONE, BOND_BONDING, BOND_BONDED

// Create socket for RFCOMM connection
fun createRfcommSocketToServiceRecord(uuid: UUID): BluetoothSocket
// Parameters:
//   - uuid: UUID - Service UUID to connect to
// Returns: BluetoothSocket - Communication socket
// Throws: IOException if socket creation fails
// Requires: BLUETOOTH, BLUETOOTH_CONNECT (API 31+) permissions

// Get device type
fun getType(): Int
// Returns: Int - TYPE_UNKNOWN, TYPE_BREDR, TYPE_LE, TYPE_DUAL

// Create Bluetooth socket on specific channel
fun createRfcommSocket(channel: Int): BluetoothSocket?
// Parameters:
//   - channel: Int - RFCOMM channel (1-30)
// Returns: BluetoothSocket? - Socket or null if creation fails
```

**Bond States:**
```kotlin
BluetoothDevice.BOND_NONE = 10        // Not bonded
BluetoothDevice.BOND_BONDING = 11     // Bonding in progress
BluetoothDevice.BOND_BONDED = 12      // Bonded
```

**Device Types:**
```kotlin
BluetoothDevice.TYPE_UNKNOWN = 0
BluetoothDevice.TYPE_BREDR = 1        // Classic Bluetooth
BluetoothDevice.TYPE_LE = 2           // Bluetooth Low Energy
BluetoothDevice.TYPE_DUAL = 3         // Both BR/EDR and LE
```

---

#### 3. `android.bluetooth.BluetoothSocket`
**Package:** `android.bluetooth`  
**Import:** `import android.bluetooth.BluetoothSocket`

**Key Methods:**
```kotlin
// Connect socket to remote device
fun connect(): Unit
// Throws: IOException if connection fails
// Blocks until connection established or fails
// Requires: BLUETOOTH, BLUETOOTH_CONNECT (API 31+) permissions

// Get input stream for reading
fun getInputStream(): InputStream
// Returns: InputStream - For reading data from remote device
// Throws: IOException if socket is closed

// Get output stream for writing
fun getOutputStream(): OutputStream
// Returns: OutputStream - For writing data to remote device
// Throws: IOException if socket is closed

// Close the socket
fun close(): Unit
// Throws: IOException
// Note: Must call this to clean up resources

// Check if socket is connected
fun isConnected(): Boolean
// Returns: Boolean - true if connected, false otherwise

// Get remote device
fun getRemoteDevice(): BluetoothDevice
// Returns: BluetoothDevice - The device this socket connects to
```

**Constraints & Notes:**
- Connection is blocking - use threads/coroutines
- Must handle IOException for all operations
- Always close socket in try-finally or use try-with-resources
- I/O operations throw IOException if socket is closed

---

#### 4. `android.bluetooth.BluetoothServerSocket`
**Package:** `android.bluetooth`  
**Import:** `import android.bluetooth.BluetoothServerSocket`

**Key Methods:**
```kotlin
// Accept incoming connection (blocking)
fun accept(): BluetoothSocket
// Returns: BluetoothSocket - Connected socket to incoming client
// Throws: IOException if error occurs
// Blocks until connection arrives
// Can be cancelled by calling close()

// Accept connection with timeout
fun accept(timeout: Int): BluetoothSocket
// Parameters:
//   - timeout: Int - Timeout in milliseconds
// Returns: BluetoothSocket - Connected socket
// Throws: SocketTimeoutException, IOException

// Close the server socket
fun close(): Unit
// Throws: IOException
// Stops listening for new connections
```

---

## 📚 USB API DOCUMENTATION

### Core Classes & Methods

#### 1. `android.hardware.usb.UsbManager`
**Package:** `android.hardware.usb`  
**Import:** `import android.hardware.usb.UsbManager`

**Key Methods:**
```kotlin
// Get all connected USB devices
fun getDeviceList(): Map<String, UsbDevice>
// Returns: Map<String, UsbDevice> - Key: device name, Value: device object
// Returns empty map if no devices connected

// Get specific USB device by name
fun getDeviceList(name: String): UsbDevice?
// Parameters:
//   - name: String - Device name
// Returns: UsbDevice? - Device or null if not found

// Request permission to communicate with USB device
fun requestPermission(device: UsbDevice, permissionIntent: PendingIntent): Unit
// Parameters:
//   - device: UsbDevice - Device to request permission for
//   - permissionIntent: PendingIntent - Broadcast intent when permission granted/denied
// Note: User confirmation dialog appears
// Result received via BroadcastReceiver with action matching intent

// Check if we have permission to communicate with device
fun hasPermission(device: UsbDevice): Boolean
// Parameters:
//   - device: UsbDevice - Device to check permission for
// Returns: Boolean - true if permission granted, false otherwise

// Open USB device connection
fun openDevice(device: UsbDevice): UsbDeviceConnection?
// Parameters:
//   - device: UsbDevice - Device to open
// Returns: UsbDeviceConnection? - Connection object or null if open fails
// Requires: USB permission via requestPermission() first
// Must call close() on returned connection when done

// Get file descriptor for raw USB access
fun openFileDescriptor(device: UsbDevice): FileDescriptor?
// Parameters:
//   - device: UsbDevice - Device to access
// Returns: FileDescriptor? - For native USB access
// Requires: Android API 21+
```

**Permissions Required:**
- `android.permission.USB` - For USB operations
- Manifest declaration: `<uses-feature android:name="android.hardware.usb.host" />`

**Intent Action:**
```kotlin
UsbManager.ACTION_USB_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
UsbManager.ACTION_USB_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"
UsbManager.EXTRA_DEVICE = "device"
UsbManager.EXTRA_PERMISSION_GRANTED = "permission"
```

---

#### 2. `android.hardware.usb.UsbDevice`
**Package:** `android.hardware.usb`  
**Import:** `import android.hardware.usb.UsbDevice`

**Key Methods & Properties:**
```kotlin
// Get device name
fun getDeviceName(): String
// Returns: String - USB device name (e.g., "/dev/bus/usb/001/002")

// Get device vendor ID
fun getVendorId(): Int
// Returns: Int - Vendor ID (16-bit)

// Get device product ID
fun getProductId(): Int
// Returns: Int - Product ID (16-bit)

// Get number of configurations
fun getConfigurationCount(): Int
// Returns: Int - Number of USB configurations

// Get configuration
fun getConfiguration(index: Int): UsbConfiguration?
// Parameters:
//   - index: Int - Configuration index (0 to configurationCount-1)
// Returns: UsbConfiguration? - Configuration object or null

// Get interface count
fun getInterfaceCount(): Int
// Returns: Int - Number of interfaces

// Get interface by index
fun getInterface(index: Int): UsbInterface
// Parameters:
//   - index: Int - Interface index
// Returns: UsbInterface - Interface object

// Get device class
fun getDeviceClass(): Int
// Returns: Int - Device class code
// Examples: 0xFF (vendor-specific), 0x00 (class per interface)

// Get device subclass
fun getDeviceSubclass(): Int
// Returns: Int - Device subclass code

// Get device protocol
fun getDeviceProtocol(): Int
// Returns: Int - Device protocol code
```

**Device Class Codes:**
```kotlin
UsbConstants.USB_CLASS_VENDOR_SPEC = 0xFF     // Vendor specific
UsbConstants.USB_CLASS_MISC = 0xEF            // Miscellaneous
UsbConstants.USB_CLASS_HID = 0x03             // Human interface device
```

---

#### 3. `android.hardware.usb.UsbDeviceConnection`
**Package:** `android.hardware.usb`  
**Import:** `import android.hardware.usb.UsbDeviceConnection`

**Key Methods:**
```kotlin
// Get file descriptor for raw access
fun getFileDescriptor(): Int
// Returns: Int - File descriptor number

// Claim interface for exclusive use
fun claimInterface(intf: UsbInterface, force: Boolean): Boolean
// Parameters:
//   - intf: UsbInterface - Interface to claim
//   - force: Boolean - Force claim if already claimed
// Returns: Boolean - true if claim successful, false otherwise

// Release interface
fun releaseInterface(intf: UsbInterface): Boolean
// Parameters:
//   - intf: UsbInterface - Interface to release
// Returns: Boolean - true if release successful

// Set interface alternate setting
fun setInterface(intf: UsbInterface): Boolean
// Parameters:
//   - intf: UsbInterface - Interface to activate
// Returns: Boolean - true if successful

// Perform bulk transfer (read/write)
fun bulkTransfer(
    endpoint: UsbEndpoint,
    buffer: ByteArray,
    length: Int,
    timeout: Int
): Int
// Parameters:
//   - endpoint: UsbEndpoint - Endpoint to transfer on
//   - buffer: ByteArray - Data buffer
//   - length: Int - Number of bytes to transfer
//   - timeout: Int - Timeout in milliseconds (0 = no timeout)
// Returns: Int - Number of bytes transferred (negative = error)

// Perform control transfer
fun controlTransfer(
    requestType: Int,
    request: Int,
    value: Int,
    index: Int,
    buffer: ByteArray?,
    length: Int,
    timeout: Int
): Int
// Parameters:
//   - requestType: Int - bmRequestType (direction, type, recipient)
//   - request: Int - bRequest code
//   - value: Int - wValue
//   - index: Int - wIndex
//   - buffer: ByteArray? - Data buffer (null for zero-length)
//   - length: Int - Data length
//   - timeout: Int - Timeout in milliseconds
// Returns: Int - Number of bytes transferred (negative = error)

// Get serial number
fun getSerial(): String?
// Returns: String? - Device serial number or null

// Close connection
fun close(): Unit
// Closes the USB connection and releases resources
```

**Transfer Direction Constants:**
```kotlin
UsbConstants.USB_DIR_OUT = 0x00   // Host to device
UsbConstants.USB_DIR_IN = 0x80    // Device to host
```

---

#### 4. `android.hardware.usb.UsbEndpoint`
**Package:** `android.hardware.usb`  
**Import:** `import android.hardware.usb.UsbEndpoint`

**Key Methods:**
```kotlin
// Get endpoint address
fun getAddress(): Int
// Returns: Int - Endpoint address (includes direction bit)

// Get endpoint direction
fun getDirection(): Int
// Returns: Int - UsbConstants.USB_DIR_IN or USB_DIR_OUT

// Get endpoint type
fun getType(): Int
// Returns: Int - Endpoint type (control, bulk, interrupt, isoch)

// Get maximum packet size
fun getMaxPacketSize(): Int
// Returns: Int - Maximum bytes per packet
```

**Endpoint Types:**
```kotlin
UsbConstants.USB_ENDPOINT_XFERTYPE_MASK = 0x03
UsbConstants.USB_ENDPOINT_XFER_CONTROL = 0x00
UsbConstants.USB_ENDPOINT_XFER_ISOC = 0x01
UsbConstants.USB_ENDPOINT_XFER_BULK = 0x02
UsbConstants.USB_ENDPOINT_XFER_INT = 0x03
```

---

## 📚 NFC API DOCUMENTATION

### Core Classes & Methods

#### 1. `android.nfc.NfcAdapter`
**Package:** `android.nfc`  
**Import:** `import android.nfc.NfcAdapter`

**Key Methods:**
```kotlin
// Get default NFC adapter
fun getDefaultAdapter(context: Context): NfcAdapter?
// Parameters:
//   - context: Context - Application context
// Returns: NfcAdapter? - NFC adapter or null if no NFC hardware
// Note: Must check null before using

// Check if NFC is enabled
fun isEnabled(): Boolean
// Returns: Boolean - true if NFC is on, false if off

// Enable reader mode (Android 4.4+)
fun enableReaderMode(
    activity: Activity,
    callback: ReaderCallback,
    flags: Int,
    extras: Bundle?
): Unit
// Parameters:
//   - activity: Activity - Activity to enable for
//   - callback: ReaderCallback - Callback when tag discovered
//   - flags: Int - Reader mode flags (NFCF, NFCA, NFCB, etc.)
//   - extras: Bundle? - Optional extras
// Requires: ANDROID 4.4+ (API 19)
// Requires: NFC permission

// Disable reader mode
fun disableReaderMode(activity: Activity): Unit
// Parameters:
//   - activity: Activity - Activity to disable for

// Set NDEF push message (Android Beam)
fun setNdefPushMessage(message: NdefMessage?, activity: Activity): Unit
// Parameters:
//   - message: NdefMessage? - Message to push (null to disable)
//   - activity: Activity - Activity context
// Requires: ANDROID 4.1+ (API 16)

// Enable foreground dispatch
fun enableForegroundDispatch(
    activity: Activity,
    pendingIntent: PendingIntent,
    filters: Array<IntentFilter>?,
    techLists: Array<Array<String>>?
): Unit
// Parameters:
//   - activity: Activity - Activity to enable for
//   - pendingIntent: PendingIntent - Intent when tag discovered
//   - filters: Array<IntentFilter>? - NDEF filters or null
//   - techLists: Array<Array<String>>? - Tech filters or null
// Note: Call in onResume()
// Requires: ANDROID 2.3.3+ (API 10)

// Disable foreground dispatch
fun disableForegroundDispatch(activity: Activity): Unit
// Parameters:
//   - activity: Activity - Activity to disable for
// Note: Call in onPause()
```

**Reader Mode Flags:**
```kotlin
NfcAdapter.FLAG_READER_NFC_A = 0x01          // ISO14443 Type A
NfcAdapter.FLAG_READER_NFC_B = 0x02          // ISO14443 Type B
NfcAdapter.FLAG_READER_NFC_F = 0x04          // FeliCa (JIS X6319-4)
NfcAdapter.FLAG_READER_NFC_V = 0x08          // ISO15693
NfcAdapter.FLAG_READER_NFC_BARCODE = 0x10    // NFC Barcode
NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK = 0x80   // Skip NDEF check
```

**Intent Actions:**
```kotlin
NfcAdapter.ACTION_NDEF_DISCOVERED = "android.nfc.action.NDEF_DISCOVERED"
NfcAdapter.ACTION_TECH_DISCOVERED = "android.nfc.action.TECH_DISCOVERED"
NfcAdapter.ACTION_TAG_DISCOVERED = "android.nfc.action.TAG_DISCOVERED"
```

**Extra Keys:**
```kotlin
NfcAdapter.EXTRA_TAG = "android.nfc.extra.TAG"
NfcAdapter.EXTRA_NDEF_MESSAGES = "android.nfc.extra.NDEF_MESSAGES"
```

---

#### 2. `android.nfc.Tag`
**Package:** `android.nfc`  
**Import:** `import android.nfc.Tag`

**Key Methods:**
```kotlin
// Get tag ID
fun getId(): ByteArray
// Returns: ByteArray - Unique tag ID

// Get tag technologies
fun getTechList(): Array<String>
// Returns: Array<String> - Available technologies
// Examples: "android.nfc.tech.NfcA", "android.nfc.tech.Ndef"

// Get tag as specific technology
fun <T : TagTechnology> getTagTechnology(tech: Class<T>): T?
// Parameters:
//   - tech: Class<T> - Technology class (NfcA, Ndef, etc.)
// Returns: T? - Technology object or null if not supported
```

**Available Technologies:**
```kotlin
"android.nfc.tech.NfcA"
"android.nfc.tech.NfcB"
"android.nfc.tech.NfcF"
"android.nfc.tech.NfcV"
"android.nfc.tech.IsoDep"
"android.nfc.tech.Ndef"
"android.nfc.tech.NdefFormatable"
```

---

#### 3. `android.nfc.tech.NfcA`
**Package:** `android.nfc.tech`  
**Import:** `import android.nfc.tech.NfcA`

**Key Methods:**
```kotlin
// Get from tag
companion object {
    fun get(tag: Tag): NfcA?
    // Returns: NfcA? - NfcA object or null
}

// Connect to tag
fun connect(): Unit
// Throws: IOException if connection fails

// Check if connected
fun isConnected(): Boolean
// Returns: Boolean

// Send raw command and receive response
fun transceive(data: ByteArray): ByteArray
// Parameters:
//   - data: ByteArray - Command to send
// Returns: ByteArray - Response from tag
// Throws: IOException if error

// Get ATQA (Answer to SELECT)
fun getAtqa(): ByteArray
// Returns: ByteArray - Tag ATQA response

// Get SAK (Select Acknowledge)
fun getSak(): Short
// Returns: Short - Tag SAK value

// Close connection
fun close(): Unit
// Throws: IOException
```

---

#### 4. `android.nfc.tech.Ndef`
**Package:** `android.nfc.tech`  
**Import:** `import android.nfc.tech.Ndef`

**Key Methods:**
```kotlin
// Get from tag
companion object {
    fun get(tag: Tag): Ndef?
    // Returns: Ndef? - Ndef object or null
}

// Connect to tag
fun connect(): Unit
// Throws: IOException

// Read NDEF message
fun getNdefMessage(): NdefMessage?
// Returns: NdefMessage? - Message or null if tag not read

// Check if tag is writable
fun isWritable(): Boolean
// Returns: Boolean

// Write NDEF message
fun writeNdefMessage(msg: NdefMessage): Unit
// Parameters:
//   - msg: NdefMessage - Message to write
// Throws: IOException, FormatException if write fails
// Requires: isWritable() == true

// Get tag type
fun getType(): String
// Returns: String - Tag type string

// Get maximum message size
fun getMaxSize(): Int
// Returns: Int - Maximum message bytes

// Close connection
fun close(): Unit
// Throws: IOException
```

---

#### 5. `android.nfc.NdefMessage`
**Package:** `android.nfc`  
**Import:** `import android.nfc.NdefMessage`

**Key Methods:**
```kotlin
// Constructor
constructor(records: Array<NdefRecord>)
// Parameters:
//   - records: Array<NdefRecord> - NDEF records

// Get records
fun getRecords(): Array<NdefRecord>
// Returns: Array<NdefRecord> - All records in message

// Get message bytes (for transmission)
fun toByteArray(): ByteArray
// Returns: ByteArray - Serialized message

// Get message size
fun getByteArrayLength(): Int
// Returns: Int - Serialized size in bytes
```

---

#### 6. `android.nfc.NdefRecord`
**Package:** `android.nfc`  
**Import:** `import android.nfc.NdefRecord`

**Key Methods:**
```kotlin
// Constructor - text record
companion object {
    fun createTextRecord(languageCode: String, text: String): NdefRecord
    // Parameters:
    //   - languageCode: String - e.g., "en", "fr"
    //   - text: String - Text content
    // Returns: NdefRecord - Text record

    // Constructor - URI record
    fun createUriRecord(uri: Uri): NdefRecord
    // Parameters:
    //   - uri: Uri - URI to embed
    // Returns: NdefRecord - URI record
}

// Get record type
fun getType(): ByteArray
// Returns: ByteArray - Record type

// Get record ID
fun getId(): ByteArray
// Returns: ByteArray - Record ID

// Get record payload
fun getPayload(): ByteArray
// Returns: ByteArray - Record data

// Get TNF (Type Name Format)
fun getTnf(): Short
// Returns: Short - TNF value (0-6)
```

**TNF Values:**
```kotlin
NdefRecord.TNF_EMPTY = 0x00
NdefRecord.TNF_WELL_KNOWN = 0x01
NdefRecord.TNF_MIME_MEDIA = 0x02
NdefRecord.TNF_ABSOLUTE_URI = 0x03
NdefRecord.TNF_EXTERNAL_TYPE = 0x04
NdefRecord.TNF_UNKNOWN = 0x05
NdefRecord.TNF_UNCHANGED = 0x06
```

---

## 🔐 PERMISSIONS SUMMARY

### Bluetooth Permissions
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

<uses-feature android:name="android.hardware.bluetooth" android:required="true" />
```

### USB Permissions
```xml
<uses-permission android:name="android.permission.USB" />
<uses-feature android:name="android.hardware.usb.host" />
```

### NFC Permissions
```xml
<uses-permission android:name="android.permission.NFC" />
<uses-feature android:name="android.hardware.nfc" android:required="false" />
```

---

## 📋 NEXT STEPS

- ⏳ **STEP 5:** Complete definition reading (this step covers both)
- ⏹️ Ready for **STEP 6:** Generation with Precision

---

**Documentation Completed:** October 20, 2025  
**Status:** STEPS 4 & 5 Complete - Ready for Code Generation
