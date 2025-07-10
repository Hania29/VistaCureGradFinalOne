package com.example.vistacuregrad.model

import com.google.gson.annotations.SerializedName

data class OtpRequest(
    @SerializedName("code") val code: String
)

data class OtpResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("token")
    val token: String?,
    @SerializedName("userProfileExists")
    val userProfileExists: Boolean?,
    @SerializedName("medicalHistoryExists")
    val medicalHistoryExists: Boolean?
)

