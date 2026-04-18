package com.example.mcard.ui.navigation

sealed class Screen(val route: String) {
    data object MessageList : Screen("message_list")
    data object SettingsDashboard : Screen("settings_dashboard")
    data object SourceConfig : Screen("source_config")
}
