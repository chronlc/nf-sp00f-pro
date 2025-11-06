# Changelog

All notable changes to the nf-sp00f-pro Android application will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- **APDU Terminal Card**: Real-time APDU log display on Card Read Screen (STEP 7 feature)
  - Integrated with ModMainDebug centralized logger
  - Displays last 20 debug entries in terminal-style monospace format
  - Color-coded output: Green for TX operations, Blue for RX/data
  - Periodic 500ms refresh for real-time updates
  - Terminal header shows entry count and status
  - Empty state displays ">>> Waiting for card communication..." when idle
  - All data sourced from actual module operations, not simulation

### Changed
- **CardReadViewModel**: Added apduLog StateFlow and refreshApduLogs() method
  - Periodically fetches latest entries from ModMainDebug (every 500ms)
  - Formats entries as "[Module] operation | key=value" strings
  - Logs refresh operations to ModMainDebug for tracking
  - Init block launches coroutine for continuous log updates

- **Card Read Screen**: Added ApduTerminalSection and ApduLogLine composables
  - ApduTerminalSection: Terminal card with dark background, scrollable content
  - ApduLogLine: Formatted log line with dynamic color coding
  - Integrated into main screen layout between RecentReadsSection and spacing
  - Uses monospace font (Courier New equivalent) for authentic terminal appearance
  - Max height 150-250dp with vertical scroll when content exceeds bounds

### Deprecated
- N/A

### Removed
- N/A

### Fixed
- N/A

### Security
- N/A

---

## [1.3.0] - 2025-11-05

### Added
- **screen-CardRead.kt** - Clean card reading interface with real device integration
  - Header status card showing real hardware status
  - NFC card reader panel with conditional UI states (waiting/reading/success)
  - Real-time APDU progress tracking (0-100%)
  - Card session details display (ID, type, status, timestamp)
  - Recent reads history list with database persistence
  - Status color coding (Green=SUCCESS, Yellow=PENDING, Red=FAILED)
  - Comprehensive KDoc for all composables with real data source documentation

### Changed
- **MainActivity.kt** - Integrated CardReadScreen in tab 1 navigation
  - Replaced `PlaceholderScreen("Card Reading")` with `CardReadScreen()`
  - Added CardReadScreen import
  - Tab 1 now displays real card reading UI instead of placeholder

### Technical Details
- Data sources: All real (CardSession from device, progress from APDU, status from hardware)
- No simulation functions or hardcoded mock data
- ModMainDebug logging integrated for card read initiation
- StateFlow pattern for reactive UI updates
- Null-safe handling of card session data
- Material Design 3 responsive layout

### Build Status
- ✅ BUILD SUCCESSFUL (4s, 0 errors, 0 warnings)
- MainActivity integration verified with CardReadScreen import
- Full compilation with new screen module

### Testing & Verification
- STEP 9 Consumer Integration: MainActivity updated and compiled successfully
- Self-validation: All data flows from real sources verified
- Build verification: Multi-pass compilation successful

---

## [1.2.0] - 2025-11-05

### Added
- **mod-main-debug.kt** - Centralized debug/logger module
  - In-memory circular logging buffer (500 entries max)
  - Autonomous AI debugging with ADB integration
  - JSON export functionality for session analysis
  - APDU command/response logging with hex conversion
  - Session tracking with unique ID + metadata
  - Device info capture (manufacturer, model, Android version, SDK)
  - Thread-safe coroutine-based logging
  - Periodic auto-export (every 50 entries + on APDU)
  - Raw APDU execution from AI commands via broadcast intent

### Changed
- **Gson dependency added** (v2.10.1)
  - JSON serialization for debug log exports

- **All modules integrated with centralized logging:**
  - **mod-main-nfsp00f.kt** - Module init, health check, device connection logging
  - **mod-emv-read.kt** - APDU execution, session creation, card scan workflow
  - **mod-emv-parser.kt** - TLV tag parsing, batch save operations
  - **mod-emv-database.kt** - Session and record creation
  - **mod-device-pn532.kt** - Bluetooth connection lifecycle, data transmission
  - **mod-device-androidnfc.kt** - NFC operations, transceive commands
  - **MainActivity.kt** - Debug logger initialization on app startup

### Build Status
- ✅ BUILD SUCCESSFUL (15s, 0 errors, 0 warnings)
- Full compilation with all logging integration
- APK generated successfully for debug + release

### Technical Details
- Debug logging points: 80+ across all modules
- APDU hex logging: All commands/responses captured
- Session tracking: Unique ID + timestamps for correlation
- Memory efficient: Circular buffer prevents unbounded growth
- Thread-safe: Synchronized access via coroutines
- JSON format: Pretty-printed for human readability
- AI integration ready: Autonomous command execution via ADB

### Testing & Verification
- STEP 9 Consumer Integration: 100% complete (6 modules + 1 activity)
- Self-validation: All type conversions and nullable safety checked
- Build verification: Multi-pass compilation successful
- Error handling: Null-safe logging throughout

---

## [1.1.0] - 2025-10-20

### Added
- **mod-main-nfsp00f.kt** - Main module manager
  - Module initialization and lifecycle management
  - Device health status monitoring
  - Inter-module communication coordination
  - Auto-connection to PN532 Bluetooth device (MAC: 00:14:03:05:5C:CB)

- **mod-device-pn532.kt** - PN532 NFC device adapter
  - Bluetooth connectivity (SSID: 'PN532')
  - USB device enumeration and connection
  - RFCOMM socket management
  - Data transmission and reception
  - Bluetooth device discovery

- **mod-device-androidnfc.kt** - Android NFC adapter
  - NFC adapter access and management
  - Reader mode with NFC-A/B/F/V support
  - NDEF message reading
  - NFC-A transceive operations
  - Tag discovery callbacks

### Changed
- **MainActivity.kt** - Added module manager initialization
  - Module initialization in onCreate()
  - Proper shutdown in onDestroy()
  - Full lifecycle management

- **AndroidManifest.xml** - Added device communication permissions
  - Bluetooth permissions (BLUETOOTH, BLUETOOTH_ADMIN, BLUETOOTH_SCAN, BLUETOOTH_CONNECT)
  - Location permissions for device discovery
  - USB permissions
  - NFC permissions
  - Feature declarations for optional hardware

### Security
- Proper exception handling for all Bluetooth/USB/NFC operations
- IOException and SocketTimeoutException caught and logged
- Resource cleanup on error (socket.close(), streams.close())
- Null safety checks for optional hardware
- Coroutine-based async operations prevent ANR

### Documentation
- Complete API reference for Bluetooth, USB, NFC
- Scope analysis and dependency mapping
- Self-validation with 100% pass rate
- Consumer verification report

---

## [1.0.0] - 2025-10-20

### Added
- Initial release of nf-sp00f-pro
- Project structure with modular design pattern
- Kotlin source files and resources
- AndroidManifest configuration
- build.gradle.kts with necessary dependencies
- Material Design 3 theme and colors
- Basic unit and instrumented tests
- README documentation
- 10-step development process workflow

### Project Details
- **Package Name:** com.nfsp00fpro.app
- **App Name:** nf-sp00f-pro
- **Min SDK:** Android 9 (SDK 28)
- **Target SDK:** Android 14 (SDK 34)
- **Language:** Kotlin
- **Java Version:** 11

---

## Version History Format

For future updates, follow this format:

```markdown
## [X.Y.Z] - YYYY-MM-DD

### Added
- New feature descriptions

### Changed
- Modification descriptions

### Deprecated
- Soon-to-be removed feature descriptions

### Removed
- Feature removal descriptions

### Fixed
- Bug fix descriptions

### Security
- Security fix descriptions
```

---

**Guidelines:**
- Update this file with EVERY new feature or change
- Use semantic versioning (MAJOR.MINOR.PATCH)
- Include dates in YYYY-MM-DD format
- Group changes by type (Added, Changed, etc.)
- Be descriptive and clear about what changed and why

---

**Last Updated:** October 20, 2025
