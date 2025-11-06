package com.nfsp00fpro.app.modules

import java.nio.ByteBuffer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * EMV BER-TLV Parser Module
 *
 * Parses raw binary EMV (Europay, Mastercard, Visa) tag data following
 * BER-TLV (Basic Encoding Rules - Tag-Length-Value) specification.
 *
 * Features:
 * - Single-byte, two-byte, and multi-byte tag parsing
 * - Proper length encoding (single vs. multi-byte)
 * - Constructed tag recursive parsing
 * - Full byte-level operations (no conversions)
 * - Support for 200+ EMV tag definitions
 * - Async database persistence (batch save after parse, non-blocking)
 *
 * Specification Reference:
 * - EMV Book 1 (Tag definitions)
 * - ISO/IEC 8825-1 (BER encoding rules)
 */
class EmvParser {

    // Coroutine scope for async database saves
    private val parserScope = CoroutineScope(Dispatchers.IO)

    /**
     * Parse raw EMV binary data into structured tag map
     *
     * Parameters:
     * - data: ByteArray - Raw binary EMV data
     * - database: EmvDatabase? - Database for async tag persistence (optional)
     * - sessionId: String? - Session ID for database (required if database provided)
     * - aidId: Long - AID ID for database (0 if not applicable)
     *
     * Returns: Map<String, EmvTag> - Map of tag hex strings to parsed tag objects
     *
     * Process:
     * 1. Parse tag type (single vs multi-byte)
     * 2. Parse length value
     * 3. Extract tag data
     * 4. Recursively parse constructed tags
     * 5. Batch save all parsed tags to database (async, non-blocking)
     * 6. Return structured map
     */
    fun emvParser(
        data: ByteArray,
        database: EmvDatabase? = null,
        sessionId: String? = null,
        aidId: Long = 0L
    ): Map<String, EmvTag> {
        val tagMap = mutableMapOf<String, EmvTag>()
        val allTags = mutableListOf<TlvTag>()
        var offset = 0

        ModMainDebug.debugLog("EmvParser", "parsing_start", mapOf(
            "data_length" to data.size,
            "session_id" to (sessionId ?: "unknown") as Any
        ))

        while (offset < data.size) {
            try {
                val tagParseResult = parseTagAt(data, offset)
                if (tagParseResult == null) {
                    logStatus("⚠ Failed to parse tag at offset $offset")
                    ModMainDebug.debugLog("EmvParser", "parsing_warning", mapOf(
                        "reason" to "failed_to_parse_tag",
                        "offset" to offset,
                        "session_id" to (sessionId ?: "unknown") as Any
                    ))
                    break
                }

                val (tagHex, tagObject, nextOffset) = tagParseResult
                tagMap[tagHex] = tagObject
                
                // Collect for database (if database provided)
                if (database != null && sessionId != null) {
                    collectTlvTags(tagObject, tagHex, sessionId, aidId, allTags, depth = 0)
                }
                
                offset = nextOffset
            } catch (e: Exception) {
                logStatus("✗ Parse error at offset $offset: ${e.message}")
                ModMainDebug.debugLog("EmvParser", "parsing_error", mapOf(
                    "reason" to "parse_exception",
                    "offset" to offset,
                    "error" to (e.message ?: "Unknown error") as Any,
                    "session_id" to (sessionId ?: "unknown") as Any
                ))
                break
            }
        }

        // Batch save all collected tags to database (async, non-blocking)
        if (database != null && sessionId != null && allTags.isNotEmpty()) {
            parserScope.launch {
                database.saveTlvTagBatch(allTags)
                logStatus("✓ Batch saved ${allTags.size} TLV tags to database")
                ModMainDebug.debugLog("EmvParser", "parsing_complete", mapOf(
                    "tags_parsed" to tagMap.size,
                    "tlv_tags_saved" to allTags.size,
                    "session_id" to (sessionId ?: "unknown") as Any
                ))
            }
        }

        return tagMap
    }
    
    /**
     * Collect TLV tags recursively for database persistence
     */
    private fun collectTlvTags(
        tag: EmvTag,
        tagHex: String,
        sessionId: String,
        aidId: Long,
        allTags: MutableList<TlvTag>,
        depth: Int
    ) {
        // Add this tag
        allTags.add(TlvTag(
            sessionId = sessionId,
            aidId = aidId,
            tagHex = tagHex,
            tagBytes = tag.tagBytes,
            valueBytes = if (tag.value is EmvTagValue.Primitive) {
                (tag.value as EmvTagValue.Primitive).data
            } else {
                byteArrayOf()
            },
            isConstructed = tag.isConstructed,
            depth = depth
        ))
        
        // Recursively add nested tags
        if (tag.isConstructed && tag.value is EmvTagValue.Constructed) {
            val nestedTags = (tag.value as EmvTagValue.Constructed).tags
            for ((nestedHex, nestedTag) in nestedTags) {
                collectTlvTags(nestedTag, nestedHex, sessionId, aidId, allTags, depth + 1)
            }
        }
    }

    /**
     * Parse single tag at given offset
     *
     * Returns: Triple of (tagHex, EmvTag, nextOffset) or null if error
     */
    private fun parseTagAt(data: ByteArray, offset: Int): Triple<String, EmvTag, Int>? {
        if (offset >= data.size) {
            return null
        }

        // Parse tag bytes
        val tagParseResult = parseTagBytes(data, offset)
        if (tagParseResult == null) {
            return null
        }

        val (tagBytes, tagHex, tagLength, isConstructed, nextOffset) = tagParseResult

        // Parse length value
        val lengthParseResult = parseLengthAt(data, nextOffset)
        if (lengthParseResult == null) {
            return null
        }

        val (lengthValue, lengthByteCount, dataStartOffset) = lengthParseResult

        // Check bounds
        val dataEndOffset = dataStartOffset + lengthValue
        if (dataEndOffset > data.size) {
            logStatus("✗ Tag $tagHex: Data exceeds bounds (requires $dataEndOffset, have ${data.size})")
            return null
        }

        // Extract tag data
        val tagData = data.copyOfRange(dataStartOffset, dataEndOffset)

        // Parse tag value
        val tagValue = if (isConstructed) {
            // Recursively parse nested TLV structure
            val nestedTags = emvParser(tagData)
            EmvTagValue.Constructed(nestedTags)
        } else {
            // Store raw bytes
            EmvTagValue.Primitive(tagData)
        }

        // Create EmvTag object
        val emvTag = EmvTag(
            tagHex = tagHex,
            tagBytes = tagBytes,
            tagLength = tagLength,
            isConstructed = isConstructed,
            length = lengthValue,
            value = tagValue
        )

        return Triple(tagHex, emvTag, dataEndOffset)
    }

    /**
     * Parse tag bytes (1-3 bytes possible)
     *
     * Returns: Tuple of (tagBytes, tagHex, tagLength, isConstructed, nextOffset) or null
     *
     * Tag structure:
     * - Bits 7-6: Class (00=Universal, 01=Application, 10=Context, 11=Private)
     * - Bit 5: Constructed (1) or Primitive (0)
     * - Bits 4-0: Tag number (or 31 if multi-byte)
     */
    private fun parseTagBytes(data: ByteArray, offset: Int): Tuple5<ByteArray, String, Int, Boolean, Int>? {
        if (offset >= data.size) {
            return null
        }

        val byte1 = data[offset].toInt() and 0xFF
        val isConstructed = (byte1 and 0x20) != 0

        // Check if single-byte or multi-byte tag
        if ((byte1 and 0x1F) != 0x1F) {
            // Single-byte tag
            val tagHex = String.format("%02X", byte1)
            val tagBytes = byteArrayOf(data[offset])
            return Tuple5(tagBytes, tagHex, 1, isConstructed, offset + 1)
        }

        // Multi-byte tag (read until bit 8 is clear)
        var currentOffset = offset + 1
        val tagBytesList = mutableListOf<Byte>(data[offset])

        while (currentOffset < data.size) {
            val nextByte = data[currentOffset].toInt() and 0xFF
            tagBytesList.add(data[currentOffset])
            currentOffset++

            // If bit 8 is clear, this is the last tag byte
            if ((nextByte and 0x80) == 0) {
                break
            }

            // Safety limit: tags shouldn't be more than 3 bytes
            if (tagBytesList.size > 3) {
                logStatus("✗ Tag exceeds 3 bytes at offset $offset")
                return null
            }
        }

        val tagBytes = tagBytesList.toByteArray()
        val tagHex = tagBytes.joinToString("") { b -> String.format("%02X", b.toInt() and 0xFF) }

        return Tuple5(tagBytes, tagHex, tagBytes.size, isConstructed, currentOffset)
    }

    /**
     * Parse length value at given offset
     *
     * Returns: Tuple of (lengthValue, lengthByteCount, dataStartOffset) or null
     *
     * Length encoding:
     * - If bit 7 clear: length is in this byte (0-127)
     * - If bit 7 set: bits 6-0 indicate number of following length bytes
     */
    private fun parseLengthAt(data: ByteArray, offset: Int): Tuple3<Int, Int, Int>? {
        if (offset >= data.size) {
            return null
        }

        val lengthByte = data[offset].toInt() and 0xFF

        // Single-byte length (0-127)
        if ((lengthByte and 0x80) == 0) {
            return Tuple3(lengthByte, 1, offset + 1)
        }

        // Multi-byte length
        val numLengthBytes = lengthByte and 0x7F

        // Safety checks
        if (numLengthBytes == 0) {
            logStatus("✗ Indefinite length not supported at offset $offset")
            return null
        }

        if (numLengthBytes > 4) {
            logStatus("✗ Length byte count exceeds 4 at offset $offset")
            return null
        }

        if (offset + 1 + numLengthBytes > data.size) {
            logStatus("✗ Not enough bytes for length at offset $offset")
            return null
        }

        // Read multi-byte length as big-endian
        var lengthValue = 0
        for (i in 0 until numLengthBytes) {
            val b = data[offset + 1 + i].toInt() and 0xFF
            lengthValue = (lengthValue shl 8) or b
        }

        return Tuple3(lengthValue, 1 + numLengthBytes, offset + 1 + numLengthBytes)
    }

    /**
     * Get tag name from hex string
     *
     * Uses EMV tag database to return human-readable names
     */
    private fun getTagName(tagHex: String): String {
        return emvTagDatabase[tagHex] ?: "Unknown Tag"
    }

    /**
     * Get tag type from hex string (Generic, String, Numeric, etc.)
     */
    private fun getTagType(tagHex: String): String {
        return emvTagTypeDatabase[tagHex] ?: "Generic"
    }

    /**
     * Internal logging
     */
    private fun logStatus(message: String) {
        ModMainDebug.debugLog("EmvParser", "operation", mapOf("message" to message))
    }

    companion object {
        /**
         * EMV Tag Database - Maps tag hex to human-readable names
         * Extracted from emv_tag_database.md
         */
        private val emvTagDatabase = mapOf(
            // Single-byte tags
            "41" to "Country Code",
            "42" to "IIN",
            "4F" to "ADF Name",
            "50" to "App Label",
            "56" to "Track 1",
            "57" to "Track 2",
            "5A" to "PAN",
            "61" to "App Template",
            "6F" to "FCI Template",
            "70" to "READ RECORD",
            "71" to "Issuer Script 1",
            "72" to "Issuer Script 2",
            "73" to "Directory Entry",
            "77" to "Response Format 2",
            "80" to "Response Format 1",
            "81" to "Amount Auth Bin",
            "82" to "AIP",
            "83" to "Command Template",
            "84" to "DF Name",
            "86" to "Issuer Script Cmd",
            "87" to "App Priority",
            "88" to "SFI",
            "89" to "Auth Code",
            "8A" to "Auth Resp Code",
            "8C" to "CDOL1",
            "8D" to "CDOL2",
            "8E" to "CVM List",
            "8F" to "CA PK Index",
            "90" to "Issuer PK Cert",
            "91" to "Issuer Auth Data",
            "92" to "Issuer PK Rem",
            "93" to "Signed SAD",
            "94" to "AFL",
            "95" to "TVR",
            "97" to "TDOL",
            "98" to "TC Hash",
            "99" to "Trans PIN Data",
            "9A" to "Trans Date",
            "9B" to "TSI",
            "9C" to "Trans Type",
            "9D" to "DDF Name",
            // Two-byte tags (5F)
            "5F20" to "Cardholder Name",
            "5F24" to "Expiration Date",
            "5F25" to "Effective Date",
            "5F28" to "Issuer Country",
            "5F2A" to "Currency Code",
            "5F2D" to "Language Pref",
            "5F30" to "Service Code",
            "5F34" to "PAN Sequence",
            "5F36" to "Trans Currency Exp",
            "5F50" to "Issuer URL",
            "5F53" to "IBAN",
            "5F54" to "Bank ID",
            "5F55" to "Issuer Country A2",
            "5F56" to "Issuer Country A3",
            // Two-byte tags (9F)
            "9F01" to "Acquirer ID",
            "9F02" to "Amount Auth",
            "9F03" to "Amount Other",
            "9F04" to "Amount Other Bin",
            "9F05" to "App Disc Data",
            "9F06" to "AID Terminal",
            "9F07" to "App Usage Ctrl",
            "9F08" to "App Version Num",
            "9F09" to "App Version Term",
            "9F0A" to "App Selection",
            "9F0B" to "Cardholder Name Ext",
            "9F0D" to "IAC Default",
            "9F0E" to "IAC Denial",
            "9F0F" to "IAC Online",
            "9F10" to "IAD",
            "9F11" to "Issuer Code Tbl",
            "9F12" to "App Preferred Name",
            "9F13" to "Last Online ATC",
            "9F14" to "Lower Consec Lim",
            "9F15" to "Merchant Cat Code",
            "9F16" to "Merchant ID",
            "9F17" to "PIN Try Counter",
            "9F18" to "Issuer Script ID",
            "9F1A" to "Term Country",
            "9F1B" to "Term Floor Limit",
            "9F1C" to "Term ID",
            "9F1D" to "Term Risk Data",
            "9F1E" to "IFD Serial",
            "9F1F" to "Track 1 DD",
            "9F20" to "Track 2 DD",
            "9F21" to "Trans Time",
            "9F22" to "Cert Auth PK Idx",
            "9F23" to "Upper Consec Lim",
            "9F26" to "AC",
            "9F27" to "CID",
            "9F2A" to "Kernel ID",
            "9F2D" to "ICC PIN Encipher",
            "9F2E" to "ICC PIN Exp",
            "9F2F" to "ICC PIN Rem",
            "9F32" to "Issuer PK Exp",
            "9F33" to "Term Caps",
            "9F34" to "CVM Results",
            "9F35" to "Term Type",
            "9F36" to "ATC",
            "9F37" to "Unpred Number",
            "9F38" to "PDOL",
            "9F39" to "POS Entry Mode",
            "9F3A" to "Amount Ref Curr",
            "9F3B" to "App Ref Currency",
            "9F3C" to "Trans Ref Curr",
            "9F3D" to "Trans Ref Curr Exp",
            "9F40" to "Add Term Caps",
            "9F41" to "Trans Seq Counter",
            "9F42" to "App Currency",
            "9F43" to "App Ref Curr Exp",
            "9F44" to "App Currency Exp",
            "9F45" to "Data Auth Code",
            "9F46" to "ICC PK Cert",
            "9F47" to "ICC PK Exp",
            "9F48" to "ICC PK Rem",
            "9F49" to "DDOL",
            "9F4A" to "SDA Tag List",
            "9F4B" to "Signed Dynamic AD",
            "9F4C" to "ICC Dynamic Num",
            "9F4D" to "Log Entry",
            "9F4E" to "Merchant Name",
            "9F4F" to "Log Format",
            "9F50" to "Offline Acc Cum1",
            "9F51" to "App Currency Code",
            "9F52" to "App Default Action",
            "9F53" to "Trans Category",
            "9F54" to "Cumulative Offline",
            "9F55" to "Geographic Ind",
            "9F56" to "Issuer Auth Ind",
            "9F57" to "Issuer Country A2",
            "9F58" to "Lower Consec Lim",
            "9F59" to "Upper Consec Lim",
            "9F5A" to "App Program ID",
            "9F5B" to "DSDOL",
            "9F5C" to "Cust Exclusive Data",
            "9F5D" to "App Capabilities",
            "9F5E" to "Data Storage Version",
            "9F5F" to "DS Slot Mgmt Ctrl",
            "9F60" to "CVC3 Track1",
            "9F61" to "CVC3 Track2",
            "9F62" to "PCVC3 Track1",
            "9F63" to "PCVC3 Track2",
            "9F64" to "Natl Disc Track1",
            "9F65" to "Natl Disc Track2",
            "9F66" to "TTQ",
            "9F67" to "Natl Disc Track2",
            "9F68" to "Card Add Proc",
            "9F69" to "Card Auth Related",
            "9F6A" to "Unpred Number",
            "9F6B" to "Card CVM Limit",
            "9F6C" to "CTQ",
            "9F6D" to "Mag App Version",
            "9F6E" to "Form Factor Ind",
            "9F6F" to "DS Slot Availability",
            // Three-byte tags
            "BF0C" to "FCI Issuer Disc",
            "DF01" to "Reference PIN",
            "DF02" to "Authorised Amount",
            "DF03" to "Status Check Support",
            "DF04" to "VLP Funds Limit",
            "DF05" to "VLP Single Trans Lim",
            "DF3E" to "Account Type",
            "DF60" to "UDOL",
            "DF61" to "DS ID",
            "DF62" to "DS ODS Info",
            "DF63" to "DS ODS Term"
        )

        /**
         * EMV Tag Type Database - Maps tag hex to data type
         */
        private val emvTagTypeDatabase = mapOf(
            "41" to "Generic",
            "42" to "Generic",
            "4F" to "Generic",
            "50" to "String",
            "56" to "Generic",
            "57" to "Generic",
            "5A" to "Numeric",
            "61" to "Constructed",
            "6F" to "Constructed",
            "70" to "Constructed",
            "71" to "Constructed",
            "72" to "Constructed",
            "73" to "Constructed",
            "77" to "Constructed",
            "80" to "Primitive",
            "81" to "Binary",
            "82" to "Bitmask",
            "83" to "Constructed",
            "84" to "Generic",
            "86" to "Generic",
            "87" to "Generic",
            "88" to "Generic",
            "89" to "Generic",
            "8A" to "Generic",
            "8C" to "DOL",
            "8D" to "DOL",
            "8E" to "CVM List",
            "8F" to "Numeric",
            "90" to "Generic",
            "91" to "Generic",
            "92" to "Generic",
            "93" to "Generic",
            "94" to "AFL",
            "95" to "Bitmask",
            "97" to "DOL",
            "98" to "Generic",
            "99" to "Generic",
            "9A" to "YYMMDD",
            "9B" to "Bitmask",
            "9C" to "Numeric",
            "9D" to "Generic",
            "5F20" to "String",
            "5F24" to "YYMMDD",
            "5F25" to "YYMMDD",
            "5F28" to "Numeric",
            "5F2A" to "Numeric",
            "5F2D" to "String",
            "5F30" to "Numeric",
            "5F34" to "Numeric",
            "5F36" to "Numeric",
            "5F50" to "String",
            "5F53" to "Numeric",
            "5F54" to "Numeric",
            "5F55" to "String",
            "5F56" to "String",
            "9F01" to "Numeric",
            "9F02" to "Numeric",
            "9F03" to "Numeric",
            "9F04" to "Binary",
            "9F05" to "Generic",
            "9F06" to "Generic",
            "9F07" to "Bitmask",
            "9F08" to "Numeric",
            "9F09" to "Numeric",
            "9F0A" to "Generic",
            "9F0B" to "String",
            "9F0D" to "Bitmask",
            "9F0E" to "Bitmask",
            "9F0F" to "Bitmask",
            "9F10" to "Generic",
            "9F11" to "Numeric",
            "9F12" to "String",
            "9F13" to "Numeric",
            "9F14" to "Numeric",
            "9F15" to "Numeric",
            "9F16" to "String",
            "9F17" to "Numeric",
            "9F18" to "Generic",
            "9F1A" to "Numeric",
            "9F1B" to "Numeric",
            "9F1C" to "String",
            "9F1D" to "Generic",
            "9F1E" to "String",
            "9F1F" to "Generic",
            "9F20" to "Generic",
            "9F21" to "Numeric",
            "9F22" to "Numeric",
            "9F23" to "Numeric",
            "9F26" to "Generic",
            "9F27" to "CID",
            "9F2A" to "Generic",
            "9F2D" to "Generic",
            "9F2E" to "Generic",
            "9F2F" to "Generic",
            "9F32" to "Numeric",
            "9F33" to "Bitmask",
            "9F34" to "Bitmask",
            "9F35" to "Numeric",
            "9F36" to "Numeric",
            "9F37" to "Generic",
            "9F38" to "DOL",
            "9F39" to "Numeric",
            "9F3A" to "Numeric",
            "9F3B" to "Numeric",
            "9F3C" to "Numeric",
            "9F3D" to "Numeric",
            "9F40" to "Bitmask",
            "9F41" to "Numeric",
            "9F42" to "Numeric",
            "9F43" to "Numeric",
            "9F44" to "Numeric",
            "9F45" to "Generic",
            "9F46" to "Generic",
            "9F47" to "Generic",
            "9F48" to "Generic",
            "9F49" to "DOL",
            "9F4A" to "Generic",
            "9F4B" to "Generic",
            "9F4C" to "Generic",
            "9F4D" to "Generic",
            "9F4E" to "String",
            "9F4F" to "Generic",
            "9F50" to "Generic",
            "9F51" to "Numeric",
            "9F52" to "Generic",
            "9F53" to "Generic",
            "9F54" to "Numeric",
            "9F55" to "Generic",
            "9F56" to "Generic",
            "9F57" to "String",
            "9F58" to "Numeric",
            "9F59" to "Numeric",
            "9F5A" to "Generic",
            "9F5B" to "DOL",
            "9F5C" to "Generic",
            "9F5D" to "Generic",
            "9F5E" to "Numeric",
            "9F5F" to "Generic",
            "9F60" to "Generic",
            "9F61" to "Generic",
            "9F62" to "Generic",
            "9F63" to "Generic",
            "9F64" to "Generic",
            "9F65" to "Generic",
            "9F66" to "Bitmask",
            "9F67" to "Generic",
            "9F68" to "Generic",
            "9F69" to "Generic",
            "9F6A" to "Numeric",
            "9F6B" to "Numeric",
            "9F6C" to "Bitmask",
            "9F6D" to "Numeric",
            "9F6E" to "Bitmask",
            "9F6F" to "Generic",
            "BF0C" to "Constructed",
            "DF01" to "Generic",
            "DF02" to "Numeric",
            "DF03" to "Generic",
            "DF04" to "Numeric",
            "DF05" to "Numeric",
            "DF3E" to "Numeric",
            "DF60" to "DOL",
            "DF61" to "Generic",
            "DF62" to "Generic",
            "DF63" to "Generic"
        )
    }
}

/**
 * EMV Tag representation
 */
data class EmvTag(
    val tagHex: String,
    val tagBytes: ByteArray,
    val tagLength: Int,
    val isConstructed: Boolean,
    val length: Int,
    val value: EmvTagValue
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is EmvTag) return false
        return tagHex == other.tagHex && length == other.length
    }

    override fun hashCode(): Int {
        return tagHex.hashCode() * 31 + length
    }
}

/**
 * EMV Tag Value - Can be primitive (bytes) or constructed (nested tags)
 */
sealed class EmvTagValue {
    data class Primitive(val data: ByteArray) : EmvTagValue() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Primitive) return false
            return data.contentEquals(other.data)
        }

        override fun hashCode(): Int {
            return data.contentHashCode()
        }
    }

    data class Constructed(val tags: Map<String, EmvTag>) : EmvTagValue()
}

/**
 * Helper data classes for multi-value returns
 */
data class Tuple3<A, B, C>(val first: A, val second: B, val third: C)
data class Tuple5<A, B, C, D, E>(val first: A, val second: B, val third: C, val fourth: D, val fifth: E)
