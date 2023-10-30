package com.example.wealthwise

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import at.favre.lib.crypto.bcrypt.BCrypt
import com.example.wealthwise.DataClass.RegistrationData
import java.util.regex.Pattern
import java.text.SimpleDateFormat
import java.text.ParseException
import okhttp3.*
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Callback
import retrofit2.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor

class RegistrationActivity : AppCompatActivity() {

    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var birthDateEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var passwordNewEditText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        // Znajdź i zainicjuj pola wejściowe
        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        birthDateEditText = findViewById(R.id.birthDateEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        passwordNewEditText = findViewById(R.id.passwordNewEditText)


        val backButton = findViewById<ImageView>(R.id.backButton)
        val submitButton = findViewById<Button>(R.id.submitButton)
        val context: Context = applicationContext


        backButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Dodaj obsługę przycisku "Wyślij"
        submitButton.setOnClickListener {
            val name = firstNameEditText.text.toString()
            val surname = lastNameEditText.text.toString()
            val email = emailEditText.text.toString()
            val birthDay = birthDateEditText.text.toString()
            val password = passwordEditText.text.toString()
            val confirmPassword = passwordNewEditText.text.toString()
            val BASE_URL = "http://10.0.2.2:8080"


            if (name.isEmpty() || surname.isEmpty() || email.isEmpty() || birthDay.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Wszystkie pola muszą być wypełnione", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            // Walidacja adresu email
            val emailPattern = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                    "[a-zA-Z0-9_+&*-]+)*@" +
                    "(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"

            if (!Pattern.matches(emailPattern, email)) {
                Toast.makeText(this, "Nieprawidłowy adres email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Walidacja hasła
            val passwordPattern = "^[a-zA-Z]{6,9}\\d$"

            if (!Pattern.matches(passwordPattern, password) || password != confirmPassword) {
                Toast.makeText(
                    this,
                    "Nieprawidłowe hasło lub hasło nie pasuje do powtórzonego hasła",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }



            // Walidacja daty urodzenia w formacie "DD-MM-RRRR"
            val datePattern = "^(0[1-9]|[12][0-9]|3[01])-(0[1-9]|1[0-2])-\\d{4}$"

            if (!Pattern.matches(datePattern, birthDay)) {
                Toast.makeText(
                    this,
                    "Nieprawidłowy format daty urodzenia (DD-MM-RRRR)",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            try {
                // Sprawdzenie, czy data jest poprawna
                val dateFormat = SimpleDateFormat("dd-MM-yyyy")
                dateFormat.isLenient = false
                dateFormat.parse(birthDay)
            } catch (e: ParseException) {
                Toast.makeText(this, "Nieprawidłowa data urodzenia", Toast.LENGTH_SHORT).show()
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

            // Utwórz obiekt reprezentujący dane rejestracji
            val registrationData = RegistrationData(name, surname, birthDay, email, password, confirmPassword)
            val call = apiService.registerUser(registrationData)

            call.enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        // Pomyślna odpowiedź
                        Toast.makeText(context, "Zarejestrowano pomyślnie", Toast.LENGTH_SHORT)
                            .show()
                        val intent = Intent(context, LoginActivity::class.java)
                        startActivity(intent)
                    } else {
                        Toast.makeText(
                            context,
                            "Użytkownik już istnieje o podanym adresie email",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                }
            })
        }
    }
}

