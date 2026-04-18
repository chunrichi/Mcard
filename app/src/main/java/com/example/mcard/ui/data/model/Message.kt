package com.example.mcard.ui.data.model

data class Message(
    val id: String,
    val title: String,
    val preview: String,
    val content: String,
    val timestamp: Long,
    val source: String,
    val url: String? = null,
    val isExpanded: Boolean = false
)
