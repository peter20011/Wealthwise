package com.example.wealthwise


import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class StatisticActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistic)

        val homeIcon = findViewById<ImageView>(R.id.homeIcon)
        val statisticIcon = findViewById<ImageView>(R.id.statisticIcon)
        val profileIcon = findViewById<ImageView>(R.id.profileIcon)
        val assetsIcon = findViewById<ImageView>(R.id.assetsIcon)

        homeIcon.setBackgroundResource(0)
        statisticIcon.setBackgroundResource(R.drawable.blue_border)
        profileIcon.setBackgroundResource(0)
        assetsIcon.setBackgroundResource(0)

        homeIcon.setOnClickListener {
            val intent = Intent(this, DashboardActivity::class.java)
            startActivity(intent)
        }

        profileIcon.setOnClickListener{
            val intent = Intent(this, UserProfileActivity::class.java)
            startActivity(intent)
        }

        // Pobierz wykresy dla poszczególnych miesięcy
        val chartJanuary: BarChart = findViewById(R.id.chartJanuary)
        val chartFebruary: BarChart = findViewById(R.id.chartFebruary)
        val chartMarch: BarChart = findViewById(R.id.chartMarch)
        val chartApril: BarChart = findViewById(R.id.chartApril)
        val chartMay: BarChart = findViewById(R.id.chartMay)
        val chartJune: BarChart = findViewById(R.id.chartJune)
        val chartJuly: BarChart = findViewById(R.id.chartJuly)
        val chartSeptember: BarChart =  findViewById(R.id.chartSeptember)
        val chartAugust: BarChart = findViewById(R.id.chartAugust)
        val chartOctober: BarChart = findViewById(R.id.chartOctober)
        val chartNovember: BarChart = findViewById(R.id.chartNovember)
        val chartDecember: BarChart = findViewById(R.id.chartDecember)

        // Przykładowe dane dla miesięcy
        val dataJanuary = createSampleDataForMonth(1)
        val dataFebruary = createSampleDataForMonth(2)
        val dataMarch = createSampleDataForMonth(3)
        val dataApril = createSampleDataForMonth(4)
        val dataMay = createSampleDataForMonth(5)
        val dataJune = createSampleDataForMonth(6)
        val dataJuly = createSampleDataForMonth(7)
        val dataAugust = createSampleDataForMonth(8)
        val dataSeptember = createSampleDataForMonth(9)
        val dataOctober = createSampleDataForMonth(10)
        val dataNovember = createSampleDataForMonth(11)
        val dataDecember = createSampleDataForMonth(12)

        // Ustaw dane na wykresach
        setBarChartData(chartJanuary, dataJanuary, "Styczeń")
        setBarChartData(chartFebruary, dataFebruary, "Luty")
        setBarChartData(chartMarch, dataMarch, "Marzec")
        setBarChartData(chartApril, dataApril, "Kwiecień")
        setBarChartData(chartMay, dataMay, "Maj")
        setBarChartData(chartJune, dataJune, "Czerwiec")
        setBarChartData(chartJuly, dataJuly, "Lipiec")
        setBarChartData(chartAugust, dataAugust, "Sierpień")
        setBarChartData(chartSeptember,dataSeptember,"Wrzesień")
        setBarChartData(chartOctober, dataOctober, "Październik")
        setBarChartData(chartNovember, dataNovember, "Listopad")
        setBarChartData(chartDecember, dataDecember, "Grudzień")

    }

    private fun createSampleDataForMonth(month: Int): List<BarEntry> {
        // Przykładowe dane (do zastąpienia własnymi danymi)
        val entries = mutableListOf<BarEntry>()
        entries.add(BarEntry(1f, 1500f)) // Wydatek
        entries.add(BarEntry(2f, 2600f)) // Przychód
        return entries
    }

    private fun setBarChartData(chart: BarChart, data: List<BarEntry>, monthLabel: String) {
        val dataSet = BarDataSet(data, monthLabel)
        dataSet.colors = listOf(Color.GRAY, Color.BLUE) // Kolory dla wydatków i przychodów
        dataSet.valueTextSize=15f
        val barData = BarData(dataSet)
        chart.data = barData

        // Dostosuj wykres wg potrzeb
        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("","Wydatek", "Przychód", ""))
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f // Ustala odstęp między etykietami na osi X
        xAxis.labelCount = 1 // Ilość etykiet na osi X (tu trzy: puste, Wydatek, Przychód)
        xAxis.setCenterAxisLabels(false) // Wyśrodkowanie etykiet pod słupkami
        xAxis.textSize=16f

        val leftAxis: YAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawGridLines(false)
        leftAxis.textSize=16f

        val rightAxis: YAxis = chart.axisRight
        rightAxis.isEnabled = false

        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.legend.textSize=16f
        chart.legend.textColor = Color.BLACK
        chart.setFitBars(true)

        // Zmniejsz szerokość słupków
        val barWidth = 0.4f // Domyślnie 0.85f
        barData.barWidth = barWidth

        // Odśwież wykres
        chart.invalidate()
    }
}