package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ChatMessage
import com.example.data.ChatSession
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*

class MainViewModel : ViewModel() {
    
    private val _chatSessions = MutableStateFlow<List<ChatSession>>(emptyList())
    val chatSessions: StateFlow<List<ChatSession>> = _chatSessions
    
    private val _currentMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val currentMessages: StateFlow<List<ChatMessage>> = _currentMessages
    
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    
    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail
    
    private val _userDisplayName = MutableStateFlow<String?>(null)
    val userDisplayName: StateFlow<String?> = _userDisplayName
    
    private val _activeSessionId = MutableStateFlow<String?>(null)
    val activeSessionId: StateFlow<String?> = _activeSessionId
    
    init {
        loadSessions()
    }
    
    fun loadSessions() {
        viewModelScope.launch {
            _chatSessions.value = generateMockSessions()
        }
    }
    
    fun createNewSession(mode: String = "Normal") {
        viewModelScope.launch {
            val newSession = ChatSession(
                id = UUID.randomUUID().toString(),
                title = "New Chat - $mode",
                mode = mode,
                createdAt = System.currentTimeMillis()
            )
            _chatSessions.value = _chatSessions.value + newSession
            _activeSessionId.value = newSession.id
            _currentMessages.value = emptyList()
        }
    }
    
    fun selectSession(sessionId: String) {
        viewModelScope.launch {
            _activeSessionId.value = sessionId
            _currentMessages.value = generateMockMessages(sessionId)
        }
    }
    
    fun sendMessage(content: String, imageBase64: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            
            val userMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = "user",
                content = content,
                imageBase64 = imageBase64
            )
            
            _currentMessages.value = _currentMessages.value + userMessage
            
            // Simulate AI response delay
            kotlinx.coroutines.delay(1000)
            
            val assistantMessage = ChatMessage(
                id = UUID.randomUUID().toString(),
                role = "assistant",
                content = "This is a simulated response to: $content"
            )
            
            _currentMessages.value = _currentMessages.value + assistantMessage
            _isLoading.value = false
        }
    }
    
    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            _chatSessions.value = _chatSessions.value.filter { it.id != sessionId }
            if (_activeSessionId.value == sessionId) {
                _activeSessionId.value = null
                _currentMessages.value = emptyList()
            }
        }
    }
    
    fun renameSession(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            _chatSessions.value = _chatSessions.value.map { session ->
                if (session.id == sessionId) {
                    session.copy(title = newTitle)
                } else {
                    session
                }
            }
        }
    }
    
    fun togglePin(sessionId: String) {
        viewModelScope.launch {
            _chatSessions.value = _chatSessions.value.map { session ->
                if (session.id == sessionId) {
                    session.copy(isPinned = !session.isPinned)
                } else {
                    session
                }
            }
        }
    }
    
    fun toggleFavorite(sessionId: String) {
        viewModelScope.launch {
            _chatSessions.value = _chatSessions.value.map { session ->
                if (session.id == sessionId) {
                    session.copy(isFavorite = !session.isFavorite)
                } else {
                    session
                }
            }
        }
    }
    
    fun login(email: String, displayName: String) {
        _userEmail.value = email
        _userDisplayName.value = displayName
    }
    
    fun logout() {
        _userEmail.value = null
        _userDisplayName.value = null
    }
    
    private fun generateMockSessions(): List<ChatSession> {
        return listOf(
            ChatSession(
                id = UUID.randomUUID().toString(),
                title = "প্রোগ্রামিং সাহায্য",
                mode = "Debugger",
                messageCount = 12
            ),
            ChatSession(
                id = UUID.randomUUID().toString(),
                title = "গল্প লেখা",
                mode = "Writer",
                messageCount = 8
            ),
            ChatSession(
                id = UUID.randomUUID().toString(),
                title = "ফ্রেন্ডলি চ্যাট",
                mode = "Girlfriend",
                messageCount = 25,
                isFavorite = true
            )
        )
    }
    
    private fun generateMockMessages(sessionId: String): List<ChatMessage> {
        return listOf(
            ChatMessage(
                id = UUID.randomUUID().toString(),
                role = "user",
                content = "হ্যালো, আজ কেমন আছো?"
            ),
            ChatMessage(
                id = UUID.randomUUID().toString(),
                role = "assistant",
                content = "আমি ভালো আছি! তুমি কেমন আছো? আমি তোমাকে সাহায্য করতে পারি কিছু করতে?"
            )
        )
    }
}
