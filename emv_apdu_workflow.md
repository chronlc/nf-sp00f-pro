# Complete EMV Scan APDU Workflow Breakdown

## Overview
The EMV scan command performs a complete card read by executing a series of APDU commands following the EMV specification. The workflow adapts based on card brand (Visa, Mastercard, etc.) and supported transaction modes.

---

## Transaction Modes & Brand Detection

### Transaction Types (Based on Proxmark3)
1. **MSD (Magnetic Stripe Data)** - Default, simplest mode
2. **VSDC** (Visa Smart Debit/Credit) - Visa-specific
3. **qVSDC/M/Chip** (quick VSDC/Mastercard Chip) - Fast contactless
4. **CDA** (Combined DDA) - With cryptographic authentication

### Brand-Specific AIDs
The scanner identifies card brands through Application Identifiers (AIDs):

**Visa Cards:**
- `A0000000031010` - Visa Debit/Credit (Classic)
- `A000000003101001` - Visa Credit
- `A000000003101002` - Visa Debit
- `A0000000032010` - Visa Electron
- `A0000000033010` - Visa Interlink

**Mastercard:**
- `A0000000041010` - Mastercard
- `A00000000410101213` - Maestro
- `A0000000043060` - Maestro UK

**American Express:**
- `A000000025` - AmEx base AID
- `A0000000250000` - AmEx Credit

**Other brands have their own AIDs (Discover, JCB, UnionPay, etc.)**

---

## Complete APDU Workflow

### Phase 1: Card Detection & ATR
```
Command: Power on card / Field activation
Response: ATR (Answer To Reset) - for contact
          RATS (Request for Answer To Select) - for contactless
```

**ATR provides:**
- Protocol type (T=0 or T=1)
- Card capabilities
- Historical bytes

---

### Phase 2: Application Selection

#### Step 2A: Select PPSE/PSE (Payment System Environment)

**For Contactless (PPSE):**
```
Command APDU: 00 A4 04 00 0E 32 50 41 59 2E 53 59 53 2E 44 44 46 30 31 00
              CLA INS P1 P2 Lc [2PAY.SYS.DDF01]                        Le

Breakdown:
- CLA: 00 (Standard class)
- INS: A4 (SELECT command)
- P1:  04 (Select by name/DF name)
- P2:  00 (First or only occurrence)
- Lc:  0E (14 bytes of data)
- Data: "2PAY.SYS.DDF01" (PPSE directory name)
- Le:  00 (Expect up to 256 bytes response)

Expected Response: 6F XX 84 ... A5 ... 90 00
                   FCI Template with directory information
```

**For Contact (PSE):**
```
Command APDU: 00 A4 04 00 0E 31 50 41 59 2E 53 59 53 2E 44 44 46 30 31 00
                                 [1PAY.SYS.DDF01]
```

**Response contains:**
- Tag `6F` - FCI Template
- Tag `84` - DF Name (echoes back PSE/PPSE)
- Tag `A5` - FCI Proprietary Template
  - Tag `BF0C` - FCI Issuer Discretionary Data
    - Tag `61` - Application Template (one or more)
      - Tag `4F` - AID (Application Identifier)
      - Tag `50` - Application Label (e.g., "VISA DEBIT")
      - Tag `87` - Application Priority Indicator
      - Tag `9F2A` - Kernel Identifier (for contactless)
- Tag `88` - SFI (Short File Identifier) for directory

**If PPSE/PSE fails (6A82 - File Not Found):**
- Fall back to explicit AID selection (brute force known AIDs)

---

#### Step 2B: Read PSE Directory (if SFI present)

```
Command APDU: 00 B2 [REC] [SFI] 00
              CLA INS P1    P2    Le

Example: 00 B2 01 0C 00
- CLA: 00
- INS: B2 (READ RECORD)
- P1:  01 (Record number, starts at 1)
- P2:  0C (SFI=1, bits 7-3: 00001, bit 2-1: 10 for SFI, bit 0: 0)
       Formula: (SFI << 3) | 0x04
- Le:  00 (Read up to 256 bytes)

Response: 70 XX 61 ... (Application Template entries)
```

**Loop through records:**
- Increment record number (01, 02, 03...)
- Continue until `6A83` (Record Not Found)
- Parse `Tag 4F` (AID) from each record

---

#### Step 2C: Application Priority Selection

From all discovered AIDs:
1. Check Application Priority Indicator (Tag `87`)
   - Lower value = higher priority
   - Bits 0-3 contain priority (1-15)
2. Match against terminal-supported AIDs
3. User confirmation (if multiple high-priority apps)
4. Automatic selection (if only one candidate)

---

#### Step 2D: Select Application (Final AID)

```
Command APDU: 00 A4 04 00 [Lc] [AID] 00

Example (Visa): 00 A4 04 00 07 A0 00 00 00 03 10 10 00
- CLA: 00
- INS: A4 (SELECT)
- P1:  04 (Select by name)
- P2:  00 (First occurrence)
- Lc:  07 (7 bytes)
- Data: A0 00 00 00 03 10 10 (Visa AID)
- Le:  00

Response: 6F XX ... 90 00 (FCI with application data)
```

**Response Tags:**
- Tag `50` - Application Label
- Tag `9F38` - PDOL (Processing Options Data Object List)
- Tag `5F2D` - Language Preference
- Tag `9F11` - Issuer Code Table Index
- Tag `9F12` - Application Preferred Name
- Tag `BF0C` - FCI Issuer Discretionary Data
  - Tag `9F4D` - Log Entry
  - Tag `9F6E` - Form Factor Indicator

---

### Phase 3: Initiate Application Processing

#### Step 3A: Analyze PDOL (Processing Options Data Object List)

**PDOL Structure (Tag 9F38):**
```
Example: 9F 66 04 9F 02 06 9F 37 04 9F 1A 02
Means:
- Tag 9F66, Length 04 (Terminal Transaction Qualifiers)
- Tag 9F02, Length 06 (Amount Authorized)
- Tag 9F37, Length 04 (Unpredictable Number)
- Tag 9F1A, Length 02 (Terminal Country Code)
```

**If no PDOL (empty or absent):**
- Send GPO with minimal data: `83 00` (empty PDOL data)

**If PDOL present:**
- Construct PDOL data with terminal values
- Wrap in Tag `83` (Command Template)

---

#### Step 3B: GET PROCESSING OPTIONS (GPO)

**Without PDOL:**
```
Command APDU: 80 A8 00 00 02 83 00 00

- CLA: 80 (Proprietary class)
- INS: A8 (GET PROCESSING OPTIONS)
- P1:  00
- P2:  00
- Lc:  02 (2 bytes of data)
- Data: 83 00 (Empty command template)
- Le:  00

Response Format 1 (Primitive): 80 XX [AIP][AFL] 90 00
Response Format 2 (TLV): 77 XX 82 02 [AIP] 94 XX [AFL] ... 90 00
```

**With PDOL:**
```
Command APDU: 80 A8 00 00 [Lc] 83 [PDOL_Len] [PDOL_Data] 00

Example with PDOL:
80 A8 00 00 23 83 21 [21 bytes of PDOL data] 00

PDOL Data construction:
- 9F6604: 36000000 (TTQ - Terminal Transaction Qualifiers)
- 9F0206: 000000000100 (Amount = 1.00)
- 9F3704: 12345678 (Unpredictable Number/nonce)
- 9F1A02: 0840 (Country Code = US)
```

**CRITICAL: Le Byte for Contactless**
- Contactless cards often REQUIRE Le byte
- Missing Le causes `6700` (Wrong Length) error
- Always append `00` as Le for contactless

---

#### Step 3C: Parse GPO Response

**Response Format 1 (Primitive) - Tag 80:**
```
80 0E 3C 00 08 01 01 00 10 01 02 01 18 01 01 00
   │  └──┬──┘ └──────────┬──────────────────┘
   │   AIP(2)          AFL(variable)
   Length
```

**Response Format 2 (TLV) - Tag 77:**
```
77 XX 
   82 02 [AIP]           (Application Interchange Profile)
   94 [Len] [AFL]        (Application File Locator)
   [Other optional tags]
90 00
```

**Key Tags:**
- **Tag 82/94 or 80** - AIP (Application Interchange Profile)
  - Bit 7: SDA supported
  - Bit 6: DDA supported
  - Bit 5: Cardholder verification supported
  - Bit 4: Terminal risk management required
  - Bit 3: Issuer authentication supported
  - Bit 2-1: CDA supported

- **Tag 94 or within 80** - AFL (Application File Locator)
  - 4 bytes per entry: `[SFI][First Rec][Last Rec][Offline Auth Records]`
  - Example: `10 01 03 01` means:
    - SFI: 2 (10 >> 3)
    - Records 1-3
    - 1 record used for offline authentication

- **Tag 57** - Track 2 Equivalent Data (if present in GPO)
- **Tag 9F10** - Issuer Application Data
- **Tag 9F26** - Application Cryptogram (if CDA in GPO)
- **Tag 9F36** - Application Transaction Counter (ATC)

---

### Phase 4: Read Application Records

#### Step 4A: Parse AFL and Generate Read Commands

**AFL Format:** 4-byte entries
```
AFL: 08 01 01 00 10 01 05 03 18 01 02 00

Entry 1: 08 01 01 00
- Byte 1: 08 → SFI = 1 (08 >> 3 = 1)
- Byte 2: 01 → Start record = 1
- Byte 3: 01 → End record = 1
- Byte 4: 00 → Records for offline auth = 0

Entry 2: 10 01 05 03
- SFI = 2 (10 >> 3)
- Records 1 to 5
- 3 records for offline auth

Entry 3: 18 01 02 00
- SFI = 3 (18 >> 3)
- Records 1 to 2
- 0 records for offline auth
```

---

#### Step 4B: Execute READ RECORD Commands

```
Command APDU: 00 B2 [Record] [SFI] 00

Formula for P2: (SFI << 3) | 0x04

Example commands for AFL above:
1. 00 B2 01 0C 00  (SFI=1, Record 1)
2. 00 B2 01 14 00  (SFI=2, Record 1)
3. 00 B2 02 14 00  (SFI=2, Record 2)
4. 00 B2 03 14 00  (SFI=2, Record 3)
5. 00 B2 04 14 00  (SFI=2, Record 4)
6. 00 B2 05 14 00  (SFI=2, Record 5)
7. 00 B2 01 1C 00  (SFI=3, Record 1)
8. 00 B2 02 1C 00  (SFI=3, Record 2)

Response: 70 XX [BER-TLV data] 90 00
```

**Continue reading until:**
- All AFL entries exhausted, OR
- `6A83` (Record Not Found), OR
- `6A82` (File Not Found)

---

#### Step 4C: Extract Data from Records

**Common Tags Found:**
- **Tag 5A** - Application PAN (Primary Account Number)
- **Tag 5F20** - Cardholder Name
- **Tag 5F24** - Application Expiration Date (YYMMDD)
- **Tag 5F25** - Application Effective Date
- **Tag 5F28** - Issuer Country Code
- **Tag 5F30** - Service Code
- **Tag 5F34** - Application PAN Sequence Number
- **Tag 57** - Track 2 Equivalent Data
  - Format: `PAN D YYMM ServiceCode DiscretionaryData FFFF`
  - D = separator (often 'D' in hex)
  - Contains CVV in some positions
- **Tag 8C** - CDOL1 (Card Risk Management DOL)
- **Tag 8D** - CDOL2 (Card Risk Management DOL)
- **Tag 8E** - CVM List (Cardholder Verification Method)
- **Tag 8F** - CA Public Key Index
- **Tag 90** - Issuer Public Key Certificate
- **Tag 92** - Issuer Public Key Remainder
- **Tag 9F07** - Application Usage Control
- **Tag 9F08** - Application Version Number (Card)
- **Tag 9F0D** - Issuer Action Code - Default
- **Tag 9F0E** - Issuer Action Code - Denial
- **Tag 9F0F** - Issuer Action Code - Online
- **Tag 9F32** - Issuer Public Key Exponent
- **Tag 9F42** - Application Currency Code
- **Tag 9F44** - Application Currency Exponent
- **Tag 9F46** - ICC Public Key Certificate
- **Tag 9F47** - ICC Public Key Exponent
- **Tag 9F48** - ICC Public Key Remainder
- **Tag 9F49** - DDOL (Dynamic DOL)
- **Tag 9F4A** - SDA Tag List
- **Tag 9F62** - PCVC3 Track1
- **Tag 9F63** - PCVC3 Track2
- **Tag 9F6C** - Card Transaction Qualifiers (CTQ)

---

### Phase 5: Brand-Specific Mode Selection

The scanner decides which mode to use based on:

#### Detection Logic:

1. **Check AIP (from GPO response):**
   ```
   if (AIP & 0x40) → DDA supported → Can use CDA
   if (AIP & 0x80) → SDA supported → Can use VSDC
   if (AIP & 0x01) → CDA supported → Prefer CDA
   ```

2. **Check Card Brand (from AID):**
   ```
   if (AID starts with A00000000310) → Visa
       Check for VSDC/qVSDC support
       Check Tag 9F6C (CTQ) for quick path
   
   if (AID starts with A00000000410) → Mastercard
       Check for M/Chip mode
       Check kernel identifier (Tag 9F2A)
   
   if (AID starts with A000000025) → AmEx
       Usually MSD mode for contactless
   ```

3. **Check Transaction Type Flags:**
   ```
   Tag 9F6C (CTQ - Card Transaction Qualifiers):
   - Bit settings indicate online-only, offline-only, or combined
   
   Tag 9F66 (TTQ - Terminal Transaction Qualifiers):
   - Terminal's supported features
   ```

4. **Command Line Flags (Proxmark3 specific):**
   ```
   Default: MSD mode
   -v: Force qVSDC/M/Chip
   -c: Force qVSDC+CDA
   -x: Force VSDC
   -g: VISA Generate AC from GPO
   ```

#### Mode Characteristics:

**MSD Mode (Magnetic Stripe Data):**
- Simplest, fastest
- No cryptographic authentication
- Reads Track 2 equivalent data
- No GENERATE AC needed
- Used for legacy compatibility

**VSDC Mode (Visa Smart Debit/Credit):**
- Full EMV transaction
- Offline authentication (SDA/DDA)
- Terminal risk management
- GENERATE AC required
- More secure than MSD

**qVSDC/M/Chip Mode (Quick VSDC/Mastercard):**
- Optimized for contactless
- Reduced transaction time
- May combine steps (AC in GPO)
- Balance between speed and security
- Kernel-specific implementations

**CDA Mode (Combined DDA):**
- Highest security
- Dynamic authentication in every message
- Application Cryptogram with dynamic signature
- Requires DDA-capable card and terminal
- Prevents cloning and relay attacks

---

### Phase 6: Cryptographic Operations (Advanced Modes)

#### Step 6A: GENERATE AC (Application Cryptogram)

Only needed for VSDC/qVSDC/CDA modes, not MSD.

```
Command APDU: 80 AE [RefCtrl] 00 [Lc] [CDOL_Data] 00

RefCtrl values:
- 40: Request AAC (Application Authentication Cryptogram - Decline)
- 80: Request TC (Transaction Certificate - Approve offline)
- C0: Request ARQC (Auth Request Cryptogram - Go online)

Example: 80 AE 80 00 [Lc] [CDOL1 data] 00
```

**CDOL1 Construction:**
- Similar to PDOL, found in Tag 8C
- Contains transaction details, random numbers, dates
- Exact format varies by card

**Response:**
```
77 XX
   9F26 08 [AC]         (Application Cryptogram - 8 bytes)
   9F27 01 [CID]        (Cryptogram Information Data)
   9F36 02 [ATC]        (Application Transaction Counter)
   9F10 XX [IAD]        (Issuer Application Data)
   [Other tags]
90 00
```

---

#### Step 6B: INTERNAL AUTHENTICATE (for DDA/CDA)

```
Command APDU: 00 88 00 00 [Lc] [DDOL_Data] 00

DDOL from Tag 9F49 in card records
Response: Dynamic signature over challenge data
```

---

#### Step 6C: Offline PIN Verification (if required)

```
GET CHALLENGE: 00 84 00 00 08
Response: 8-byte random number

VERIFY PIN (Plaintext): 00 20 00 80 08 [2][Len][PIN][Padding]
VERIFY PIN (Enciphered): 00 20 00 88 08 [Encrypted PIN block]
```

---

### Phase 7: Data Extraction & Storage

#### Save to JSON (Proxmark3 'scan' command):

```json
{
  "PSE/PPSE": {
    "aids": ["A0000000031010"],
    "labels": ["VISA DEBIT"]
  },
  "AID": "A0000000031010",
  "ApplicationLabel": "VISA DEBIT",
  "AIP": "3C00",
  "AFL": "080101001001050318010200",
  "Records": {
    "01-01": "70xx5A...",
    "02-01": "70xx57...",
    "02-02": "70xx8F...",
    ...
  },
  "PAN": "1234567890123456",
  "CardholderName": "CARDHOLDER NAME",
  "ExpirationDate": "2512",
  "Track2": "1234567890123456D2512101...",
  "PublicKeys": [...],
  "Logs": [...]
}
```

---

## Brand-Specific Workflows

### Visa Cards

**Typical Flow:**
1. Select PPSE → Find Visa AID
2. Select AID (A0000000031010)
3. Check PDOL (usually present)
4. GPO with PDOL data
5. Parse AFL (typically 2-3 SFIs)
6. Read records (SFI 1-3)
7. Extract Track 2 (Tag 57)

**Visa-Specific Tags:**
- `9F7F` - Card Production Life Cycle
- `9F63` - PCVC3 Track2
- `9F66` - Terminal Transaction Qualifiers (TTQ)

**Mode Selection:**
- Check `9F6C` (CTQ) for qVSDC support
- If bit 7 set → Online-only PIN
- Use `-g` flag for "AC from GPO" (VISA specific optimization)

---

### Mastercard

**Typical Flow:**
1. Select PPSE → Find MC AID
2. Select AID (A0000000041010)
3. Check kernel ID (Tag `9F2A`)
4. GPO with PDOL
5. M/Chip specific processing
6. Read records (usually SFI 1-2)

**Mastercard-Specific Tags:**
- `9F6D` - Mag-stripe Application Version Number (Reader)
- `9F6E` - Form Factor Indicator
- `9F53` - Transaction Category Code

**Mode Selection:**
- Kernel 2 (M/Chip) vs Kernel 3 (M/Chip Advance)
- Check `9F2A` to determine kernel
- M/Chip often combines GPO + first GENERATE AC

---

### American Express

**Typical Flow:**
1. Select PPSE → Find AmEx AID
2. Select AID (A000000025)
3. Usually simpler structure
4. GPO (often minimal PDOL)
5. Read records (fewer than Visa/MC)

**AmEx-Specific:**
- Often defaults to MSD for contactless
- Fewer records to read
- Simpler authentication flow
- Different tag usage for discretionary data

---

## Error Handling & Status Words

### Common Status Words:

- `9000` - Success
- `6283` - Selected file deactivated
- `6700` - Wrong length (missing Le)
- `6882` - Secure messaging not supported
- `6982` - Security status not satisfied
- `6983` - Authentication method blocked
- `6984` - Referenced data invalidated
- `6985` - Conditions of use not satisfied
- `6986` - Command not allowed
- `6A80` - Incorrect parameters in data field
- `6A81` - Function not supported
- `6A82` - File not found
- `6A83` - Record not found
- `6A84` - Not enough memory
- `6A86` - Incorrect P1-P2
- `6A88` - Referenced data not found
- `6D00` - Instruction not supported
- `6E00` - Class not supported
- `6F00` - Unknown error

### Status Word Handling:

```
if (SW == 0x9000):
    Process response normally
    
elif (SW == 0x6100):
    More data available
    Send GET RESPONSE: 00 C0 00 00 XX
    
elif (SW & 0xFF00 == 0x6100):
    More data available
    XX = SW & 0xFF
    Send GET RESPONSE: 00 C0 00 00 XX
    
elif (SW == 0x6A82 or SW == 0x6A83):
    File/Record not found - stop reading
    
elif (SW == 0x6700):
    Add Le byte and retry
    
else:
    Error - log and continue or abort
```

---

## Complete Scan Pseudo-Code

```python
def emv_scan(contactless=True):
    # Phase 1: Card detection
    atr = power_on_card()
    
    # Phase 2: Application selection
    if contactless:
        ppse_response = send_apdu("00A404000E325041592E5359532E444446303100")
    else:
        pse_response = send_apdu("00A404000E315041592E5359532E444446303100")
    
    if ppse_response.sw == 0x9000:
        sfi = parse_tag(ppse_response, "88")
        aids = []
        for rec in range(1, 17):
            rec_data = read_record(sfi, rec)
            if rec_data.sw == 0x6A83:
                break
            aids.append(parse_tag(rec_data, "4F"))
        selected_aid = select_by_priority(aids)
    else:
        # Fallback: Try known AIDs
        selected_aid = try_aid_list(KNOWN_AIDS)
    
    # Phase 3: Select application
    select_resp = select_application(selected_aid)
    pdol = parse_tag(select_resp, "9F38")
    
    # Phase 4: GPO
    if pdol:
        pdol_data = construct_pdol_data(pdol)
        gpo_cmd = "80A80000" + len(pdol_data) + "83" + pdol_data + "00"
    else:
        gpo_cmd = "80A8000002830000"
    
    if contactless and not gpo_cmd.endswith("00"):
        gpo_cmd += "00"  # Add Le
    
    gpo_response = send_apdu(gpo_cmd)
    aip, afl = parse_gpo_response(gpo_response)
    
    # Phase 5: Read records
    records = {}
    afl_entries = parse_afl(afl)
    
    for entry in afl_entries:
        sfi = entry.sfi
        for rec in range(entry.start_rec, entry.end_rec + 1):
            p2 = (sfi << 3) | 0x04
            cmd = f"00B2{rec:02X}{p2:02X}00"
            rec_data = send_apdu(cmd)
            if rec_data.sw == 0x9000:
                records[f"{sfi:02X}-{rec:02X}"] = rec_data.data
    
    # Phase 6: Extract key data
    pan = find_tag_in_records(records, "5A")
    track2 = find_tag_in_records(records, "57")
    name = find_tag_in_records(records, "5F20")
    exp_date = find_tag_in_records(records, "5F24")
    
    # Phase 7: Mode-specific operations
    brand = detect_brand(selected_aid)
    mode = determine_mode(aip, brand, flags)
    
    if mode in ["VSDC", "qVSDC", "CDA"]:
        # Generate AC
        cdol1 = find_tag_in_records(records, "8C")
        ac_data = construct_cdol_data(cdol1)
        ac_cmd = f"80AE8000{len(ac_data):02X}{ac_data}00"
        ac_response = send_apdu(ac_cmd)
        
    # Return all collected data
    return {
        "AID": selected_aid,
        "Brand": brand,
        "Mode": mode,
        "AIP": aip,
        "AFL": afl,
        "Records": records,
        "PAN": pan,
        "Track2": track2,
        "CardholderName": name,
        "ExpirationDate": exp_date
    }
```

---

## Key Differences: Contact vs Contactless

| Aspect | Contact (Chip) | Contactless (NFC) |
|--------|----------------|-------------------|
| PSE Name | 1PAY.SYS.DDF01 | 2PAY.SYS.DDF01 |
| Channel | ISO 7816 T=0/T=1 | ISO 14443 Type A |
| Le Byte | Often optional | Usually required |
| Speed | Slower, more data | Faster, less data |
| Max Response | 256+ bytes | Often limited |
| Timeout | Longer | Shorter (prefer quick) |
| Authentication | Full DDA/CDA | Often qVSDC (quick) |
| Default Mode | VSDC/Full EMV | MSD or qVSDC |

---

## Summary

The complete EMV scan workflow:
1. **Detect** card and get ATR/RATS
2. **Select** PPSE/PSE directory
3. **Read** directory to find AIDs
4. **Select** application by priority
5. **Execute** GPO to get AIP and AFL
6. **Read** all records specified in AFL
7. **Extract** PAN, name, dates, track data
8. **Determine** brand and transaction mode
9. **Perform** mode-specific cryptographic operations
10. **Save** all data to JSON/output

Mode selection is based on:
- AID (determines brand)
- AIP bits (determines card capabilities)
- Brand-specific tags (CTQ, TTQ, Kernel ID)
- Terminal capabilities
- Command-line flags (in Proxmark3)

Implement to intelligently adapt to different card types and automatically selects the optimal reading strategy while providing manual override options for testing and research purposes.
