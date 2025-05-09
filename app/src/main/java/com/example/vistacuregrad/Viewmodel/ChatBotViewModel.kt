package com.example.vistacuregrad.Viewmodel

// ChatBotViewModel.kt
import androidx.lifecycle.*
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.model.ChatHistoryResponse
import com.example.vistacuregrad.model.ChatResponse
import kotlinx.coroutines.launch

class ChatBotViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _chatResponse = MutableLiveData<ChatResponse>()
    val chatResponse: LiveData<ChatResponse> = _chatResponse

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _chatHistory = MutableLiveData<ChatHistoryResponse>()
    val chatHistory: LiveData<ChatHistoryResponse> = _chatHistory

    fun fetchChatHistory(token: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getChatHistory(token)
                _chatHistory.value = response
            } catch (e: Exception) {
                _chatHistory.value = ChatHistoryResponse(
                    status = "Error",
                    message = e.localizedMessage ?: "Unknown error",
                    chatHistory = null
                )
            } finally {
                _loading.value = false
            }
        }
    }

    fun sendMessage(token: String, message: String) {
        _loading.value = true
        viewModelScope.launch {
            try {
                val response = repository.askChatbot(token, message)
                _chatResponse.value = response
            } catch (e: Exception) {
                _chatResponse.value = ChatResponse(
                    status = "Error",
                    message = e.localizedMessage ?: "Unknown error",
                    response = null
                )
            } finally {
                _loading.value = false
            }
        }
    }
}