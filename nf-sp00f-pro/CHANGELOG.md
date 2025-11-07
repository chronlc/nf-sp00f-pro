# Changelog

All notable changes to the nf-sp00f-pro Android application will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [Unreleased]

### Added
- **Professional Sleek Card Reading Screen** (Complete UI redesign - v1.4.0)
  - **Design Philosophy**: Clean, data-focused, premium production-ready interface
  - **Reference Design**: Based on sleek EMV scanner reference design for professional appearance
  - **Color Scheme**: Professional dark blue theme (Navy #0A0E27 background, #131C42 cards) with strategic accent colors
  - **Status Header Card**:
    - EMV CARD SCANNER title in success green (#4CAF50)
    - Reader Status chip with checkmark/error icon (green=connected, red=no reader)
    - Statistics row: Cards scanned, APDUs, Tags (from real database counts)
  - **Control Panel Card**:
    - Reader dropdown: "Android NFC", "PN532 USB", "PN532 BT" (default Android NFC)
    - Protocol dropdown: "EMV/ISO-DEP", "Auto-Detect" (default EMV/ISO-DEP)
    - Start/Stop Scan button with icons (Play icon start, Stop icon stop, colors dynamic)
    - Button colors: Success green when idle, error red when scanning
  - **Advanced Settings Section** (collapsible):
    - Transaction Amount field (numeric input)
    - TTQ field (hex input for terminal transaction qualifiers)
    - Transaction Type dropdown (Purchase, Withdrawal, Refund)
    - Cryptographic Select dropdown (ARQC, TC, AAC)
  - **ROCA Vulnerability Status Card**:
    - Shown only when ROCA analysis data available from database
    - Background color indicates status: Green (#1B5E20) safe, Red (#7F0000) vulnerable
    - Icon: Checkmark (safe) or Warning (vulnerable) with appropriate coloring
  - **Active Cards Section**:
    - Title: "ACTIVE CARDS (n)" in success green
    - Horizontal scrollable LazyRow of virtual cards
    - Each card shows: Card status, session ID prefix, blue gradient background (#1A237E)
    - Pagination info: "X cards scanned - scroll to view all" when > 3 cards
  - **EMV Data Display Section**:
    - Title: "EMV DATA EXTRACTED (n fields)" in success green
    - Organized key-value display of parsed EMV tags from database
    - Each field: Key (uppercase, secondary color), Value (success green, bold)
    - Scrollable LazyColumn with max height 300dp
  - **APDU Terminal Section**:
    - Title: "APDU TERMINAL" in success green with command count
    - Black terminal background (#0D0D0D) with monospace font
    - Shows last 50 APDU entries from ModMainDebug logs
    - Green-on-black aesthetic (#4CAF50 text, #0D0D0D background)
    - Placeholder text: ">>> Waiting for card communication..." when no APDUs yet
  - **Data Sources**: ALL real from hardware/database, ZERO mock/simulation:
    - CardSession from device NFC reads (real sessions persisted in database)
    - APDU logs from ModMainDebug (real TX/RX communication logs)
    - EMV data from parsed TLV tags in database (real card data extraction)
    - Statistics from database count queries (real count operations)
    - ROCA analysis from EmvReader module (real cryptographic analysis)
  - **Professional Theme Objects**:
    - CardReadingTheme: Complete color palette (16 colors for all UI states)
    - CardReadingSpacing: Consistent spacing scale (Tiny 2dp to Huge 32dp)
    - CardReadingRadius: Rounded corner radius scale (Small 4dp to ExtraLarge 24dp)
    - CardReadingDimensions: Standard component sizes (button heights, terminal height)
  - **KDoc Comments**: Every composable documented with purpose, data sources, and display logic
  - **Production Code**: Zero hardcoded data, zero simulation functions, zero test stubs

- **CardReadViewModel Enhancements**: New stats capability
  - Added CardReadStats data class: cardsScanned, apduCount, tagsCount
  - Added _cardReadStats StateFlow: Real-time statistics from database
  - Added refreshCardReadStats() method: Queries total counts from CardSession/ApduLog/TlvTag tables
  - Periodic stats refresh via LaunchedEffect on screen load

- **EmvDatabase Query Methods**: New count operations
  - Added getSessionCount(): Total CardSession records (cards scanned count)
  - Added getApduLogCount(): Total ApduLog records (APDU commands/responses count)
  - Added getTlvTagCount(): Total TlvTag records (parsed EMV tags count)
  - Added DAO methods in CardSessionDao, ApduLogDao, TlvTagDao for efficient queries

  - EmvTagDataSection: New EMV data display showing sessionId, card type, status, and read timestamp
  - ApduTerminalSection: Improved terminal with scrollable display (NO line limits), EMV-only filtering
    - Color-coded log lines: Green for TX, Blue for RX, Orange for AID/errors
    - Monospace terminal font for authenticity
  - All data sourced from real database (CardSession, AidRecord, TlvTag, ApduLog entities)

- **EmvReader Integration in CardReadViewModel**: Production card reading workflow
  - CardReadViewModel now initializes EmvReader module and calls emvReader() when card detected
  - Added onCardDetected() callback method for NFC detection integration
  - Card data automatically extracted, parsed, and saved to database via EmvReader module
  - Progress updates: 0→25→50→100% during card reading
  - Real data flow: NFC tap → emvReader() → database → UI StateFlow

### Fixed
- **Stop Button Display Bug**: Stop button now visible during card reading
  - Root cause: startCardReading() was setting _isReading=false in finally block
  - Solution: Removed finally block, keep _isReading=true until stopCardReading() called
  - Stop button conditional visibility now works: `if (isReading) { Stop Button }`
  - User can now tap Stop during reading to gracefully terminate NFC polling

### Architecture
- All card reading logic moved to modules (EmvReader, EmvDatabase)
- Screen files only handle UI display and state reflection
- Module-to-ViewModel pattern: modules perform operations, viewModel orchestrates

### Added (Previous)
- **Reader Selection & Control Panel**: Dynamic NFC reader selection on Card Read Screen
  - Dropdown selector for "Android NFC" (built-in) and "PN532 Bluetooth" (wireless) readers
  - Dropdown only displays when no card is being read (clean UI)
  - Stop button appears during card reading for graceful termination
  - Clicking Stop calls moduleManager.disableNfcReaderMode() to halt NFC listening
  - Reader selection persists in ViewModel and logs to ModMainDebug
  - All UI state driven by real device status, no hardcoded values

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
