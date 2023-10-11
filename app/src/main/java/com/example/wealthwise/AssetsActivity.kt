package com.example.wealthwise

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException

class AssetsActivity : AppCompatActivity() {
    private lateinit var currency1Name: TextView
    private lateinit var currency2Name: TextView
    private lateinit var currency3Name: TextView
    private lateinit var currency4Name: TextView
    private lateinit var currency1Rate: TextView
    private lateinit var currency2Rate: TextView
    private lateinit var currency3Rate: TextView
    private lateinit var currency4Rate: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_assets)

        val homeIcon = findViewById<ImageView>(R.id.homeIcon)
        val statisticIcon = findViewById<ImageView>(R.id.statisticIcon)
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        val assetsIcon = findViewById<ImageView>(R.id.assetsIcon)

        homeIcon.setBackgroundResource(0)
        statisticIcon.setBackgroundResource(0)
        profileIcon.setBackgroundResource(0)
        assetsIcon.setBackgroundResource(R.drawable.blue_border)

        homeIcon.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        profileIcon.setOnClickListener{
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        statisticIcon.setOnClickListener{
            val intent = Intent(this, StatisticActivity::class.java)
            startActivity(intent)
        }

        // Inicjalizacja widoków
        currency1Name = findViewById(R.id.currency1Name)
        currency2Name = findViewById(R.id.currency2Name)
        currency3Name = findViewById(R.id.currency3Name)
        currency4Name = findViewById(R.id.currency4Name)
        currency1Rate = findViewById(R.id.currency1Rate)
        currency2Rate = findViewById(R.id.currency2Rate)
        currency3Rate = findViewById(R.id.currency3Rate)
        currency4Rate = findViewById(R.id.currency4Rate)

        // Pobieranie kursów walut
        fetchCurrencyRates()
    }

    private fun fetchCurrencyRates() {
        val client = OkHttpClient()
        val request = Request.Builder()
            .url("https://api.nbp.pl/api/exchangerates/tables/A/?format=json")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body()?.string()
                val currencyRates = parseCurrencyRates(body)
                runOnUiThread {
                    updateCurrencyViews(currencyRates)
                }
            }
        })
    }

    private fun parseCurrencyRates(json: String?): Map<String, Double> {
        val rates = mutableMapOf<String, Double>()

        try {
            val jsonArray = JSONArray(json)
            val ratesArray = jsonArray.getJSONObject(0).getJSONArray("rates")
            for (i in 0 until ratesArray.length()) {
                val rate = ratesArray.getJSONObject(i)
                val currency = rate.getString("code")
                val value = rate.getDouble("mid")
                rates[currency] = value
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return rates
    }

    private fun updateCurrencyViews(currencyRates: Map<String, Double>) {

        currency1Rate.text = currencyRates["USD"]?.toString() ?: "Brak danych"
        currency2Rate.text = currencyRates["EUR"]?.toString() ?: "Brak danych"
        currency3Rate.text = currencyRates["CHF"]?.toString() ?: "Brak danych"
        currency4Rate.text = currencyRates["GBP"]?.toString() ?: "Brak danych"
    }
}