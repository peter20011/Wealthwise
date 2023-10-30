package com.example.wealthwise

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Callback
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Response


class AssetsActivity : AppCompatActivity() {
    private lateinit var currency1Name: TextView
    private lateinit var currency2Name: TextView
    private lateinit var currency3Name: TextView
    private lateinit var currency4Name: TextView
    private lateinit var currency1Rate: TextView
    private lateinit var currency2Rate: TextView
    private lateinit var currency3Rate: TextView
    private lateinit var currency4Rate: TextView
    private var selectedCurrency: String? = null
    private lateinit var assetsContainer : LinearLayout
    private val selectedAssets = mutableListOf<View>()

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

        if(tokenManager.refreshTokenIfNeeded()){
            Toast.makeText(this, "Token odświeżony", Toast.LENGTH_SHORT).show()
        }



        setContentView(R.layout.activity_assets)

        val homeIcon = findViewById<ImageView>(R.id.homeIcon)
        val statisticIcon = findViewById<ImageView>(R.id.statisticIcon)
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        val assetsIcon = findViewById<ImageView>(R.id.assetsIcon)


        homeIcon.setBackgroundResource(0)
        statisticIcon.setBackgroundResource(0)
        profileIcon.setBackgroundResource(0)
        assetsIcon.setBackgroundResource(R.drawable.blue_border)

        homeIcon.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
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

        // Inicjalizacja widoków
        currency1Name = findViewById(R.id.currency1Name)
        currency2Name = findViewById(R.id.currency2Name)
        currency3Name = findViewById(R.id.currency3Name)
        currency4Name = findViewById(R.id.currency4Name)
        currency1Rate = findViewById(R.id.currency1Rate)
        currency2Rate = findViewById(R.id.currency2Rate)
        currency3Rate = findViewById(R.id.currency3Rate)
        currency4Rate = findViewById(R.id.currency4Rate)
        assetsContainer =  findViewById(R.id.assetsContainer)
        assetsContainer.visibility = View.GONE

        // Pobieranie kursów walut
        fetchCurrencyRates()

        // Obsługa przycisku "Dodaj Aktywo"
        val addAssetButton = findViewById<Button>(R.id.addAssetButton)
        addAssetButton.setOnClickListener {
            // Wybierz typ aktywa
            val selectedType = findViewById<RadioGroup>(R.id.assetTabs).checkedRadioButtonId
            when (selectedType) {
                R.id.tabCurrency -> showCurrencyDialog()
                R.id.tabStocks -> showStocksDialog()
                R.id.tabBonds -> showBondsDialog()
            }
        }

        val deleteAssetButton = findViewById<Button>(R.id.deleteAssetButton)
        deleteAssetButton.setOnClickListener {
            deleteSelectedAssets()
        }

    }

    private fun fetchCurrencyRates() {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.nbp.pl/api/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

        val service = retrofit.create(ApiService::class.java)
        val callCurrency: Call<List<CurrencyData>> = service.getCurrencyRates()

        callCurrency.enqueue(object : Callback<List<CurrencyData>> {
            override fun onResponse(call: Call<List<CurrencyData>>, response: Response<List<CurrencyData>>) {
                if (response.isSuccessful) {
                    val currencyDataList = response.body()
                    if (currencyDataList != null) {
                        val currencyMap = HashMap<String, Double>()

                        // Przetwarzanie danych i tworzenie mapy
                        for (rate in currencyDataList[0].rates) {
                            currencyMap[rate.code] = rate.mid
                        }

                        // Oto masz gotową mapę kursów walut: currencyMap
                                updateCurrencyViews(currencyMap)
                    } else {
                        println("Brak danych o kursach walut.")
                    }
                } else {
                    println("Błąd w pobieraniu danych.")
                }
            }

            override fun onFailure(call: Call<List<CurrencyData>>, t: Throwable) {
                println("Wystąpił błąd podczas pobierania danych: ${t.message}")
            }
        })
    }

    private fun updateCurrencyViews(currencyRates: Map<String, Double>) {
        currency1Rate.text = formatCurrencyValue(currencyRates["USD"])
        currency2Rate.text = formatCurrencyValue(currencyRates["EUR"])
        currency3Rate.text = formatCurrencyValue(currencyRates["CHF"])
        currency4Rate.text = formatCurrencyValue(currencyRates["GBP"])
    }

    private fun formatCurrencyValue(value: Double?): String {
        return if (value != null) {
            String.format("%.2f", value)
        } else {
            "Brak danych"
        }
    }

    private fun showCurrencyDialog() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle("Dodaj Aktywo Typu Waluty")

        // Create a RadioGroup to allow selecting one currency
        val currencyTypes = arrayOf("USD", "EUR", "CHF", "GBP")
        val checkedItem = currencyTypes.indexOf(selectedCurrency) // Set the initially checked item

        builder.setSingleChoiceItems(currencyTypes, checkedItem) { dialog, which ->
            // Handle the selected currency here
            selectedCurrency = currencyTypes[which]
        }

        builder.setPositiveButton("OK") { dialog, _ ->
            if (selectedCurrency != null) {
                showValueInputDialog()
                dialog.dismiss()
            } else {
                // Inform the user that they must select a currency
                Toast.makeText(this, "Proszę wybrać walutę", Toast.LENGTH_SHORT).show()
            }
        }

        builder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create() // Create the AlertDialog

        dialog.show()
    }

    private fun showValueInputDialog() {
        val valueBuilder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
        valueBuilder.setTitle("Podaj Wartość")

        // Set up the input field in the AlertDialog
        val inputField = EditText(this)
        inputField.inputType= InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        inputField.setBackgroundResource(R.drawable.blue_border)
        inputField.setTextColor(resources.getColor(android.R.color.black))
        valueBuilder.setView(inputField)

        valueBuilder.setPositiveButton("Dodaj") { _, _ ->
            // Handle the entered value
            val enteredValue = inputField.text.toString()

            if(enteredValue.isNotEmpty()){
                val value=enteredValue.toDouble()
                if(value > 0){
                    // Show an AlertDialog to inform about the currency and entered value
                    assetsContainer.visibility = View.VISIBLE
                    showCurrencyAndValueAlert(selectedCurrency, enteredValue)
                }else{
                    Toast.makeText(this, "Wartość musi być większa od zera", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Proszę wprowadzić wartość", Toast.LENGTH_SHORT).show()
            }
        }

        valueBuilder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.dismiss()
        }

        valueBuilder.show()
    }

    private fun showCurrencyAndValueAlert(currency: String?, value: String) {
        val alertBuilder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
        alertBuilder.setTitle("Dodano Aktywo")
        alertBuilder.setMessage("Wybrano walutę: $currency\n\nWartość: $value")
        alertBuilder.setPositiveButton("OK") { dialog, _ ->
            // Handle the OK button click if needed
            addAssetToContainerWithAnimation(currency,value,"Waluta")
            dialog.dismiss()
        }

        alertBuilder.show()
    }

    private fun showStocksDialog() {
        val sharesBuilder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
        sharesBuilder.setTitle("Podaj Wartość")

        // Set up the input field in the AlertDialog
        val inputField = EditText(this)
        inputField.inputType= InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        inputField.setBackgroundResource(R.drawable.blue_border)
        inputField.setTextColor(resources.getColor(android.R.color.black))
        sharesBuilder.setView(inputField)

        sharesBuilder.setPositiveButton("Dodaj") { _, _ ->
            // Handle the entered value
            val enteredValue = inputField.text.toString()

            // Show an AlertDialog to inform about the currency and entered value
            if(enteredValue.isNotEmpty()){
                val value = enteredValue.toDouble()
                if(value > 0 ){
                    showAssetAndValueAlert("Akcje", enteredValue)
                }else{
                    Toast.makeText(this, "Wartość musi być większa od zera", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Proszę wprowadzić wartość", Toast.LENGTH_SHORT).show()
            }

        }

        sharesBuilder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.dismiss()
        }
        sharesBuilder.show()
    }

    private fun showBondsDialog() {
        val sharesBuilder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
        sharesBuilder.setTitle("Podaj Wartość")

        // Set up the input field in the AlertDialog
        val inputField = EditText(this)
        inputField.inputType= InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        inputField.setBackgroundResource(R.drawable.blue_border)
        inputField.setTextColor(resources.getColor(android.R.color.black))
        sharesBuilder.setView(inputField)

        sharesBuilder.setPositiveButton("Dodaj") { _, _ ->
            // Handle the entered value
            val enteredValue = inputField.text.toString()

            if(enteredValue.isNotEmpty()){
                val value = enteredValue.toDouble();
                if(value > 0 ){
                    // Show an AlertDialog to inform about the currency and entered value
                    showAssetAndValueAlert("Obligacje", enteredValue)
                }else{
                    Toast.makeText(this, "Wartość musi być większa od zera", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(this, "Proszę wprowadzić wartość", Toast.LENGTH_SHORT).show()
            }

        }

        sharesBuilder.setNegativeButton("Anuluj") { dialog, _ ->
            dialog.dismiss()
        }
        sharesBuilder.show()
    }

    private fun showAssetAndValueAlert(asset: String, value: String) {
        val alertBuilder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
        alertBuilder.setTitle("Dodano Aktywo")
        alertBuilder.setMessage("Aktywo: $asset\nWaluta: PLN \nWartość: $value")
        alertBuilder.setPositiveButton("OK") { dialog, _ ->
            // Handle the OK button click if needed
            assetsContainer.visibility = View.VISIBLE
            addAssetToContainerWithAnimation("PLN",value,asset)
            dialog.dismiss()
        }

        alertBuilder.show()
    }

    private fun toggleSelection(assetView: View) {
        if (selectedAssets.contains(assetView)) {
            selectedAssets.remove(assetView)
            assetView.setBackgroundResource(0) // Usunięcie zaznaczenia
        } else {
            selectedAssets.add(assetView)
            assetView.setBackgroundResource(R.drawable.selected_background) // Dodanie zaznaczenia
        }
    }

    private fun addAssetToContainerWithAnimation(currency: String?, value: String, assetType: String) {
        val assetsContainer = findViewById<LinearLayout>(R.id.assetsContainer)
        val assetView = layoutInflater.inflate(R.layout.asset_item, null)

        val assetNameTextView = assetView.findViewById<TextView>(R.id.assetNameTextView)
        assetNameTextView.text = assetType

        val assetCurrencyTextView = assetView.findViewById<TextView>(R.id.assetCurrencyTextView)
        assetCurrencyTextView.text = currency

        val assetValueTextView = assetView.findViewById<TextView>(R.id.assetValueTextView)
        assetValueTextView.text = value

        assetView.setOnClickListener {
            toggleSelection(assetView)
        }

        // Dodaj animację do elementu
        assetView.startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in))

        assetsContainer.addView(assetView)
    }

    private fun deleteSelectedAssets() {
        if (selectedAssets.isEmpty()) {
            Toast.makeText(this, "Wybierz aktywo do usunięcia.", Toast.LENGTH_SHORT).show()
            return
        }

        val assetsContainer = findViewById<LinearLayout>(R.id.assetsContainer)
        val fadeOutAnimation = AnimationUtils.loadAnimation(this, R.anim.fade_out)
        val animationDuration = fadeOutAnimation.duration

        val dialogBuilder = AlertDialog.Builder(this,R.style.AlertDialogTheme)
        dialogBuilder.setTitle("Potwierdź Usunięcie Aktywów")
        dialogBuilder.setMessage("Czy na pewno chcesz usunąć zaznaczone aktywa?")

        dialogBuilder.setPositiveButton("Tak") { dialog, _ ->
            for (assetView in selectedAssets) {
                assetView.startAnimation(fadeOutAnimation)
                assetView.postDelayed({
                    assetsContainer.removeView(assetView)
                }, animationDuration)
            }

            selectedAssets.clear()
            Toast.makeText(this, "Usunięto zaznaczone aktywa.", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }

        dialogBuilder.setNegativeButton("Nie") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = dialogBuilder.create()
        dialog.show()
    }


}


data class CurrencyData(
    val table: String,
    val no: String,
    val effectiveDate: String,
    val rates: List<Rate>
)

data class Rate(
    val currency: String,
    val code: String,
    val mid: Double
)