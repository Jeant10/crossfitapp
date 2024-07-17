package com.jeantituana2024.tesis.kpis

import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.storage.TokenPreferences
import com.jeantituana2024.tesis.storage.UserPreferences
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.jeantituana2024.tesis.models.EarningsByPlan
import com.jeantituana2024.tesis.models.EarningsByPlanResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EarningsByPlanActivity : AppCompatActivity() {

    private lateinit var barChart: HorizontalBarChart
    private lateinit var progressBar: ProgressBar
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_earnings_by_plan)

        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar el Toolbar
        supportActionBar?.title = "Ganancias Por Plan"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        barChart = findViewById(R.id.barChart)
        progressBar = findViewById(R.id.progressBar)
        tokenPreferences = TokenPreferences(this)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        loadEarningsByPlan()
    }

    private fun loadEarningsByPlan() {
        val token = tokenPreferences.getToken()

        if (token != null) {
            progressBar.visibility = View.VISIBLE

            val call = RetrofitClient.instance.getEarningsByPlan("Bearer $token")

            call.enqueue(object : Callback<EarningsByPlanResponse> {
                override fun onResponse(call: Call<EarningsByPlanResponse>, response: Response<EarningsByPlanResponse>) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val earningsByPlan = response.body()?.earnings_by_plan?.values?.toList() ?: emptyList()
                        if (earningsByPlan.isNotEmpty()) {
                            barChart.visibility = View.VISIBLE
                            setupBarChart(earningsByPlan)
                        } else {
                            showToast("No se encontraron datos de ganancias por plan.")
                        }
                    } else {
                        showToast("Error al obtener datos: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<EarningsByPlanResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    showToast("Error de red: ${t.message}")
                }
            })
        } else {
            showToast("Session token not found")
        }
    }

    private fun setupBarChart(earningsByPlan: List<EarningsByPlan>) {
        val entries = mutableListOf<BarEntry>()

        earningsByPlan.forEachIndexed { index, plan ->
            entries.add(BarEntry(index.toFloat(), plan.earnings.toFloat(), plan.plan_name))
        }

        val dataSet = BarDataSet(entries, "Ganancias por Plan")

        // Aplicar los colores
        val colors = listOf(
            getColor(R.color.color1),
            getColor(R.color.color2),
            getColor(R.color.color3),
            getColor(R.color.color4),
            getColor(R.color.color5)
        )
        dataSet.colors = colors

        // Configurar el dataset para que muestre etiquetas dentro de las barras
        dataSet.setDrawValues(true)
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTypeface = Typeface.DEFAULT_BOLD
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getBarLabel(barEntry: BarEntry?): String {
                return barEntry?.data as? String ?: ""
            }
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.9f

        barChart.apply {
            data = barData
            setFitBars(true)
            description.isEnabled = false

            // Configuración del eje X
            xAxis.apply {
                granularity = 1f
                position = XAxis.XAxisPosition.BOTTOM_INSIDE
                setDrawGridLines(false)
                labelRotationAngle = -45f // Rotar etiquetas para mejor visualización
                setAvoidFirstLastClipping(true) // Evitar que las primeras y últimas etiquetas se corten
            }

            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f

            // Ajuste de la visualización de los datos
            setVisibleXRangeMaximum(10f) // Máximo número de entradas visibles sin desplazamiento
            moveViewToX(0f) // Mover la vista al inicio del eje X

            animateY(1000)
            invalidate() // Refrescar el gráfico
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}