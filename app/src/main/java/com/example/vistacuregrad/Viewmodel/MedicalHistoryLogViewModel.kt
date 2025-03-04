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
                    Log.e(
                        "MedicalHistoryLogViewModel",
                        "No original token found, please log in again."
                    )
                    return@launch
                }

                val response = repository.getMedicalHistory("Bearer $token")
                _medicalHistoryLogResponse.postValue(response)

                if (response.isSuccessful) {
                    Log.d(
                        "MedicalHistoryLogViewModel",
                        "Medical history fetched successfully: ${response.body()?.status}"
                    )
                } else {
                    when (response.code()) {
                        401 -> Log.e("MedicalHistoryLogViewModel", "Unauthorized: Invalid token")
                        404 -> Log.e(
                            "MedicalHistoryLogViewModel",
                            "Not Found: Medical history not found"
                        )

                        500 -> Log.e(
                            "MedicalHistoryLogViewModel",
                            "Internal Server Error: ${response.errorBody()?.string()}"
                        )

                        else -> Log.e("MedicalHistoryLogViewModel", "Error: ${response.message()}")
                    }
                }

            } catch (e: Exception) {
                Log.e(
                    "MedicalHistoryLogViewModel",
                    "Exception during medical history fetch: ${e.message}",
                    e
                )
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

                val response = repository.updateMedicalHistory("Bearer $token", request)
                _updateMedicalHistoryResponse.postValue(response)

                if (response.isSuccessful) {
                    Log.d(
                        "MedicalHistoryLogViewModel",
                        "Medical history updated successfully: ${response.body()?.message}"
                    )
                } else {
                    when (response.code()) {
                        401 -> Log.e("MedicalHistoryLogViewModel", "Unauthorized: Invalid token")
                        404 -> Log.e(
                            "MedicalHistoryLogViewModel",
                            "Not Found: Medical history not found"
                        )

                        500 -> Log.e(
                            "MedicalHistoryLogViewModel",
                            "Internal Server Error: ${response.errorBody()?.string()}"
                        )

                        else -> Log.e("MedicalHistoryLogViewModel", "Error: ${response.message()}")
                    }
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
        return sharedPreferences.getString("ORIGINAL_TOKEN", null)
    }

}