package com.jeantituana2024.tesis.kpis

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.models.MonthlyEarning
import com.jeantituana2024.tesis.models.MonthlyEarningsResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EarningsMonthActivity : AppCompatActivity() {

    private lateinit var stackedAreaChart: LineChart
    private lateinit var progressBar: ProgressBar
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earnings_month)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar el Toolbar
        supportActionBar?.title = "Ganancias Mensuales"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        stackedAreaChart = findViewById(R.id.stackedAreaChart)
        progressBar = findViewById(R.id.progressBar)
        tokenPreferences = TokenPreferences(this)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        loadMonthlyEarnings()
    }

    private fun loadMonthlyEarnings() {
        val token = tokenPreferences.getToken()

        if (token != null) {
            progressBar.visibility = View.VISIBLE

            val call = RetrofitClient.instance.getMonthlyEarnings("Bearer $token")

            call.enqueue(object : Callback<MonthlyEarningsResponse> {
                override fun onResponse(call: Call<MonthlyEarningsResponse>, response: Response<MonthlyEarningsResponse>) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val monthlyEarnings = response.body()?.monthly_earning?.values?.toList() ?: emptyList()
                        if (monthlyEarnings.isNotEmpty()) {
                            stackedAreaChart.visibility = View.VISIBLE
                            setupStackedAreaChart(monthlyEarnings)
                        } else {
                            showToast("No se encontraron datos de ganancias mensuales.")
                        }
                    } else {
                        showToast("Error al obtener datos: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<MonthlyEarningsResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    showToast("Error de red: ${t.message}")
                }
            })
        } else {
            showToast("Session token not found")
        }
    }

    private fun setupStackedAreaChart(monthlyEarnings: List<MonthlyEarning>) {
        val years = monthlyEarnings.map { it.year }.distinct().sorted()

        val dataSets = mutableListOf<ILineDataSet>()
        val colors = getColors()

        // Configurar nombres de meses estáticos
        val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

        years.forEachIndexed { index, year ->
            val entries = mutableListOf<Entry>()

            // Crear un conjunto de entradas por cada mes del año
            months.forEachIndexed { monthIndex, monthName ->
                val earningsForMonth = monthlyEarnings.find { it.year == year && it.month == monthIndex + 1 }
                val earningsValue = earningsForMonth?.monthly_earnings?.toFloat() ?: 0f
                entries.add(Entry(monthIndex.toFloat(), earningsValue))
            }

            // Configurar el conjunto de datos para el año actual
            val dataSet = LineDataSet(entries, "$year")
            dataSet.colors = listOf(colors[index % colors.size])
            dataSet.setDrawFilled(true)
            dataSet.fillColor = colors[index % colors.size]
            dataSet.fillAlpha = 100
            dataSet.valueTextSize = 10f

            dataSets.add(dataSet)
        }

        val lineData = LineData(dataSets)
        stackedAreaChart.data = lineData

        // Configuración adicional del gráfico
        stackedAreaChart.apply {
            description.isEnabled = false
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(months)
                position = XAxis.XAxisPosition.BOTTOM
            }
            axisLeft.axisMinimum = 0f
            axisRight.axisMinimum = 0f

            animateY(1000)
            invalidate()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun getColors(): List<Int> {
        return listOf(
            getColor(R.color.color1),
            getColor(R.color.color2),
            getColor(R.color.color3),
            getColor(R.color.color4),
            getColor(R.color.color5)
            // Add more colors if needed
        )
    }
}


