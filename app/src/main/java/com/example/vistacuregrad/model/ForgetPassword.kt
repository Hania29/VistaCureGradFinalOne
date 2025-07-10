package com.example.vistacuregrad.model

import com.google.gson.annotations.SerializedName

data class ForgotPasswordRequest(
    @SerializedName("email")
    val email: String
)

data class ForgotPasswordResponse(
    @SerializedName("status")
    val status: String,
    @SerializedName("message")
    val message: String,
    @SerializedName("token") val token: String,
    @SerializedName("email") val email: String
)
