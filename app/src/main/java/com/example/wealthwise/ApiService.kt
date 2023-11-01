package com.example.wealthwise

import com.example.wealthwise.DataClass.AddCashSavingsGoalRequest
import com.example.wealthwise.DataClass.AssetsRequest
import com.example.wealthwise.DataClass.AssetsRequestDelete
import com.example.wealthwise.DataClass.AssetsRequestListDelete
import com.example.wealthwise.DataClass.ChangePassword
import com.example.wealthwise.DataClass.CurrencyData
import com.example.wealthwise.DataClass.RefreshToken
import com.example.wealthwise.DataClass.RegistrationData
import com.example.wealthwise.DataClass.SavingsGoal
import com.example.wealthwise.DataClass.SavingsGoalRequest
import com.example.wealthwise.DataClass.TokenRequest
import com.example.wealthwise.DataClass.UserDataResponse
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

interface ApiService {
    @POST("/auth/register")
    fun registerUser(@Body registrationData : RegistrationData) : Call<ResponseBody>

    @GET("exchangerates/tables/A/?format=json")
    fun getCurrencyRates(): Call<List<CurrencyData>>

    @POST("/auth/login")
    fun loginUser(@Body loginData: LoginData) : Call<TokenResponse>

    @POST("/auth/refreshToken")
    fun refreshToken(@Body refreshToken: RefreshToken) : Call<TokenResponse>

    @POST("/auth/logout")
    fun logoutUser(@Header("Authorization") token: String) : Call<ResponseBody>

    @POST("/user/changePassword")
    fun changePassword(@Header("Authorization") token: String, @Body changePassword: ChangePassword) : Call<ResponseBody>

    @POST("/user/getDataUser")
    fun getUserData(@Header("Authorization") token: String, @Body tokenRequest: TokenRequest) : Call<UserDataResponse>

    @POST("/savingsGoal/createSavingsGoal")
    fun createSavingsGoal(@Header("Authorization") token: String, @Body savingsGoalRequest: SavingsGoalRequest) : Call<ResponseBody>

    @POST("/savingsGoal/addCashSavingsGoal")
    fun addCashSavingsGoal(@Header("Authorization") token: String, @Body addCashSavingsGoalRequest: AddCashSavingsGoalRequest) : Call<ResponseBody>

    @POST("/savingsGoal/getSavingsGoal")
    fun getSavingsGoal(@Header("Authorization") token: String, @Body tokenRequest: TokenRequest) : Call<List<SavingsGoal>>

    @POST("/asset/addAsset")
    fun addAsset(@Header("Authorization") token: String, @Body assetsRequest: AssetsRequest) : Call<ResponseBody>

    @POST("/asset/getAsset")
    fun getAsset(@Header("Authorization") token: String, @Body tokenRequest: TokenRequest) : Call<List<AssetsRequestDelete>>

    @POST("/asset/deleteAsset")
    fun deleteAsset(@Header("Authorization") token: String, @Body  assetsRequestListDelete: AssetsRequestListDelete) : Call<ResponseBody>

}