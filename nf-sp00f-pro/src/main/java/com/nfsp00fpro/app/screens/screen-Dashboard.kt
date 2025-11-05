@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.nfsp00fpro.app.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nfsp00fpro.app.R
import com.nfsp00fpro.app.modules.CardSession

/**
 * Dashboard Screen - Main Application Hub
 * Displays hardware status, recent card readings, and system statistics
 * Reference UI from nf-sp00f33r project adapted for nf-sp00f-pro
 */
@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(context))
    val recentSessions by viewModel.recentSessions.collectAsState(initial = emptyList())
    val hardwareReady by viewModel.hardwareReady.collectAsState(initial = false)
    
    // Collect real hardware status from ViewModel
    val androidNfcStatus by viewModel.androidNfcStatus.collectAsState()
    val bluetoothStatus by viewModel.bluetoothStatus.collectAsState()
    val pn532BluetoothStatus by viewModel.pn532BluetoothStatus.collectAsState()
    val pn532UsbStatus by viewModel.pn532UsbStatus.collectAsState()
    val emvParserStatus by viewModel.emvParserStatus.collectAsState()

    if (!hardwareReady) {
        InitializingScreen()
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A))
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with branding and hardware score
        HeaderCard()

        // Stats cards row (multiple rows)
        StatsCardsRow(recentSessions.size)

        // Hardware status grid with real data
        HardwareStatusGrid(
            androidNfcStatus = androidNfcStatus,
            bluetoothStatus = bluetoothStatus,
            pn532BluetoothStatus = pn532BluetoothStatus,
            pn532UsbStatus = pn532UsbStatus,
            emvParserStatus = emvParserStatus
        )

        // Recent cards section
        RecentCardsSection(recentSessions)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun InitializingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = Color(0xFF4CAF50))
            Text(
                "Initializing Dashboard...",
                color = Color(0xFFFFFFFF),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun HeaderCard() {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Main Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121717)),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Image(
                    painter = painterResource(id = R.drawable.nfspoof3),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    alpha = 0.15f
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "NFC PhreaK BoX",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF4CAF50),
                        textAlign = TextAlign.Center
                    )

                    Text(
                        "RFiD TooLKiT",
                        style = MaterialTheme.typography.titleMedium.copy(
                            textDecoration = androidx.compose.ui.text.style.TextDecoration.Underline
                        ),
                        color = Color(0xFFFFFFFF),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hardware Score Display with Progress
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "System Status:",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFFFFFFF)
                            )
                            Text(
                                "Ready",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color(0xFF4CAF50)
                            )
                        }

                        LinearProgressIndicator(
                            progress = 1.0f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            color = Color(0xFF4CAF50),
                            trackColor = Color(0xFF333333)
                        )
                    }

                    // Status Message
                    Text(
                        "All hardware components detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFBBBBBB),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsCardsRow(cardCount: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // First row of stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Cards Scanned",
                value = cardCount.toString(),
                icon = Icons.Default.CreditCard,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                label = "Status",
                value = "Active",
                icon = Icons.Default.Devices,
                color = Color(0xFF4FC3F7),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                label = "Ready For",
                value = "Scan",
                icon = Icons.Default.TouchApp,
                color = Color(0xFFFFB74D),
                modifier = Modifier.weight(1f)
            )
        }

        // Second row of stats
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "APDU Commands",
                value = "0",
                icon = Icons.Default.Code,
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                label = "Card Brands",
                value = "0",
                icon = Icons.Default.Label,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f)
            )

            StatCard(
                label = "PN532 Status",
                value = "Scanning",
                icon = Icons.Default.BluetoothSearching,
                color = Color(0xFF00BCD4),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1419)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier
                    .size(24.dp)
                    .padding(bottom = 4.dp)
            )
            Text(
                value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = color,
                textAlign = TextAlign.Center
            )
            Text(
                label,
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HardwareStatusGrid(
    androidNfcStatus: String,
    bluetoothStatus: String,
    pn532BluetoothStatus: String,
    pn532UsbStatus: String,
    emvParserStatus: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F1F)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Hardware Components",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFFFFFFFF)
            )

            // Android Native Hardware
            HardwareComponentRow(
                title = "Android NFC Controller",
                status = androidNfcStatus,
                details = "NFC Type-A/B/F supported"
            )

            HardwareComponentRow(
                title = "Android Bluetooth Stack",
                status = bluetoothStatus,
                details = "BLE & Classic supported"
            )

            // PN532 External Hardware
            HardwareComponentRow(
                title = "PN532 NFC Module (Bluetooth)",
                status = pn532BluetoothStatus,
                details = "Looking for device..."
            )

            HardwareComponentRow(
                title = "PN532 NFC Module (USB)",
                status = pn532UsbStatus,
                details = "USB device not detected"
            )

            HardwareComponentRow(
                title = "EMV Parser Engine",
                status = emvParserStatus,
                details = "EMV L1 & L2 support"
            )
        }
    }
}

@Composable
private fun HardwareComponentRow(
    title: String,
    status: String,
    details: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = Color(0xFFFFFFFF)
            )
            if (details.isNotEmpty()) {
                Text(
                    details,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888),
                    maxLines = 2
                )
            }
        }

        Text(
            status,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = when {
                status.contains("Disconnected", ignoreCase = true) -> Color(0xFFF44336)
                status.contains("Not Available", ignoreCase = true) -> Color(0xFFF44336)
                status.contains("Unavailable", ignoreCase = true) -> Color(0xFFF44336)
                status.contains("Error", ignoreCase = true) -> Color(0xFFF44336)
                status.contains("Connected", ignoreCase = true) -> Color(0xFF2196F3)
                status.contains("Ready", ignoreCase = true) -> Color(0xFF2196F3)
                status.contains("Available", ignoreCase = true) -> Color(0xFF2196F3)
                status.contains("Detected", ignoreCase = true) -> Color(0xFF2196F3)
                status.contains("Searching", ignoreCase = true) -> Color(0xFFFFC107)
                status.contains("Scanning", ignoreCase = true) -> Color(0xFFFFC107)
                status.contains("Connecting", ignoreCase = true) -> Color(0xFFFFC107)
                else -> Color(0xFF888888)
            },
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun RecentCardsSection(sessions: List<CardSession>) {
    Column {
        Text(
            "Recent Cards",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold
            ),
            color = Color(0xFF4CAF50),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (sessions.isEmpty()) {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B1F1F)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        Icons.Default.CreditCard,
                        contentDescription = "No Cards",
                        tint = Color(0xFF666666),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        "No cards scanned yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color(0xFF888888),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Start scanning NFC cards to see them here",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF666666),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.heightIn(max = 300.dp)
            ) {
                items(sessions.take(5)) { session ->
                    CardSessionItem(session)
                }
            }
        }
    }
}

@Composable
private fun CardSessionItem(session: CardSession) {
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
                    "Session #${session.id}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFFFFFFFF)
                )
                Text(
                    "Status: ${session.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    "Type: ${if (session.isContactless) "Contactless" else "Contact"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF888888)
                )
            }

            Icon(
                Icons.Default.ArrowForward,
                contentDescription = "View Details",
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
