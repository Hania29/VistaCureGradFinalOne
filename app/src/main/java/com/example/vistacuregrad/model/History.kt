package com.example.vistacuregrad.model

data class HistoryItem(
    val image: String,
    val diseaseName: String,
    val probability: String,
    val predictionDate: String
)

data class HistoryResponse(
    val status: String,
    val message: String,
    val results: List<HistoryItem>
)