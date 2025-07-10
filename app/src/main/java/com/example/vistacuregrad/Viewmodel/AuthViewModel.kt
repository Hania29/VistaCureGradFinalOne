package com.example.vistacuregrad.viewmodel

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.model.ForgotPasswordResponse
import kotlinx.coroutines.launch
import retrofit2.Response

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _forgotPasswordResponse = MutableLiveData<Response<ForgotPasswordResponse>>()
    val forgotPasswordResponse: LiveData<Response<ForgotPasswordResponse>> get() = _forgotPasswordResponse

    fun forgotPassword(email: String) {
        viewModelScope.launch {
            try {
                val response = authRepository.forgotPassword(email)
                _forgotPasswordResponse.postValue(response)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun saveResetToken(context: Context, token: String) {
        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("RESET_TOKEN", token).apply()
    }
}
