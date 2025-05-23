package com.example.vistacuregrad.network

import com.example.vistacuregrad.model.ChatHistoryResponse
import com.example.vistacuregrad.model.ChatRequest
import com.example.vistacuregrad.model.ChatResponse
import com.example.vistacuregrad.model.ForgotPasswordResponse
import com.example.vistacuregrad.model.HistoryResponse
import com.example.vistacuregrad.model.LoginResponse
import com.example.vistacuregrad.model.MedicalHistoryLogResponse
import com.example.vistacuregrad.model.MedicalHistoryResponse
import com.example.vistacuregrad.model.OtpResponse
import com.example.vistacuregrad.model.RegisterResponse
import com.example.vistacuregrad.model.ResetPasswordResponse
import com.example.vistacuregrad.model.UploadResponse
import com.example.vistacuregrad.model.UserProfileLogResponse
import com.example.vistacuregrad.model.UserProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Query

interface ApiService {
    @Multipart
    @POST("api/Authentication/Register")
    suspend fun registerUser(
        @Part("UserName") userName: RequestBody,
        @Part("Password") password: RequestBody,
        @Part("Email") email: RequestBody
    ): Response<RegisterResponse>

    @FormUrlEncoded
    @POST("api/Authentication/Login")
    suspend fun loginUser(
        @Field("Username") username: String,
        @Field("Password") password: String
    ): Response<LoginResponse>

    @FormUrlEncoded
    @POST("api/Authentication/VerifyOTP")
    suspend fun verifyOtp(
        @Field("code") code: String,
        @Header("Authorization") token: String
    ): Response<OtpResponse>



    @Multipart
    @POST("api/Authentication/CreateUserProfile")
    suspend fun createUserProfile(
        @Header("Authorization") token: String,  // For JWT authentication
        @Part("FirstName") firstName: RequestBody,
        @Part("LastName") lastName: RequestBody,
        @Part("DateOfBirth") dateOfBirth: RequestBody,
        @Part("Height") height: RequestBody,
        @Part("Weight") weight: RequestBody,
        @Part("Gender") gender: RequestBody
    ): Response<UserProfileResponse>

    @Multipart
    @POST("api/Authentication/MedicalHistory")
    suspend fun createMedicalHistory(
        @Header("Authorization") token: String,  // For JWT authentication
        @Part("allergies") allergies: RequestBody,
        @Part("chronicConditions") chronicConditions: RequestBody,
        @Part("medications") medications: RequestBody,
        @Part("surgeries") surgeries: RequestBody,
        @Part("familyHistory") familyHistory: RequestBody,
        @Part("lastCheckupDate") lastCheckupDate: RequestBody
    ): Response<MedicalHistoryResponse>

    @FormUrlEncoded
    @POST("api/Authentication/forgetPasswordlogin")
    suspend fun forgotPassword(
        @Field("email") email: String
    ): Response<ForgotPasswordResponse>


    @FormUrlEncoded
    @POST("api/Authentication/ResetPasswordlogin")
    suspend fun resetPassword(
        @Field("password") password: String,
        @Field("confirmPassword") confirmPassword: String,
        @Query("token") token: String,
        @Field("email") email: String
    ): Response<ResetPasswordResponse>

    @Multipart
    @POST("/api/Detection/UploadImages")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Part Image: MultipartBody.Part
    ): Response<UploadResponse>

    @GET("api/Authentication/UserProfile")
    suspend fun getUserProfileLog(
        @Header("Authorization") token: String
    ): Response<UserProfileLogResponse>

    @Multipart
    @PUT("api/Authentication/UpdateUserProfile")
    suspend fun updateUserProfileLog(
        @Header("Authorization") token: String,
        @Part("FirstName") firstName: RequestBody?,
        @Part("LastName") lastName: RequestBody?,
        @Part("DateOfBirth") dateOfBirth: RequestBody?,
        @Part("Height") height: RequestBody?,
        @Part("Weight") weight: RequestBody?,
        @Part("Gender") gender: RequestBody?
    ): Response<UserProfileLogResponse>

    @DELETE("api/Authentication/DeleteUserProfile")
    suspend fun deleteUserProfileLog(
        @Header("Authorization") token: String
    ): Response<UserProfileLogResponse>

    @GET("api/Authentication/MedicalHistory")
    suspend fun getMedicalHistory(
        @Header("Authorization") token: String
    ): Response<MedicalHistoryLogResponse>

    @Multipart
    @PUT("api/Authentication/UpdateMedicalHistory")
    suspend fun updateMedicalHistory(
        @Header("Authorization") token: String,
        @Part("allergies") allergies: RequestBody,
        @Part("chronicConditions") chronicConditions: RequestBody,
        @Part("medications") medications: RequestBody,
        @Part("surgeries") surgeries: RequestBody,
        @Part("familyHistory") familyHistory: RequestBody,
        @Part("lastCheckupDate") lastCheckupDate: RequestBody
    ): Response<MedicalHistoryResponse>


        @GET("api/Detection/GetUserImagesWithInfo")
        suspend fun getUserImagesWithInfo(
            @Header("Authorization") token: String,
        ): Response<HistoryResponse>

    @POST("api/Chatbot/ask")
    suspend fun askChatbot(
        @Header("Authorization") token: String,
        @Body request: ChatRequest): ChatResponse

    @GET("api/Chatbot/history")
    suspend fun getChatHistory(
        @Header("Authorization") token: String
    ): ChatHistoryResponse


}




