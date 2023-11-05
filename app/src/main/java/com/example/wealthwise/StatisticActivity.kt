package com.example.wealthwise


import android.content.Intent

import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wealthwise.DataClass.StatisticResponse
import com.example.wealthwise.DataClass.TokenRequest
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class StatisticActivity : AppCompatActivity() {
    private val BASE_URL = "http://10.0.2.2:8080"
    private lateinit var adapter: ChartAdapter
    private lateinit var listView: ListView
    private var listOfMonthlyExpenses = listOf<StatisticResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val tokenManager = TokenManager(this)
        val tokenAccess = tokenManager.getTokenAccess()
        val tokenRefresh = tokenManager.getTokenRefresh()

        if (tokenAccess == null || tokenRefresh == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            Toast.makeText(this, "Brak uprawnień", Toast.LENGTH_SHORT).show()
        }

        if (tokenManager.refreshTokenIfNeeded()) {
            Toast.makeText(this, "Token odświeżony", Toast.LENGTH_SHORT).show()
        }

        setContentView(R.layout.activity_statistic)

        val list =  getData()

        val homeIcon = findViewById<ImageView>(R.id.homeIcon)
        val statisticIcon = findViewById<ImageView>(R.id.statisticIcon)
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        val assetsIcon = findViewById<ImageView>(R.id.assetsIcon)

        homeIcon.setBackgroundResource(0)
        statisticIcon.setBackgroundResource(R.drawable.blue_border)
        profileIcon.setBackgroundResource(0)
        assetsIcon.setBackgroundResource(0)

        homeIcon.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        profileIcon.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        assetsIcon.setOnClickListener {
            val intent = Intent(this, AssetsActivity::class.java)
            startActivity(intent)
        }

        listView = findViewById<ListView>(R.id.statisticListView)
        adapter = ChartAdapter(this)
        listView.adapter = adapter


    }
    private fun getData(){
        val tokenManager = TokenManager(this)
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val authHeader = "Bearer " + tokenManager.getTokenAccess().toString()
        val apiService = retrofit.create(ApiService::class.java)

        val tokenRequest = TokenRequest(tokenManager.getTokenAccess().toString())

        val call = apiService.getMonthlyExpenseAndIncome(authHeader, tokenRequest)

        call.enqueue(object : Callback<List<StatisticResponse>> {
            override fun onResponse(
                call: Call<List<StatisticResponse>>,
                response: Response<List<StatisticResponse>>
            ) {
                if (response.isSuccessful) {
                    val statisticResponse = response.body()
                    if (statisticResponse != null) {
                        adapter.updateData(statisticResponse)
                    }
                }
            }

            override fun onFailure(call: Call<List<StatisticResponse>>, t: Throwable) {
                Toast.makeText(this@StatisticActivity, "Błąd pobierania danych", Toast.LENGTH_SHORT).show()
            }
        })

    }
}