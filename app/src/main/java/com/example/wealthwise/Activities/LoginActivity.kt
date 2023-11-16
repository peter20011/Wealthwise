package com.example.wealthwise.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.wealthwise.ApiService
import com.example.wealthwise.DataClass.LoginData
import com.example.wealthwise.DataClass.TokenResponse
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
import java.util.regex.Pattern
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class LoginActivity : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        val context : Context = applicationContext
        val tokenManager = TokenManager(this)

        loginButton.setOnClickListener {
            // Tutaj można dodać logikę logowania
            // Po zalogowaniu, przekieruje użytkownika do odpowiedniej aktywności
            // Na przykład, jeśli zalogowano pomyślnie, można użyć poniższej linii kodu
            // do przejścia do aktywności głównej (MainActivity):

            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val BASE_URL = "https://10.0.2.2:8443"

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Wszystkie pola muszą być wypełnione", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                    "[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"

            if (!Pattern.matches(emailPattern, email)) {
                Toast.makeText(this, "Nieprawidłowy adres email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
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

            val apiService = retrofit.create(ApiService::class.java)
            val loginData = LoginData(email, password)

            val call = apiService.loginUser(loginData)

            call.enqueue(object : Callback<TokenResponse> {
                @SuppressLint("SuspiciousIndentation")
                override fun onResponse(
                    call: Call<TokenResponse>,
                    response: Response<TokenResponse>
                ) {
                    if (response.isSuccessful) {
                        // Pomyślna odpowiedź
                        var tokenResponse = response.body()
                            if(tokenResponse != null){
                                tokenManager.saveToken(tokenResponse.tokenAccess,tokenResponse.tokenRefresh)
                                val intent = Intent(this@LoginActivity, DashboardActivity::class.java)
                                startActivity(intent)
                            }
                        Toast.makeText(context,"Użytkownik zalogowany pomyślnie",Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context,"Błąd danych logowania",Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<TokenResponse>, t: Throwable) {
                }
            })

        }

        registerButton.setOnClickListener {
            // Po kliknięciu przycisku "Zarejestruj się", przejdź do aktywności rejestracji
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }
}


