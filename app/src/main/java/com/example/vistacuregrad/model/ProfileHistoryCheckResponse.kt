package com.example.vistacuregrad.model

import com.google.gson.annotations.SerializedName // Assuming you use Gson

data class ProfileHistoryCheckResponse(
    @SerializedName("UserProfileExists") // Match the JSON key exactly
    val UserProfileExists: Boolean,
    @SerializedName("MedicalHistoryExists") // Match the JSON key exactly
    val MedicalHistoryExists: Boolean
)