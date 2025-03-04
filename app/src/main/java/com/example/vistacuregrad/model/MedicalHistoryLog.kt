package com.example.vistacuregrad.model

import com.google.gson.annotations.SerializedName

data class MedicalHistoryLogRequest(
    @SerializedName("allergies") val allergies: String,
    @SerializedName("chronicConditions") val chronicConditions: String,
    @SerializedName("medications") val medications: String,
    @SerializedName("surgeries") val surgeries: String,
    @SerializedName("familyHistory") val familyHistory: String,
    @SerializedName("lastCheckupDate") val lastCheckupDate: String
)

data class MedicalHistoryLogResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: MedicalHistoryLogRequest
)