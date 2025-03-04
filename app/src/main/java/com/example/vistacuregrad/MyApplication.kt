package com.example.vistacuregrad

import android.app.Application
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.network.ApiService
import com.example.vistacuregrad.network.RetrofitClient

class MyApplication : Application() {

    // Create a singleton instance of the repository
    val repository: AuthRepository by lazy {
        val apiService = RetrofitClient.apiService
        AuthRepository(apiService)
    }
}