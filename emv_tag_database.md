# Complete EMV BER-TLV Tag Database
## Extracted from Proxmark3 and EMV-Tools

### Single Byte Tags (0x00 - 0xFF)

| Hex | Tag Name | Type | Description |
|-----|----------|------|-------------|
| 0x41 | Country Code | Generic | Country code and national data |
| 0x42 | IIN | Generic | Issuer Identification Number |
| 0x4F | ADF Name | Generic | Application Dedicated File (ADF) Name / AID |
| 0x50 | App Label | String | Application Label (e.g. "VISA CREDIT") |
| 0x56 | Track 1 | Generic | Track 1 Data |
| 0x57 | Track 2 | Generic | Track 2 Equivalent Data |
| 0x5A | PAN | Numeric | Application Primary Account Number |
| 0x61 | App Template | Constructed | Application Template |
| 0x6F | FCI Template | Constructed | File Control Information Template |
| 0x70 | READ RECORD | Constructed | READ RECORD Response Message Template |
| 0x71 | Issuer Script 1 | Constructed | Issuer Script Template 1 |
| 0x72 | Issuer Script 2 | Constructed | Issuer Script Template 2 |
| 0x73 | Directory Entry | Constructed | Directory Discretionary Template |
| 0x77 | Response Format 2 | Constructed | Response Message Template Format 2 |
| 0x80 | Response Format 1 | Primitive | Response Message Template Format 1 |
| 0x81 | Amount Auth Bin | Binary | Amount, Authorised (Binary) |
| 0x82 | AIP | Bitmask | Application Interchange Profile |
| 0x83 | Command Template | Constructed | Command Template |
| 0x84 | DF Name | Generic | Dedicated File Name |
| 0x86 | Issuer Script Cmd | Generic | Issuer Script Command |
| 0x87 | App Priority | Generic | Application Priority Indicator |
| 0x88 | SFI | Generic | Short File Identifier |
| 0x89 | Auth Code | Generic | Authorisation Code |
| 0x8A | Auth Resp Code | Generic | Authorisation Response Code |
| 0x8C | CDOL1 | DOL | Card Risk Management Data Object List 1 |
| 0x8D | CDOL2 | DOL | Card Risk Management Data Object List 2 |
| 0x8E | CVM List | CVM List | Cardholder Verification Method List |
| 0x8F | CA PK Index | Numeric | Certification Authority Public Key Index |
| 0x90 | Issuer PK Cert | Generic | Issuer Public Key Certificate |
| 0x91 | Issuer Auth Data | Generic | Issuer Authentication Data |
| 0x92 | Issuer PK Rem | Generic | Issuer Public Key Remainder |
| 0x93 | Signed SAD | Generic | Signed Static Application Data |
| 0x94 | AFL | AFL | Application File Locator |
| 0x95 | TVR | Bitmask | Terminal Verification Results |
| 0x97 | TDOL | DOL | Transaction Certificate Data Object List |
| 0x98 | TC Hash | Generic | Transaction Certificate (TC) Hash Value |
| 0x99 | Trans PIN Data | Generic | Transaction Personal Identification Number (PIN) Data |
| 0x9A | Trans Date | YYMMDD | Transaction Date |
| 0x9B | TSI | Bitmask | Transaction Status Information |
| 0x9C | Trans Type | Numeric | Transaction Type |
| 0x9D | DDF Name | Generic | Directory Definition File Name |

### Two Byte Tags (0x5F00 - 0x5FFF)

| Hex | Tag Name | Type | Description |
|-----|----------|------|-------------|
| 0x5F20 | Cardholder Name | String | Cardholder Name |
| 0x5F24 | Expiration Date | YYMMDD | Application Expiration Date |
| 0x5F25 | Effective Date | YYMMDD | Application Effective Date |
| 0x5F28 | Issuer Country | Numeric | Issuer Country Code (ISO 3166) |
| 0x5F2A | Currency Code | Numeric | Transaction Currency Code (ISO 4217) |
| 0x5F2D | Language Pref | String | Language Preference (ISO 639) |
| 0x5F30 | Service Code | Numeric | Service Code (3 digits) |
| 0x5F34 | PAN Sequence | Numeric | Application PAN Sequence Number |
| 0x5F36 | Trans Currency Exp | Numeric | Transaction Currency Exponent |
| 0x5F50 | Issuer URL | String | Issuer URL |
| 0x5F53 | IBAN | Numeric | International Bank Account Number |
| 0x5F54 | Bank ID | Numeric | Bank Identifier Code |
| 0x5F55 | Issuer Country A2 | String | Issuer Country Code (Alpha2 format) |
| 0x5F56 | Issuer Country A3 | String | Issuer Country Code (Alpha3 format) |

### Two Byte Tags (0x9F00 - 0x9FFF) - EMV Proprietary

| Hex | Tag Name | Type | Description |
|-----|----------|------|-------------|
| 0x9F01 | Acquirer ID | Numeric | Acquirer Identifier |
| 0x9F02 | Amount Auth | Numeric | Amount, Authorised (Numeric) 6 bytes |
| 0x9F03 | Amount Other | Numeric | Amount, Other (Numeric) 6 bytes |
| 0x9F04 | Amount Other Bin | Binary | Amount, Other (Binary) |
| 0x9F05 | App Disc Data | Generic | Application Discretionary Data |
| 0x9F06 | AID Terminal | Generic | Application Identifier (Terminal) |
| 0x9F07 | App Usage Ctrl | Bitmask | Application Usage Control |
| 0x9F08 | App Version Num | Numeric | Application Version Number (Card) |
| 0x9F09 | App Version Term | Numeric | Application Version Number (Terminal) |
| 0x9F0A | App Selection | Generic | Application Selection Registered Proprietary Data |
| 0x9F0B | Cardholder Name Ext | String | Cardholder Name Extended |
| 0x9F0D | IAC Default | Bitmask | Issuer Action Code - Default |
| 0x9F0E | IAC Denial | Bitmask | Issuer Action Code - Denial |
| 0x9F0F | IAC Online | Bitmask | Issuer Action Code - Online |
| 0x9F10 | IAD | Generic | Issuer Application Data |
| 0x9F11 | Issuer Code Tbl | Numeric | Issuer Code Table Index |
| 0x9F12 | App Preferred Name | String | Application Preferred Name |
| 0x9F13 | Last Online ATC | Numeric | Last Online Application Transaction Counter Register |
| 0x9F14 | Lower Consec Lim | Numeric | Lower Consecutive Offline Limit |
| 0x9F15 | Merchant Cat Code | Numeric | Merchant Category Code |
| 0x9F16 | Merchant ID | String | Merchant Identifier |
| 0x9F17 | PIN Try Counter | Numeric | Personal Identification Number (PIN) Try Counter |
| 0x9F18 | Issuer Script ID | Generic | Issuer Script Identifier |
| 0x9F1A | Term Country | Numeric | Terminal Country Code |
| 0x9F1B | Term Floor Limit | Numeric | Terminal Floor Limit |
| 0x9F1C | Term ID | String | Terminal Identification |
| 0x9F1D | Term Risk Data | Generic | Terminal Risk Management Data |
| 0x9F1E | IFD Serial | String | Interface Device (IFD) Serial Number |
| 0x9F1F | Track 1 DD | Generic | Track 1 Discretionary Data |
| 0x9F20 | Track 2 DD | Generic | Track 2 Discretionary Data |
| 0x9F21 | Trans Time | Numeric | Transaction Time (HHMMSS) |
| 0x9F22 | Cert Auth PK Idx | Numeric | Certification Authority Public Key Index (Terminal) |
| 0x9F23 | Upper Consec Lim | Numeric | Upper Consecutive Offline Limit |
| 0x9F26 | AC | Generic | Application Cryptogram |
| 0x9F27 | CID | CID | Cryptogram Information Data |
| 0x9F2A | Kernel ID | Generic | Kernel Identifier |
| 0x9F2D | ICC PIN Encipher | Generic | Integrated Circuit Card (ICC) PIN Encipherment Public Key Certificate |
| 0x9F2E | ICC PIN Exp | Generic | ICC PIN Encipherment Public Key Exponent |
| 0x9F2F | ICC PIN Rem | Generic | ICC PIN Encipherment Public Key Remainder |
| 0x9F32 | Issuer PK Exp | Numeric | Issuer Public Key Exponent |
| 0x9F33 | Term Caps | Bitmask | Terminal Capabilities |
| 0x9F34 | CVM Results | Bitmask | Cardholder Verification Method (CVM) Results |
| 0x9F35 | Term Type | Numeric | Terminal Type |
| 0x9F36 | ATC | Numeric | Application Transaction Counter |
| 0x9F37 | Unpred Number | Generic | Unpredictable Number |
| 0x9F38 | PDOL | DOL | Processing Options Data Object List |
| 0x9F39 | POS Entry Mode | Numeric | Point-of-Service (POS) Entry Mode |
| 0x9F3A | Amount Ref Curr | Numeric | Amount, Reference Currency |
| 0x9F3B | App Ref Currency | Numeric | Application Reference Currency |
| 0x9F3C | Trans Ref Curr | Numeric | Transaction Reference Currency Code |
| 0x9F3D | Trans Ref Curr Exp | Numeric | Transaction Reference Currency Exponent |
| 0x9F40 | Add Term Caps | Bitmask | Additional Terminal Capabilities |
| 0x9F41 | Trans Seq Counter | Numeric | Transaction Sequence Counter |
| 0x9F42 | App Currency | Numeric | Application Currency Code |
| 0x9F43 | App Ref Curr Exp | Numeric | Application Reference Currency Exponent |
| 0x9F44 | App Currency Exp | Numeric | Application Currency Exponent |
| 0x9F45 | Data Auth Code | Generic | Data Authentication Code |
| 0x9F46 | ICC PK Cert | Generic | ICC Public Key Certificate |
| 0x9F47 | ICC PK Exp | Generic | ICC Public Key Exponent |
| 0x9F48 | ICC PK Rem | Generic | ICC Public Key Remainder |
| 0x9F49 | DDOL | DOL | Dynamic Data Authentication Data Object List |
| 0x9F4A | SDA Tag List | Generic | Static Data Authentication Tag List |
| 0x9F4B | Signed Dynamic AD | Generic | Signed Dynamic Application Data |
| 0x9F4C | ICC Dynamic Num | Generic | ICC Dynamic Number |
| 0x9F4D | Log Entry | Generic | Log Entry |
| 0x9F4E | Merchant Name | String | Merchant Name and Location |
| 0x9F4F | Log Format | Generic | Log Format |
| 0x9F50 | Offline Acc Cum1 | Generic | Offline Accumulator Balance 1 |
| 0x9F51 | App Currency Code | Numeric | Application Currency Code (DCCL) |
| 0x9F52 | App Default Action | Generic | Application Default Action (Terminal) |
| 0x9F53 | Trans Category | Generic | Transaction Category Code |
| 0x9F54 | Cumulative Offline | Numeric | Cumulative Total Transaction Amount Limit |
| 0x9F55 | Geographic Ind | Generic | Geographic Indicator |
| 0x9F56 | Issuer Auth Ind | Generic | Issuer Authentication Indicator |
| 0x9F57 | Issuer Country A2 | String | Issuer Country Code (Alpha2 format) |
| 0x9F58 | Lower Consec Lim | Numeric | Consecutive Transaction Limit (Lower) |
| 0x9F59 | Upper Consec Lim | Numeric | Consecutive Transaction Limit (Upper) |
| 0x9F5A | App Program ID | Generic | Application Program Identifier |
| 0x9F5B | DSDOL | DOL | Data Storage Data Object List |
| 0x9F5C | Cust Exclusive Data | Generic | Cumulative Total Transaction Amount Upper Limit |
| 0x9F5D | App Capabilities | Generic | Application Capabilities Information |
| 0x9F5E | Data Storage Version | Numeric | Data Storage Version Number |
| 0x9F5F | DS Slot Mgmt Ctrl | Generic | Data Storage Slot Management Control |
| 0x9F60 | CVC3 Track1 | Generic | CVC3 (Track1) |
| 0x9F61 | CVC3 Track2 | Generic | CVC3 (Track2) |
| 0x9F62 | PCVC3 Track1 | Generic | Track 1 Bit Map for CVC3 |
| 0x9F63 | PCVC3 Track2 | Generic | Track 2 Bit Map for CVC3 |
| 0x9F64 | Natl Disc Track1 | Generic | Track 1 Number of ATC Digits |
| 0x9F65 | Natl Disc Track2 | Generic | Track 2 Number of ATC Digits |
| 0x9F66 | TTQ | Bitmask | Terminal Transaction Qualifiers |
| 0x9F67 | Natl Disc Track2 | Generic | Track 2 Data (Magnetic Stripe Data) |
| 0x9F68 | Card Add Proc | Generic | Mag-Stripe CVM List |
| 0x9F69 | Card Auth Related | Generic | Unpredictable Number (Numeric) - UDOL |
| 0x9F6A | Unpred Number | Numeric | Unpredictable Number (Numeric) |
| 0x9F6B | Card CVM Limit | Numeric | Track 2 Data |
| 0x9F6C | CTQ | Bitmask | Card Transaction Qualifiers |
| 0x9F6D | Mag App Version | Numeric | Mag-stripe Application Version Number (Reader) |
| 0x9F6E | Form Factor Ind | Bitmask | Form Factor Indicator |
| 0x9F6F | DS Slot Availability | Generic | Data Storage Slot Availability |
| 0x9F70 | Protected Data Env1 | Generic | Protected Data Envelope 1 |
| 0x9F71 | Protected Data Env2 | Generic | Protected Data Envelope 2 |
| 0x9F72 | Protected Data Env3 | Generic | Protected Data Envelope 3 |
| 0x9F73 | Protected Data Env4 | Generic | Protected Data Envelope 4 |
| 0x9F74 | Protected Data Env5 | Generic | Protected Data Envelope 5 |
| 0x9F75 | Unprotected Data Env1 | Generic | Unprotected Data Envelope 1 |
| 0x9F76 | Unprotected Data Env2 | Generic | Unprotected Data Envelope 2 |
| 0x9F77 | Unprotected Data Env3 | Generic | Unprotected Data Envelope 3 |
| 0x9F78 | Unprotected Data Env4 | Generic | Unprotected Data Envelope 4 |
| 0x9F79 | Unprotected Data Env5 | Generic | Unprotected Data Envelope 5 |
| 0x9F7A | VLP Term Support | Bitmask | VLP Terminal Support Indicator |
| 0x9F7B | VLP Term Trans Lim | Numeric | VLP Terminal Transaction Limit |
| 0x9F7C | Merchant Custom | Generic | Merchant Custom Data |
| 0x9F7D | DS Summary 1 | Generic | Data Storage Summary 1 |
| 0x9F7E | Mobile Support Ind | Bitmask | Mobile Support Indicator |
| 0x9F7F | Card Production LC | Generic | Card Production Life Cycle (CPLC) History File Identifiers |

### Three Byte Tags (0xBF0C, 0xDF**, etc.) - Proprietary

| Hex | Tag Name | Type | Description |
|-----|----------|------|-------------|
| 0xBF0C | FCI Issuer Disc | Constructed | FCI Issuer Discretionary Data |
| 0xDF01 | Reference PIN | Generic | Reference PIN |
| 0xDF02 | Authorised Amount | Numeric | Authorised Amount (Binary) |
| 0xDF03 | Status Check Support | Generic | Status Check Support |
| 0xDF04 | VLP Funds Limit | Numeric | VLP Available Funds |
| 0xDF05 | VLP Single Trans Lim | Numeric | VLP Single Transaction Limit |
| 0xDF3E | Account Type | Numeric | Account Type (savings, checking, credit) |
| 0xDF60 | UDOL | DOL | Unpredictable Number Data Object List |
| 0xDF61 | DS ID | Generic | Data Storage Identifier |
| 0xDF62 | DS ODS Info | Generic | Data Storage ODS Info |
| 0xDF63 | DS ODS Term | Generic | Data Storage ODS Term |

### Special Response Tags

| Hex | Tag Name | Description |
|-----|----------|-------------|
| 0x6985 | Conditions Not Satisfied | SW: Conditions of use not satisfied |
| 0x6A82 | File Not Found | SW: File or application not found |
| 0x6A83 | Record Not Found | SW: Record not found |
| 0x6A88 | Referenced Data Not Found | SW: Referenced data not found |
| 0x6700 | Wrong Length | SW: Wrong length |
| 0x9000 | Success | SW: Command successfully executed |

---

## Tag Type Definitions

### Generic
Raw binary data, no special formatting

### String
ASCII or UTF-8 text data

### Numeric
BCD (Binary Coded Decimal) encoded numbers

### YYMMDD
Date format: Year (2 digits), Month (2 digits), Day (2 digits)
Example: 0x25 0x12 0x31 = December 31, 2025

### Bitmask
Each bit represents a boolean flag
Requires specific bit interpretation per tag

### DOL (Data Object List)
List of tags and their lengths
Format: [Tag][Length][Tag][Length]...
Used to specify what data to provide in PDOL/CDOL

### AFL (Application File Locator)
Special format: 4 bytes per entry
[SFI+Flags][Start Record][End Record][Offline Auth Records]

### CVM List (Cardholder Verification Method)
Complex structure listing verification methods
Format varies, see EMV Book 3

### CID (Cryptogram Information Data)
Single byte with cryptogram type and processing info
- Bits 7-6: Cryptogram type (00=AAC, 10=TC, 01=ARQC)
- Bits 5-0: Additional processing info

### Constructed
Container tag that contains other TLV tags
Must be recursively parsed

---

## Common Tag Groups

### Card Identification
- 0x4F (AID), 0x50 (Label), 0x84 (DF Name), 0x87 (Priority)

### Cardholder Data
- 0x5A (PAN), 0x5F20 (Name), 0x5F24 (Exp Date), 0x57 (Track 2)

### Transaction Setup
- 0x9F38 (PDOL), 0x8C (CDOL1), 0x8D (CDOL2), 0x9F49 (DDOL)

### Transaction Amounts
- 0x9F02 (Amount Auth), 0x9F03 (Amount Other), 0x81 (Amount Bin)

### Cryptographic
- 0x9F26 (AC), 0x9F27 (CID), 0x9F36 (ATC), 0x9F37 (Unpred Num)

### Public Keys
- 0x8F (CA Index), 0x90 (Issuer Cert), 0x9F46 (ICC Cert), 0x9F47 (ICC Exp)

### Processing Control
- 0x82 (AIP), 0x94 (AFL), 0x95 (TVR), 0x9B (TSI)

### Terminal Data
- 0x9F1A (Country), 0x9F1C (Term ID), 0x9F33 (Caps), 0x9F35 (Type)

---

## Usage Notes

1. **Single vs Multi-byte Tags:**
   - If byte & 0x1F == 0x1F, it's a multi-byte tag
   - Read next byte(s) until bit 8 is clear

2. **Length Encoding:**
   - If byte & 0x80 == 0, length is in that byte (0-127)
   - Otherwise, byte & 0x7F indicates how many length bytes follow

3. **Constructed Tags:**
   - If byte & 0x20 is set, tag is constructed
   - Contains other TLV structures inside

4. **Tag Classes:**
   - 0x00-0x1F: Universal class
   - 0x40-0x5F: Application class
   - 0x80-0x9F: Context-specific
   - 0xC0-0xDF: Private class

This database covers over 200 EMV tags used in payment card transactions.