package com.nfsp00fpro.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.nfsp00fpro.app.R
import kotlinx.coroutines.delay

/**
 * Splash Screen - NFC PhreaK BoX Application Intro
 * Shows loading animation and app branding
 * Auto-transitions to dashboard after initialization
 */
@Composable
fun SplashScreen(onNavigateToDashboard: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(3000) // 3 second splash duration
        onNavigateToDashboard()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo/Background Image
            Image(
                painter = painterResource(id = R.drawable.nfspoof3),
                contentDescription = "NFC PhreaK BoX Logo",
                modifier = Modifier
                    .size(200.dp)
                    .padding(bottom = 32.dp),
                contentScale = ContentScale.Fit
            )

            // App Title
            Text(
                text = "NFC PhreaK BoX",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF4CAF50),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Subtitle
            Text(
                text = "RFiD TooLKiT",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = Color(0xFFFFFFFF),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            // Loading Indicator
            CircularProgressIndicator(
                color = Color(0xFF4CAF50),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Loading Message
            Text(
                text = "Initializing Modules...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF888888),
                textAlign = TextAlign.Center
            )
        }
    }
}
