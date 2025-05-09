package com.example.vistacuregrad.model

// ChatRequest.kt
data class ChatRequest(
    val inputText: String
)

// ChatResponse.kt
data class ChatResponse(
    val status: String,
    val message: String,
    val response: String? // nullable, only present on success
)

// ChatHistoryItem.kt
data class ChatHistoryItem(
    val inputText: String,
    val responseText: String,
    val timestamp: String
)

// ChatHistoryResponse.kt
data class ChatHistoryResponse(
    val status: String,
    val message: String,
    val chatHistory: List<ChatHistoryItem>?
)