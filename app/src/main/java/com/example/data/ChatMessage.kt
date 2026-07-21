package com.example.data

data class ChatMessage(
    val id: String = "",
    val role: String = "user", // "user" or "assistant"
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val imageBase64: String? = null
)
