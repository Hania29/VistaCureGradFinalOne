package com.example.vistacuregrad.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.model.ResetPasswordResponse
import kotlinx.coroutines.launch
import retrofit2.Response
import android.util.Log

class ResetPasswordViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _resetPasswordResponse = MutableLiveData<Response<ResetPasswordResponse>>()
    val resetPasswordResponse: LiveData<Response<ResetPasswordResponse>> get() = _resetPasswordResponse

    fun resetPassword(password: String, confirmPassword: String, token: String, email: String) {
        viewModelScope.launch {
            try {
                Log.d("ResetPasswordViewModel", "Sending Reset Password Request")
                val response = authRepository.resetPassword(password, confirmPassword, token, email)

                if (response.isSuccessful) {
                    Log.d("ResetPasswordViewModel", "Success: ${response.body()?.message}")
                } else {
                    Log.e("ResetPasswordViewModel", "Error: ${response.errorBody()?.string()}")
                }

                _resetPasswordResponse.postValue(response)
            } catch (e: Exception) {
                Log.e("ResetPasswordViewModel", "Exception: ${e.message}")
            }
        }
    }
    fun getResetToken(context: Context): String? {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("RESET_TOKEN", null)
    }

}
