package com.example.vistacuregrad.Repository

import com.example.vistacuregrad.model.RegisterResponse
import com.example.vistacuregrad.network.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

class AuthRepository(private val apiService: ApiService) {

    suspend fun registerUser(
        username: String,
        email: String,
        password: String
    ): Response<RegisterResponse> {
        val userNameBody = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val passwordBody = password.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.registerUser(
            userName = userNameBody,
            password = passwordBody,
            email = emailBody
        )
    }
}
