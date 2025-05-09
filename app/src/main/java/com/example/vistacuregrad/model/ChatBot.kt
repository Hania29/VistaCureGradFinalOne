package com.example.vistacuregrad.model

// ChatRequest.kt
data class ChatRequest(
    val inputText: String
)

// ChatResponse.kt
data class ChatResponse(
    val status: String,
    val message: String,
    val response: String? // nullable for error cases
)