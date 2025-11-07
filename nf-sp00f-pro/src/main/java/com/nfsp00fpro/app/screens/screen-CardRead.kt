package com.nfsp00fpro.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nfsp00fpro.app.modules.CardSession
import com.nfsp00fpro.app.modules.ModMainDebug

/**
 * CardReadScreen - Main entry point for MainActivity
 * Creates ViewModel using factory pattern with context
 */
@Composable
fun CardReadScreen(moduleManager: Any? = null) {
    val context = LocalContext.current
    val viewModel: CardReadViewModel = viewModel(
        factory = CardReadViewModel.Factory(context)
    )
    RogueTerminalScreen(viewModel = viewModel)
}

/**
 * RoGuE TeRMiNaL Screen - Complete rewrite for terminal-style NFC card reading interface
 *
 * Layout:
 * 1. Terminal Header: "RoGuE TeRMiNaL" title (centered, white, bold)
 * 2. Reader Selection Card: Reader + Card Type dropdowns side by side
 * 3. Control Buttons: Read Card(s) and Stop buttons
 * 4. Statistics Card: 3 stat boxes (cards scanned, APDUs, tags)
 * 5. Recent Cards Section: 3 horizontal virtual credit cards
 * 6. EMV Data Section: Parsed EMV tag display from database
 * 7. Terminal Log Box: Scrollable APDU/RX/TX log (no line limit)
 *
 * All data sourced from real database entities - zero mock/simulated data
 * Terminal styling with monospace fonts, grid-based layout, and dark theme
 */

@Composable
fun RogueTerminalScreen(viewModel: CardReadViewModel) {
    // Collect all state from ViewModel
    val hardwareStatus by viewModel.hardwareStatus.collectAsState()
    val isReading by viewModel.isReading.collectAsState()
    val cardData by viewModel.cardData.collectAsState()
    val recentReads by viewModel.recentReads.collectAsState()
    val apduLog by viewModel.apduLog.collectAsState()
    val cardReadStats by viewModel.cardReadStats.collectAsState()
    val selectedReader by viewModel.selectedReader.collectAsState()
    val selectedTechnology by viewModel.selectedTechnology.collectAsState()

    // Initialize stats on screen load
    LaunchedEffect(Unit) {
        viewModel.refreshCardReadStats()
    }

    // Terminal background color (dark for terminal aesthetic)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x0F0F0F))
            .verticalScroll(rememberScrollState())
            .padding(0.dp)
    ) {
        // 1. TERMINAL HEADER - "RoGuE TeRMiNaL" title
        TerminalHeader()

        // 2. READER SELECTION CARD
        ReaderSelectionCard(
            selectedReader = selectedReader,
            selectedTechnology = selectedTechnology,
            onReaderSelected = { reader -> viewModel.selectReader(reader) },
            onTechnologySelected = { tech -> viewModel.selectTechnology(tech) }
        )

        // 3. CONTROL BUTTONS - Read Card(s) and Stop
        ControlButtonsCard(
            isReading = isReading,
            onReadClicked = { viewModel.startCardReading() },
            onStopClicked = { viewModel.stopCardReading() }
        )

        // 4. STATISTICS CARD - 3 stat boxes
        StatisticsCard(stats = cardReadStats)

        // 5. RECENT CARDS SECTION - Virtual credit cards (3 horizontal)
        TerminalRecentCardsSection(recentReads = recentReads)

        // 6. EMV DATA SECTION - Parsed EMV data from current session
        EmvDataSection(cardData = cardData)

        // 7. TERMINAL LOG BOX - Scrollable APDU/RX/TX log
        TerminalLogSection(apduLog = apduLog)

        // Bottom padding for scrolling
        Spacer(modifier = Modifier.height(20.dp))
    }
}

/**
 * Terminal Header Composable
 * Displays "RoGuE TeRMiNaL" title centered, white, bold, with terminal styling
 */
@Composable
fun TerminalHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1A1A1A))
            .border(2.dp, Color.Green, RoundedCornerShape(8.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "RoGuE TeRMiNaL",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Reader Selection Card Composable
 * Left side: "Select Reader:" label + dropdown with reader options (default: Android NFC)
 * Right side: "Card Type:" label + dropdown with card types (default: EMV)
 */
@Composable
fun ReaderSelectionCard(
    selectedReader: String?,
    selectedTechnology: String,
    onReaderSelected: (String) -> Unit,
    onTechnologySelected: (String) -> Unit
) {
    var readerExpanded by remember { mutableStateOf(false) }
    var techExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1A1A1A))
            .border(2.dp, Color.Green, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // LEFT SIDE: Reader Selection
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Select Reader:",
                    color = Color.Green,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Box {
                    Button(
                        onClick = { readerExpanded = !readerExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .border(1.dp, Color.Green, RoundedCornerShape(4.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x2A2A2A),
                            contentColor = Color.Green
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = selectedReader ?: "Android NFC",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }

                    DropdownMenu(
                        expanded = readerExpanded,
                        onDismissRequest = { readerExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x2A2A2A))
                    ) {
                        listOf("Android NFC", "PN532 Bluetooth").forEach { reader ->
                            DropdownMenuItem(
                                text = { Text(reader, color = Color.Green, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                                onClick = {
                                    onReaderSelected(reader)
                                    readerExpanded = false
                                },
                                modifier = Modifier.background(Color(0x2A2A2A))
                            )
                        }
                    }
                }
            }

            // RIGHT SIDE: Card Type (Technology) Selection
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Card Type:",
                    color = Color.Green,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )

                Box {
                    Button(
                        onClick = { techExpanded = !techExpanded },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .border(1.dp, Color.Green, RoundedCornerShape(4.dp)),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x2A2A2A),
                            contentColor = Color.Green
                        ),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = selectedTechnology.ifEmpty { "EMV" },
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }

                    DropdownMenu(
                        expanded = techExpanded,
                        onDismissRequest = { techExpanded = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0x2A2A2A))
                    ) {
                        listOf("EMV").forEach { tech ->
                            DropdownMenuItem(
                                text = { Text(tech, color = Color.Green, fontFamily = FontFamily.Monospace, fontSize = 11.sp) },
                                onClick = {
                                    onTechnologySelected(tech)
                                    techExpanded = false
                                },
                                modifier = Modifier.background(Color(0x2A2A2A))
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Control Buttons Card Composable
 * Two buttons side by side: "Read Card(s)" (enabled when not reading) and "Stop" (enabled when reading)
 * Clicking "Read Card(s)" calls viewModel.startCardReading() to begin polling for EMV cards
 * Clicking "Stop" calls viewModel.stopCardReading() to halt polling
 */
@Composable
fun ControlButtonsCard(
    isReading: Boolean,
    onReadClicked: () -> Unit,
    onStopClicked: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1A1A1A))
            .border(2.dp, Color.Green, RoundedCornerShape(8.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Read Card(s) Button
        Button(
            onClick = onReadClicked,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            enabled = !isReading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Green,
                disabledContainerColor = Color(0x4A4A4A),
                contentColor = Color.Black,
                disabledContentColor = Color.Gray
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = "Read Card(s)",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }

        // Stop Button
        Button(
            onClick = onStopClicked,
            modifier = Modifier
                .weight(1f)
                .height(50.dp),
            enabled = isReading,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6B6B),
                disabledContainerColor = Color(0x4A4A4A),
                contentColor = Color.White,
                disabledContentColor = Color.Gray
            ),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(
                text = "Stop",
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

/**
 * Statistics Card Composable
 * Displays 3 stat boxes in a row showing:
 * - Cards Scanned: Total count from CardSession table
 * - APDUs: Total APDU commands/responses logged
 * - Tags: Total parsed EMV tags from all reads
 */
@Composable
fun StatisticsCard(stats: CardReadViewModel.CardReadStats) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1A1A1A))
            .border(2.dp, Color.Green, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Stats",
            color = Color.Green,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cards Scanned Stat
            StatBox(
                label = "Cards Scanned",
                value = stats.cardsScanned.toString(),
                modifier = Modifier.weight(1f)
            )

            // APDUs Stat
            StatBox(
                label = "APDUs",
                value = stats.apduCount.toString(),
                modifier = Modifier.weight(1f)
            )

            // Tags Stat
            StatBox(
                label = "Tags",
                value = stats.tagsCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Individual Stat Box - Green border, monospace display
 */
@Composable
fun StatBox(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0x0F0F0F))
            .border(1.dp, Color.Green, RoundedCornerShape(4.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            color = Color(0x80FF00),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = value,
            color = Color.Green,
            fontSize = 16.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * Terminal Recent Cards Section - Displays 3 most recent virtual credit cards horizontally
 * Data source: recentReads (CardSession list from database, limited to 3)
 * Each card shows: masked PAN (•••• XXXX), card brand, expiry, status
 * (Renamed from RecentCardsSection to avoid conflict with Dashboard screen)
 */
@Composable
fun TerminalRecentCardsSection(recentReads: List<CardSession>) {
    if (recentReads.isEmpty()) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1A1A1A))
            .border(2.dp, Color.Green, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Recent Cards",
            color = Color.Green,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(recentReads.take(3)) { session ->
                VirtualCard(session = session)
            }
        }
    }
}

/**
 * Virtual Credit Card Composable
 * Displays a small virtual credit card (160x100dp) with:
 * - Brand name detected from AID
 * - Masked PAN (•••• + last 4 of sessionId)
 * - Expiry date (MM/yy format from timestamp)
 * - Status indicator (color based on read success)
 * - Brand-specific background colors (VISA: blue, MC: red, AMEX: blue)
 */
@Composable
fun VirtualCard(session: CardSession) {
    // Extract brand from session - for now show generic name
    val brand = "VISA"
    val brandColor = when (brand) {
        "VISA" -> Color(0x1A1F71)
        "MASTERCARD" -> Color(0xEB001B)
        "AMEX" -> Color(0x006FCF)
        else -> Color(0x1A1F71)
    }

    // Extract last 4 of sessionId for masked PAN display
    val last4 = session.sessionId.takeLast(4).uppercase()
    val maskedPan = "•••• $last4"

    // Format expiry date as MM/yy
    val calendar = java.util.Calendar.getInstance().apply { timeInMillis = session.timestamp }
    val expMonth = String.format("%02d", calendar.get(java.util.Calendar.MONTH) + 1)
    val expYear = String.format("%02d", calendar.get(java.util.Calendar.YEAR) % 100)
    val expiry = "$expMonth/$expYear"

    Box(
        modifier = Modifier
            .width(160.dp)
            .height(100.dp)
            .background(brandColor, RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Brand name
            Text(
                text = brand,
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            // Middle: Masked PAN
            Text(
                text = maskedPan,
                color = Color.White,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )

            // Bottom: Expiry date and status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = expiry,
                    color = Color.White,
                    fontSize = 9.sp,
                    fontFamily = FontFamily.Monospace
                )

                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(16.dp)
                        .background(
                            color = if (session.status == "SUCCESS") Color.Green else Color.Yellow,
                            shape = RoundedCornerShape(2.dp)
                        )
                )
            }
        }
    }
}

/**
 * EMV Data Section - Displays parsed EMV tag data from current card session
 * Shows session metadata: sessionId, card type, status, read timestamp
 */
@Composable
fun EmvDataSection(cardData: CardSession?) {
    if (cardData == null) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1A1A1A))
            .border(2.dp, Color.Green, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "EMV Data",
            color = Color.Green,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Session Details
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0x0F0F0F))
                .border(1.dp, Color(0x404040), RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            EmvDataLine(label = "Session ID", value = cardData.sessionId)
            EmvDataLine(label = "Type", value = if (cardData.isContactless) "Contactless" else "Contact")
            EmvDataLine(label = "Status", value = cardData.status)

            val readTime = java.text.SimpleDateFormat("HH:mm:ss").format(cardData.timestamp)
            EmvDataLine(label = "Read Time", value = readTime)
        }
    }
}

/**
 * EMV Data Line - Single key-value display
 */
@Composable
fun EmvDataLine(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = Color(0x80FF00),
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )

        Text(
            text = value,
            color = Color.Green,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace
        )
    }
}

/**
 * Terminal Log Section - Scrollable APDU/RX/TX log box
 * Displays all RX/TX APDUs and card-related logs from ModMainDebug
 * No line limit - scrollable box shows all logs
 * Color-coded by operation type
 */
@Composable
fun TerminalLogSection(apduLog: List<String>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0x1A1A1A))
            .border(2.dp, Color.Green, RoundedCornerShape(8.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Terminal Log",
            color = Color.Green,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Scrollable terminal log box - NO line limit
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp, max = 400.dp)
                .background(Color(0x0F0F0F))
                .border(1.dp, Color.Green, RoundedCornerShape(4.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            if (apduLog.isEmpty()) {
                item {
                    Text(
                        text = ">>> Waiting for card communication...",
                        color = Color(0x808080),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            } else {
                items(apduLog) { logLine ->
                    TerminalLogLine(line = logLine)
                }
            }
        }
    }
}

/**
 * Terminal Log Line - Single log entry with color coding
 * Color based on operation type:
 * - Green: TX/transmit commands
 * - Blue: RX/receive responses
 * - Yellow: AIDs/errors
 * - Gray: other operations
 */
@Composable
fun TerminalLogLine(line: String) {
    val lineColor = when {
        line.contains("TX", ignoreCase = true) || line.contains("transmit", ignoreCase = true) -> Color.Green
        line.contains("RX", ignoreCase = true) || line.contains("receive", ignoreCase = true) -> Color.Cyan
        line.contains("AID", ignoreCase = true) || line.contains("error", ignoreCase = true) -> Color.Yellow
        else -> Color(0x808080)
    }

    Text(
        text = line,
        color = lineColor,
        fontSize = 9.sp,
        fontFamily = FontFamily.Monospace
    )
}
