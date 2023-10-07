package com.example.wealthwise

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton = findViewById<Button>(R.id.loginButton)
        val registerButton = findViewById<Button>(R.id.registerButton)

        loginButton.setOnClickListener {
            // Tutaj można dodać logikę logowania
            // Po zalogowaniu, przekieruje użytkownika do odpowiedniej aktywności
            // Na przykład, jeśli zalogowano pomyślnie, można użyć poniższej linii kodu
            // do przejścia do aktywności głównej (MainActivity):

             val intent = Intent(this, DashboardActivity::class.java)
             startActivity(intent)
        }

        registerButton.setOnClickListener {
            // Po kliknięciu przycisku "Zarejestruj się", przejdź do aktywności rejestracji
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
        }
    }
}