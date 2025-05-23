// File: app/src/main/java/com/example/vistacuregrad/Viewmodel/OtpViewModel.kt
package com.example.vistacuregrad.Viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.model.OtpResponse
import com.example.vistacuregrad.model.ProfileHistoryCheckResponse // Import
import kotlinx.coroutines.launch
import retrofit2.Response

class OtpViewModel(application: Application, private val authRepository: AuthRepository) : AndroidViewModel(application) {

    private val _otpResponse = MutableLiveData<Response<OtpResponse>>()
    val otpResponse: LiveData<Response<OtpResponse>> = _otpResponse

    // LiveData for the profile and history check result
    private val _profileHistoryCheckResult = MutableLiveData<Result<ProfileHistoryCheckResponse>>()
    val profileHistoryCheckResult: LiveData<Result<ProfileHistoryCheckResponse>> = _profileHistoryCheckResult

    fun verifyOtp(code: String) {
        val token = getTempToken()

        if (token.isNullOrEmpty()) {
            Log.e("OtpViewModel", "Temp token is missing. User needs to log in again.")
            _profileHistoryCheckResult.postValue(Result.failure(Exception("Temporary token missing. Please log in again.")))
            return
        }

        viewModelScope.launch {
            try {
                val response = authRepository.verifyOtp(code, "Bearer $token")
                _otpResponse.postValue(response) // Post for initial OTP message/error

                if (response.isSuccessful) {
                    val otpResponse = response.body()
                    val originalToken = otpResponse?.token

                    if (!originalToken.isNullOrEmpty()) {
                        saveOriginalToken(originalToken)
                        Log.d("OtpViewModel", "Original token saved successfully: $originalToken")

                        // *** Call the check function after saving the token ***
                        checkUserProfileAndMedicalHistory(originalToken)

                    } else {
                        Log.e("OtpViewModel", "Original token is null or empty from OTP response")
                        _profileHistoryCheckResult.postValue(Result.failure(Exception("Original token missing from OTP response.")))
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("OtpViewModel", "OTP verification failed: ${response.code()} - $errorBody")
                    _profileHistoryCheckResult.postValue(Result.failure(Exception("OTP verification failed: ${response.code()} - $errorBody")))
                }

            } catch (e: Exception) {
                Log.e("OtpViewModel", "Error verifying OTP: ${e.message}")
                _profileHistoryCheckResult.postValue(Result.failure(e))
            }
        }
    }

    // Function to call the repository and update LiveData with profile/history check result
    private fun checkUserProfileAndMedicalHistory(token: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.checkProfileAndMedicalHistory("Bearer $token") // Pass the original token
                if (response.isSuccessful && response.body() != null) {
                    _profileHistoryCheckResult.postValue(Result.success(response.body()!!))
                    Log.d("OtpViewModel", "Profile and history check successful: UserProfileExists=${response.body()!!.UserProfileExists}, MedicalHistoryExists=${response.body()!!.MedicalHistoryExists}")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("OtpViewModel", "Profile and history check failed: ${response.code()} - $errorBody")
                    _profileHistoryCheckResult.postValue(Result.failure(Exception("Failed to check profile/history: ${response.code()} - $errorBody")))
                }
            } catch (e: Exception) {
                Log.e("OtpViewModel", "Error checking profile and history: ${e.message}")
                _profileHistoryCheckResult.postValue(Result.failure(e))
            }
        }
    }

    private fun saveOriginalToken(token: String) {
        val sharedPreferences = getApplication<Application>().applicationContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("ORIGINAL_TOKEN", token).apply()
    }

    private fun getTempToken(): String? {
        val sharedPreferences = getApplication<Application>().applicationContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("TEMP_TOKEN", null)
    }

    fun getOriginalToken(): String? {
        val sharedPreferences = getApplication<Application>().applicationContext.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("ORIGINAL_TOKEN", null)
    }
}