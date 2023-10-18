package com.example.wealthwise


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
    private lateinit var lackOfData : TextView
    private lateinit var lastSpendVisibility : TextView
    private lateinit var lastSpendVisibilityFrame : ListView
    private var totalIncome = 0.00
    private var freeFounds = 100.00
    private val expensesList = arrayListOf<Expense>()
    private var categories = arrayOf(
        "Żywność",
        "Chemia gospodarcza",
        "Inne wydatki",
        "Rachunki",
        "Ubranie",
        "Relaks",
        "Transport",
        "Mieszkanie"
    )
    private var entries = ArrayList<PieEntry>()
    private var dataSet = PieDataSet(entries, "")

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
        lackOfData = findViewById(R.id.lackOfData)
        lastSpendVisibility = findViewById(R.id.lastSpend)
        lastSpendVisibilityFrame = findViewById(R.id.expensesListView)

        val homeIcon = findViewById<ImageView>(R.id.homeIcon)
        val statisticIcon = findViewById<ImageView>(R.id.statisticIcon)
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        val assetsIcon = findViewById<ImageView>(R.id.assetsIcon)

        val logoutButton = findViewById<Button>(R.id.logoutButton)

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

        // Ustawienie tekstu powitania
        val username = "John"
        welcomeText.text = "Witaj, $username"

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
            val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            builder.setTitle("Podaj dochód")

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
            input.setBackgroundResource(R.drawable.blue_border) // Dodaj obramowanie
            input.setTextColor(resources.getColor(android.R.color.black))
            builder.setView(input)

            builder.setPositiveButton("OK") { dialog, _ ->
                val income = input.text.toString()
                if(income.isNotEmpty() && income.toDouble() != 0.00){
                    // Tutaj można dodać kod do obsługi wprowadzonego dochodu
                    // np. zaktualizować wykres lub listę
                    totalIncome=income.toDouble()
                    expenseButton.visibility = View.VISIBLE
                    pieChart.visibility = View.VISIBLE
                    categorySpinner.visibility = View.VISIBLE
                    lackOfData.visibility= View.GONE
                    setUpDiagram()
                }else{
                    Toast.makeText(this, "Wprowadź poprawną wartość dochodu (niezerową).", Toast.LENGTH_SHORT).show()
                }

                dialog.dismiss()
            }

            builder.setNegativeButton("Anuluj") { dialog, _ -> dialog.cancel() }
            builder.show()
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
                                addExpense(selectedCategoryIndex,enteredAmount.toFloat())
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

            // Usuń ostatni element z listy (jeśli jest więcej niż 5 elementów)
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
        dataSet.setDrawValues(false)

        val data = PieData(dataSet)
        pieChart.data = data
        pieChart.notifyDataSetChanged() // Powiadom o zmianie danych w wykresie
        pieChart.invalidate() // Przerysuj wykres
    }

    data class Expense(val amount: Float, val category: String, val value : String)

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