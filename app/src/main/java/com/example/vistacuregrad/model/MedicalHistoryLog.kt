package com.example.vistacuregrad.model

import com.google.gson.annotations.SerializedName

data class MedicalHistoryLogResponse(
    @SerializedName("status") val status: String,
    @SerializedName("medicalhistory") val medicalHistory: MedicalHistoryData
)

data class MedicalHistoryData(
    @SerializedName("allergies") val allergies: String, // Match backend key
    @SerializedName("chronicConditions") val chronicConditions: String, // Match backend key
    @SerializedName("medications") val medications: String, // Match backend key
    @SerializedName("surgeries") val surgeries: String, // Match backend key
    @SerializedName("familyHistory") val familyHistory: String, // Match backend key
    @SerializedName("lastCheckupDate") val lastCheckupDate: String // Match backend key
)