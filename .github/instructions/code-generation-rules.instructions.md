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

**Anti-Pattern:**
```kotlin
// Typing from memory, guessing
val items = viewModel.items.collectAsState()  // Wrong! It's "products" not "items"
Text(product.cost)                            // Wrong! It's "price" not "cost", and needs .toString()
```

**Correct Pattern:**
```kotlin
// Referencing documentation from Step 4
val products by viewModel.products.collectAsState()  // ✅ Exact name from Step 4
Text("${product.price}")                             // ✅ Exact name + explicit conversion from Step 5
```

---

### STEP 7: SELF-VALIDATION (5-10 minutes)

**Review before compile. Catch errors in review, not compilation.**

For generated code:
1. Compare every property name with Step 4 documentation
2. Compare every method call with Step 4 documentation
3. Verify every type conversion from Step 5 is applied
4. Check for any guessed names (if you didn't document it, you guessed it)

**Validation Checklist:**
```
□ Every property access verified against documentation
□ Every method call verified against documentation
□ Every type conversion applied as documented
□ No names used that weren't in documentation
□ No assumptions about nullability
□ No assumptions about types
```

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
□ Update documentation
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
git commit -m "[SCOPE-N] Feature/Fix: Brief description

Files Changed:
- src/main/java/.../filename.kt (change summary)
- src/main/java/.../anotherfile.kt (change summary)

Build Status: ✅ Success
Test Status: ✅ All tests passing
Warnings: 0

Details:
- Root cause: [if bug fix, explain the root cause]
- Impact: [explain what this change does]
- Breaking changes: [none, or list them]"
```

4. Verify commit:
```bash
git log -1 --stat
```

**Commit Message Requirements:**
- ✅ Include scope identifier: `[SCOPE-N]` or `[FEATURE]` or `[BUG-FIX]`
- ✅ Brief one-line description of change
- ✅ List ALL files modified with summaries
- ✅ Include Build Status: ✅ Success
- ✅ Include Test Status: ✅ All passing (or specify failures resolved)
- ✅ Include warning count (should be 0)
- ✅ Reference root cause if bug fix
- ✅ Document any breaking changes

**Pre-Commit Checklist:**
- ✅ Code compiles with zero errors
- ✅ Zero build warnings
- ✅ All tests passing
- ✅ All consumers updated (if ripple effect)
- ✅ CHANGELOG.md updated
- ✅ Code review complete
- ✅ Root cause documented (if bug fix)

**Post-Commit Actions (Mandatory):**
```bash
# View commit details
git log -1

# Push to remote
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
```

**If ANY answer is NO → STOP and complete that step first.**

### Self-Check After Generation:
```
Before I declare task COMPLETE, I must answer YES to ALL:

TODO LIST:
1. [ ] Have I marked all intermediate tasks as completed?
2. [ ] Will I mark the final task as completed before finishing?

NAMING COMPLIANCE:
3. [ ] Does generated code follow ALL naming conventions?
4. [ ] Are there ANY naming violations in new code?
5. [ ] Do all consumer updates follow naming schemes?

PROVIDER VERIFICATION:
6. [ ] Does the generated code compile (BUILD SUCCESSFUL)?
7. [ ] Have I validated all names/types against documentation?
8. [ ] Are all type conversions explicit?

CONSUMER VERIFICATION:
9. [ ] If ripple effect identified: Have I updated ALL consumers?
10. [ ] If ripple effect identified: Does code still compile with consumers?
11. [ ] If ripple effect identified: Have I tested affected features?
12. [ ] If ripple effect identified: Have I removed old code (if applicable)?

COMPLETION:
13. [ ] Is there ANY broken code left behind?
14. [ ] Is there ANY TODO related to this change?
15. [ ] Can I honestly say this task is 100% COMPLETE?
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
