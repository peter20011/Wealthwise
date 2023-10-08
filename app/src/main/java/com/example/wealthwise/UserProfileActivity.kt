package com.example.wealthwise

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AlertDialog
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast

class UserProfileActivity : AppCompatActivity() {

    private lateinit var firstNameTextView: TextView
    private lateinit var lastNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var birthDateTextView: TextView
    private lateinit var resetPasswordButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        // Inicjalizacja widoków
        firstNameTextView = findViewById(R.id.firstNameTextView)
        lastNameTextView = findViewById(R.id.lastNameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        birthDateTextView = findViewById(R.id.birthDateTextView)
        resetPasswordButton = findViewById(R.id.resetPasswordButton)

        // Ustawienie informacji o użytkowniku (możesz pobrać te dane z serwera lub SharedPreferences)
        firstNameTextView.text = "Imię: John"
        lastNameTextView.text = "Nazwisko: Doe"
        emailTextView.text = "Email: johndoe@example.com"
        birthDateTextView.text = "Data urodzenia: 01-01-1990"

        // Obsługa przycisku "Zresetuj hasło"
        resetPasswordButton.setOnClickListener {
            showResetPasswordDialog()
        }

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


    }

    // Metoda do wyświetlenia okna dialogowego do resetowania hasła
    private fun showResetPasswordDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Zresetuj hasło")

        val currentPasswordInput = EditText(this)
        currentPasswordInput.hint = "Obecne hasło"
        currentPasswordInput.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(currentPasswordInput)

        val newPasswordInput = EditText(this)
        newPasswordInput.hint = "Nowe hasło"
        newPasswordInput.inputType = InputType.TYPE_TEXT_VARIATION_PASSWORD
        builder.setView(newPasswordInput)

        builder.setPositiveButton("Zmień") { _, _ ->
            val currentPassword = currentPasswordInput.text.toString()
            val newPassword = newPasswordInput.text.toString()

            // Tutaj możesz dodać logikę do zmiany hasła na serwerze lub w lokalnym składowisku
            // Upewnij się, że nowe hasło spełnia wymagania bezpieczeństwa
            Toast.makeText(this, "Hasło zostało zmienione", Toast.LENGTH_SHORT).show()
        }

        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.cancel()
        }

        builder.show()
    }
}