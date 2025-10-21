---
applyTo: '**'
description: Workspace-specific AI memory for this project
lastOptimized: '2025-10-21T06:13:40.610150+00:00'
entryCount: 1
optimizationVersion: 1
autoOptimize: true
sizeThreshold: 50000
entryThreshold: 20
timeThreshold: 7
---
# Workspace AI Memory
This file contains workspace-specific information for AI conversations.

## Personal Context (name, location, role, etc.)
- None stored.

## Professional Context (team, goals, projects, etc.)
- None stored.

## Technical Preferences (coding styles, tools, workflows)
- None stored.

## Communication Preferences (style, feedback preferences)
- None stored.

## Universal Laws (strict rules that must always be followed)
- None stored.

## Policies (guidelines and standards)
- None stored.

## Suggestions/Hints (recommendations and tips)
- None stored.

## Memories/Facts (chronological events and information)
- **2025-10-20 23:13:** Project Directory Structure: `/nf-sp00f-pro/` is the root workspace. Within it:
  - `/nf-sp00f-pro/nf-sp00f-pro/` - Android app source directory containing all app code.
  - `/nf-sp00f-pro/docs/` - Documentation directory, all files in `.md` (Markdown) format.
  - `/nf-sp00f-pro/tmp/` - Temporary files directory for scratch work and temporary items.- **2025-10-20 23:17:** Android App Configuration: Package name: com.nfsp00fpro.app, App name: nf-sp00f-pro, Language: Kotlin, Target SDK: Android 14 (SDK 34), Min SDK: Android 9 (SDK 28), Java Version: 11. Project structure includes: src/main/java (Kotlin source), src/main/res (resources: layout, values, drawable), src/test (unit tests), src/androidTest (instrumented tests).
- **2025-10-20 23:20:** Project Architecture: nf-sp00f-pro uses a modular design pattern. All feature modules are organized under /src/main/java/com/nfsp00fpro/app/modules/. Each module is self-contained and follows SOLID principles for clean architecture.
- **2025-10-20 23:25:** Documentation Files Created: README.md and CHANGELOG.md have been created in the nf-sp00f-pro directory. README.md contains project overview, structure, tech stack, and development workflow. CHANGELOG.md tracks all feature additions and changes - UPDATE THIS FILE EVERY TIME A NEW FEATURE IS ADDED.
- **2025-10-20 23:34:** Three Core Modules Scope - STEPS 1-3 Complete: (1) mod-main-nfsp00f.kt: Module/device manager initializing modules, managing health, coordinating communication. (2) mod-device-pn532.kt: PN532 NFC adapter supporting Bluetooth (SSID: 'PN532', MAC: '00:14:03:05:5C:CB') and USB with auto-connect. (3) mod-device-androidnfc.kt: Android internal NFC adapter. Consumer impact: MainActivity needs module init, AndroidManifest needs permissions (BLUETOOTH, BLUETOOTH_ADMIN, BLUETOOTH_SCAN, BLUETOOTH_CONNECT, USB, NFC), build.gradle.kts verified. Dependencies: android.bluetooth.*, android.hardware.usb.*, android.nfc.*, kotlinx.coroutines.
