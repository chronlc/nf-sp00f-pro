package com.nfsp00fpro.app.modules

import android.content.Context
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * EMV Card Database Module
 *
 * Manages persistent storage of all card read data:
 * - APDU commands and responses (complete workflow)
 * - All parsed TLV tags per AID
 * - PDOL data constructed for each AID
 * - Card session metadata
 *
 * Features:
 * - Structured per-AID data storage
 * - Async batch writes (non-blocking)
 * - Room ORM with SQLite backend
 * - ByteArray binary storage
 * - Efficient indexing on sessionId and aidId
 *
 * Performance:
 * - APDU logs: Batch saved after each phase
 * - TLV tags: Batch saved after complete parse
 * - No per-operation database hits during scan
 * - IO dispatcher (doesn't block card reading)
 */

// ============================================================================
// Data Models / Entities
// ============================================================================

/**
 * Card Session - Represents one complete card read
 *
 * Stores metadata for the entire scan including timestamp,
 * contactless flag, and overall status
 */
@Entity(tableName = "card_sessions")
data class CardSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val isContactless: Boolean = true,
    val status: String = "PENDING" // PENDING, SUCCESS, PARTIAL, FAILED
)

/**
 * AID Record - One Application ID per card session
 *
 * Stores AID-specific data including the actual AID bytes,
 * card brand detected, PDOL data, and priority used
 */
@Entity(
    tableName = "aid_records",
    foreignKeys = [
        ForeignKey(
            entity = CardSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AidRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String = "",
    val aidHex: String = "", // e.g., "A0000000031010"
    val aidBytes: ByteArray = byteArrayOf(),
    val cardBrand: String = "", // VISA, MASTERCARD, AMEX, MAESTRO, etc.
    val pdolData: ByteArray? = null, // PDOL data built for this AID
    val priority: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as AidRecord
        if (id != other.id) return false
        if (aidBytes.contentEquals(other.aidBytes)) return false
        if (pdolData?.contentEquals(other.pdolData) == false) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + aidBytes.contentHashCode()
        result = 31 * result + (pdolData?.contentHashCode() ?: 0)
        return result
    }
}

/**
 * APDU Log - Individual APDU command/response pair
 *
 * Stores the complete command (CLA, INS, P1, P2, data) and response
 * with status word. Batch inserted after each phase to minimize overhead.
 */
@Entity(
    tableName = "apdu_logs",
    foreignKeys = [
        ForeignKey(
            entity = CardSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AidRecord::class,
            parentColumns = ["id"],
            childColumns = ["aidId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ApduLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String = "",
    val aidId: Long = 0, // 0 if not AID-specific (e.g., PPSE select)
    val commandPhase: String = "", // SELECT, GPO, READ_RECORD, GET_RESPONSE
    val commandData: ByteArray = byteArrayOf(), // Complete APDU bytes
    val responseData: ByteArray = byteArrayOf(), // Response payload (excluding SW)
    val statusWord: Int = 0x0000, // SW1 << 8 | SW2
    val timestamp: Long = System.currentTimeMillis()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as ApduLog
        if (id != other.id) return false
        if (commandData.contentEquals(other.commandData)) return false
        if (responseData.contentEquals(other.responseData)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + commandData.contentHashCode()
        result = 31 * result + responseData.contentHashCode()
        return result
    }
}

/**
 * TLV Tag Record - Parsed EMV tag with value
 *
 * Stores all 200+ parsed tags per card session and AID.
 * Batch inserted after complete parser run to minimize overhead.
 */
@Entity(
    tableName = "tlv_tags",
    foreignKeys = [
        ForeignKey(
            entity = CardSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AidRecord::class,
            parentColumns = ["id"],
            childColumns = ["aidId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TlvTag(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessionId: String = "",
    val aidId: Long = 0,
    val tagHex: String = "", // e.g., "9F34"
    val tagBytes: ByteArray = byteArrayOf(), // Binary tag (1-3 bytes)
    val valueBytes: ByteArray = byteArrayOf(), // Tag value
    val isConstructed: Boolean = false,
    val depth: Int = 0 // Nesting depth (0 = top-level)
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as TlvTag
        if (id != other.id) return false
        if (tagBytes.contentEquals(other.tagBytes)) return false
        if (valueBytes.contentEquals(other.valueBytes)) return false
        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + tagBytes.contentHashCode()
        result = 31 * result + valueBytes.contentHashCode()
        return result
    }
}

// ============================================================================
// Type Converters (for ByteArray storage in Room)
// ============================================================================

class ByteArrayConverter {
    @TypeConverter
    fun fromByteArray(value: ByteArray?): ByteArray? = value

    @TypeConverter
    fun toByteArray(value: ByteArray?): ByteArray? = value
}

// ============================================================================
// DAO (Data Access Objects)
// ============================================================================

@Dao
interface CardSessionDao {
    @Insert
    suspend fun insertSession(session: CardSession): Long

    @Query("SELECT * FROM card_sessions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getSession(sessionId: String): CardSession?

    @Query("SELECT * FROM card_sessions ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getRecentSessions(limit: Int = 50): List<CardSession>

    @Delete
    suspend fun deleteSession(session: CardSession)

    @Query("DELETE FROM card_sessions WHERE timestamp < :beforeTime")
    suspend fun deleteOldSessions(beforeTime: Long)
}

@Dao
interface AidRecordDao {
    @Insert
    suspend fun insertAid(aid: AidRecord): Long

    @Query("SELECT * FROM aid_records WHERE sessionId = :sessionId")
    suspend fun getSessionAids(sessionId: String): List<AidRecord>

    @Query("SELECT * FROM aid_records WHERE id = :aidId LIMIT 1")
    suspend fun getAid(aidId: Long): AidRecord?

    @Insert
    suspend fun insertAidBatch(aids: List<AidRecord>): List<Long>

    @Delete
    suspend fun deleteAid(aid: AidRecord)
}

@Dao
interface ApduLogDao {
    @Insert
    suspend fun insertLog(log: ApduLog): Long

    @Query("SELECT * FROM apdu_logs WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    suspend fun getSessionLogs(sessionId: String): List<ApduLog>

    @Query("SELECT * FROM apdu_logs WHERE aidId = :aidId ORDER BY timestamp ASC")
    suspend fun getAidLogs(aidId: Long): List<ApduLog>

    @Insert
    suspend fun insertLogBatch(logs: List<ApduLog>): List<Long>

    @Delete
    suspend fun deleteLog(log: ApduLog)

    @Query("SELECT COUNT(*) FROM apdu_logs WHERE sessionId = :sessionId")
    suspend fun getLogCountForSession(sessionId: String): Int
}

@Dao
interface TlvTagDao {
    @Insert
    suspend fun insertTag(tag: TlvTag): Long

    @Query("SELECT * FROM tlv_tags WHERE sessionId = :sessionId ORDER BY tagHex ASC")
    suspend fun getSessionTags(sessionId: String): List<TlvTag>

    @Query("SELECT * FROM tlv_tags WHERE aidId = :aidId ORDER BY tagHex ASC")
    suspend fun getAidTags(aidId: Long): List<TlvTag>

    @Query("SELECT * FROM tlv_tags WHERE sessionId = :sessionId AND tagHex = :tagHex")
    suspend fun getTagsByHex(sessionId: String, tagHex: String): List<TlvTag>

    @Insert
    suspend fun insertTagBatch(tags: List<TlvTag>): List<Long>

    @Delete
    suspend fun deleteTag(tag: TlvTag)

    @Query("SELECT COUNT(*) FROM tlv_tags WHERE sessionId = :sessionId")
    suspend fun getTagCountForSession(sessionId: String): Int
}

// ============================================================================
// Room Database
// ============================================================================

@Database(
    entities = [CardSession::class, AidRecord::class, ApduLog::class, TlvTag::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ByteArrayConverter::class)
abstract class EmvCardDatabase : RoomDatabase() {
    abstract fun cardSessionDao(): CardSessionDao
    abstract fun aidRecordDao(): AidRecordDao
    abstract fun apduLogDao(): ApduLogDao
    abstract fun tlvTagDao(): TlvTagDao
}

// ============================================================================
// EMV Database Manager
// ============================================================================

/**
 * EmvDatabase - Manages all card data persistence
 *
 * Provides async methods for:
 * - Saving card session metadata
 * - Saving APDU command/response logs (batch)
 * - Saving parsed TLV tags (batch)
 * - Querying historical card data
 *
 * All database operations are non-blocking (IO dispatcher)
 */
class EmvDatabase(private val context: Context) {

    // Database instance (lazy-initialized, singleton)
    private val database: EmvCardDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            EmvCardDatabase::class.java,
            "emv_card_database"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    private val sessionDao: CardSessionDao by lazy { database.cardSessionDao() }
    private val aidDao: AidRecordDao by lazy { database.aidRecordDao() }
    private val apduDao: ApduLogDao by lazy { database.apduLogDao() }
    private val tagDao: TlvTagDao by lazy { database.tlvTagDao() }

    // Coroutine scope for async operations
    private val databaseScope = CoroutineScope(Dispatchers.IO)

    // ========================================================================
    // Session Management
    // ========================================================================

    /**
     * Create new card session
     *
     * Parameters:
     * - isContactless: Boolean - true for NFC, false for contact
     *
     * Returns: sessionId (String) for reference in logs/tags
     */
    suspend fun createSession(isContactless: Boolean): String {
        val session = CardSession(
            isContactless = isContactless,
            status = "PENDING"
        )
        sessionDao.insertSession(session)
        return session.sessionId
    }

    /**
     * Update session status
     *
     * Parameters:
     * - sessionId: String - Session to update
     * - status: String - New status (SUCCESS, PARTIAL, FAILED)
     */
    fun updateSessionStatus(sessionId: String, status: String) {
        databaseScope.launch {
            val session = sessionDao.getSession(sessionId)
            if (session != null) {
                val updated = session.copy(status = status)
                sessionDao.insertSession(updated)
            }
        }
    }

    /**
     * Get recent card sessions
     *
     * Parameters:
     * - limit: Int - Number of sessions to retrieve (default 50)
     *
     * Returns: List<CardSession> - Recent card reads
     */
    suspend fun getRecentSessions(limit: Int = 50): List<CardSession> {
        return sessionDao.getRecentSessions(limit)
    }

    // ========================================================================
    // AID Management
    // ========================================================================

    /**
     * Save AID record for a session
     *
     * Parameters:
     * - sessionId: String
     * - aidHex: String - AID in hex format
     * - aidBytes: ByteArray - AID binary data
     * - cardBrand: String - Detected brand (VISA, MASTERCARD, etc.)
     * - pdolData: ByteArray? - PDOL data constructed for this AID
     * - priority: Int - Priority used in selection
     *
     * Returns: aidId (Long) for reference in APDU/tag logs
     */
    suspend fun saveAidRecord(
        sessionId: String,
        aidHex: String,
        aidBytes: ByteArray,
        cardBrand: String,
        pdolData: ByteArray? = null,
        priority: Int = 0
    ): Long {
        val aid = AidRecord(
            sessionId = sessionId,
            aidHex = aidHex,
            aidBytes = aidBytes,
            cardBrand = cardBrand,
            pdolData = pdolData,
            priority = priority
        )
        return aidDao.insertAid(aid)
    }

    /**
     * Get all AIDs for a session
     *
     * Parameters:
     * - sessionId: String
     *
     * Returns: List<AidRecord> - All AIDs found in this session
     */
    suspend fun getSessionAids(sessionId: String): List<AidRecord> {
        return aidDao.getSessionAids(sessionId)
    }

    // ========================================================================
    // APDU Log Management (Batch Operations)
    // ========================================================================

    /**
     * Save APDU command/response pair
     *
     * Parameters:
     * - sessionId: String
     * - aidId: Long - AID ID (0 if not AID-specific)
     * - phase: String - Command phase (SELECT, GPO, READ_RECORD, etc.)
     * - commandBytes: ByteArray - Complete APDU command
     * - responseBytes: ByteArray - Response payload (excluding SW)
     * - statusWord: Int - SW1 << 8 | SW2
     *
     * Executed asynchronously (non-blocking)
     */
    fun saveApduLog(
        sessionId: String,
        aidId: Long = 0,
        phase: String,
        commandBytes: ByteArray,
        responseBytes: ByteArray,
        statusWord: Int
    ) {
        databaseScope.launch {
            val log = ApduLog(
                sessionId = sessionId,
                aidId = aidId,
                commandPhase = phase,
                commandData = commandBytes,
                responseData = responseBytes,
                statusWord = statusWord
            )
            apduDao.insertLog(log)
        }
    }

    /**
     * Batch save multiple APDU logs
     *
     * Parameters:
     * - logs: List<ApduLog> - Multiple log entries to save
     *
     * Executed asynchronously (non-blocking)
     * Preferred over individual saveApduLog calls when possible
     */
    fun saveApduLogBatch(logs: List<ApduLog>) {
        databaseScope.launch {
            apduDao.insertLogBatch(logs)
        }
    }

    /**
     * Get all APDU logs for a session
     *
     * Parameters:
     * - sessionId: String
     *
     * Returns: List<ApduLog> - Complete command/response history
     */
    suspend fun getSessionApduLogs(sessionId: String): List<ApduLog> {
        return apduDao.getSessionLogs(sessionId)
    }

    /**
     * Get APDU logs for specific AID
     *
     * Parameters:
     * - aidId: Long
     *
     * Returns: List<ApduLog> - Commands executed for this AID
     */
    suspend fun getAidApduLogs(aidId: Long): List<ApduLog> {
        return apduDao.getAidLogs(aidId)
    }

    /**
     * Get APDU log count for session
     *
     * Parameters:
     * - sessionId: String
     *
     * Returns: Int - Total APDU commands executed
     */
    suspend fun getApduLogCount(sessionId: String): Int {
        return apduDao.getLogCountForSession(sessionId)
    }

    // ========================================================================
    // TLV Tag Management (Batch Operations)
    // ========================================================================

    /**
     * Save single parsed TLV tag
     *
     * Parameters:
     * - sessionId: String
     * - aidId: Long - AID this tag belongs to
     * - tagHex: String - Tag in hex format (e.g., "9F34")
     * - tagBytes: ByteArray - Binary tag bytes
     * - valueBytes: ByteArray - Tag value
     * - isConstructed: Boolean - Is this a constructed tag?
     * - depth: Int - Nesting depth
     *
     * Executed asynchronously (non-blocking)
     */
    fun saveTlvTag(
        sessionId: String,
        aidId: Long,
        tagHex: String,
        tagBytes: ByteArray,
        valueBytes: ByteArray,
        isConstructed: Boolean = false,
        depth: Int = 0
    ) {
        databaseScope.launch {
            val tag = TlvTag(
                sessionId = sessionId,
                aidId = aidId,
                tagHex = tagHex,
                tagBytes = tagBytes,
                valueBytes = valueBytes,
                isConstructed = isConstructed,
                depth = depth
            )
            tagDao.insertTag(tag)
        }
    }

    /**
     * Batch save multiple TLV tags
     *
     * Parameters:
     * - tags: List<TlvTag> - Multiple tags to save (usually 200+)
     *
     * Executed asynchronously (non-blocking)
     * PREFERRED: Call this once after complete parse instead of per-tag
     */
    fun saveTlvTagBatch(tags: List<TlvTag>) {
        databaseScope.launch {
            tagDao.insertTagBatch(tags)
        }
    }

    /**
     * Get all TLV tags for session
     *
     * Parameters:
     * - sessionId: String
     *
     * Returns: List<TlvTag> - All 200+ parsed tags
     */
    suspend fun getSessionTlvTags(sessionId: String): List<TlvTag> {
        return tagDao.getSessionTags(sessionId)
    }

    /**
     * Get TLV tags for specific AID
     *
     * Parameters:
     * - aidId: Long
     *
     * Returns: List<TlvTag> - Tags for this AID
     */
    suspend fun getAidTlvTags(aidId: Long): List<TlvTag> {
        return tagDao.getAidTags(aidId)
    }

    /**
     * Query tags by hex value
     *
     * Parameters:
     * - sessionId: String
     * - tagHex: String - Tag hex to find (e.g., "9F34")
     *
     * Returns: List<TlvTag> - Matching tags (usually 1, but may have duplicates)
     */
    suspend fun getTlvTagsByHex(sessionId: String, tagHex: String): List<TlvTag> {
        return tagDao.getTagsByHex(sessionId, tagHex)
    }

    /**
     * Get TLV tag count for session
     *
     * Parameters:
     * - sessionId: String
     *
     * Returns: Int - Total tags parsed (usually 200+)
     */
    suspend fun getTlvTagCount(sessionId: String): Int {
        return tagDao.getTagCountForSession(sessionId)
    }

    // ========================================================================
    // Cleanup & Maintenance
    // ========================================================================

    /**
     * Delete old sessions (cleanup)
     *
     * Parameters:
     * - beforeTime: Long - Milliseconds timestamp
     *
     * Cascades delete: session → AIDs → APDUs → Tags
     */
    fun deleteOldSessions(beforeTime: Long) {
        databaseScope.launch {
            sessionDao.deleteOldSessions(beforeTime)
        }
    }

    /**
     * Close database connection
     */
    fun close() {
        database.close()
    }
}

// ============================================================================
// Logging Helper
// ============================================================================

private fun logStatus(message: String) {
    println("[EmvDatabase] $message")
}
