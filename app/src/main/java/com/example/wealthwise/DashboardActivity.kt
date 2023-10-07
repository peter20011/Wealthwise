package com.example.wealthwise


import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class DashboardActivity : AppCompatActivity() {

    private lateinit var incomeButton: Button
    private lateinit var expenseButton: Button
    private lateinit var incomeEditText: EditText
    private lateinit var expenseEditText: EditText
    private lateinit var categorySpinner: Spinner
    private lateinit var pieChart: PieChart
    private lateinit var expensesListView: ListView
    private lateinit var welcomeText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        val logoutButton = findViewById<Button>(R.id.logoutButton)

        logoutButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        // Ustawienie tekstu powitania
        val username = "John"
        welcomeText.text = "Witaj, $username"

        // Obsługa przycisku "Dochód"
        incomeButton.setOnClickListener {
            // Wyświetl dymek z miejscem do wprowadzenia dochodu
            val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            builder.setTitle("Podaj dochód")

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            input.setBackgroundResource(R.drawable.gray_border) // Dodaj obramowanie
            input.setTextColor(resources.getColor(android.R.color.black))
            builder.setView(input)

            builder.setPositiveButton("OK") { dialog, _ ->
                val income = input.text.toString().toFloat()
                // Tutaj można dodać kod do obsługi wprowadzonego dochodu
                // np. zaktualizować wykres lub listę
                dialog.dismiss()
            }

            builder.setNegativeButton("Anuluj") { dialog, _ -> dialog.cancel() }
            builder.show()
        }

        // Obsługa przycisku "Expense"
        expenseButton.setOnClickListener {
            // Wyświetl dymek z miejscem do wyboru kategorii
            val categories = arrayOf(
                "Żywność",
                "Chemia gospodarcza",
                "Inne wydatki",
                "Rachunki",
                "Ubranie",
                "Relaks",
                "Transport",
                "Mieszkanie"
            )

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
                    input.setBackgroundResource(R.drawable.gray_border) // Dodaj obramowanie
                    input.setTextColor(resources.getColor(android.R.color.black))
                    amountDialog.setView(input)

                    amountDialog.setPositiveButton("OK") { _, _ ->
                        val selectedAmount = input.text.toString().toFloat()
                        // Tutaj można dodać kod do obsługi wyboru kategorii i wprowadzonej kwoty
                        // np. zaktualizować wykres lub listę
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

        // Przykładowe dane dla wykresu kołowego


        val entries = ArrayList<PieEntry>()
        val dataSet = PieDataSet(entries, "Rozkład towich wydatków")
        entries.add(PieEntry(30f, "Żywność"))
        entries.add(PieEntry(20f, "Chemia gospodarcza"))
        entries.add(PieEntry(10f, "Inne wydatki"))
        entries.add(PieEntry(15f, "Rachunki"))
        entries.add(PieEntry(25f, "Ubranie"))


        dataSet.colors = ArrayList<Int>()
        dataSet.colors.add(resources.getColor(R.color.teal_200))
        dataSet.colors.add(resources.getColor(R.color.teal_700))
        dataSet.colors.add(resources.getColor(R.color.purple_200))
        dataSet.colors.add(resources.getColor(R.color.purple_500))
        dataSet.colors.add(resources.getColor(R.color.purple_700))

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

        // Przykładowe dane dla listy wydatków
        val expenses = ArrayList<String>()
        expenses.add("Wydatek 1")
        expenses.add("Wydatek 2")
        expenses.add("Wydatek 3")
        expenses.add("Wydatek 4")
        expenses.add("Wydatek 5")

        val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, expenses)
        expensesListView.adapter = adapter
    }
}