package com.example.data

data class ChatSession(
    val id: String = "",
    val title: String = "New Chat",
    val mode: String = "Normal", // Normal, Girlfriend, Writer, Debugger, Summarizer, Team
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val messageCount: Int = 0,
    val isPinned: Boolean = false,
    val isFavorite: Boolean = false
)
