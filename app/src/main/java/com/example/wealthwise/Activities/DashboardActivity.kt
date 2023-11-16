package com.example.wealthwise.Activities


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.wealthwise.ApiService
import com.example.wealthwise.DataClass.Expense
import com.example.wealthwise.DataClass.ExpenseRequest
import com.example.wealthwise.DataClass.ExpenseResponse
import com.example.wealthwise.DataClass.IncomeRequest
import com.example.wealthwise.DataClass.IncomeResponse
import com.example.wealthwise.DataClass.TokenRequest
import com.example.wealthwise.DataClass.UserDataResponse
import com.example.wealthwise.R
import com.example.wealthwise.Manager.TokenManager
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
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
import java.time.LocalDate
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager


class DashboardActivity : AppCompatActivity() {

    private lateinit var incomeButton: Button
    private lateinit var expenseButton: Button
    private lateinit var incomeEditText: EditText
    private lateinit var expenseEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var pieChart: PieChart
    private lateinit var expensesListView: ListView
    private lateinit var welcomeText: TextView
    private lateinit var lackOfData : TextView
    private lateinit var lastSpendVisibility : TextView
    private lateinit var lastSpendVisibilityFrame : ListView
    private lateinit var logoutButton : Button
    private var totalIncome = 0.00
    private var freeFounds = 100.00
    private val expensesList = arrayListOf<Expense>()
    private var categories = arrayOf(
        "Żywność",
        "Chemia gospodarcza",
        "Inne wydatki",
        "Rachunki",
        "Ubrania",
        "Relaks",
        "Transport",
        "Mieszkanie",
        "Zdrowie"
    )
    private var entries = ArrayList<PieEntry>()
    private var dataSet = PieDataSet(entries, "")
    private val BASE_URL = "https://10.0.2.2:8443"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Zdefiniuj zmienną przechowującą aktualny miesiąc i rok
        val currentDate = LocalDate.now()
        val currentMonth = currentDate.month
        val currentYear = currentDate.year

        // Zdefiniuj zmienną przechowującą poprzedni miesiąc i rok
        var previousMonth = currentMonth
        var previousYear = currentYear

        // Sprawdź, czy rozpoczął się nowy miesiąc
        if (currentMonth != previousMonth || currentYear != previousYear) {
            // Nowy miesiąc, zresetuj zmienną freeFounds
            freeFounds = 100.00

            // Zaktualizuj zmienne przechowujące poprzedni miesiąc i rok
            previousMonth = currentMonth
            previousYear = currentYear
        }


        val tokenManager = TokenManager(this)
        val tokenAccess = tokenManager.getTokenAccess()
        val tokenRefresh = tokenManager.getTokenRefresh()

        if(tokenAccess == null || tokenRefresh == null){
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
            Toast.makeText(this, "Brak uprawnień", Toast.LENGTH_SHORT).show()
        }

        if(tokenManager.refreshTokenIfNeeded(resources)){
            Toast.makeText(this, "Token odświeżony", Toast.LENGTH_SHORT).show()
        }


        setContentView(R.layout.activity_dashboard)
        // Inicjalizacja widoków
        incomeButton = findViewById(R.id.incomeButton)
        expenseButton = findViewById(R.id.expenseButton)
        incomeEditText = findViewById(R.id.incomeEditText)
        expenseEditText = findViewById(R.id.expenseEditText)
        categorySpinner = findViewById(R.id.categorySpinner)
        pieChart = findViewById(R.id.pieChart)
        expensesListView = findViewById(R.id.expensesListView)
        welcomeText = findViewById(R.id.welcomeText)
        lackOfData = findViewById(R.id.lackOfData)
        lastSpendVisibility = findViewById(R.id.lastSpend)
        lastSpendVisibilityFrame = findViewById(R.id.expensesListView)
        logoutButton = findViewById(R.id.logoutButton)

        val homeIcon = findViewById<ImageView>(R.id.homeIcon)
        val statisticIcon = findViewById<ImageView>(R.id.statisticIcon)
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        val assetsIcon = findViewById<ImageView>(R.id.assetsIcon)
        val logoutButton = findViewById<Button>(R.id.logoutButton)
        val context : Context = applicationContext

        homeIcon.setBackgroundResource(R.drawable.blue_border)
        statisticIcon.setBackgroundResource(0) // Usuwa tło lub obramowanie
        profileIcon.setBackgroundResource(0)
        assetsIcon.setBackgroundResource(0)

        logoutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        profileIcon.setOnClickListener {
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        statisticIcon.setOnClickListener {
            val intent = Intent(this, StatisticActivity::class.java)
            startActivity(intent)
        }

        assetsIcon.setOnClickListener {
            val intent = Intent(this, AssetsActivity::class.java)
            startActivity(intent)
        }


        logoutButton.setOnClickListener{
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

            val call = apiService.logoutUser(authHeader)

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        // Pomyślna odpowiedź
                        tokenManager.clearToken()
                        val intent = Intent(context, LoginActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(context,"Użytkownik wylogowany pomyślnie",Toast.LENGTH_SHORT).show()
                    }
                }
                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                }
            })

        }

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

        val call = apiService.getUserData(authHeader,tokenRequest)

        call.enqueue(object : Callback<UserDataResponse> {
            override fun onResponse(
                call: Call<UserDataResponse>,
                response: Response<UserDataResponse>
            ) {
                if (response.isSuccessful) {
                    val userDataResponse = response.body()
                    welcomeText.text = "Witaj " + userDataResponse?.name
                }else{
                    welcomeText.text ="Witaj: Brak danych"
                    Toast.makeText(this@DashboardActivity, "Wystąpił błąd podczas pobierania danych użytkownika", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<UserDataResponse>, t: Throwable) {
            }
        })

        updateIncome()
        getExpenseList()

        if(expensesList.isNotEmpty()) {
            expensesListView.adapter
        }

        if(expensesList.isEmpty()){
            lastSpendVisibility.visibility = View.GONE
            lastSpendVisibilityFrame.visibility = View.GONE
        }

        if(totalIncome == 0.0){
            expenseButton.visibility = View.GONE
            pieChart.visibility = View.GONE
            categorySpinner.visibility = View.GONE
            lackOfData.visibility = View.VISIBLE
        }

        // Obsługa przycisku "Dochód"
        incomeButton.setOnClickListener {
            // Wyświetl dymek z miejscem do wprowadzenia dochodu
          if(totalIncome == 0.0) {

              val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
              builder.setTitle("Podaj dochód")

              val input = EditText(this)
              input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
              input.setBackgroundResource(R.drawable.blue_border) // Dodaj obramowanie
              input.setTextColor(resources.getColor(android.R.color.black))
              builder.setView(input)

              builder.setPositiveButton("OK") { dialog, _ ->
                  val income = input.text.toString()
                  if (income.isNotEmpty() && income.toDouble() != 0.00) {
                      // Tutaj można dodać kod do obsługi wprowadzonego dochodu
                      // np. zaktualizować wykres lub listę
                      totalIncome = income.toDouble()
                      expenseButton.visibility = View.VISIBLE
                      pieChart.visibility = View.VISIBLE
                      categorySpinner.visibility = View.VISIBLE
                      lackOfData.visibility = View.GONE
                      setUpDiagram()

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

                      val incomeRequest =
                          IncomeRequest(tokenManager.getTokenAccess().toString(), income.toDouble())

                      val call = apiService.addIncome(authHeader, incomeRequest)

                      call.enqueue(object : Callback<ResponseBody> {
                          override fun onResponse(
                              call: Call<ResponseBody>,
                              response: Response<ResponseBody>
                          ) {
                              if (response.isSuccessful) {
                                  // Pomyślna odpowiedź
                                  Toast.makeText(
                                      context,
                                      "Dochód dodany pomyślnie",
                                      Toast.LENGTH_SHORT
                                  ).show()
                              }
                          }

                          override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                          }
                      })


                  } else {
                      Toast.makeText(
                          this,
                          "Wprowadź poprawną wartość dochodu (niezerową).",
                          Toast.LENGTH_SHORT
                      ).show()
                  }

                  dialog.dismiss()
              }

              builder.setNegativeButton("Anuluj") { dialog, _ -> dialog.cancel() }
              builder.show()
          }else{
              Toast.makeText(this, "Dochód został już wprowadzony", Toast.LENGTH_SHORT).show()
          }
        }

        // Obsługa przycisku "Expense"
        expenseButton.setOnClickListener {
            // Wyświetl dymek z miejscem do wyboru kategorii

            val selectedCategory = arrayOf(-1) // Tablica do przechowywania wybranej kategorii

            val categoryDialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            categoryDialog.setTitle("Wybierz kategorię wydatku")
            categoryDialog.setSingleChoiceItems(categories, -1) { dialog, which ->
                selectedCategory[0] = which // Zapisz wybraną kategorię
            }

            categoryDialog.setPositiveButton("OK") { dialog, _ ->
                if (selectedCategory[0] != -1) {
                    // Wyświetl drugi dymek do wprowadzenia kwoty
                    val amountDialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
                    amountDialog.setTitle("Podaj kwotę wydatku")

                    val input = EditText(this)
                    input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                    input.setBackgroundResource(R.drawable.blue_border) // Dodaj obramowanie
                    input.setTextColor(resources.getColor(android.R.color.black))
                    amountDialog.setView(input)

                    amountDialog.setPositiveButton("OK") { _, _ ->
                        val enteredAmount = input.text.toString().trim()
                        if(enteredAmount.isNotEmpty() && enteredAmount.toDouble() != 0.00){
                            val selectedCategoryIndex = selectedCategory[0]
                            if(selectedCategoryIndex != -1 && (totalIncome * freeFounds/100.0)>=enteredAmount.toFloat()){
                                lastSpendVisibility.visibility = View.VISIBLE
                                lastSpendVisibilityFrame.visibility = View.VISIBLE

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

                                val expenseRequest = ExpenseRequest(tokenManager.getTokenAccess().toString(),categories[selectedCategoryIndex],enteredAmount.toDouble())

                                val call = apiService.saveExpense(authHeader,expenseRequest)

                                call.enqueue(object : Callback<ResponseBody> {
                                    override fun onResponse(
                                        call: Call<ResponseBody>,
                                        response: Response<ResponseBody>
                                    ) {
                                        if (response.isSuccessful) {
                                            // Pomyślna odpowiedź
                                            addExpense(selectedCategoryIndex,enteredAmount.toFloat())
                                            Toast.makeText(context,"Wydatek dodany pomyślnie",Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                    override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                                    }
                                })

                            }else{
                                Toast.makeText(this, "Nie masz wystarczających środków na koncie.", Toast.LENGTH_SHORT).show()
                            }
                        }else{
                            Toast.makeText(this, "Wprowadź poprawną wartość wydatku (nie zerową).", Toast.LENGTH_SHORT).show()
                        }

                    }

                    amountDialog.setNegativeButton("Anuluj") { dialog, _ -> dialog.cancel() }
                    amountDialog.show()
                } else {
                    Toast.makeText(this, "Proszę wybrać kategorię wydatku", Toast.LENGTH_SHORT).show()
                }
            }

            categoryDialog.setNegativeButton("Anuluj") { dialog, _ -> dialog.cancel() }
            categoryDialog.show()
        }


    }

    private fun addExpense(category: Int, amount: Float) {
        if (category != -1) {
            // Dodaj nowy wydatek na początku listy
            val selectedCategory = resources.getStringArray(R.array.categories)[category]
            expensesList.add(0, Expense(amount, selectedCategory,"PLN"))
            updateDiagram(category,amount)
            // Uaktualnij widok listy z animacją tylko w przypadku nowego elementu
            if (expensesList.size == 1) {
                val animation = AnimationUtils.loadAnimation(this, R.anim.slide_down)
                expensesListView.startAnimation(animation)
            }

            // Aktualizacja widoku listy
            (expensesListView.adapter as ExpenseAdapter).notifyDataSetChanged()

//             Usuń ostatni element z listy (jeśli jest więcej niż 5 elementów)
            if (expensesList.size > 5) {
                expensesList.removeAt(expensesList.size - 1)
            }
        }
    }

    private fun setUpDiagram(){
        entries.add(PieEntry(100f, "Wolne środki"))
        entries.add(PieEntry(0f,"Żywność"))
        entries.add(PieEntry(0f, "Chemia gospodarcza"))
        entries.add(PieEntry(0f,"Inne wydatki"))
        entries.add(PieEntry(0f,"Rachunki"))
        entries.add(PieEntry(0f,"Ubranie"))
        entries.add(PieEntry(0f,"Relaks"))
        entries.add(PieEntry(0f,"Transport"))
        entries.add(PieEntry(0f,"Mieszkanie"))
        entries.add(PieEntry(0f,"Zdrowie"))


        dataSet.colors = ArrayList<Int>()
        dataSet.colors.add(resources.getColor(R.color.teal_200))
        dataSet.colors.add(resources.getColor(R.color.teal_700))
        dataSet.colors.add(resources.getColor(R.color.purple_200))
        dataSet.colors.add(resources.getColor(R.color.purple_500))
        dataSet.colors.add(resources.getColor(R.color.purple_700))
        dataSet.colors.add(resources.getColor(R.color.light_green))
        dataSet.colors.add(resources.getColor(R.color.light_orange))
        dataSet.colors.add(resources.getColor(R.color.light_red))
        dataSet.colors.add(resources.getColor(R.color.light_yellow))
        dataSet.colors.add(resources.getColor(R.color.colorAccent))
        dataSet.setDrawValues(false)

        dataSet.valueTextSize = 18f
        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.description.isEnabled = false
        pieChart.setEntryLabelColor(android.R.color.black)
        pieChart.isRotationEnabled = true
        pieChart.legend.isEnabled = true
        pieChart.legend.isWordWrapEnabled = true
        pieChart.legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        pieChart.legend.textSize = 18f // Ustawienie rozmiaru czcionki legendy
        pieChart.animateY(1000)

        val expenseAdapter = ExpenseAdapter(this, expensesList)
        expensesListView.adapter = expenseAdapter
    }

    private fun updateDiagram(category: Int, amount: Float) {
        val categoryLabelPlot = resources.getStringArray(R.array.categories)[category]
        val newPercentage = (amount / totalIncome * 100).toFloat()
        freeFounds -= newPercentage
        val existingEntryIndex = entries.indexOfFirst { it.label == categoryLabelPlot }
        entries[existingEntryIndex] = PieEntry(entries[existingEntryIndex].value + newPercentage,categoryLabelPlot)
        entries[0]= PieEntry(freeFounds.toFloat(),"Wolne środki")

        // Tworzenie nowego obiektu PieDataSet z aktualnymi danymi
        dataSet = PieDataSet(entries, "")
        dataSet.colors = ArrayList<Int>()
        dataSet.colors.add(resources.getColor(R.color.teal_200))
        dataSet.colors.add(resources.getColor(R.color.teal_700))
        dataSet.colors.add(resources.getColor(R.color.purple_200))
        dataSet.colors.add(resources.getColor(R.color.purple_500))
        dataSet.colors.add(resources.getColor(R.color.purple_700))
        dataSet.colors.add(resources.getColor(R.color.light_green))
        dataSet.colors.add(resources.getColor(R.color.light_orange))
        dataSet.colors.add(resources.getColor(R.color.light_red))
        dataSet.colors.add(resources.getColor(R.color.light_yellow))
        dataSet.colors.add(resources.getColor(R.color.colorAccent))
        dataSet.setDrawValues(false)

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.notifyDataSetChanged() // Powiadom o zmianie danych w wykresie
        pieChart.invalidate() // Przerysuj wykres
    }

    private fun updateIncome(){
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

        val call = apiService.getIncome(authHeader,tokenRequest)

        call.enqueue(object : Callback<IncomeResponse> {
            override fun onResponse(
                call: Call<IncomeResponse>,
                response: Response<IncomeResponse>
            ) {
                if (response.isSuccessful) {
                    val incomeResponse = response.body()
                    if(incomeResponse?.value != null && incomeResponse.value > 0.0 ){
                        totalIncome = incomeResponse?.value!!
                        expenseButton.visibility = View.VISIBLE
                        pieChart.visibility = View.VISIBLE
                        categorySpinner.visibility = View.VISIBLE
                        lackOfData.visibility= View.GONE
                        setUpDiagram()}
                    else{
                        totalIncome = incomeResponse?.value!!
                    }
                }
            }
            override fun onFailure(call: Call<IncomeResponse>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Wystąpił błąd podczas pobierania danych użytkownika: " + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getExpenseList(){
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

        val call = apiService.getExpense(authHeader,tokenRequest)

        call.enqueue(object : Callback<List<ExpenseResponse>> {
            override fun onResponse(
                call: Call<List<ExpenseResponse>>,
                response: Response<List<ExpenseResponse>>
            ) {
                if (response.isSuccessful) {
                    val expenseResponse = response.body()
                    if(expenseResponse != null){
                        for (expense in expenseResponse){
                            addExpense(expense.category_id-1,expense.value.toFloat())
                        }
                        lastSpendVisibility.visibility = View.VISIBLE
                        lastSpendVisibilityFrame.visibility = View.VISIBLE
                    }
                }else{
                    Toast.makeText(this@DashboardActivity, "Wystąpił błąd podczas pobierania wydatków użytkownika", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<ExpenseResponse>>, t: Throwable) {
                Toast.makeText(this@DashboardActivity, "Wystąpił błąd podczas pobierania wydatków użytkownika: " + t.message, Toast.LENGTH_SHORT).show()
            }
        })
    }

    class ExpenseAdapter(private val context: Context, private val expenses: List<Expense>) :
        BaseAdapter() {
        override fun getCount(): Int {
            return expenses.size
        }

        override fun getItem(position: Int): Any {
            return expenses[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.expense_list_item, parent, false)

            val expense = expenses[position]
            val amountTextView = view.findViewById<TextView>(R.id.amountTextView)
            val categoryTextView = view.findViewById<TextView>(R.id.categoryTextView)
            val valueTextView = view.findViewById<TextView>(R.id.valueTextView)
            amountTextView.text = expense.amount.toString()
            categoryTextView.text = expense.category
            valueTextView.text=expense.value
            return view
        }
    }
}

