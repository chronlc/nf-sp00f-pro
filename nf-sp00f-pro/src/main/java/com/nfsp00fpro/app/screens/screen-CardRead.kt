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
import com.nfsp00fpro.app.ui.NfSp00fIcons
import java.text.SimpleDateFormat
import java.util.Date

/**
 * Card Read Screen - NFC Card Reading Interface
 * Displays real-time card reading with APDU logs and history
 */
@Composable
fun CardReadScreen() {
    val context = LocalContext.current
    val viewModel: CardReadViewModel = viewModel(factory = CardReadViewModel.Factory(context))

    // Collect real state from ViewModel
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
        // Header Card with status
        HeaderStatusCard(hardwareStatus = hardwareStatus)

        // Large card reader panel
        CardReaderPanel(
            cardData = cardData,
            isReading = isReading,
            readingProgress = readingProgress,
            onStartRead = { viewModel.startCardReading() }
        )

        // Card details when card is read
        if (cardData != null) {
            CardDetailsPanel(cardData = cardData!!)
        }

        // Recent reads list
        RecentReadsSection(recentReads = recentReads)

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun HeaderStatusCard(hardwareStatus: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF121717)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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
                Spacer(modifier = Modifier.height(24.dp))
                CircularProgressIndicator(
                    progress = readingProgress / 100f,
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 3.dp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "$readingProgress%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF4CAF50)
                )
            } else {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onStartRead,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Read", tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Start Reading", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

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

            CardDetailRow(label = "Session ID", value = cardData.sessionId)
            CardDetailRow(label = "Type", value = if (cardData.isContactless) "Contactless NFC" else "Contact")
            CardDetailRow(label = "Status", value = cardData.status)
            CardDetailRow(label = "Time", value = SimpleDateFormat("HH:mm:ss").format(Date(cardData.timestamp)))
        }
    }
}

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
            verticalArrangement = Arrangement.spacedBy(8.dp)
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

@Composable
private fun RecentReadItem(session: CardSession) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0A0A)),
        shape = RoundedCornerShape(6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    session.sessionId.takeLast(8),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFFFFFFF),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    SimpleDateFormat("HH:mm:ss").format(Date(session.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888)
                )
            }

            Text(
                session.status,
                style = MaterialTheme.typography.labelSmall,
                color = if (session.status == "SUCCESS") Color(0xFF4CAF50) else Color(0xFFFF6B6B)
            )
        }
    }
}
