package com.nfsp00fpro.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
 * Splash Screen - nf-sp00f-pro Application Intro
 * Shows loading animation and app branding in a centered card
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
        // Background image (subtle, full screen with low opacity)
        Image(
            painter = painterResource(id = R.drawable.nfspoof3),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.08f
        )

        // Centered Card
        Card(
            modifier = Modifier
                .width(280.dp)
                .wrapContentHeight(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF121717)),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Image
                Image(
                    painter = painterResource(id = R.drawable.nfspoof3),
                    contentDescription = "nf-sp00f-pro Logo",
                    modifier = Modifier
                        .size(120.dp)
                        .padding(bottom = 24.dp),
                    contentScale = ContentScale.Fit
                )

                // App Title
                Text(
                    text = "nf-sp00f-pro",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF4CAF50),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Subtitle
                Text(
                    text = "RFiD TooLKiT",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFFFFFFFF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 28.dp)
                )

                // Loading Indicator
                CircularProgressIndicator(
                    color = Color(0xFF4CAF50),
                    modifier = Modifier
                        .size(40.dp)
                        .padding(bottom = 16.dp),
                    strokeWidth = 3.dp
                )

                // Loading Message
                Text(
                    text = "Initializing...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
