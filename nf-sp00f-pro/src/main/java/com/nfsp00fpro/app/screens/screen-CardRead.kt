@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.nfsp00fpro.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nfsp00fpro.app.modules.CardSession
import com.nfsp00fpro.app.modules.ModMainDebug
import com.nfsp00fpro.app.ui.NfSp00fIcons
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Card Read Screen - NFC Card Reading Interface
 * 
 * Clean, professional design focused on card reading operations
 * Displays real-time card data, recent reads, and hardware status
 * 
 * Data Flow:
 * - CardReadViewModel loads real session history from database
 * - User taps "Start Reading" → initiates real NFC device polling
 * - System awaits actual card detection and reading
 * - UI updates via StateFlows with real device data (no simulation)
 * - Recent reads loaded from EmvDatabase (persistent history)
 * 
 * All displayed data is REAL - from device modules or database, never hardcoded
 */
@Composable
fun CardReadScreen() {
    val context = LocalContext.current
    val viewModel: CardReadViewModel = viewModel(factory = CardReadViewModel.Factory(context))

    // Collect real state from ViewModel - all from database and device modules
    val cardData by viewModel.cardData.collectAsState()
    val isReading by viewModel.isReading.collectAsState()
    val readingProgress by viewModel.readingProgress.collectAsState()
    val hardwareStatus by viewModel.hardwareStatus.collectAsState()
    val recentReads by viewModel.recentReads.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Status header with real hardware status
        HeaderStatusCard(hardwareStatus = hardwareStatus)

        // Main card reader control panel
        CardReaderPanel(
            cardData = cardData,
            isReading = isReading,
            readingProgress = readingProgress,
            onStartRead = { 
                // Start real device card reading - awaits actual NFC device
                viewModel.startCardReading()
                ModMainDebug.debugLog(
                    module = "CardReadScreen",
                    operation = "start_reading_initiated",
                    data = mapOf("timestamp" to System.currentTimeMillis().toString())
                )
            }
        )

        // Display card details when a card has been successfully read (real device data)
        if (cardData != null) {
            CardDetailsPanel(cardData = cardData!!)
        }

        // Recent card reads from database history (real persistent data)
        RecentReadsSection(recentReads = recentReads)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/**
 * Header card displaying real hardware status
 * 
 * @param hardwareStatus Real device status string from ViewModel
 * 
 * Shows current NFC/Bluetooth device readiness and overall system health
 * Data source: Actual device state from hardware modules
 */
@Composable
private fun HeaderStatusCard(hardwareStatus: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F1F)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Card Reader Status",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFFFFFFF)
                )
                Text(
                    hardwareStatus,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
            }

            Icon(
                Icons.Default.Info,
                contentDescription = "Status",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Main card reader control panel with real device integration
 * 
 * @param cardData Real CardSession from device (null until card is read)
 * @param isReading Actual reading state from hardware (true during active NFC polling)
 * @param readingProgress Real progress 0-100% from device APDU exchanges
 * @param onStartRead Callback to trigger real device card read
 * 
 * Display States:
 * - No card: Shows NFC icon, "Ready for Card" message, start button
 * - Card read: Shows checkmark, "Card Read Successfully", displays card details
 * - During read: Shows progress bar with percentage, disabled start button
 * 
 * All data from CardReadViewModel StateFlows (database and device state)
 */
@Composable
private fun CardReaderPanel(
    cardData: CardSession?,
    isReading: Boolean,
    readingProgress: Int,
    onStartRead: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 250.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F1F)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (cardData == null) {
                // Real data not yet available - waiting for device input
                NfSp00fIcons.Nfc(
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Ready for Card",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFFFFFFFF),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Tap card to NFC reader",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF888888),
                    textAlign = TextAlign.Center
                )
            } else {
                // Real card data successfully read from device
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Card Read",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Card Read Successfully",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color(0xFF4CAF50),
                    textAlign = TextAlign.Center
                )
            }

            if (isReading) {
                // Real progress from actual device APDU transaction (0-100%)
                Spacer(modifier = Modifier.height(24.dp))
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = readingProgress / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFF333333)
                    )
                    Text(
                        "$readingProgress%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF888888)
                    )
                }
            }

            // Start/Stop reading button - triggers real device interaction
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onStartRead,
                enabled = !isReading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4CAF50),
                    disabledContainerColor = Color(0xFF666666)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Read", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Reading", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Display details of a card session read by actual device
 * 
 * @param cardData Real CardSession from database - contains actual device read data
 * 
 * Shows:
 * - Session ID (last 8 chars for brevity)
 * - Card Type (Contactless NFC or Contact)
 * - Status (PENDING, SUCCESS, PARTIAL, FAILED - from actual device)
 * - Read Time (formatted from real timestamp)
 * 
 * Data Source: Real CardSession from database (actual device read record)
 * Visible only after device successfully reads a card
 */
@Composable
private fun CardDetailsPanel(cardData: CardSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F1F)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Session Details",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFFFFFFF)
            )

            CardDetailRow(label = "Session ID", value = cardData.sessionId.takeLast(8))
            CardDetailRow(label = "Type", value = if (cardData.isContactless) "Contactless NFC" else "Contact")
            CardDetailRow(label = "Status", value = cardData.status)
            CardDetailRow(label = "Time", value = SimpleDateFormat("HH:mm:ss").format(Date(cardData.timestamp)))
        }
    }
}

/**
 * Single row displaying a session detail field
 * 
 * Label and value display with consistent formatting
 */
@Composable
private fun CardDetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF888888)
        )
        Text(
            value.ifEmpty { "N/A" },
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFFFFFFF),
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Display list of recent card sessions from database history
 * 
 * @param recentReads Real list of CardSession objects from database (last 10 reads)
 *
 * Data Source: Loaded from EmvDatabase.getRecentSessions() - actual device read history
 * Updates when new cards are read (real persistent data)
 * Shows up to 5 most recent sessions in scrollable list
 * Classified as "Real Data" - comes from persistent database storage
 */
@Composable
private fun RecentReadsSection(recentReads: List<CardSession>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F1F)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Recent Reads",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFFFFFFF)
            )

            if (recentReads.isEmpty()) {
                Text(
                    "No recent card reads",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF888888),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(recentReads) { session ->
                        RecentReadItem(session = session)
                    }
                }
            }
        }
    }
}

/**
 * Single card session item for recent reads list
 * 
 * @param session Real CardSession from database - represents one actual device read
 * 
 * Displays:
 * - Session ID (last 8 chars for brevity)
 * - Card Type icon and label
 * - Status with color coding
 * - Read timestamp (formatted as HH:mm:ss)
 * 
 * Real Data Indicator: Shows real session data from database
 * Status colors: Green=SUCCESS, Yellow=PENDING, Red=FAILED
 * Updated from actual persistent database on each read
 */
@Composable
private fun RecentReadItem(session: CardSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1419)),
        shape = RoundedCornerShape(6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    session.sessionId.takeLast(8),
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFFFFFFF)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = "Card Type",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        if (session.isContactless) "NFC" else "Contact",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF888888)
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    SimpleDateFormat("HH:mm:ss").format(Date(session.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888)
                )
                
                // Real status from device - Green=SUCCESS, Yellow=PENDING, Red=FAILED
                val statusColor = when (session.status) {
                    "SUCCESS" -> Color(0xFF4CAF50)
                    "PENDING" -> Color(0xFFFFB74D)
                    "FAILED" -> Color(0xFFEF5350)
                    else -> Color(0xFF888888)
                }
                
                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        session.status,
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
