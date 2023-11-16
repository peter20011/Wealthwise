package com.example.wealthwise.Activities


import android.content.Intent

import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.wealthwise.ApiService
import com.example.wealthwise.Adapters.ChartAdapter
import com.example.wealthwise.DataClass.StatisticResponse
import com.example.wealthwise.DataClass.TokenRequest
import com.example.wealthwise.R
import com.example.wealthwise.Manager.TokenManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class StatisticActivity : AppCompatActivity() {
    private val BASE_URL = "https://10.0.2.2:8443"
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

        if (tokenManager.refreshTokenIfNeeded(resources)) {
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

        val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)

        val certificateFactory = CertificateFactory.getInstance("X.509")
        val certificateInputStream1 = resources.openRawResource(R.raw.ca)
        val yourTrustedCertificate1 = certificateFactory.generateCertificate(certificateInputStream1) as X509Certificate
        certificateInputStream1.close()


        keyStore.setCertificateEntry("ca", yourTrustedCertificate1)
        trustManagerFactory.init(keyStore)
        val trustManagers = trustManagerFactory.trustManagers

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, trustManagers, SecureRandom())

        val client = OkHttpClient.Builder()
            .sslSocketFactory(sslContext.socketFactory, trustManagers[0] as X509TrustManager)
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