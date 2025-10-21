# Project Scope: Three Core Modules

**Date:** October 20, 2025  
**Status:** In Development  
**10-Step Process:** STEPS 1-3 Complete

---

## 🎯 STEP 1: SCOPE DEFINITION

### What We're Building

Building three core modules for device communication and management:

1. **Main Module Manager** (`mod-main-nfsp00f.kt`)
   - Initializes modules and devices
   - Manages device health status
   - Coordinates inter-module communication

2. **PN532 NFC Device Adapter** (`mod-device-pn532.kt`)
   - Supports Bluetooth connectivity (SSID: 'PN532', MAC: '00:14:03:05:5C:CB')
   - Supports USB connectivity
   - Auto-connects to PN532 Bluetooth device
   - Handles PN532 communication protocol

3. **Android NFC Adapter** (`mod-device-androidnfc.kt`)
   - Communicates with Android's internal NFC adapter
   - Provides high-level NFC interface
   - Handles NFC tag reading/writing

### Success Criteria

- ✅ Compiles without errors
- ✅ Modules follow SOLID principles and modular design
- ✅ PN532 auto-connects to Bluetooth (SSID: 'PN532', MAC: '00:14:03:05:5C:CB')
- ✅ All three modules communicate correctly
- ✅ Health status checks work for all devices
- ✅ All unit tests pass
- ✅ No consumer ripple effect failures

### Identified Dependencies

**Android Framework APIs:**
- `android.bluetooth.*` - Bluetooth communication
- `android.hardware.usb.*` - USB communication
- `android.nfc.*` - NFC adapter access
- `android.Manifest.permission` - Permission declarations

**External Libraries:**
- `kotlinx.coroutines` - Async/concurrent operations
- `androidx.appcompat` - Activity/Fragment base classes

**Internal Code:**
- `MainActivity.kt` - Entry point requiring module initialization

### Affected Existing Code

- ✅ `MainActivity.kt` - Will initialize main module manager
- ✅ `AndroidManifest.xml` - Needs Bluetooth/USB/NFC permissions
- ✅ `build.gradle.kts` - May need additional dependencies

---

## 📋 STEP 2: CONSUMER IMPACT ANALYSIS

**Ripple Effect Status:** ✅ IDENTIFIED

### Identified Consumers

| Consumer | Impact | Required Changes |
|----------|--------|------------------|
| `MainActivity.kt` | Module initialization required | Add module manager initialization call |
| `AndroidManifest.xml` | Permissions needed | Add BLUETOOTH, BLUETOOTH_ADMIN, USB, NFC permissions |
| `build.gradle.kts` | Dependencies may be needed | Verify Coroutines included |

### Files to Update

1. **MainActivity.kt**
   - Import module manager
   - Initialize modules in onCreate()

2. **AndroidManifest.xml**
   - Add permission declarations
   - Add required features

3. **build.gradle.kts**
   - Verify Kotlin Coroutines dependency
   - No new dependencies needed (already included)

---

## 🗂️ STEP 3: DEPENDENCY & CONSUMER MAPPING

### Class/Interface Interactions

| Dependency | Package | Type | Usage |
|-----------|---------|------|-------|
| `BluetoothAdapter` | android.bluetooth | Class | Device discovery, connection |
| `BluetoothSocket` | android.bluetooth | Class | Bluetooth communication channel |
| `UsbManager` | android.hardware.usb | Class | USB device management |
| `UsbDevice` | android.hardware.usb | Class | USB device representation |
| `NfcAdapter` | android.nfc | Class | NFC hardware access |
| `NfcManager` | android.nfc | Class | NFC service manager |
| `CoroutineScope` | kotlinx.coroutines | Interface | Async operations |
| `MainActivity` | com.nfsp00fpro.app | Class | Entry point, initialization |
| `AppCompatActivity` | androidx.appcompat.app | Class | Base activity class |

### API Signatures to Reference

**Bluetooth APIs:**
- `BluetoothAdapter.startDiscovery(): Boolean`
- `BluetoothAdapter.getRemoteDevice(address: String): BluetoothDevice`
- `BluetoothDevice.createRfcommSocketToServiceRecord(uuid: UUID): BluetoothSocket`
- `BluetoothSocket.connect(): Unit`

**USB APIs:**
- `UsbManager.getDeviceList(): Map<String, UsbDevice>`
- `UsbManager.requestPermission(device: UsbDevice, permissionIntent: PendingIntent): Unit`
- `UsbManager.openDevice(device: UsbDevice): UsbDeviceConnection`

**NFC APIs:**
- `NfcAdapter.getDefaultAdapter(context: Context): NfcAdapter?`
- `NfcAdapter.enableReaderMode(activity: Activity, callback: ReaderCallback, flags: Int, extras: Bundle?): Unit`

### File Structure

```
src/main/java/com/nfsp00fpro/app/
├── modules/
│   ├── mod-main-nfsp00f.kt           ← NEW
│   ├── mod-device-pn532.kt           ← NEW
│   └── mod-device-androidnfc.kt      ← NEW
├── MainActivity.kt                    ← UPDATE
└── ... other files
```

### Permission Requirements

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.USB" />
<uses-permission android:name="android.permission.NFC" />

<uses-feature android:name="android.hardware.bluetooth" android:required="true" />
<uses-feature android:name="android.hardware.nfc" android:required="false" />
```

---

## 📊 Next Steps

- ⏭️ **STEP 4:** Definition Reading - Deep dive into each API
- ⏭️ **STEP 5:** Definition Reading - Continue dependencies
- ⏭️ **STEP 6:** Generation with Precision - Write module code
- ⏭️ **STEP 7:** Self-Validation - Review before compile
- ⏭️ **STEP 8:** Compile & Verify - Build verification
- ⏭️ **STEP 9:** Consumer Update - Ensure all consumers work
- ⏭️ **STEP 10:** Commit to GitHub - Push changes

---

**Analysis Completed By:** AI Assistant  
**Review Date:** October 20, 2025
