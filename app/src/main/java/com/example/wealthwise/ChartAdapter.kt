package com.example.wealthwise

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.example.wealthwise.DataClass.StatisticResponse
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class ChartAdapter(private val context: Context) : BaseAdapter() {
    private var data = listOf<StatisticResponse>()

    fun updateData(data: List<StatisticResponse>) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        if (data[position].month ==  null) {
            return View(context)
        }
        val chartData = data[position]
        val chartView = LayoutInflater.from(context).inflate(R.layout.chart_item, null)

        val chartMonthly = chartView.findViewById<BarChart>(R.id.chartMonthly)

        // Przykładowe dane dla miesiąca
        val dataMonthly = createSampleData(chartData.totalIncome, chartData.totalExpenses)

        // Ustaw dane na wykresie
        setBarChartData(chartMonthly, dataMonthly, chartData.month.trim())

        return chartView
    }

    private fun createSampleData(totalIncome: Double, totalExpenses: Double): List<BarEntry> {
        // Przykładowe dane
        val entries = mutableListOf<BarEntry>()
        entries.add(BarEntry(1f, totalExpenses.toFloat())) // Wydatek
        entries.add(BarEntry(2f, totalIncome.toFloat())) // Przychód
        return entries
    }

    private fun setBarChartData(chart: BarChart, data: List<BarEntry>, monthLabel: String) {
        val dataSet = BarDataSet(data, monthLabel)
        dataSet.colors = listOf(Color.GRAY, Color.BLUE) // Kolory dla wydatków i przychodów
        dataSet.valueTextSize = 15f
        val barData = BarData(dataSet)
        chart.data = barData

        // Dostosuj wykres wg potrzeb
        val xAxis = chart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(arrayOf("", "Wydatek", "Przychód", ""))
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = 1
        xAxis.setCenterAxisLabels(false)
        xAxis.textSize = 16f

        val leftAxis: YAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawGridLines(false)
        leftAxis.textSize = 16f

        val rightAxis: YAxis = chart.axisRight
        rightAxis.isEnabled = false

        chart.description.isEnabled = false
        chart.legend.isEnabled = true
        chart.legend.textSize = 16f
        chart.legend.textColor = Color.BLACK
        chart.setFitBars(true)

        val barWidth = 0.4f
        barData.barWidth = barWidth

        chart.invalidate()
    }
}