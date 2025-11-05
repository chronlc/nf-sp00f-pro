package com.nfsp00fpro.app.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Card Reading Session - represents a complete NFC card reading event
 */
data class CardSession(
    val id: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val pan: String = "",
    val cardHolder: String = "",
    val expiryDate: String = "",
    val applicationName: String = "",
    val transactionCounter: String = "",
    val cvmMethod: String = "",
    val terminalCapabilities: String = "",
    val apdus: List<String> = emptyList(),
    val tags: Map<String, String> = emptyMap()
)

/**
 * APDU Command - represents a single APDU command sent to device
 */
data class ApduCommand(
    val command: String = "",
    val response: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val success: Boolean = false
)

/**
 * EMV Card record in database
 */
@Entity(tableName = "emv_cards")
data class EmvCard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pan: String = "",
    val cardHolder: String = "",
    val expiryDate: String = "",
    val emvName: String = "",
    val appPan: String = "",
    val transactionCounter: Int = 0,
    val cvmMethod: String = "",
    val terminalCapabilities: String = "",
    val readAt: Long = System.currentTimeMillis(),
    val aid: String = "",
    val dedicatedFileName: String = "",
    val effectiveDate: String = "",
    val serviceCode: String = "",
    val issuerUrl: String = ""
)
