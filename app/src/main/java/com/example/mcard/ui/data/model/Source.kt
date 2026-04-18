package com.example.mcard.ui.data.model

enum class AuthType {
    NONE,
    API_KEY,
    BEARER_TOKEN,
    BASIC_AUTH,
    CUSTOM_HEADER
}

data class Source(
    val id: String,
    val name: String,
    val url: String = "",
    val isEnabled: Boolean = true,
    val authType: AuthType = AuthType.NONE,
    val authKey: String = "",
    val authValue: String = ""
)
