package com.example.vistacuregrad.Viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.model.HistoryItem
import kotlinx.coroutines.launch

class HistoryViewModel(private val repository: AuthRepository) : ViewModel() {
    private val _historyItems = MutableLiveData<List<HistoryItem>>()
    val historyItems: LiveData<List<HistoryItem>> = _historyItems

    fun fetchHistory(token: String) {
        viewModelScope.launch {
            val result = repository.getHistory(token)
            Log.d("HistoryViewModel", "Fetched items: ${result?.results?.size}")
            _historyItems.value = result?.results ?: emptyList()
        }
    }
}
