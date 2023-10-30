package com.example.wealthwise

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.regex.Pattern

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
            val BASE_URL = "http://10.0.2.2:8080"

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

            val client = OkHttpClient.Builder()
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


data class LoginData( val email: String, val password: String)
data class TokenResponse( val tokenAccess: String, val tokenRefresh: String)