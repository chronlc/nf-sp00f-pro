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

// Color definitions from reference
object CardReadingColors {
    val Background = Color(0xFF0A0A0A)
    val CardBackground = Color(0xFF1B1F1F)
    val TerminalBackground = Color(0xFF0F1419)
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF888888)
    val TextTertiary = Color(0xFF666666)
    val SuccessGreen = Color(0xFF4CAF50)
    val ErrorRed = Color(0xFFEF5350)
    val BrightRed = Color(0xFFEF5350)
    val WarningOrange = Color(0xFFFFB74D)
    val InfoBlue = Color(0xFF64B5F6)
    val LightBlue = Color(0xFF81D4FA)
    val Cyan = Color(0xFF26C6DA)
    val Purple = Color(0xFFCE93D8)
    val Orange = Color(0xFFFFB74D)
    val DangerRed = Color(0xFFEF5350)
    val BrightGreen = Color(0xFF66BB6A)
    val ButtonBackground = Color(0xFF252525)
    val BorderDark = Color(0xFF404040)
    val SafeBackground = Color(0xFF1B3A1B)
    val VulnerableBackground = Color(0xFF3A1B1B)
}

object CardReadingSpacing {
    val Tiny = 4.dp
    val Small = 8.dp
    val Medium = 16.dp
    val Large = 24.dp
    val Huge = 40.dp
}

object CardReadingRadius {
    val Small = 4.dp
    val Medium = 8.dp
    val Large = 12.dp
    val ExtraLarge = 24.dp
}

object CardReadingDimensions {
    val ButtonHeightSmall = 36.dp
    val ButtonHeightMedium = 48.dp
    val TerminalHeight = 300.dp
}

/**
 * ENHANCED Card Reading Screen - Reference UI with Modular Architecture
 * 
 * Features:
 * - Status header with data stats row
 * - Control panel with reader/protocol dropdowns  
 * - Advanced EMV settings
 * - APDU terminal with TX/RX logging
 * - EMV data display organized by category
 * - ROCA vulnerability analysis
 * - Active cards virtual display
 * 
 * All UI powered by real module data (EmvReader, EmvDatabase, ModMainDebug)
 */
@Composable
fun CardReadScreen(moduleManager: ModMainNfsp00f?) {
    val context = LocalContext.current
    val activity = LocalContext.current as? android.app.Activity
    val viewModel: CardReadViewModel = viewModel(factory = CardReadViewModel.Factory(context, moduleManager, activity))

    // Collect real state from ViewModel
    val cardData by viewModel.cardData.collectAsState()
    val isReading by viewModel.isReading.collectAsState()
    val readingProgress by viewModel.readingProgress.collectAsState()
    val hardwareStatus by viewModel.hardwareStatus.collectAsState()
    val recentReads by viewModel.recentReads.collectAsState(initial = emptyList())
    val showAdvancedSettings by viewModel.showAdvancedSettings.collectAsState()
    val selectedReader by viewModel.selectedReader.collectAsState()
    val selectedTechnology by viewModel.selectedTechnology.collectAsState()
    val apduLog by viewModel.apduLog.collectAsState()
    val parsedEmvFields by viewModel.parsedEmvFields.collectAsState()
    val rocaVulnerabilityStatus by viewModel.rocaVulnerabilityStatus.collectAsState()
    val isRocaVulnerable by viewModel.isRocaVulnerable.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CardReadingColors.Background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(CardReadingSpacing.Medium)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(CardReadingSpacing.Medium)
        ) {
            // Data-focused status header with stats
            StatusHeaderCard(
                hardwareStatus = hardwareStatus,
                cardCount = recentReads.size,
                apduCount = apduLog.size,
                nfcDetected = cardData != null,
                rocaVulnerable = isRocaVulnerable
            )

            // Compact control panel
            ControlPanelCard(
                cardData = cardData,
                isReading = isReading,
                readingProgress = readingProgress,
                selectedReader = selectedReader,
                selectedTechnology = selectedTechnology,
                onStartRead = { 
                    viewModel.startCardReading()
                    ModMainDebug.debugLog(
                        module = "CardReadScreen",
                        operation = "start_reading_initiated",
                        data = mapOf("timestamp" to System.currentTimeMillis().toString())
                    )
                },
                onSelectReader = { viewModel.selectReader(it) },
                onSelectTechnology = { viewModel.selectTechnology(it) }
            )

            // Advanced Settings Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { viewModel.toggleAdvancedSettings() }) {
                    Icon(
                        if (showAdvancedSettings) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = CardReadingColors.TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        if (showAdvancedSettings) "Hide Advanced" else "Show Advanced",
                        color = CardReadingColors.TextSecondary
                    )
                }
            }

            if (showAdvancedSettings) {
                AdvancedSettingsSection(viewModel)
            }

            // ROCA Vulnerability Status
            if (rocaVulnerabilityStatus != null) {
                RocaVulnerabilityStatusCard(
                    isVulnerable = isRocaVulnerable,
                    status = rocaVulnerabilityStatus ?: "Not checked"
                )
            }

            // Active Cards Section
            if (recentReads.isNotEmpty()) {
                ActiveCardsSection(recentReads = recentReads)
            }

            // Real-time EMV data display
            if (parsedEmvFields.isNotEmpty()) {
                EmvDataDisplaySection(fields = parsedEmvFields)
            }

            // Terminal-style APDU log
            ApduTerminalSection(apduLog = apduLog)

            Spacer(modifier = Modifier.height(CardReadingSpacing.Medium))
        }
    }
}

@Composable
private fun StatusHeaderCard(
    hardwareStatus: String,
    cardCount: Int,
    apduCount: Int,
    nfcDetected: Boolean,
    rocaVulnerable: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardReadingColors.CardBackground),
        shape = RoundedCornerShape(CardReadingRadius.Large),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(CardReadingSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(CardReadingSpacing.Medium)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "EMV CARD SCANNER",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = CardReadingColors.SuccessGreen
                )

                Surface(
                    color = if (nfcDetected) CardReadingColors.SafeBackground else CardReadingColors.VulnerableBackground,
                    shape = RoundedCornerShape(CardReadingRadius.ExtraLarge)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = CardReadingSpacing.Medium, vertical = CardReadingSpacing.Small / 2),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CardReadingSpacing.Small / 2)
                    ) {
                        Icon(
                            if (nfcDetected) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = "NFC Status",
                            tint = if (nfcDetected) CardReadingColors.SuccessGreen else CardReadingColors.BrightRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            if (nfcDetected) "Card Detected" else "Waiting",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = if (nfcDetected) CardReadingColors.SuccessGreen else CardReadingColors.BrightRed
                        )
                    }
                }
            }

            // Data Stats Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                DataStat("Cards", cardCount.toString(), CardReadingColors.LightBlue)
                DataStat("APDUs", apduCount.toString(), CardReadingColors.WarningOrange)
                DataStat("NFC", if (nfcDetected) "ACTIVE" else "IDLE", if (nfcDetected) CardReadingColors.BrightGreen else CardReadingColors.Purple)
                DataStat("ROCA", if (rocaVulnerable) "VULN" else "SAFE", if (rocaVulnerable) CardReadingColors.DangerRed else CardReadingColors.BrightGreen)
            }

            Text(
                hardwareStatus,
                style = MaterialTheme.typography.bodySmall,
                color = CardReadingColors.TextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun DataStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = color,
            textAlign = TextAlign.Center
        )
        Text(
            label,
            style = MaterialTheme.typography.bodySmall,
            color = CardReadingColors.TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ControlPanelCard(
    cardData: CardSession?,
    isReading: Boolean,
    readingProgress: Int,
    selectedReader: String?,
    selectedTechnology: String,
    onStartRead: () -> Unit,
    onSelectReader: (String) -> Unit,
    onSelectTechnology: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardReadingColors.CardBackground),
        shape = RoundedCornerShape(CardReadingRadius.Large),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(CardReadingSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(CardReadingSpacing.Large)
        ) {
            // Reader & Technology Selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CardReadingSpacing.Medium)
            ) {
                ReaderSelectionDropdown(
                    selectedReader = selectedReader,
                    onSelectReader = onSelectReader,
                    modifier = Modifier.weight(1f)
                )
                ProtocolSelectionDropdown(
                    selectedTechnology = selectedTechnology,
                    onSelectTechnology = onSelectTechnology,
                    modifier = Modifier.weight(1f)
                )
            }

            // Main Display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (cardData == null) {
                    NfSp00fIcons.Nfc(modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(CardReadingSpacing.Medium))
                    Text(
                        "Ready for Card",
                        style = MaterialTheme.typography.headlineSmall,
                        color = CardReadingColors.TextPrimary,
                        textAlign = TextAlign.Center
                    )
                } else {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Card Read",
                        tint = CardReadingColors.SuccessGreen,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(CardReadingSpacing.Medium))
                    Text(
                        "Card Read Successfully",
                        style = MaterialTheme.typography.headlineSmall,
                        color = CardReadingColors.SuccessGreen,
                        textAlign = TextAlign.Center
                    )
                }

                if (isReading) {
                    Spacer(modifier = Modifier.height(CardReadingSpacing.Large))
                    LinearProgressIndicator(
                        progress = readingProgress / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = CardReadingColors.SuccessGreen,
                        trackColor = Color(0xFF333333)
                    )
                    Text(
                        "$readingProgress%",
                        style = MaterialTheme.typography.bodySmall,
                        color = CardReadingColors.TextSecondary
                    )
                }
            }

            // Start Button
            val scanButtonColor = if (isReading) CardReadingColors.ErrorRed else CardReadingColors.SuccessGreen
            val scanButtonText = if (isReading) "STOP SCAN" else "START SCAN"

            Button(
                onClick = onStartRead,
                enabled = !isReading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CardReadingDimensions.ButtonHeightMedium),
                colors = ButtonDefaults.buttonColors(
                    containerColor = scanButtonColor,
                    disabledContainerColor = Color(0xFF666666)
                ),
                shape = RoundedCornerShape(CardReadingRadius.Medium)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(CardReadingSpacing.Small)
                ) {
                    Icon(
                        if (isReading) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = scanButtonText,
                        tint = CardReadingColors.Background,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        scanButtonText,
                        color = CardReadingColors.Background,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReaderSelectionDropdown(
    selectedReader: String?,
    onSelectReader: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(CardReadingSpacing.Small / 2)
    ) {
        Text(
            "READER",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = CardReadingColors.TextSecondary
        )

        var readerExpanded by remember { mutableStateOf(false) }
        val readers = listOf("PN532-Bluetooth", "Android NFC", "USB Reader")

        Box {
            OutlinedButton(
                onClick = { readerExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CardReadingDimensions.ButtonHeightSmall),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardReadingColors.ButtonBackground,
                    contentColor = CardReadingColors.TextPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardReadingColors.BorderDark)
            ) {
                Text(
                    selectedReader ?: "Select Reader",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            DropdownMenu(
                expanded = readerExpanded,
                onDismissRequest = { readerExpanded = false },
                modifier = Modifier.background(CardReadingColors.ButtonBackground)
            ) {
                readers.forEach { reader ->
                    DropdownMenuItem(
                        text = { Text(reader, color = CardReadingColors.TextPrimary) },
                        onClick = {
                            onSelectReader(reader)
                            readerExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ProtocolSelectionDropdown(
    selectedTechnology: String,
    onSelectTechnology: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(CardReadingSpacing.Small / 2)
    ) {
        Text(
            "PROTOCOL",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = CardReadingColors.TextSecondary
        )

        var techExpanded by remember { mutableStateOf(false) }
        val techs = listOf("EMV/ISO-DEP", "Auto-Detect")

        Box {
            OutlinedButton(
                onClick = { techExpanded = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CardReadingDimensions.ButtonHeightSmall),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = CardReadingColors.ButtonBackground,
                    contentColor = CardReadingColors.TextPrimary
                ),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardReadingColors.BorderDark)
            ) {
                Text(
                    selectedTechnology,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }

            DropdownMenu(
                expanded = techExpanded,
                onDismissRequest = { techExpanded = false },
                modifier = Modifier.background(CardReadingColors.ButtonBackground)
            ) {
                techs.forEach { tech ->
                    DropdownMenuItem(
                        text = { Text(tech, color = CardReadingColors.TextPrimary) },
                        onClick = {
                            onSelectTechnology(tech)
                            techExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun AdvancedSettingsSection(viewModel: CardReadViewModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardReadingColors.CardBackground),
        shape = RoundedCornerShape(CardReadingRadius.Large),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(CardReadingSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(CardReadingSpacing.Medium)
        ) {
            Text(
                "Advanced EMV Options",
                style = MaterialTheme.typography.titleSmall,
                color = CardReadingColors.Cyan
            )

            val advancedAmount by viewModel.advancedAmount.collectAsState()
            OutlinedTextField(
                value = advancedAmount,
                onValueChange = { viewModel.updateAdvancedAmount(it) },
                label = { Text("Transaction Amount (e.g. 1.00)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CardReadingColors.TextPrimary,
                    unfocusedTextColor = CardReadingColors.TextPrimary,
                    focusedLabelColor = CardReadingColors.Cyan,
                    unfocusedLabelColor = CardReadingColors.TextSecondary
                )
            )

            val advancedTtq by viewModel.advancedTtq.collectAsState()
            OutlinedTextField(
                value = advancedTtq,
                onValueChange = { viewModel.updateAdvancedTtq(it) },
                label = { Text("TTQ (hex, e.g. 36000000)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = CardReadingColors.TextPrimary,
                    unfocusedTextColor = CardReadingColors.TextPrimary,
                    focusedLabelColor = CardReadingColors.Cyan,
                    unfocusedLabelColor = CardReadingColors.TextSecondary
                )
            )

            val advancedTransactionType by viewModel.advancedTransactionType.collectAsState()
            var typeExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { typeExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CardReadingColors.TextPrimary)
                ) {
                    Text(advancedTransactionType)
                }
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    listOf("Purchase", "Refund", "Cash Advance").forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type) },
                            onClick = {
                                viewModel.updateAdvancedTransactionType(type)
                                typeExpanded = false
                            }
                        )
                    }
                }
            }

            val advancedCryptoSelect by viewModel.advancedCryptoSelect.collectAsState()
            var cryptoExpanded by remember { mutableStateOf(false) }
            Box {
                OutlinedButton(
                    onClick = { cryptoExpanded = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CardReadingColors.TextPrimary)
                ) {
                    Text(advancedCryptoSelect)
                }
                DropdownMenu(expanded = cryptoExpanded, onDismissRequest = { cryptoExpanded = false }) {
                    listOf("ARQC", "TC", "AAC").forEach { crypto ->
                        DropdownMenuItem(
                            text = { Text(crypto) },
                            onClick = {
                                viewModel.updateAdvancedCryptoSelect(crypto)
                                cryptoExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RocaVulnerabilityStatusCard(isVulnerable: Boolean, status: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isVulnerable) CardReadingColors.VulnerableBackground else CardReadingColors.SafeBackground
        ),
        shape = RoundedCornerShape(CardReadingRadius.Large),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(CardReadingSpacing.Large),
            horizontalArrangement = Arrangement.spacedBy(CardReadingSpacing.Medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isVulnerable) Icons.Default.Warning else Icons.Default.CheckCircle,
                contentDescription = "ROCA Status",
                tint = if (isVulnerable) CardReadingColors.BrightRed else CardReadingColors.SuccessGreen,
                modifier = Modifier.size(40.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "ROCA SECURITY CHECK",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = CardReadingColors.TextSecondary
                )
                Text(
                    status,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isVulnerable) CardReadingColors.BrightRed else CardReadingColors.SuccessGreen
                )
            }
        }
    }
}

@Composable
private fun ActiveCardsSection(recentReads: List<CardSession>) {
    Column(verticalArrangement = Arrangement.spacedBy(CardReadingSpacing.Medium)) {
        Text(
            "RECENT CARDS (${recentReads.size})",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = CardReadingColors.SuccessGreen
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(CardReadingSpacing.Large),
            contentPadding = PaddingValues(horizontal = CardReadingSpacing.Tiny)
        ) {
            items(recentReads.take(5)) { session ->
                RecentReadCard(session = session)
            }
        }

        if (recentReads.size > 5) {
            Text(
                "${recentReads.size} cards total - scroll to view all",
                style = MaterialTheme.typography.bodySmall,
                color = CardReadingColors.TextTertiary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun RecentReadCard(session: CardSession) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .heightIn(min = 140.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252020)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                session.sessionId.takeLast(6),
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = CardReadingColors.TextPrimary
            )

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = null,
                        tint = CardReadingColors.SuccessGreen,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        if (session.isContactless) "NFC" else "Contact",
                        style = MaterialTheme.typography.labelSmall,
                        color = CardReadingColors.TextSecondary
                    )
                }

                val statusColor = when (session.status) {
                    "SUCCESS" -> CardReadingColors.SuccessGreen
                    "PENDING" -> CardReadingColors.WarningOrange
                    else -> CardReadingColors.ErrorRed
                }

                Surface(
                    color = statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        session.status,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            Text(
                SimpleDateFormat("HH:mm").format(Date(session.timestamp)),
                style = MaterialTheme.typography.labelSmall,
                color = CardReadingColors.TextTertiary
            )
        }
    }
}

@Composable
private fun ApduTerminalSection(apduLog: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardReadingColors.CardBackground),
        shape = RoundedCornerShape(CardReadingRadius.Large),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(CardReadingSpacing.Large),
            verticalArrangement = Arrangement.spacedBy(CardReadingSpacing.Medium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "APDU TERMINAL",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                    color = CardReadingColors.SuccessGreen
                )
                Text(
                    "${apduLog.size} cmds",
                    style = MaterialTheme.typography.bodySmall,
                    color = CardReadingColors.TextTertiary
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(CardReadingDimensions.TerminalHeight)
                    .background(CardReadingColors.TerminalBackground, RoundedCornerShape(CardReadingRadius.Large))
                    .padding(CardReadingSpacing.Medium)
            ) {
                if (apduLog.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            ">>> Waiting for card communication...",
                            color = CardReadingColors.SuccessGreen,
                            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(CardReadingSpacing.Small / 2)
                    ) {
                        items(apduLog.takeLast(15)) { log ->
                            ApduLogLine(log)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ApduLogLine(log: String) {
    val isTx = log.contains("TX>") || log.contains("CMD")
    val color = if (isTx) CardReadingColors.SuccessGreen else CardReadingColors.InfoBlue

    Text(
        log,
        color = color,
        style = MaterialTheme.typography.bodySmall.copy(
            fontFamily = FontFamily.Monospace,
            fontSize = 9.sp
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EmvDataDisplaySection(fields: Map<String, String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardReadingColors.CardBackground),
        shape = RoundedCornerShape(CardReadingRadius.Large),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(CardReadingSpacing.Large)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "EMV DATA EXTRACTED",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = CardReadingColors.SuccessGreen
                    )
                )

                Text(
                    "${fields.size} fields",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = CardReadingColors.TextTertiary
                    )
                )
            }

            Spacer(modifier = Modifier.height(CardReadingSpacing.Medium))

            LazyColumn(
                modifier = Modifier.heightIn(max = 300.dp),
                verticalArrangement = Arrangement.spacedBy(CardReadingSpacing.Tiny)
            ) {
                items(fields.toList()) { (key, value) ->
                    EmvFieldRow(key = key, value = value)
                }
            }
        }
    }
}

@Composable
private fun EmvFieldRow(key: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = key.uppercase().replace("_", " "),
            style = MaterialTheme.typography.bodySmall.copy(
                color = CardReadingColors.TextSecondary,
                fontWeight = FontWeight.Medium
            ),
            modifier = Modifier.weight(1.5f)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall.copy(
                color = CardReadingColors.SuccessGreen,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}