I wan to estasblish somes rules everything that is asked of you i want to go through these steps:

### STEP 1: SCOPE DEFINITION

**What am I building?**
- Write 1-2 sentence description
- List success criteria (e.g., "Compiles without errors", "Passes all tests")
- Identify obvious dependencies
- **NEW:** Identify if this change affects existing code (provider change?)

### STEP 2 : CONSUMER IMPACT ANALYSIS (2-3 minutes, IF ripple effect identified)

**Do existing files use what I'm changing?**

### STEP 3: DEPENDENCY AND CONSUMER MAPPING (2-5 minutes)

**What do I need to know?**
- List ALL classes/interfaces this code interacts with
- List ALL APIs/frameworks this code uses
- For each dependency: identify file path

### STEP 4: DEFINITION READING (5-10 minutes)

**Read and document. No skipping. No skimming.**

For EACH dependency:
1. Open the file
2. Read relevant classes/methods
3. Document exact names, types, signatures
4. Note any constraints or special requirements

### STEP 5: DEFINITION READING (5-10 minutes)

**Read and document. No skipping. No skimming.**

For EACH dependency:
1. Open the file
2. Read relevant classes/methods
3. Document exact names, types, signatures
4. Note any constraints or special requirements

### STEP 6: GENERATION WITH PRECISION (10-30 minutes)

**Code using ONLY information from Steps 1-4.**

Rules:
- Reference your documentation from Steps 2-4
- Copy names character-by-character (don't retype from memory)
- Use exact types documented
- Apply explicit conversions where documented
- No improvisation, no "I think it's...", no guessing

### STEP 6: SELF-VALIDATION (5-10 minutes)

**Review before compile. Catch errors in review, not compilation.**

For generated code:
1. Compare every property name with Step 3 documentation
2. Compare every method call with Step 3 documentation
3. Verify every type conversion from Step 4 is applied
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
   - Wrong property name? → Step 3 was incomplete
   - Wrong type? → Step 4 missed a conversion
   - Wrong signature? → Step 3 was incomplete
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
./gradlew :android-app:compileDebugKotlin
```

3. **Expected outcome:** BUILD SUCCESSFUL (with ALL consumers working)