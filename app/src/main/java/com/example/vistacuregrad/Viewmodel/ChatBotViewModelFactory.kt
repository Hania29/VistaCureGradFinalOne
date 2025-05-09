package com.example.vistacuregrad.Viewmodel

// ChatBotViewModelFactory.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.vistacuregrad.Repository.AuthRepository

class ChatBotViewModelFactory(private val repository: AuthRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatBotViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChatBotViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}