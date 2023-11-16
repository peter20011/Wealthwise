package com.example.wealthwise.Activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wealthwise.ApiService
import com.example.wealthwise.DataClass.ChangePassword
import com.example.wealthwise.DataClass.SavingsGoal
import com.example.wealthwise.DataClass.SavingsGoalRequest
import com.example.wealthwise.DataClass.TokenRequest
import com.example.wealthwise.DataClass.UserDataResponse
import com.example.wealthwise.R
import com.example.wealthwise.Adapters.SavingsGoalAdapter
import com.example.wealthwise.Manager.TokenManager
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
import java.util.regex.Pattern
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class UserProfileActivity : AppCompatActivity() {

    private lateinit var firstNameTextView: TextView
    private lateinit var lastNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var birthDateTextView: TextView
    private lateinit var resetPasswordButton: Button
    private lateinit var addSavingsGoalButton : Button
    private var savingsGoals = mutableListOf<SavingsGoal>()
    private lateinit var adapter: SavingsGoalAdapter
    private val BASE_URL = "https://10.0.2.2:8443"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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


        setContentView(R.layout.activity_user_profile)

        // Inicjalizacja widoków
        firstNameTextView = findViewById(R.id.firstNameTextView)
        lastNameTextView = findViewById(R.id.lastNameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        birthDateTextView = findViewById(R.id.birthDateTextView)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)
        addSavingsGoalButton = findViewById(R.id.addSavingsGoalButton)

        val recyclerView = findViewById<RecyclerView>(R.id.savingsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = SavingsGoalAdapter(savingsGoals,resources)
        recyclerView.adapter = adapter

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
                    firstNameTextView.text = "Imię: " + userDataResponse?.name
                    lastNameTextView.text = "Nazwisko: " + userDataResponse?.surname
                    emailTextView.text = "Email: " + userDataResponse?.email
                    birthDateTextView.text = "Data urodzenia: " + userDataResponse?.birthDay
                }else{
                    firstNameTextView.text = "Imię: Brak danych"
                    lastNameTextView.text = "Nazwisko: Brak danych"
                    emailTextView.text = "Email: Brak danych"
                    birthDateTextView.text = "Data urodzenia: Brak danych"
                    Toast.makeText(this@UserProfileActivity, "Wystąpił błąd podczas pobierania danych użytkownika", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<UserDataResponse>, t: Throwable) {
                Toast.makeText(this@UserProfileActivity, "Wystąpił błąd podczas pobierania danych użytkownika: " + t.message, Toast.LENGTH_SHORT).show()
            }
        })

        val call2 = apiService.getSavingsGoal(authHeader,tokenRequest)

        call2.enqueue(object : Callback<List<SavingsGoal>> {
            override fun onResponse(
                call: Call<List<SavingsGoal>>,
                response: Response<List<SavingsGoal>>
            ) {
                if (response.isSuccessful) {
                    val savingsGoalResponse = response.body()
                    if (savingsGoalResponse != null) {
                        savingsGoals.addAll(savingsGoalResponse)
                        adapter.notifyDataSetChanged()
                    }
                }else{
                    Toast.makeText(this@UserProfileActivity, "Wystąpił błąd podczas pobierania celu oszczędzania", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<List<SavingsGoal>>, t: Throwable) {
                Toast.makeText(this@UserProfileActivity, "Wystąpił błąd podczas pobierania  celu oszczędzania: " + t.message, Toast.LENGTH_SHORT).show()
            }
        })


        // Pasek nawigacyjny - obsługa ikon
        val homeIcon = findViewById<ImageView>(R.id.homeIcon)
        val statisticIcon = findViewById<ImageView>(R.id.statisticIcon)
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        val assetsIcon = findViewById<ImageView>(R.id.assetsIcon)

        homeIcon.setBackgroundResource(0)
        statisticIcon.setBackgroundResource(0) // Usuwa tło lub obramowanie
        profileIcon.setBackgroundResource(R.drawable.blue_border)
        assetsIcon.setBackgroundResource(0)

        homeIcon.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        statisticIcon.setOnClickListener{
            val intent=Intent(this, StatisticActivity::class.java)
            startActivity(intent)
        }

        assetsIcon.setOnClickListener{
            val intent=Intent(this, AssetsActivity::class.java)
            startActivity(intent)
        }

        resetPasswordButton.setOnClickListener {
            showResetPasswordDialog()
        }

        addSavingsGoalButton.setOnClickListener{
            addSavingsGoal(adapter)
        }
    }

    // Metoda do wyświetlenia dwuetapowego okna dialogowego do resetowania hasła
     fun showResetPasswordDialog() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle("Podaj obecne hasło")

        val currentPasswordInput = EditText(this)
        currentPasswordInput.hint = "Obecne hasło"
        currentPasswordInput.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        currentPasswordInput.setBackgroundResource(R.drawable.blue_border)
        currentPasswordInput.setTextColor(resources.getColor(android.R.color.black))
        builder.setView(currentPasswordInput)

        builder.setPositiveButton("Dalej") { _, _ ->
            val newPasswordInput = EditText(this)
            newPasswordInput.hint = "Podaj nowe haslo"
            newPasswordInput.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
            newPasswordInput.setBackgroundResource(R.drawable.blue_border) // Dodaj obramowanie
            newPasswordInput.setTextColor(resources.getColor(android.R.color.black))

            val newPasswordLayout = LinearLayout(this)
            newPasswordLayout.orientation = LinearLayout.VERTICAL
            newPasswordLayout.addView(newPasswordInput)


            val newPasswordBuilder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            newPasswordBuilder.setTitle("Nowe hasło")
            newPasswordBuilder.setView(newPasswordLayout)

            newPasswordBuilder.setPositiveButton("Zmień") { _, _ ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val passwordPattern = "^[a-zA-Z]{6,9}\\d$"

                // Tutaj można dodać logikę do zmiany hasła na serwerze lub w lokalnym składowisku
                if (currentPassword.isNotEmpty() && newPassword.isNotEmpty() && Pattern.matches(passwordPattern, newPassword)){
                    val tokenManager = TokenManager(this)
                    if(tokenManager.refreshTokenIfNeeded(resources)){
                        Toast.makeText(this, "Token odświeżony", Toast.LENGTH_SHORT).show()
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
                    val changePassword = ChangePassword(tokenManager.getTokenAccess().toString(),currentPassword, newPassword)
                    val apiService = retrofit.create(ApiService::class.java)

                    val call = apiService.changePassword(authHeader, changePassword)

                    call.enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@UserProfileActivity, "Hasło zostało zmienione", Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(this@UserProfileActivity, "Wystąpił błąd podczas zmiany hasła", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Toast.makeText(this@UserProfileActivity, "Wystąpił błąd podczas zmiany hasła: " + t.message, Toast.LENGTH_SHORT).show()
                        }
                    })

                } else {
                    Toast.makeText(this@UserProfileActivity, "Błąd podczas zmiany hasła. Sprawdź dane i spróbuj ponownie.", Toast.LENGTH_SHORT).show()
                }
            }

            newPasswordBuilder.setNegativeButton("Anuluj") { dialog, _ ->
                dialog.cancel()
            }

            newPasswordBuilder.show()
        }

        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

    private fun addSavingsGoal(adapter: SavingsGoalAdapter) {
        val tokenManager = TokenManager(this)
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle("Podaj cel oszczędzania")

        val currentSavingsGoal = EditText(this)
        currentSavingsGoal.inputType = InputType.TYPE_CLASS_TEXT
        currentSavingsGoal.setBackgroundResource(R.drawable.blue_border)
        currentSavingsGoal.setTextColor(resources.getColor(android.R.color.black))
        builder.setView(currentSavingsGoal)

        builder.setPositiveButton("Dalej") { _, _ ->
            val newSavingsInput = EditText(this)
            newSavingsInput.hint = "Podaj kwotę"
            newSavingsInput.inputType = InputType.TYPE_CLASS_NUMBER
            newSavingsInput.setBackgroundResource(R.drawable.blue_border) // Dodaj obramowanie
            newSavingsInput.setTextColor(resources.getColor(android.R.color.black))

            val newSavingsLayout = LinearLayout(this)
            newSavingsLayout.orientation = LinearLayout.VERTICAL
            newSavingsLayout.addView(newSavingsInput)


            val newGoalBuilder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            newGoalBuilder.setTitle("Kwota docelowa")
            newGoalBuilder.setView(newSavingsLayout)

            newGoalBuilder.setPositiveButton("Dodaj") { _, _ ->
                val savingsGoalTopic = currentSavingsGoal.text.toString()
                val savingsCosts = newSavingsInput.text.toString()

                if (savingsGoalTopic.isNotEmpty() && savingsCosts.isNotEmpty()) {
                    val newSavingsGoal = SavingsGoal(savingsGoalTopic, 0.0, savingsCosts.toDouble(), true)
                    savingsGoals.add(newSavingsGoal)

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

                    val call = apiService.createSavingsGoal(authHeader,
                        SavingsGoalRequest(tokenManager.getTokenAccess().toString(), savingsGoalTopic, savingsCosts.toDouble())
                    )

                    call.enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@UserProfileActivity, "Cel oszczędzania został dodany", Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(this@UserProfileActivity, "Wystąpił błąd podczas dodawania celu oszczędzania", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            Toast.makeText(this@UserProfileActivity, "Wystąpił błąd podczas dodawania celu oszczędzania: " + t.message, Toast.LENGTH_SHORT).show()
                        }
                    })

                    // Aktualizuj wartość postępu w mapie
                    adapter.updateProgress(savingsGoals.indexOf(newSavingsGoal), 0)
                    adapter.notifyDataSetChanged()
                }
            }

            newGoalBuilder.setNegativeButton("Anuluj") { dialog, _ ->
                dialog.cancel()
            }

            newGoalBuilder.show()
        }

        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }

}