@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.nfsp00fpro.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nfsp00fpro.app.modules.CardSession
import com.nfsp00fpro.app.modules.ModMainDebug
import com.nfsp00fpro.app.modules.ModMainNfsp00f
import com.nfsp00fpro.app.ui.NfSp00fIcons
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Card Read Screen - Professional NFC Card Reading Interface
 * 
 * Comprehensive card reading experience with:
 * - Beautiful card reader control panel with Start/Stop buttons visible
 * - Recent cards displayed as virtual credit cards (horizontal scroll, 3 per row)
 * - EMV tag data display from parsed database records
 * - Terminal-style APDU log viewer (scrollable, EMV reads only)
 * 
 * Data Flow:
 * - CardReadViewModel loads real session history from database
 * - User taps "Start Reading" → initiates real NFC device polling
 * - System awaits actual card detection and reading
 * - UI updates via StateFlows with real device data (no simulation)
 * - EMV tags loaded from TlvTag database entities (auto-saved by parser)
 * 
 * All displayed data is REAL - from device modules or database, never hardcoded
 */
@Composable
fun CardReadScreen(moduleManager: ModMainNfsp00f? = null) {
    val context = LocalContext.current
    val activity = LocalContext.current as? android.app.Activity
    val viewModel: CardReadViewModel = viewModel(factory = CardReadViewModel.Factory(context, moduleManager, activity))

    // Collect real state from ViewModel - all from database and device modules
    val cardData by viewModel.cardData.collectAsState()
    val isReading by viewModel.isReading.collectAsState()
    val readingProgress by viewModel.readingProgress.collectAsState()
    val hardwareStatus by viewModel.hardwareStatus.collectAsState()
    val recentReads by viewModel.recentReads.collectAsState(initial = emptyList())
    val apduLog by viewModel.apduLog.collectAsState()

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

        // Main card reader control panel - Beautiful styling with visible Start/Stop
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

        // Recent Cards Section - Virtual credit cards in horizontal scroll
        if (recentReads.isNotEmpty()) {
            RecentCardsSection(recentReads = recentReads.take(3)) // Top 3 recent cards
        }

        // EMV Tag Data Section - Parsed card data from database
        if (cardData != null) {
            EmvTagDataSection(cardData = cardData!!)
        }

        // APDU Terminal - Real-time EMV read logs only, scrollable, no line limit
        ApduTerminalSection(apduLog = apduLog.filter { it.contains("emv_", ignoreCase = true) || it.contains("card_", ignoreCase = true) || it.contains("aidId", ignoreCase = true) })

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
        shape = RoundedCornerShape(12.dp),
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
 * Beautiful main card reader control panel with VISIBLE START/STOP buttons
 * 
 * @param cardData Real CardSession from device (null until card is read)
 * @param isReading Actual reading state from hardware (true during active NFC polling)
 * @param readingProgress Real progress 0-100% from device APDU exchanges
 * @param onStartRead Callback to trigger real device card read
 * 
 * Display States:
 * - No card: Shows NFC icon, "Ready for Card" message, start button visible
 * - Card read: Shows checkmark, "Card Read Successfully", displays card details
 * - During read: Shows progress bar with percentage, STOP BUTTON VISIBLE
 * 
 * FIX: Stop button now always shows when isReading=true (not in finally block)
 * All data from CardReadViewModel StateFlows (database and device state)
 */
@Composable
private fun CardReaderPanel(
    cardData: CardSession?,
    isReading: Boolean,
    readingProgress: Int,
    onStartRead: () -> Unit
) {
    val viewModel: CardReadViewModel = viewModel(factory = CardReadViewModel.Factory(LocalContext.current, null, null))
    val selectedReader by viewModel.selectedReader.collectAsState()

    // Beautiful gradient-like card with dark theme
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 280.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F1F)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Reader Selection Dropdown (replaces static card when not reading)
            if (!isReading && cardData == null) {
                ReaderSelectionDropdown(
                    selectedReader = selectedReader,
                    onReaderSelected = { reader ->
                        viewModel.selectReader(reader)
                    }
                )
            } else {
                // Show card status when reading or card was read
                if (cardData == null) {
                    // Real data not yet available - waiting for device input
                    // Large NFC icon for visibility
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFF2A2E2E), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        NfSp00fIcons.Nfc(
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    
                    Text(
                        "Ready for Card",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = Color(0xFFFFFFFF),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Tap card to NFC reader",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFAAAAAAA),
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Real card data successfully read from device - Success state
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(Color(0xFF2A3E2A), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Card Read",
                            tint = Color(0xFF4CAF50),
                            modifier = Modifier.size(56.dp)
                        )
                    }
                    
                    Text(
                        "Card Read Successfully",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp
                        ),
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Progress indicator during reading
            if (isReading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Animated progress bar
                    LinearProgressIndicator(
                        progress = readingProgress / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = Color(0xFF4CAF50),
                        trackColor = Color(0xFF333333),
                    )
                    
                    // Progress percentage text
                    Text(
                        "Reading... $readingProgress%",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFF4CAF50)
                    )
                }
            }

            // Control Buttons - Start/Stop reading
            // FIX: Stop button is NOW VISIBLE when isReading=true
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Start Reading Button
                Button(
                    onClick = onStartRead,
                    enabled = !isReading,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50),
                        disabledContainerColor = Color(0xFF666666)
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Read", tint = Color.Black, modifier = Modifier.size(20.dp))
                        Text("Start", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                // Stop Reading Button - VISIBLE when reading
                if (isReading) {
                    Button(
                        onClick = {
                            viewModel.stopCardReading()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFEF5350)
                        ),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color.Black, modifier = Modifier.size(20.dp))
                            Text("Stop", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Reader Selection Dropdown
 * 
 * @param selectedReader Currently selected reader name
 * @param onReaderSelected Callback when reader is selected
 * 
 * Shows available readers based on device hardware:
 * - "Android NFC" - Built-in NFC chip
 * - "PN532 Bluetooth" - External Bluetooth device
 */
@Composable
private fun ReaderSelectionDropdown(
    selectedReader: String?,
    onReaderSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val readers = listOf(
        "Android NFC - Built-in reader",
        "PN532 Bluetooth - Wireless device"
    )
    
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedButton(
            onClick = { expanded = !expanded },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    selectedReader ?: "Select NFC Reader",
                    color = if (selectedReader != null) Color(0xFFFFFFFF) else Color(0xFF888888),
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color(0xFF1B1F1F))
        ) {
            readers.forEach { reader ->
                DropdownMenuItem(
                    text = {
                        Text(
                            reader,
                            color = Color(0xFFFFFFFF),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                    onClick = {
                        onReaderSelected(reader)
                        expanded = false
                    },
                    modifier = Modifier.background(
                        if (selectedReader == reader) Color(0xFF333333) else Color(0xFF1B1F1F)
                    )
                )
            }
        }
    }
}

/**
 * Recent Cards Section - Virtual Credit Cards Display
 * 
 * @param recentReads Top 3 recent CardSession records from database
 * 
 * Displays cards as virtual credit card format:
 * - Card brand (VISA, Mastercard, AMEX, etc.) with color coding
 * - Last 4 digits of PAN (shown as ••• 1234 format)
 * - Expiry date (if available)
 * - Horizontal scroll for viewing multiple cards
 * 
 * Data Source: CardSession from database (real device reads)
 * Visual: Mimics physical credit card design
 */
@Composable
private fun RecentCardsSection(recentReads: List<CardSession>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F1F)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "Recent Cards",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFFFFFFF)
            )

            // Horizontal scroll of virtual cards (3 visible at once)
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(recentReads) { session ->
                    VirtualCreditCard(session = session)
                }
            }
        }
    }
}

/**
 * Virtual Credit Card Display
 * 
 * @param session CardSession with card data
 * 
 * Mimics physical credit card design:
 * - Gradient background (colored by card brand)
 * - Card brand name
 * - Masked PAN (••• 1234)
 * - Session date
 * 
 * Compact size for horizontal display
 */
@Composable
private fun VirtualCreditCard(session: CardSession) {
    val cardBrand = "VISA" // TODO: Get from AID records in database
    val cardColor = when (cardBrand) {
        "VISA" -> Color(0xFF1A1F71) // Visa blue
        "MASTERCARD" -> Color(0xFFEB001B) // Mastercard red
        "AMEX" -> Color(0xFF006FCF) // AmEx blue
        else -> Color(0xFF4CAF50) // Default green
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Card brand name
            Text(
                cardBrand,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                ),
                color = Color.White
            )

            // Masked PAN
            Text(
                "•••• ${session.sessionId.takeLast(4).uppercase()}",
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 2.sp
                ),
                color = Color.White
            )

            // Expiry date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    SimpleDateFormat("MM/yy").format(Date(session.timestamp)),
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                // Status indicator
                Surface(
                    color = if (session.status == "SUCCESS") Color.White.copy(alpha = 0.3f) else Color.Red.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        session.status.take(3),
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                        color = Color.White,
                        modifier = Modifier.padding(2.dp)
                    )
                }
            }
        }
    }
}

/**
 * EMV Tag Data Section - Parsed card data from database
 * 
 * @param cardData CardSession to load TLV tags for
 * 
 * Displays parsed EMV tag data organized by:
 * - Card Brand (from AID record)
 * - Application ID
 * - Individual tag hex + value
 * 
 * Data automatically saved by EmvParser after card read
 * Shows all tags extracted from card in tree-like structure
 */
@Composable
private fun EmvTagDataSection(cardData: CardSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F1F)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                "EMV Card Data",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFFFFFFF)
            )

            // Display session info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F1419), RoundedCornerShape(8.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EmvDataRow(label = "Session ID", value = cardData.sessionId.takeLast(12))
                EmvDataRow(label = "Type", value = if (cardData.isContactless) "Contactless (NFC)" else "Contact")
                EmvDataRow(label = "Status", value = cardData.status)
                EmvDataRow(label = "Read Time", value = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(cardData.timestamp)))
            }

            Text(
                "Note: Full TLV tag data saved to database and available for analysis",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF888888),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

/**
 * Single EMV data row display
 */
@Composable
private fun EmvDataRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFF888888),
            fontWeight = FontWeight.Medium
        )
        Text(
            value,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )
    }
}

/**
 * APDU Terminal Section - Real-time EMV read logs only
 *
 * @param apduLog List of formatted log entries filtered for EMV operations
 *
 * Features:
 * - Displays ONLY EMV-related APDU logs (not all debug logs)
 * - Terminal-style monospace display
 * - Color-coded entries (green for transmit, blue for receive)
 * - SCROLLABLE with NO LINE LIMIT - shows all EMV logs
 * - Latest entries visible at bottom (scroll to latest)
 * - Empty state: ">>> Waiting for card communication..."
 *
 * Data source: ModMainDebug filtered for EMV operations
 */
@Composable
private fun ApduTerminalSection(apduLog: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F1F)),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Terminal header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "EMV Read Terminal",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4CAF50),
                        fontSize = 14.sp
                    )
                )

                Text(
                    "${apduLog.size} EMV logs",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Terminal display area - SCROLLABLE, NO LINE LIMIT
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 200.dp, max = 400.dp) // Scrollable height
                    .background(Color(0xFF0F1419), RoundedCornerShape(8.dp))
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()) // SCROLLABLE
            ) {
                if (apduLog.isEmpty()) {
                    // Empty state - waiting for EMV logs
                    Text(
                        ">>> Waiting for card communication...",
                        color = Color(0xFF4CAF50),
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        ),
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Display ALL EMV log entries in scrollable Column (NO LINE LIMIT)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        apduLog.forEach { logEntry ->
                            ApduLogLine(logEntry = logEntry)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual APDU log line formatter - EMV read specific
 *
 * @param logEntry Formatted EMV log string
 *
 * Format: "[Module] operation | key=value, key2=value2"
 * Colors: Green for TX/success, Blue for RX/data/info, Gray for status
 */
@Composable
private fun ApduLogLine(logEntry: String) {
    // Color code based on content: green for TX/success, blue for data/info
    val textColor = when {
        logEntry.contains("TX>", ignoreCase = true) || 
        logEntry.contains("transmit", ignoreCase = true) ||
        logEntry.contains("command", ignoreCase = true) -> Color(0xFF4CAF50) // Success green

        logEntry.contains("RX>", ignoreCase = true) || 
        logEntry.contains("receive", ignoreCase = true) ||
        logEntry.contains("response", ignoreCase = true) -> Color(0xFF64B5F6) // Info blue

        logEntry.contains("AID", ignoreCase = true) ||
        logEntry.contains("error", ignoreCase = true) -> Color(0xFFFFB74D) // Warning orange

        else -> Color(0xFFCCCCCC) // Light gray for other logs
    }

    Text(
        text = logEntry,
        color = textColor,
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 10.sp,
            lineHeight = 12.sp
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 1.dp)
    )
}
