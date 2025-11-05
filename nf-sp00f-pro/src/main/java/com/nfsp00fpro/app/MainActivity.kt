package com.nfsp00fpro.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import com.nfsp00fpro.app.modules.ModMainNfsp00f
import com.nfsp00fpro.app.screens.DashboardScreen
import com.nfsp00fpro.app.screens.SplashScreen

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    private lateinit var moduleManager: ModMainNfsp00f
    private var showSplash = mutableStateOf(true)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Use edge-to-edge display
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.statusBarColor = android.graphics.Color.BLACK
        window.navigationBarColor = android.graphics.Color.BLACK
        
        // Initialize module manager
        moduleManager = ModMainNfsp00f(this)
        moduleManager.initialize()
        
        setContent {
            MaterialTheme {
                if (showSplash.value) {
                    SplashScreen(onNavigateToDashboard = {
                        showSplash.value = false
                    })
                } else {
                    NfSp00fProApp()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        moduleManager.shutdown()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfSp00fProApp() {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = "Security",
                            tint = Color(0xFF4CAF50)
                        )
                        Column {
                            Text(
                                "NFC PhreaK BoX",
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                "RFiD TooLKiT",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF4CAF50).copy(alpha = 0.7f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.Black) {
                val items = listOf(
                    "Dashboard" to Icons.Default.Dashboard,
                    "Card Read" to Icons.Default.Nfc,
                    "Database" to Icons.Default.Storage,
                    "Debug" to Icons.Default.BugReport
                )

                items.forEachIndexed { index, (label, icon) ->
                    NavigationBarItem(
                        icon = {
                            Icon(
                                icon,
                                contentDescription = label,
                                tint = if (selectedTab == index) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.6f)
                            )
                        },
                        label = { 
                            Text(
                                label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selectedTab == index) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.6f)
                            ) 
                        },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFF4CAF50),
                            selectedTextColor = Color(0xFF4CAF50),
                            unselectedIconColor = Color(0xFF4CAF50).copy(alpha = 0.6f),
                            unselectedTextColor = Color(0xFF4CAF50).copy(alpha = 0.6f),
                            indicatorColor = Color.Transparent
                        )
                    )
                }
            }
        },
        containerColor = Color(0xFF0A0A0A)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0A0A))
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> DashboardScreen()
                1 -> PlaceholderScreen("Card Reading")
                2 -> PlaceholderScreen("Database")
                3 -> PlaceholderScreen("Debug Console")
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
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
            Icon(
                Icons.Default.Construction,
                contentDescription = null,
                tint = Color(0xFF4CAF50),
                modifier = Modifier.size(64.dp)
            )
            Text(
                "$title",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFFFFFFF),
                fontWeight = FontWeight.Bold
            )
            Text(
                "Coming Soon",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF888888)
            )
        }
    }
}
