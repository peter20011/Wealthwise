package com.example.wealthwise

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("/auth/register")
    fun registerUser(@Body registrationData : RegistrationData) : Call<ResponseBody>
}