---
applyTo: '**'
description: Code Generation Rules - Universal Laws for Production Code
priority: critical
enforceStrict: true
created: '2025-10-07'
---

# CODE GENERATION RULES (Universal Laws)

**Apply to:** */*
**Created:** October 7, 2025  
**Purpose:** Enforce systematic, precise, validated approach to ALL code generation  
**Authority:** Universal Law - NO EXCEPTIONS

---

## FUNDAMENTAL PRINCIPLE

**PRECISION OVER SPEED. VALIDATION OVER ASSUMPTION. SYSTEMATIC OVER AD-HOC.**

The root cause of compilation errors is NOT lack of knowledge - it's **lack of discipline**:
- **GUESSING** instead of **READING**
- **ASSUMING** instead of **VALIDATING**
- **INFERRING** instead of **VERIFYING**
- **IMPROVISING** instead of **PLANNING**

This applies to ALL code generation: UI, business logic, data models, tests, APIs, migrations, refactoring.

---

## THE SYSTEMATIC PROCESS (Universal Workflow)

**This process applies to EVERY code generation task. EVERY. SINGLE. ONE.**

---

### STEP 1: SCOPE DEFINITION (30 seconds)

**What am I building?**
1. Write 1-2 sentence description of the task
2. List success criteria (e.g., "Compiles without errors", "Passes all tests", "No runtime crashes")
3. Identify obvious dependencies (external libs, internal classes, APIs)
4. **CRITICAL:** Identify if this change affects existing code (provider change? replaces existing?)

**TODO LIST MANAGEMENT:**
- [ ] Create todo list in VS Code using `manage_todo_list` tool
- [ ] Mark current task as `in-progress` before starting work
- [ ] Break down complex tasks into actionable steps
- [ ] Update todo status as each step completes
- [ ] Mark final task as `completed` before declaring task done

**Example - No Ripple Effect:**
```
Task: Generate ProductListScreen
Description: New composable screen to display list of products from ViewModel
Success Criteria: Compiles first try, displays products, navigates to detail on tap
Dependencies: ProductViewModel, Product data class, Compose Material3, Navigation
Ripple Effect: NO - new screen, doesn't replace or modify existing code
```

**Example - WITH Ripple Effect:**
```
Task: Create UnifiedHardwareManager (replaces HardwareDetectionService)
Description: Consolidates NFC/Bluetooth device detection into single manager
Success Criteria: Compiles first try, all consumers updated and working, no crashes
Dependencies: NfcDeviceModule, framework.devices APIs, existing consumers
Ripple Effect: YES - replaces HardwareDetectionService
  - Consumers identified: MainActivity.kt, DashboardScreen.kt, DashboardViewModel.kt
  - Estimated updates: 18 changes across 3 files
  - Estimated time: Provider (30 min) + Consumers (20 min) = 50 min total
```

---

### STEP 2: CONSUMER IMPACT ANALYSIS (2-3 minutes, MANDATORY IF ripple effect)

**Do existing files use what I'm changing?**

**This step is NON-OPTIONAL if a ripple effect is identified in Step 1.**

#### Systematic Search Protocol:

```bash
# For class/interface replacements:
grep -r "OldClassName" src/ --include="*.kt" --include="*.java"

# For method signature changes:
grep -r "methodName(" src/ --include="*.kt" --include="*.java"

# For data structure changes:
grep -r "OldDataClass" src/ --include="*.kt" --include="*.java"

# For object property access:
grep -r "propertyName" src/ --include="*.kt" --include="*.java"

# For import statements:
grep -r "import.*OldClassName" src/ --include="*.kt" --include="*.java"
```

#### Document Findings:

Create a **Consumer Impact Report**:

```
CONSUMER IMPACT ASSESSMENT
========================

Target Change: UnifiedHardwareManager (replaces HardwareDetectionService)

Search Results:
- Direct imports: 3 files
- Direct instantiation: 4 locations
- Method calls: 12 locations
- State access: 5 locations
- Total code locations affected: 24

Files to Update:
1. MainActivity.kt
   - Line 45: import statement
   - Line 67: initialization call
   - Line 89: method call (getDeviceStatus)
   
2. DashboardScreen.kt
   - Line 12: import statement
   - Line 34: state access (deviceService.state)
   - Line 56: method call (refresh)
   
3. DashboardViewModel.kt
   - Line 8: import statement
   - Line 19: constructor dependency
   - Lines 45-67: method calls (8 locations)
   
4. [Additional files if found]

Type of Changes Required:
- Import updates: 3 locations
- Constructor/initialization: 4 locations
- Method calls: 12 locations
- State property access: 5 locations

Estimated Update Time: 20-25 minutes
Risk Level: MEDIUM (affects critical startup path)
Testing Required: Manual test of MainActivity launch and DashboardScreen render
```

#### Decision Gate:

**BEFORE proceeding to Step 3, answer:**

- [ ] Have I identified ALL consumer files?
- [ ] Do I have search results documenting each location?
- [ ] Have I estimated time for all consumer updates?
- [ ] Do I have time to complete BOTH provider AND consumer updates NOW?
- [ ] Can I commit to updating ALL consumers before declaring task complete?

**If ANY answer is NO:**
- ❌ Do NOT proceed to Step 3
- Stop and clarify scope, time, or dependencies with user
- Re-evaluate if task should be split or postponed

**If ALL answers are YES:**
- ✅ Proceed to Step 3
- Keep the Consumer Impact Report available for Step 9 verification

---

### STEP 3: DEPENDENCY AND CONSUMER MAPPING (2-5 minutes)

**What do I need to know?**
- List ALL classes/interfaces this code interacts with
- List ALL APIs/frameworks this code uses
- For each dependency: identify file path
- Cross-reference with consumer impact from Step 2 (if applicable)

**Example:**
```
Dependencies for ProductListScreen:
1. ProductViewModel - app/viewmodels/ProductViewModel.kt
2. Product data class - app/models/Product.kt
3. Compose Material3 - Text, Card, LazyColumn APIs
4. Navigation - app/navigation/NavGraph.kt
```

---

### STEP 4: DEFINITION READING & NAMING SCHEME VERIFICATION (5-10 minutes)

**Read and document. No skipping. No skimming.**

For EACH dependency:
1. Open the file
2. Read relevant classes/methods
3. Document exact names, types, signatures
4. Note any constraints or special requirements

**NAMING SCHEME COMPLIANCE CHECK (nf-sp00f-pro):**
- [ ] Kotlin files: `kebab-case` (e.g., `mod-device-pn532.kt`)
- [ ] Classes: `PascalCase` (e.g., `ModDevicePn532`, `ModuleHealth`)
- [ ] Functions: `camelCase` (e.g., `initialize()`, `connectBluetoothDevice()`)
- [ ] Constants: `UPPER_SNAKE_CASE` (e.g., `PN532_BLUETOOTH_MAC`)
- [ ] Boolean functions: `isXxxx()` or `canXxxx()` (e.g., `isInitialized()`, `canConnect()`)
- [ ] Getters: `getXxxx()` pattern (e.g., `getHealthStatus()`)
- [ ] Private functions: `private fun camelCase()`
- [ ] Data classes: PascalCase, descriptive names
- [ ] Parameters: camelCase (e.g., `bluetoothDevice`, `bufferSize`)
- [ ] Variables: camelCase, descriptive (e.g., `bluetoothSocket`, `connectedDevice`)
- [ ] Boolean variables: `isXxxx` prefix (e.g., `isInitialized`, `isBluetoothConnected`)

**If ANY naming violation found → STOP and plan naming refactor before proceeding**

**Example - Reading ProductViewModel.kt:**
```
ProductViewModel.kt opened

StateFlows found:
- products: StateFlow<List<Product>>           ← List of Product, not Product?
- isLoading: StateFlow<Boolean>
- errorMessage: StateFlow<String?>             ← String?, nullable

Methods found:
- loadProducts(): Unit                         ← No parameters, returns Unit
- refreshProducts(): Unit
- searchProducts(query: String): Unit          ← String parameter, not String?
ok now lets do our another scope, when i give you a scope you make sure you run this through our 10 step rules. Also I want you to add the generated workflow from the various steps (like code gen and the other steps) to our VSCodes internal TODO list so it displays and update the TODO list as you go with checkmarks and so on.

Scope:
- Ok now for the next module i want it to be a debug/logger for internal/debug pruposes, lets call this module 'mod-main-debug.kt'. This module main function will be called debugLog().  THis module will not only provide us a debug manager for you, yes you AI, to be able to fully debug program operations autonomosly.  I will have phone connected via ADB for you so code gen it well so you can cantrol program and be able to send raw apdus to the devices when needed.  This will also produce a log file (JSON) and will make it easier to see the programs flow and fully debug.  Fully integrate this module with our modules and UI files as needed. I Would like to update all modules and UI files to use this new logger/debug module so we can interact with app fully at debug.
No enums in this class.
```

**Example - Reading Product.kt:**
```
Product.kt opened

data class Product(
    val id: Long,                              ← Long, not Int
    val name: String,                          ← String, non-nullable
    val price: Double,                         ← Double, not Float
    val imageUrl: String?,                     ← String?, nullable
    val inStock: Boolean
)

Properties documented. No nested classes.
```

---

### STEP 5: INTERFACE MAPPING (2-3 minutes)

**How do components connect?**
- Document data flow (ViewModel → Screen → Composables)
- Note type transformations needed (e.g., Long → String for display)
- Identify potential type mismatches

**Example:**
```
Data Flow for ProductListScreen:

ViewModel                     Screen                      UI
products: List<Product>  →    val products by         →   LazyColumn {
                              viewModel.products           items(products) {
                              .collectAsState()              ProductCard(it)
                                                          }
                                                       }

Type Conversions Needed:
- Product.price (Double) → Text needs String → "${product.price}"
- Product.imageUrl (String?) → AsyncImage needs non-null → imageUrl ?: placeholderUrl

No other conversions needed.
```

---

### STEP 6: GENERATION WITH PRECISION (10-30 minutes)

**Code using ONLY information from Steps 1-5.**

Rules:
- Reference your documentation from Steps 2-5
- Copy names character-by-character (don't retype from memory)
- Use exact types documented
- Apply explicit conversions where documented
- No improvisation, no "I think it's...", no guessing
- **ADD COMPREHENSIVE COMMENTS** explaining what each block does and why
- **NO SIMULATION, NO MOCK DATA** - ONLY real data from actual sources

**Anti-Pattern - NEVER DO THIS:**
```kotlin
// ❌ Typing from memory, guessing
val items = viewModel.items.collectAsState()  // Wrong! It's "products" not "items"
Text(product.cost)                            // Wrong! It's "price" not "cost", and needs .toString()

// ❌ HARDCODED DATA - NEVER DO THIS
HardwareComponentRow(
    title = "PN532 NFC Module (USB)",
    status = "Available",                     // ❌ HARDCODED - Not real data!
    details = "Ready for connection"
)

// ❌ SIMULATION/MOCK DATA - NEVER DO THIS
fun simulateCardRead() {
    _cardData.value = CardSession(           // ❌ Creating fake data
        pan = "123456XXXXXX7890",             // ❌ This is simulation, not real device
        cardHolder = "Test User"
    )
}
```

**CRITICAL RULES - NO HARDCODED DATA, NO SIMULATION:**
- ❌ NEVER hardcode status strings like "Available", "Ready", "Connected"  
- ❌ NEVER use placeholder values that pretend to be real
- ❌ NEVER create `simulate*()` methods that generate fake data
- ❌ NEVER create mock data generators in production code
- ✅ ALWAYS bind to real data sources (StateFlow, LiveData, coroutines, actual device)
- ✅ ALWAYS read actual device state, API responses, database queries, hardware modules
- ✅ If data isn't available yet, show a loading state, not fake data
- ✅ If a feature requires device interaction, connect to actual device module

**Correct Pattern - ONLY REAL DATA:**
```kotlin
// ✅ Referencing documentation from Step 4
val products by viewModel.products.collectAsState()  // ✅ Exact name from Step 4
Text("${product.price}")                             // ✅ Exact name + explicit conversion from Step 5

// ✅ REAL DATA - Always use actual state from devices/database/API
val pn532Status by viewModel.pn532UsbStatus.collectAsState(initial = "Initializing")
HardwareComponentRow(
    title = "PN532 NFC Module (USB)",
    status = pn532Status,                     // ✅ Real data from ViewModel
    details = "USB device state: $pn532Status"
)

// ✅ Real data loading from database
fun loadRecentReads() {
    viewModelScope.launch {
        try {
            // Real data source: EmvDatabase - gets actual sessions from device storage
            val reads = emvDatabase.getRecentSessions(limit = 10)
            _recentReads.value = reads
            // Log the operation for debugging
            ModMainDebug.debugLog(
                module = "CardReadViewModel",
                operation = "recent_reads_loaded",
                data = mapOf("count" to reads.size.toString())
            )
        } catch (e: Exception) {
            e.printStackTrace()
            // Log error for debugging
            ModMainDebug.debugLog(
                module = "CardReadViewModel",
                operation = "load_error",
                data = mapOf("error" to (e.message ?: "Unknown error"))
            )
        }
    }
}
```

**COMMENTING REQUIREMENTS - EVERY GENERATED FILE:**

**KDoc for Every Function/Method:**
```kotlin
/**
 * Brief one-line description of what this does
 * 
 * Longer explanation if needed - what is the purpose, what does it return
 * 
 * @param paramName Description of this parameter - what data type, what range
 * @return Description of return value - what data is returned, when is it null
 * 
 * Real Data Source: [WHERE the data comes from - database/API/device/sensor/etc]
 * Logging: [If this logs, what operation name is used]
 */
fun methodName(paramName: Type): ReturnType
```

**Inline Comments for Every Complex Block:**
```kotlin
// Initialize real data source connection - gets actual records from database
val database = EmvDatabase(context)

// Collect real StateFlow data - represents actual card sessions previously read
val recentReads by viewModel.recentReads.collectAsState(initial = emptyList())

// Update UI with real hardware status from device module - "Ready", "Error", etc.
_hardwareStatus.value = actualDeviceStatus

// Error handling with real error logging
try {
    val data = realDataSource.fetch()  // Could throw real exception from device/database
} catch (e: Exception) {
    // Log the real error that occurred for debugging
    ModMainDebug.debugLog("ModuleName", "operation", mapOf("error" to e.message))
}
```

**StateFlow Property Comments:**
```kotlin
// Real card session from hardware read (null until device provides actual card)
private val _cardData = MutableStateFlow<CardSession?>(null)

// Actual reading state from NFC hardware polling (true only during active device scan)
private val _isReading = MutableStateFlow(false)

// Real progress from actual device APDU transaction (0-100%)
private val _readingProgress = MutableStateFlow(0)
```

**Comments Should Answer:**
- ❓ "Where does this data come from?" → Answer in comment
- ❓ "Is this real or test data?" → Make it clear
- ❓ "What values can this have?" → Document valid range/states
- ❓ "When will this be null/empty?" → Explain conditions
- ❓ "What happens if this fails?" → Document error handling

**Real Examples - GOOD Code Comments:**
```kotlin
/**
 * Load recent card reading sessions from persistent database storage
 * Returns real CardSession records from previous device reads
 * 
 * @return List of actual card sessions stored in database
 * 
 * Real Data Source: EmvDatabase.getRecentSessions() - direct database query
 * Logging: Logs count of sessions retrieved and any database errors
 */
private fun loadRecentReads() {
    viewModelScope.launch {
        try {
            // Query real database for actual card reading sessions (persistent records)
            val reads = emvDatabase.getRecentSessions(limit = 10)
            
            // Update StateFlow with real data - not null until database has records
            _recentReads.value = reads
            
            // Log successful real data retrieval for debugging
            ModMainDebug.debugLog(
                module = "CardReadViewModel",
                operation = "recent_reads_loaded",
                data = mapOf("count" to reads.size.toString())
            )
        } catch (e: Exception) {
            // Real exception from database operation - log for troubleshooting
            e.printStackTrace()
            ModMainDebug.debugLog(
                module = "CardReadViewModel",
                operation = "load_error",
                data = mapOf("error" to (e.message ?: "Unknown error"))
            )
        }
    }
}

/**
 * Start real NFC card reading from actual hardware device
 * System will listen for real card detection and update cardData StateFlow
 * 
 * Real Data Source: Awaits input from actual NFC hardware device module
 * Logging: Logs when system is ready for device input
 */
fun startCardReading() {
    viewModelScope.launch {
        // Set to true only during actual hardware device polling
        _isReading.value = true
        
        // Log that system is ready for real device to provide card data
        ModMainDebug.debugLog(
            module = "CardReadViewModel",
            operation = "card_read_start",
            data = mapOf("timestamp" to System.currentTimeMillis().toString())
        )

        try {
            // System waits for ACTUAL NFC device to detect and read card
            // When real device provides data, it will populate CardSession in database
            
            // Update status to show device is ready for actual card read
            _hardwareStatus.value = "Ready for card"
            
            // Log readiness - system is in active listening state
            ModMainDebug.debugLog(
                module = "CardReadViewModel",
                operation = "waiting_for_device_read",
                data = mapOf("ready" to "true")
            )
            
        } catch (e: Exception) {
            // Real error from device interaction attempt
            e.printStackTrace()
            
            // Update status to show error occurred
            _hardwareStatus.value = "Read Error"
            
            // Log real error for debugging device issues
            ModMainDebug.debugLog(
                module = "CardReadViewModel",
                operation = "card_read_error",
                data = mapOf("error" to (e.message ?: "Unknown error"))
            )
        } finally {
            // Reset to false after read attempt completes
            _isReading.value = false
            _readingProgress.value = 0
        }
    }
}
```

**Checklist Before Submitting Generated Code:**
```
Code Comments:
□ Every function has KDoc explaining purpose and real data source
□ Every StateFlow property has comment explaining what real data it represents
□ Every data-binding line has inline comment explaining where data comes from
□ Every database/API call has comment noting it's real data
□ Every exception handler has comment about what real error it's handling
□ Every null/empty state has comment explaining when it occurs
□ Every loop/condition has comment if not obvious what real data it's processing

Code Clarity:
□ If a developer reads a line, they KNOW where the data comes from (no guessing)
□ If a developer reads a line, they KNOW if it's real or test data
□ If a developer reads a line, they KNOW what can go wrong and how errors are logged
□ No confusing variable names - names match documentation from STEP 4 exactly
□ No "magic values" - all constants explained via comments or KDoc
```

---

### STEP 7: SELF-VALIDATION (5-10 minutes)

**Review before compile. Catch errors in review, not compilation.**

For generated code:
1. Compare every property name with Step 4 documentation
2. Compare every method call with Step 4 documentation
3. Verify every type conversion from Step 5 is applied
4. Check for any guessed names (if you didn't document it, you guessed it)
5. **CRITICAL: Search for simulate/mock functions and remove ALL of them**
6. **CRITICAL: Verify ALL data comes from real sources, never hardcoded**

**Validation Checklist:**
```
□ Every property access verified against documentation
□ Every method call verified against documentation
□ Every type conversion applied as documented
□ No names used that weren't in documentation
□ No assumptions about nullability
□ No assumptions about types
□ NO hardcoded status strings or mock data values
□ NO simulate*() functions that generate fake data
□ NO mock data constructors that create test values
□ ALL UI data bound to real sources (StateFlow, API, database, sensors)
□ Loading states used when data not yet available
□ Every StateFlow/data property has inline comment explaining data source
□ Every ViewModel method documents where real data comes from
□ Every error handling includes ModMainDebug logging
```

**SPECIFIC ANTI-PATTERNS TO ELIMINATE:**
```kotlin
// ❌ REMOVE ALL OF THESE PATTERNS:

// Mock/simulate functions
fun simulateCardRead() { ... }  // ❌ DELETE
fun mockHardwareStatus() { ... }  // ❌ DELETE
fun generateTestData() { ... }  // ❌ DELETE

// Hardcoded fake values
_cardData.value = CardSession(pan = "123456...", cardHolder = "Test")  // ❌ DELETE
_status.value = "Ready"  // ❌ Change to real data from device/API/database

// Placeholder/test data passed as parameters
createCard(pan = "1234567890", holder = "TestUser")  // ❌ DELETE or use real values
```

**CORRECT PATTERN - REAL DATA ONLY:**
```kotlin
// ✅ KEEP THESE PATTERNS:

// Real data from actual sources
fun loadRecentSessions() {
    viewModelScope.launch {
        try {
            val sessions = emvDatabase.getRecentSessions(limit = 10)  // ✅ Real database
            _sessions.value = sessions
        } catch (e: Exception) {
            // Log real errors
            ModMainDebug.debugLog("Module", "operation", mapOf("error" to e.message))
        }
    }
}

// Real hardware state
val bluetoothStatus = ModDeviceBluetoothAdapter.getStatus()  // ✅ Real device state
_status.value = bluetoothStatus  // ✅ Real value from device
```

---

### STEP 7.5: FUNCTIONALITY PRESERVATION VALIDATION (5 minutes, MANDATORY IF CODE SIMPLIFIED)

**Critical Gate: Did simplification remove intended functionality?**

**When to apply this step:**
- You simplified code by removing methods/properties
- You changed architecture or refactored existing code
- You removed mock data or test functions
- You combined multiple functions into fewer functions
- You changed how data flows through the system

**Functionality Verification Checklist:**

Before marking code complete, answer YES to ALL of these:

```
□ What was the ORIGINAL intended functionality?
  - Document what the code was supposed to do
  - List all features/operations it should perform
  - Note all user interactions it should support

□ What functionality was REMOVED or CHANGED?
  - List every method that was deleted
  - Document every property that was removed
  - Note every operation that was simplified
  - Explain WHY each removal was necessary

□ Can all original use cases still be performed?
  - Test scenario: Can user still trigger all operations?
  - Test scenario: Do all data flows still work?
  - Test scenario: Are all error conditions still handled?
  - Test scenario: Is all logging still in place?

□ Was functionality moved or just removed?
  - If moved: Where is the functionality now? Is it accessible?
  - If moved: Is it documented where functionality went?
  - If removed: Is there a TODO explaining why and when it will be restored?

□ Are there any TODO comments for future work?
  - Mark clearly what functionality is pending device integration
  - Document what real hardware interaction is needed
  - Specify what conditions need to be met before functionality is complete
  - Include timeline or priority

□ Will real device interaction eventually provide this?
  - If waiting for device: Is path clear for device to integrate?
  - If waiting for device: Are stub methods in place to accept real data?
  - If waiting for device: Is there a plan/TODO for device integration?
```

**Example - BAD (Functionality Lost):**
```kotlin
// ❌ BEFORE: Had temperature reading
fun readTemperature() {
    val temp = sensor.getTemperature()
    _temperature.value = temp
}

// ❌ AFTER: Functionality completely removed
// (No comment explaining why, no TODO, no alternative)
// User can no longer read temperature at all
```

**Example - GOOD (Functionality Preserved):**
```kotlin
// ✅ BEFORE: Had mock temperature reading for testing
fun readTemperature() {
    _temperature.value = "72°F"  // Mock data for testing
}

// ✅ AFTER: Refactored to use real hardware
// (Clear documentation of what changed and why)
/**
 * Start real temperature read from actual sensor hardware
 * TODO: Integrate with ModDeviceSensor.readTemperature() when device API available
 * Currently logs readiness for real device - no mock data
 */
fun startTemperatureRead() {
    viewModelScope.launch {
        try {
            // Real data will come from: ModDeviceSensor.readTemperature()
            // For now: System is ready to receive real sensor data
            ModMainDebug.debugLog(
                module = "TempViewModel",
                operation = "temperature_read_ready",
                data = mapOf("waiting_for" to "real_device_input")
            )
        } catch (e: Exception) {
            ModMainDebug.debugLog(
                module = "TempViewModel",
                operation = "read_error",
                data = mapOf("error" to e.message)
            )
        }
    }
}
```

**Example - Functionality Preserved with TODO:**
```kotlin
/**
 * Removed simulateCardRead() because:
 * - Mock data should not exist in production code
 * - Real device integration required instead
 * 
 * TODO: Card reading will be provided by actual NFC device when:
 * - ModMainNfsp00f completes device read integration
 * - Real APDU responses are received from card
 * - Session data is stored in database
 * 
 * Until then: startCardReading() logs readiness for device input
 * and loads real historical sessions from database
 */
fun startCardReading() {
    // Real card read will come from actual hardware
}
```

**GATE CHECK - Do NOT proceed to STEP 8 if:**
- ❌ Functionality was removed but not documented with TODO
- ❌ Functionality was removed but no replacement path exists
- ❌ Code appears to work but doesn't actually perform intended operations
- ❌ User interactions removed but not documented why
- ❌ Testing capability removed without explanation
- ❌ Any original use case can no longer be performed

**If any check fails:**
1. Add explicit TODO comments documenting what's missing
2. Ensure real data source is documented (device, API, database)
3. Add placeholder/stub for future functionality
4. Document why simplification was necessary
5. THEN proceed to STEP 8

---

### STEP 8: COMPILE AND VERIFY (1-2 minutes)

**The moment of truth.**

```bash
./gradlew compileDebugKotlin
```

**Expected outcome:** BUILD SUCCESSFUL

**If compilation fails:**
1. Read EXACT error message (don't guess what it means)
2. Identify which step failed:
   - Wrong property name? → Step 4 was incomplete
   - Wrong type? → Step 5 missed a conversion
   - Wrong signature? → Step 4 was incomplete
3. Go back to that step, complete it properly
4. Update your process to catch this earlier next time

**If compilation succeeds:**
✅ Process worked. Apply same process to next task.

---

### STEP 9: CONSUMER UPDATE VERIFICATION (5-15 minutes, IF ripple effect identified)

**Did I update ALL consumers?**

**If Step 1 identified consumers:**

1. **Verify each consumer updated:**
```
Consumer Checklist:
□ MainActivity.kt - Updated imports ✅
□ MainActivity.kt - Updated initialization ✅  
□ MainActivity.kt - Updated method calls ✅
□ DashboardScreen.kt - Updated imports ✅
□ DashboardScreen.kt - Updated state access ✅
□ DashboardViewModel.kt - Updated imports ✅
□ DashboardViewModel.kt - Updated initialization ✅

All consumers updated: YES
```

2. **Recompile with consumers:**
```bash
./gradlew :nf-sp00f-pro:compileDebugKotlin
```

3. **Expected outcome:** BUILD SUCCESSFUL (with ALL consumers working)

4. **Test affected features:**
```bash
# If UI changed, install and test
./gradlew :nf-sp00f-pro:assembleDebug
adb install -r nf-sp00f-pro/build/outputs/apk/debug/nf-sp00f-pro-debug.apk
adb shell am start -n com.nfsp00fpro.app/.MainActivity
# Verify screens load, no crashes
```

5. **Clean up old code:**
```
If new implementation fully replaces old:
□ Delete old provider file (if unused)
□ Remove @Deprecated markers (if migration complete)
□ Update documentationok now lets do our another scope, when i give you a scope you make sure you run this through our 10 step rules. Also I want you to add the generated workflow from the various steps (like code gen and the other steps) to our VSCodes internal TODO list so it displays and update the TODO list as you go with checkmarks and so on.

Scope:
- Ok now for the next module i want it to be a debug/logger for internal/debug pruposes, lets call this module 'mod-main-debug.kt'. This module main function will be called debugLog().  THis module will not only provide us a debug manager for you, yes you AI, to be able to fully debug program operations autonomosly.  I will have phone connected via ADB for you so code gen it well so you can cantrol program and be able to send raw apdus to the devices when needed.  This will also produce a log file (JSON) and will make it easier to see the programs flow and fully debug.  Fully integrate this module with our modules and UI files as needed. I Would like to update all modules and UI files to use this new logger/debug module so we can interact with app fully at debug.
□ Remove any migration TODOs
```

**Task ONLY complete when:**
- ✅ Provider changed
- ✅ ALL consumers updated  
- ✅ BUILD SUCCESSFUL with consumers
- ✅ Features tested and working
- ✅ Old code removed (if applicable)

**If ANY step incomplete → Task is NOT done, continue working**

---

### STEP 10: CHANGELOG UPDATE (5 minutes)

**Document all changes in CHANGELOG.md**

**Action Items:**
- Open `CHANGELOG.md` in nf-sp00f-pro directory
- Add entry at TOP with current date
- Format: `[Date] - [Feature/Fix]: Description of change`
- Include scope (e.g., SCOPE-6, BUG-FIX, REFACTOR)
- List affected files
- Note if this is breaking change
- Include commit hash reference

**Changelog Entry Format:**
```
## [2025-11-05] - Feature/Fix: Brief description

### Changes
- File 1: Description of changes
- File 2: Description of changes

### Details
- Commit: abc1234def
- Scope: SCOPE-6 (or feature name)
- Breaking Changes: None (or list them)
- Consumer Impact: List affected modules/files
```

**Verification:**
- ✅ CHANGELOG.md updated
- ✅ Entry is at the top (most recent)
- ✅ Date is current
- ✅ All changed files documented
- ✅ Commit hash included

---

### STEP 11: GIT COMMIT & VERSION CONTROL (5 minutes)

**Track changes properly with git**

**Action Items:**
1. Stage all changed files:
```bash
git add .
```

2. Review staged changes:
```bash
git diff --cached
```

3. Commit with clear message:
```bash
git commit -m "[SCOPE] Brief description

- File 1: Change summary
- File 2: Change summary"
```

4. Verify commit:
```bash
git log -1 --stat
```

**Commit Message Requirements (Minimal & Professional):**
- ✅ Scope: `[SCOPE-N]`, `[FEATURE]`, or `[BUG-FIX]`
- ✅ Brief one-line description
- ✅ List changed files with concise summaries

**Pre-Commit Checklist:**
- ✅ Code compiles with zero errors
- ✅ Zero build warnings
- ✅ All consumers updated (if ripple effect)
- ✅ CHANGELOG.md updated

**Post-Commit Actions:**
```bash
git log -1
git push origin master
```
git push origin master
```

---

### STEP 12: UPDATE REMEMBER MCP (2 minutes)

**Store completion and progress in workspace memory**

**Action Items:**
1. Use the remember MCP tool to document:
   - What was accomplished in this task
   - Key files created or modified
   - Any new patterns or conventions established
   - Timestamp of completion

2. Example memory entry:
```
[DATE TIME] - [TASK-NAME]: Completed successfully
- Files modified: [list key files]
- Commit: [commit hash]
- Pattern established: [if applicable]
- Next steps: [recommended follow-up tasks]
- Time elapsed: [X minutes]
```

3. Update the workspace memory file:
   - Location: `.github/instructions/memory.instructions.md`
   - Add new entry under "Memories/Facts" section
   - Include chronological timestamp
   - Keep entries concise but informative

**Memory Entry Requirements:**
- ✅ Include task name or scope identifier
- ✅ List key files created/modified
- ✅ Include commit hash reference
- ✅ Document any patterns established
- ✅ Note for future reference
- ✅ Include timestamp in format: YYYY-MM-DD HH:MM

**Verification:**
- ✅ Memory entry added to `.github/instructions/memory.instructions.md`
- ✅ Entry includes commit hash
- ✅ Timestamp is current
- ✅ Task summary is clear and actionable
- ✅ Memory file is added to git (if changed)

**Post-Memory Update:**
If memory file was modified:
```bash
git add .github/instructions/memory.instructions.md
git commit -m "[MEMORY-UPDATE] Document [TASK-NAME] completion

- Commit: [hash]
- Task: [description]"

git push origin master
```

---

## LESSONS FROM PRODUCTION DEPLOYMENTS

### Error Statistics:
- **Total errors observed:** 29 (post-generation)
- **Category 1 (Type Mismatches):** 4 errors - ALL from not reading definitions
- **Category 2 (Unresolved References):** 12 errors - ALL from guessing names/scope
- **Category 3 (Nullable Safety):** 7 errors - ALL from inconsistent null handling
- **Category 4 (Method Signatures):** 6 errors - ALL from not validating APIs

### Prevention Rate:
**100% of these errors were preventable** by following the rules in this document.

### Time Cost (Cautionary Tale):
- Code generation (with guessing): ~2 hours
- Error fixing (systematic protocol): ~3 hours
- **Total:** 5 hours

**With these rules applied BEFORE generation:** ~2 hours (60% time savings)

---

## ENFORCEMENT PROTOCOL

### Self-Check Before Generation:
```
Before I generate ANY code, I must answer YES to ALL:

TODO LIST & SCOPE:
0. [ ] Have I created a todo list and marked task as in-progress?
1. [ ] Have I defined the scope clearly?
2. [ ] Have I identified ripple effects?

NAMING COMPLIANCE:
3. [ ] Have I checked all existing code for naming scheme compliance?
4. [ ] Does my task plan maintain naming scheme consistency?
5. [ ] Will my generated code follow ALL naming conventions?

MAPPING & READING:
6. [ ] Have I READ the complete source files for ALL dependencies?
7. [ ] Have I DOCUMENTED all properties/methods with exact types?
8. [ ] Have I VERIFIED all signatures I'll call?
9. [ ] Am I generating code with EXACT names (no assumptions)?

RIPPLE EFFECT ANALYSIS:
10. [ ] Does this change replace/modify existing code?
11. [ ] If YES: Have I searched for ALL consumers?
12. [ ] If YES: Have I documented ALL required consumer updates?
13. [ ] If YES: Do I have time to update ALL consumers NOW?

SCOPE-SPECIFIC (Compose UI):
14. [ ] Have I CHECKED scope rules for remember{} states?
15. [ ] Have I VALIDATED all Compose APIs I'll use?

DATA BINDING & REAL DATA:
16. [ ] Will ALL UI data come from real sources (StateFlow, API, database, sensors)?
17. [ ] Will I use ZERO hardcoded mock values or fake data?
18. [ ] Are loading/error states defined for when data isn't available?
```

**If ANY answer is NO → STOP and complete that step first.**
### Self-Check After Generation:
```
Before declaring task COMPLETE, answer YES to ALL:

NAMING:
□ All naming follows established conventions
□ No naming violations in new code
□ Consumer updates follow naming schemes

BUILD & VALIDATION:
□ Code compiles (BUILD SUCCESSFUL)
□ All names/types validated against documentation
□ Type conversions explicit and documented

CONSUMERS:
□ All consumers updated (if ripple effect)
□ Code still compiles with consumers
□ Affected features tested

GIT:
□ Changes committed with proper message
□ Changes pushed to remote
□ Workspace memory updated

COMPLETION:
□ No broken code left behind
□ No outstanding TODOs related to this change
18. [ ] Can I honestly say this task is 100% COMPLETE?
```

**If ANY answer is NO → Task is INCOMPLETE, keep working.**

---

## THE COMMITMENT

**Every code generation task, every time:**

```
I will MAP before I CODE.
I will READ before I WRITE.
I will VALIDATE before I COMMIT.
I will UPDATE CONSUMERS when I change PROVIDERS.
I will be SYSTEMATIC, not ad-hoc.
I will be PRECISE, not approximate.
I will be COMPLETE, not partial.
I will be EFFICIENT through discipline, not speed through shortcuts.
```

**Success looks like:**
- ✅ Complete mapping in 10 minutes (includes consumer analysis)
- ✅ Generation completes in 30 minutes
- ✅ Consumer updates complete in 15 minutes
- ✅ Self-validation finds all issues in 10 minutes
- ✅ **BUILD SUCCESSFUL on first compile (with ALL consumers working)**
- ✅ Total time: 65 minutes for COMPLETE working code (provider + consumers)

**Failure looks like:**
- ❌ No mapping, start coding immediately
- ❌ Generation takes 2 hours (guessing, rewriting)
- ❌ Skip consumer analysis
- ❌ Provider works, consumers broken
- ❌ Skip self-validation
- ❌ **20 compilation errors**
- ❌ 3 hours fixing errors
- ❌ Task declared "done" but consumers still broken
- ❌ Total time: 5+ hours for INCOMPLETE code

**The math is simple:** Systematic + Complete is faster than Fast + Broken.

---

## ENFORCEMENT

**This is not optional. This is how code is generated. Period.**

Before starting ANY code generation:
```
PLANNING:
□ Do I have a written map of what I'm building?
□ Have I read ALL dependency definitions?
□ Have I documented ALL names/types/signatures I'll use?
□ Can I generate this code WITHOUT guessing anything?

RIPPLE EFFECT:
□ Does this change affect existing code?
□ If YES: Have I identified ALL consumers?
□ If YES: Have I planned ALL consumer updates?
□ If YES: Do I have time for provider + consumer updates NOW?
```

**If ANY answer is NO → STOP. Complete that step.**

After generating provider code:
```
PROVIDER VALIDATION:
□ Have I reviewed every property name against documentation?
□ Have I reviewed every method call against documentation?
□ Have I verified all type conversions are applied?
□ Am I confident this compiles first try?

CONSUMER VALIDATION (if ripple effect):
□ Have I updated ALL identified consumers?
□ Have I verified consumers still compile?
□ Have I tested affected features?
□ Have I removed old code if fully replaced?
```

**If ANY answer is NO → FIX IT NOW before declaring complete.**

---

## CONTINUOUS IMPROVEMENT

**Every compilation error is a process failure.**

When errors occur:
1. Document the error type
2. Identify which step failed (Map? Read? Generate? Validate?)
3. Update process to catch this error type earlier
4. Apply improved process to next task

**Goal:** Reduce error rate to zero over time through systematic improvement.

---

**Status:** Production Standard  
**Applies To:** ALL code generation (UI, APIs, models, tests, migrations, refactoring)  
**Violations:** Cause compilation errors, waste time, reduce code quality  
**Compliance:** Mandatory for all production code

**Remember:** Fast code that doesn't compile wastes more time than slow code that works first try.

**Last Updated:** 2025-11-05
