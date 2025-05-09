package com.example.vistacuregrad.model

data class ChatHistoryItem(
    val inputText: String,
    val responseText: String,
    val timestamp: String
)
data class ChatHistoryResponse(
    val status: String,
    val message: String,
    val chatHistory: List<ChatHistoryItem>?
)