package com.example.vistacuregrad.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.model.UploadResponse
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Response

class HomeViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uploadResponse = MutableLiveData<Response<UploadResponse>?>()
    val uploadResponse: LiveData<Response<UploadResponse>?> get() = _uploadResponse

    fun uploadImage(token: String, file: MultipartBody.Part) {


        viewModelScope.launch {
            try {
                val response = repository.uploadImage(token, file)
                _uploadResponse.value = response
            } catch (e: Exception) {
                Log.e("UploadImage", "Error uploading image: ${e.message}", e)
                _uploadResponse.value = null
            }
        }
    }
}