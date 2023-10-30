package com.example.wealthwise

import com.example.wealthwise.DataClass.ChangePassword
import com.example.wealthwise.DataClass.RefreshToken
import com.example.wealthwise.DataClass.RegistrationData
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
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


}