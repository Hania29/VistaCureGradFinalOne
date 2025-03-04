package com.example.vistacuregrad.Repository

import android.util.Log
import com.example.vistacuregrad.model.*
import com.example.vistacuregrad.network.ApiService
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

@Suppress("UNREACHABLE_CODE")
class AuthRepository(private val apiService: ApiService) {

    suspend fun registerUser(
        username: String,
        email: String,
        password: String
    ): Response<RegisterResponse> {
        val userNameBody = username.toRequestBody("text/plain".toMediaTypeOrNull())
        val emailBody = email.toRequestBody("text/plain".toMediaTypeOrNull())
        val passwordBody = password.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.registerUser(
            userName = userNameBody,
            password = passwordBody,
            email = emailBody
        )
    }

    suspend fun loginUser(username: String, password: String): Response<LoginResponse> {
        return apiService.loginUser(username, password)
    }

    suspend fun verifyOtp(code: String, token: String): Response<OtpResponse> {
        Log.d("AuthRepository", "code: $code | token: '$token'")
        return apiService.verifyOtp(code, token)
    }

    suspend fun createUserProfile(
        token: String,
        request: UserProfileRequest
    ): Response<UserProfileResponse> {
        val firstNameBody = request.firstName.toRequestBody("text/plain".toMediaTypeOrNull())
        val lastNameBody = request.lastName.toRequestBody("text/plain".toMediaTypeOrNull())
        val dateOfBirthBody = request.dateOfBirth.toRequestBody("text/plain".toMediaTypeOrNull())
        val heightBody = request.height.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val weightBody = request.weight.toString().toRequestBody("text/plain".toMediaTypeOrNull())
        val genderBody = request.gender.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.createUserProfile(
            token = token,
            firstName = firstNameBody,
            lastName = lastNameBody,
            dateOfBirth = dateOfBirthBody,
            height = heightBody,
            weight = weightBody,
            gender = genderBody
        )
    }

    suspend fun createMedicalHistory(
        token: String,
        request: MedicalHistoryRequest
    ): Response<MedicalHistoryResponse> {
        val allergiesBody = request.allergies.toRequestBody("text/plain".toMediaTypeOrNull())
        val chronicConditionsBody =
            request.chronicConditions.toRequestBody("text/plain".toMediaTypeOrNull())
        val medicationsBody = request.medications.toRequestBody("text/plain".toMediaTypeOrNull())
        val surgeriesBody = request.surgeries.toRequestBody("text/plain".toMediaTypeOrNull())
        val familyHistoryBody =
            request.familyHistory.toRequestBody("text/plain".toMediaTypeOrNull())
        val lastCheckupDateBody =
            request.lastCheckupDate.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.createMedicalHistory(
            token = token,
            allergies = allergiesBody,
            chronicConditions = chronicConditionsBody,
            medications = medicationsBody,
            surgeries = surgeriesBody,
            familyHistory = familyHistoryBody,
            lastCheckupDate = lastCheckupDateBody
        )
    }

    suspend fun forgotPassword(email: String): Response<ForgotPasswordResponse> {
        return apiService.forgotPassword(email)
    }

    suspend fun resetPassword(
        password: String,
        confirmPassword: String,
        token: String,
        email: String
    ): Response<ResetPasswordResponse> {
        val processedToken = token.replace("+", "%2B")
        return apiService.resetPassword(password, confirmPassword, processedToken, email)
    }


        suspend fun uploadImage (file: MultipartBody.Part): Response<UploadResponse> {
            return apiService.uploadImage(file)
        }

    suspend fun getUserProfileLog(token: String): Response<UserProfileLogResponse> {
        Log.d("AuthRepository", "Token sent in API request: Bearer $token")
        return apiService.getUserProfileLog("Bearer $token")
    }

    suspend fun updateUserProfileLog(token: String, request: UserProfileLogRequest): Response<UserProfileLogResponse> {
        val firstNameBody = request.firstName?.toRequestBody("text/plain".toMediaTypeOrNull())
        val lastNameBody = request.lastName?.toRequestBody("text/plain".toMediaTypeOrNull())
        val dateOfBirthBody = request.dateOfBirth?.toRequestBody("text/plain".toMediaTypeOrNull())
        val heightBody = request.height?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val weightBody = request.weight?.toString()?.toRequestBody("text/plain".toMediaTypeOrNull())
        val genderBody = request.gender?.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.updateUserProfileLog(
            token = "Bearer $token",
            firstName = firstNameBody,
            lastName = lastNameBody,
            dateOfBirth = dateOfBirthBody,
            height = heightBody,
            weight = weightBody,
            gender = genderBody
        )
    }

    suspend fun deleteUserProfileLog(token: String): Response<UserProfileLogResponse> {
        return apiService.deleteUserProfileLog("Bearer $token")
    }

    suspend fun getMedicalHistory(token: String): Response<MedicalHistoryLogResponse> {
        return apiService.getMedicalHistory("Bearer $token")
    }

    suspend fun updateMedicalHistory(
        token: String,
        request: MedicalHistoryRequest
    ): Response<MedicalHistoryResponse> {
        val allergiesBody = request.allergies.toRequestBody("text/plain".toMediaTypeOrNull())
        val chronicConditionsBody = request.chronicConditions.toRequestBody("text/plain".toMediaTypeOrNull())
        val medicationsBody = request.medications.toRequestBody("text/plain".toMediaTypeOrNull())
        val surgeriesBody = request.surgeries.toRequestBody("text/plain".toMediaTypeOrNull())
        val familyHistoryBody = request.familyHistory.toRequestBody("text/plain".toMediaTypeOrNull())
        val lastCheckupDateBody = request.lastCheckupDate.toRequestBody("text/plain".toMediaTypeOrNull())

        return apiService.updateMedicalHistory(
            token = "Bearer $token",
            allergies = allergiesBody,
            chronicConditions = chronicConditionsBody,
            medications = medicationsBody,
            surgeries = surgeriesBody,
            familyHistory = familyHistoryBody,
            lastCheckupDate = lastCheckupDateBody
        )
    }
}
