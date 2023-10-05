package com.example.wealthwise

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        // Tutaj można dodać logikę, która uruchamia się po załadowaniu aplikacji
        // np. sprawdzenie sesji użytkownika i przekierowanie do ekranu logowania lub głównego

        // Przykład opóźnienia przekierowania do innej aktywności
        Handler().postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000) // Opóźnienie w milisekundach (tutaj 2000ms lub 2 sekundy)
    }
}



