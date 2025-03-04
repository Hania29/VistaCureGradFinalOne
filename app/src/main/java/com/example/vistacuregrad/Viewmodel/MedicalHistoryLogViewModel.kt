package com.example.vistacuregrad.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.model.MedicalHistoryLogResponse
import com.example.vistacuregrad.model.MedicalHistoryRequest
import com.example.vistacuregrad.model.MedicalHistoryResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class MedicalHistoryLogViewModel(private val repository: AuthRepository, private val context: Context) : ViewModel() {

    private val _medicalHistoryLogResponse = MutableLiveData<Response<MedicalHistoryLogResponse>>()
    val medicalHistoryLogResponse: LiveData<Response<MedicalHistoryLogResponse>> get() = _medicalHistoryLogResponse

    private val _updateMedicalHistoryResponse = MutableLiveData<Response<MedicalHistoryResponse>>()
    val updateMedicalHistoryResponse: LiveData<Response<MedicalHistoryResponse>> get() = _updateMedicalHistoryResponse

    fun getMedicalHistory() {
        viewModelScope.launch {
            try {
                val token = getOriginalToken()
                if (token.isNullOrEmpty()) {
                    Log.e("MedicalHistoryLogViewModel", "No token found")
                    return@launch
                }

                val response = repository.getMedicalHistory(token)
                _medicalHistoryLogResponse.postValue(response)

                if (response.isSuccessful) {
                    Log.d("MedicalHistoryLogViewModel", "Response: ${response.body()}")
                } else {
                    Log.e("MedicalHistoryLogViewModel", "Error: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e("MedicalHistoryLogViewModel", "Exception: ${e.message}", e)
            }
        }
    }

    fun updateMedicalHistory(request: MedicalHistoryRequest) {
        viewModelScope.launch {
            try {
                val token = getOriginalToken()

                if (token.isNullOrEmpty()) {
                    Log.e(
                        "MedicalHistoryLogViewModel",
                        "No original token found, please log in again."
                    )
                    return@launch
                }

                val response = repository.updateMedicalHistory(token, request)
                _updateMedicalHistoryResponse.postValue(response)

                if (response.isSuccessful) {
                    Log.d(
                        "MedicalHistoryLogViewModel",
                        "Medical history updated successfully: ${response.body()?.message}"
                    )
                } else {
                    Log.e("MedicalHistoryLogViewModel", "Error: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e(
                    "MedicalHistoryLogViewModel",
                    "Exception during medical history update: ${e.message}",
                    e
                )
            }
        }
    }

    private fun getOriginalToken(): String? {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("ORIGINAL_TOKEN", null)
        Log.d("MedicalHistoryLogViewModel", "Token: $token")
        return token
    }
}