package com.example.mcard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import com.example.mcard.ui.data.local.MessagesPreferences
import com.example.mcard.ui.data.local.SourcesPreferences
import com.example.mcard.ui.data.local.SyncPreferences
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mcard.ui.navigation.Screen
import com.example.mcard.ui.screens.MessageListScreen
import com.example.mcard.ui.screens.SettingsDashboardScreen
import com.example.mcard.ui.screens.SourceConfigScreen
import com.example.mcard.ui.theme.McardTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            McardTheme {
                McardApp()
            }
        }
    }
}

@Composable
fun McardApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val syncPreferences = remember { SyncPreferences(context.applicationContext) }
    val sourcesPreferences = remember { SourcesPreferences(context.applicationContext) }
    val messagesPreferences = remember { MessagesPreferences(context.applicationContext) }

    NavHost(
        navController = navController,
        startDestination = Screen.MessageList.route,
        modifier = Modifier.fillMaxSize()
    ) {
        composable(Screen.MessageList.route) {
            MessageListScreen(
                onNavigateToConfig = {
                    navController.navigate(Screen.SettingsDashboard.route)
                },
                syncPreferences = syncPreferences,
                sourcesPreferences = sourcesPreferences,
                messagesPreferences = messagesPreferences
            )
        }
        composable(Screen.SettingsDashboard.route) {
            SettingsDashboardScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToSourceConfig = {
                    navController.navigate(Screen.SourceConfig.route)
                },
                syncPreferences = syncPreferences,
                sourcesPreferences = sourcesPreferences
            )
        }
        composable(Screen.SourceConfig.route) {
            SourceConfigScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                syncPreferences = syncPreferences
            )
        }
    }
}
