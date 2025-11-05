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
import com.nfsp00fpro.app.ui.NfSp00fIcons

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
                        NfSp00fIcons.Security(
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
                NavigationBarItem(
                    icon = {
                        NfSp00fIcons.Dashboard(
                            contentDescription = "Dashboard",
                            tint = if (selectedTab == 0) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.6f)
                        )
                    },
                    label = { 
                        Text(
                            "Dashboard",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedTab == 0) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.6f)
                        ) 
                    },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4CAF50),
                        selectedTextColor = Color(0xFF4CAF50),
                        unselectedIconColor = Color(0xFF4CAF50).copy(alpha = 0.6f),
                        unselectedTextColor = Color(0xFF4CAF50).copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )

                NavigationBarItem(
                    icon = {
                        NfSp00fIcons.Nfc(
                            contentDescription = "Card Read",
                            tint = if (selectedTab == 1) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.6f)
                        )
                    },
                    label = { 
                        Text(
                            "Card Read",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedTab == 1) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.6f)
                        ) 
                    },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4CAF50),
                        selectedTextColor = Color(0xFF4CAF50),
                        unselectedIconColor = Color(0xFF4CAF50).copy(alpha = 0.6f),
                        unselectedTextColor = Color(0xFF4CAF50).copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )

                NavigationBarItem(
                    icon = {
                        NfSp00fIcons.Storage(
                            contentDescription = "Database",
                            tint = if (selectedTab == 2) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.6f)
                        )
                    },
                    label = { 
                        Text(
                            "Database",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedTab == 2) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.6f)
                        ) 
                    },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4CAF50),
                        selectedTextColor = Color(0xFF4CAF50),
                        unselectedIconColor = Color(0xFF4CAF50).copy(alpha = 0.6f),
                        unselectedTextColor = Color(0xFF4CAF50).copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )

                NavigationBarItem(
                    icon = {
                        NfSp00fIcons.Analytics(
                            contentDescription = "Debug",
                            tint = if (selectedTab == 3) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.6f)
                        )
                    },
                    label = { 
                        Text(
                            "Debug",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selectedTab == 3) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.6f)
                        ) 
                    },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF4CAF50),
                        selectedTextColor = Color(0xFF4CAF50),
                        unselectedIconColor = Color(0xFF4CAF50).copy(alpha = 0.6f),
                        unselectedTextColor = Color(0xFF4CAF50).copy(alpha = 0.6f),
                        indicatorColor = Color.Transparent
                    )
                )
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
