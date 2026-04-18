package com.example.mcard.ui.data.network

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String? = null
)

data class MessageDto(
    val id: String,
    val title: String,
    val preview: String,
    val content: String,
    val timestamp: Long,
    val source: String
)

data class SourceDto(
    val id: String,
    val name: String,
    val url: String,
    val isEnabled: Boolean,
    val authType: String,
    val authKey: String,
    val authValue: String
)
