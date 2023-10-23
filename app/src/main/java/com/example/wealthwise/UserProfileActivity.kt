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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class UserProfileActivity : AppCompatActivity() {

    private lateinit var firstNameTextView: TextView
    private lateinit var lastNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var birthDateTextView: TextView
    private lateinit var resetPasswordButton: Button
    private lateinit var addSavingsGoalButton : Button
    private val savingsGoals = mutableListOf<SavingsGoal>()
    private lateinit var adapter: SavingsGoalAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        adapter = SavingsGoalAdapter(savingsGoals)
        recyclerView.adapter = adapter

        // Ustawienie informacji o użytkowniku (można pobrać te dane z serwera lub SharedPreferences)
        firstNameTextView.text = "Imię: John"
        lastNameTextView.text = "Nazwisko: Doe"
        emailTextView.text = "Email: johndoe@example.com"
        birthDateTextView.text = "Data urodzenia: 01-01-1990"

        // Obsługa przycisku "Zresetuj hasło"
        resetPasswordButton.setOnClickListener {
            showResetPasswordDialog()
        }

        addSavingsGoalButton.setOnClickListener{
            addSavingsGoal(adapter)
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

        statisticIcon.setOnClickListener{
            val intent=Intent(this,StatisticActivity::class.java)
            startActivity(intent)
        }

        assetsIcon.setOnClickListener{
            val intent=Intent(this,AssetsActivity::class.java)
            startActivity(intent)
        }
    }

    // Metoda do wyświetlenia dwuetapowego okna dialogowego do resetowania hasła
    private fun showResetPasswordDialog() {
        val builder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
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


            val newPasswordBuilder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
            newPasswordBuilder.setTitle("Nowe hasło")
            newPasswordBuilder.setView(newPasswordLayout)

            newPasswordBuilder.setPositiveButton("Zmień") { _, _ ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()

                // Tutaj można dodać logikę do zmiany hasła na serwerze lub w lokalnym składowisku
                if (currentPassword.isNotEmpty() && newPassword.isNotEmpty()) {
                    Toast.makeText(this, "Hasło zostało zmienione", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Błąd podczas zmiany hasła. Sprawdź dane i spróbuj ponownie.", Toast.LENGTH_SHORT).show()
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
        val builder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
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


            val newGoalBuilder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
            newGoalBuilder.setTitle("Kwota docelowa")
            newGoalBuilder.setView(newSavingsLayout)

            newGoalBuilder.setPositiveButton("Dodaj") { _, _ ->
                val savingsGoalTopic = currentSavingsGoal.text.toString()
                val savingsCosts = newSavingsInput.text.toString()

                if (savingsGoalTopic.isNotEmpty() && savingsCosts.isNotEmpty()) {
                    val newSavingsGoal = SavingsGoal(savingsGoalTopic, 0.0, savingsCosts.toDouble(), true)
                    savingsGoals.add(newSavingsGoal)


                    // Oblicz postęp w procentach
                    val percentProgress = (newSavingsGoal.currentAmount / newSavingsGoal.targetAmount * 100).toInt()

                    // Aktualizuj wartość postępu w mapie
                    adapter.updateProgress(savingsGoals.indexOf(newSavingsGoal), percentProgress)

                    Toast.makeText(this, "Ustalono cel oszczędzania", Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged()
                } else {
                    Toast.makeText(this, "Błąd podczas ustalania celu oszczędzania.", Toast.LENGTH_SHORT).show()
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