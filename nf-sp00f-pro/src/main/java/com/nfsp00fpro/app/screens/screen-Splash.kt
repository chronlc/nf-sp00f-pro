package com.nfsp00fpro.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
        // Background image
        Image(
            painter = painterResource(id = R.drawable.nfspoof3),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.15f
        )

        Column(
            modifier = Modifier
                .wrapContentWidth()
                .wrapContentHeight()
                .padding(28.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
                    .padding(28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo Image
                Image(
                    painter = painterResource(id = R.drawable.nfspoof3),
                    contentDescription = "nf-sp00f-pro Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(bottom = 16.dp),
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
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Subtitle
                Text(
                    text = "RFiD TooLKiT",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Color(0xFFFFFFFF),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 20.dp)
                )

                // Loading Indicator
                CircularProgressIndicator(
                    color = Color(0xFF4CAF50),
                    modifier = Modifier
                        .size(36.dp)
                        .padding(bottom = 12.dp),
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
