package com.jeantituana2024.tesis.kpis

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.models.AnnualEarning
import com.jeantituana2024.tesis.models.AnnualEarningsResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EarningsAnualActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var progressBar: ProgressBar
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earnings_anual)

        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar el Toolbar
        supportActionBar?.title = "Ganancias Anuales"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        barChart = findViewById(R.id.barChart)
        progressBar = findViewById(R.id.progressBar)
        tokenPreferences = TokenPreferences(this)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        loadAnnualEarnings()
    }

    private fun loadAnnualEarnings() {
        val token = tokenPreferences.getToken()

        if (token != null) {
            progressBar.visibility = View.VISIBLE

            val call = RetrofitClient.instance.getAnnualEarnings("Bearer $token")

            call.enqueue(object : Callback<AnnualEarningsResponse> {
                override fun onResponse(call: Call<AnnualEarningsResponse>, response: Response<AnnualEarningsResponse>) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val annualEarnings = response.body()?.annual_earning?.values?.toList() ?: emptyList()
                        if (annualEarnings.isNotEmpty()) {
                            barChart.visibility = View.VISIBLE
                            setupBarChart(annualEarnings)
                        } else {
                            showToast("No se encontraron datos de ganancias anuales.")
                        }
                    } else {
                        showToast("Error al obtener datos: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<AnnualEarningsResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    showToast("Error de red: ${t.message}")
                }
            })
        } else {
            showToast("Session token not found")
        }
    }

    private fun setupBarChart(annualEarnings: List<AnnualEarning>) {

        val dataSets = mutableListOf<IBarDataSet>()
        val years = mutableListOf<String>()

        // Aplicar los colores
        val colors = listOf(
            getColor(R.color.color1),
            getColor(R.color.color2),
            getColor(R.color.color3),
            getColor(R.color.color4),
            getColor(R.color.color5)
        )

        annualEarnings.forEachIndexed { index, earning ->

            val entries = mutableListOf<BarEntry>()
            entries.add(BarEntry(index.toFloat(), earning.annual_earnings.toFloat()))
            years.add(earning.year.toString())

            val dataSet = BarDataSet(entries, earning.year.toString())
            dataSet.colors = listOf(colors[index % colors.size])
            dataSets.add(dataSet)
        }

        val barData = BarData(dataSets)
        barData.barWidth = 0.9f

        barChart.apply {
            data = barData
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(years)
                granularity = 1f
                labelCount = years.size
            }
            axisRight.isEnabled = false
            description.isEnabled = false
            animateY(1000)
            invalidate() // Refrescar el gráfico
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}