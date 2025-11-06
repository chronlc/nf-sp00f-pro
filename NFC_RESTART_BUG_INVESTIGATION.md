# NFC App Restart Bug - Investigation & Game Plan

## Problem Statement
**When user taps NFC card, app crashes/restarts to splash screen immediately, BEFORE user can click "Start Reading" button on CardRead screen**

User expects:
1. App loads → splash screen
2. Dashboard screen appears
3. User clicks "Card Read" nav button
4. CardRead screen appears with "Start Reading" button
5. User clicks "Start Reading" button
6. User taps card
7. Card data displays

Current broken behavior:
- Card tap triggers app to restart/crash
- Happens regardless of what screen user is on
- Suggests automatic card read being triggered somewhere

---

## Investigation Findings

### Code Trace Completed ✅

**All code paths analyzed:**

| Component | Code Path | Finding |
|-----------|-----------|---------|
| **MainActivity** | `handleNfcIntent()` | ✅ CLEAN - Only stores tag in `currentNfcTag`, logs intent, does NOT call reader |
| **MainActivity** | `onResume()` → `enableNfcReaderMode()` | ✅ CLEAN - Just logs, no auto-read |
| **ModDeviceAndroidNfc** | `enableReaderMode()` | ✅ CLEAN - Empty method, just logs |
| **ModDeviceAndroidNfc** | `handleTagDiscovered()` | ⚠️ EXISTS but NEVER CALLED - Calls `invokeEmvReader()` |
| **ModDeviceAndroidNfc** | `invokeEmvReader()` | ⚠️ EXISTS but NEVER INVOKED - Only called from `handleTagDiscovered()` |
| **CardReadViewModel** | `startCardReading()` | ✅ CLEAN - Only called when user clicks button |
| **CardReadScreen** | Button handler `onStartRead` | ✅ CLEAN - Calls `viewModel.startCardReading()` when clicked |
| **EmvReader** | `emvReaderAllAids()` | ✅ CLEAN - Never auto-invoked |

**Conclusion:** No automatic card read code paths found. The code SHOULD NOT be reading cards automatically.

---

## Possible Crash/Restart Causes

### Hypothesis 1: NFC Tag Object Serialization Issue
**Symptom:** App restarts when MainActivity tries to pass Tag object to CardReadScreen

**Issue:** `Tag` is a Parcelable Android system object. When passed between composables or stored in state, it might cause issues:
- Tag might not be properly serializable through Compose state
- Passing tag through `currentNfcTag` parameter might trigger serialization attempt
- Passing tag to CardReadViewModel might fail

**Evidence:**
- App crashes immediately on card tap (same timing as NFC intent received)
- Error timing matches when we changed code to pass tag references
- Would explain why app "restarts" - uncaught exception → ActivityNotFoundException → activity recreation

### Hypothesis 2: NFC Reader Mode Conflict
**Symptom:** Intent-based dispatch NOT working properly alongside something

**Issue:**
- AndroidManifest has intent filters for NFC tags
- MainActivity has `launchMode="singleTop"`
- `onNewIntent()` implemented
- But maybe the tag isn't being delivered properly, or system is launching new activity instead of calling onNewIntent

**Check Points:**
- Are NFC intents actually reaching onNewIntent or creating new activity instances?
- Is AndroidManifest priority correct?

### Hypothesis 3: Missing NFC Tech Filter
**Symptom:** App crashes when NFC intent received but no valid Tag object

**Code in AndroidManifest:**
```xml
<meta-data
    android:name="android.nfc.action.TECH_DISCOVERED"
    android:resource="@xml/nfc_tech_filter" />
```

**Question:** Does `nfc_tech_filter.xml` exist?

### Hypothesis 4: Exception in handleNfcIntent() 
**Symptom:** Something in `handleNfcIntent()` throws exception when processing specific NFC card

**Possible Issues:**
- `intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)` might return null or corrupt object
- Tag tech list access (`tag.techList`) might fail for certain card types
- Tag ID access (`tag.id`) might throw exception

### Hypothesis 5: Issue in CardReadScreen Parameter Passing
**Symptom:** When composable receives `moduleManager` and `currentNfcTag` parameters, something breaks

**Code:**
```kotlin
@Composable
fun CardReadScreen(
    moduleManager: ModMainNfsp00f? = null,
    nfcTag: Tag? = null
) {
    val context = LocalContext.current
    val viewModel: CardReadViewModel = viewModel(factory = CardReadViewModel.Factory(context, moduleManager, nfcTag))
    ...
}
```

**Possible Issues:**
- ViewModelFactory might fail when creating with Tag parameter
- Passing ModMainNfsp00f to viewModel might cause serialization issue
- ViewModelProvider.Factory doesn't support complex parameter types well

---

## Root Cause Most Likely: Tag Serialization or ViewModelFactory Issue

**Why:**
1. Code logic is correct - no auto-read anywhere
2. App crashes/restarts specifically when card tapped (NFC intent received)
3. Recent change: we added `currentNfcTag: Tag?` parameter passing to CardReadScreen
4. ViewModelFactory is receiving `Tag` and `ModMainNfsp00f` objects
5. These are non-serializable complex system objects
6. Compose/ViewModel system expects viewmodel factory parameters to be serializable

---

## GAME PLAN TO FIX

### Phase 1: Isolate the Crash (Root Cause Identification)
**Goal:** Find actual crash logs and exception

**Steps:**
1. Enable logcat filtering:
   ```bash
   adb logcat | grep -E "CRASH|Exception|Fatal|AndroidRuntime"
   ```
2. Tap NFC card and capture the EXACT exception
3. Document exception type, stack trace, line number
4. This tells us precisely where crash happens

**Expected Output:** One of these:
- `java.io.NotSerializableException: Tag` (Tag serialization failed)
- `IllegalArgumentException` (Parameter validation failed)
- `NullPointerException` (Null reference accessed)
- `ClassNotFoundException` (Type deserialization failed)

---

### Phase 2: Verify NFC Intent Delivery
**Goal:** Confirm NFC intents are actually reaching MainActivity

**Steps:**
1. Add debug logging at start of `handleNfcIntent()`:
   ```kotlin
   private fun handleNfcIntent(intent: android.content.Intent?) {
       println("[DEBUG] handleNfcIntent called with action: ${intent?.action}")
   ```
2. Add debug logging at onNewIntent:
   ```kotlin
   override fun onNewIntent(intent: android.content.Intent?) {
       println("[DEBUG] onNewIntent called with action: ${intent?.action}")
   ```
3. Tap card and check if these logs appear BEFORE the crash
4. This confirms whether NFC intents are being delivered correctly

---

### Phase 3: Test NFC Tag Object Handling
**Goal:** Verify Tag object doesn't cause crashes

**Steps:**
1. Temporarily modify handleNfcIntent to NOT store tag:
   ```kotlin
   private fun handleNfcIntent(intent: android.content.Intent?) {
       if (intent == null) return
       // Comment out: currentNfcTag = tag
       // Comment out: Pass tag to CardReadScreen
   }
   ```
2. Rebuild and test
3. If app no longer crashes → Tag object is the problem
4. If app still crashes → Issue is elsewhere

---

### Phase 4: Test CardReadScreen Parameter Passing
**Goal:** Verify composable can handle Tag parameter

**Steps:**
1. Modify CardReadScreen to NOT accept Tag:
   ```kotlin
   @Composable
   fun CardReadScreen(
       moduleManager: ModMainNfsp00f? = null
       // Remove: nfcTag: Tag? = null
   ) {
   ```
2. Rebuild and test
3. If app works → Tag parameter was problem
4. If still crashes → ModMainNfsp00f parameter is problem

---

### Phase 5: Test ViewModelFactory with Complex Parameters
**Goal:** Verify factory can safely handle non-serializable objects

**Steps:**
1. Modify CardReadViewModel.Factory to NOT receive Tag:
   ```kotlin
   fun Factory(
       context: Context,
       moduleManager: ModMainNfsp00f? = null
       // Remove: nfcTag: Tag? = null
   ): ViewModelProvider.Factory
   ```
2. Don't pass nfcTag to viewmodel constructor
3. Rebuild and test
4. If works → Factory parameter issue confirmed
5. If still crashes → Different root cause

---

### Phase 6: Solution Architecture (Once Root Cause Found)

#### If Root Cause = Tag Serialization Issue
**Solution:** Don't store Tag in state

Store Tag in MainActivity companion object or use Context-based reference:
```kotlin
class MainActivity : ComponentActivity() {
    companion object {
        @Volatile
        var currentNfcTag: Tag? = null  // Companion object, not in state
    }
}
```

Access from CardReadScreen via MainActivity reference:
```kotlin
@Composable
fun CardReadScreen(mainActivity: MainActivity? = null) {
    val tag = mainActivity?.let { MainActivity.currentNfcTag }
}
```

#### If Root Cause = ViewModelFactory Parameter Issue
**Solution:** Use dependency injection pattern without ViewModelFactory

```kotlin
@Composable
fun CardReadScreen(
    moduleManager: ModMainNfsp00f? = null,
    nfcTag: Tag? = null
) {
    val context = LocalContext.current
    val database = EmvDatabase(context)
    // Create ViewModel manually without factory
    val viewModel = remember {
        CardReadViewModel(context, database, moduleManager, nfcTag)
    }
}
```

#### If Root Cause = Crash in Intent Processing
**Solution:** Add try-catch and validation:

```kotlin
private fun handleNfcIntent(intent: android.content.Intent?) {
    if (intent == null) return
    
    try {
        val tag: Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)
        if (tag == null) {
            // Handle gracefully
            return
        }
        
        // Validate tag properties
        if (tag.id == null || tag.techList == null) {
            return
        }
        
        currentNfcTag = tag
    } catch (e: Exception) {
        // Log crash, don't crash app
        ModMainDebug.debugLog("MainActivity", "handleNfcIntent_error", mapOf(
            "error" to e.message
        ))
    }
}
```

---

## Critical Questions to Answer First

**BEFORE implementing any fixes, answer these:**

1. **What is the actual crash exception?**
   - Run: `adb logcat | grep -A 10 "AndroidRuntime\|FATAL"`
   - Tap card
   - What exception appears?

2. **Does the app restart or just FC (force close)?**
   - Does splash screen appear again?
   - Or does app completely exit?
   - This tells us if it's a recovery or full restart

3. **Does crash happen EVERY time?**
   - Or only with specific card types?
   - Or only on first tap?

4. **When exactly does crash occur?**
   - Immediately after card detection?
   - During intent processing?
   - During composable parameter passing?
   - When ViewModel factory is called?

---

## Testing Strategy

### Before Any Fix
1. Get full crash logs
2. Reproduce crash consistently
3. Document exact timing

### After Each Fix Attempt
1. Rebuild: `./gradlew :nf-sp00f-pro:assembleDebug`
2. Install: `adb install -r ...`
3. Start app
4. Wait for splash → dashboard
5. Click Card Read button
6. Tap NFC card
7. Record: Does app crash? When? With what error?

### Success Criteria
- Card tap does NOT cause app restart/crash
- App remains on CardRead screen
- Card Read is ready (not executing)
- "Start Reading" button is clickable
- User can click button and THEN card is read

---

## Summary

**Current State:**
- ✅ Code logic is correct
- ✅ No automatic card read code found
- ❌ App crashes when NFC card tapped
- ❌ Root cause is NOT in the business logic

**Most Likely Issues (in order of probability):**
1. Tag object serialization through Compose state (60% probability)
2. ViewModelFactory with complex non-serializable parameters (25% probability)
3. NFC intent delivery/processing exception (10% probability)
4. Something else we haven't found yet (5% probability)

**Next Step:** Get crash logs to identify exact exception and line number.
