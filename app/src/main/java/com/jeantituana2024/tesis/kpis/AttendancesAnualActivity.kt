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
import com.jeantituana2024.tesis.models.AnnualAttendance
import com.jeantituana2024.tesis.models.AnnualAttendanceResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AttendancesAnualActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var progressBar: ProgressBar
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendances_anual)

        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar el Toolbar
        supportActionBar?.title = "Asistencias Anuales"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        barChart = findViewById(R.id.barChart)
        progressBar = findViewById(R.id.progressBar)
        tokenPreferences = TokenPreferences(this)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        loadAnnualAttendance()
    }

    private fun loadAnnualAttendance() {
        val token = tokenPreferences.getToken()

        if (token != null) {
            progressBar.visibility = View.VISIBLE

            val call = RetrofitClient.instance.getAnnualAttendances("Bearer $token")

            call.enqueue(object : Callback<AnnualAttendanceResponse> {
                override fun onResponse(call: Call<AnnualAttendanceResponse>, response: Response<AnnualAttendanceResponse>) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val annualAttendance = response.body()?.annual_attendance?.values?.toList() ?: emptyList()
                        if (annualAttendance.isNotEmpty()) {
                            barChart.visibility = View.VISIBLE
                            setupBarChart(annualAttendance)
                        } else {
                            showToast("No se encontraron datos de asistencias anuales.")
                        }
                    } else {
                        showToast("Error al obtener datos: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<AnnualAttendanceResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    showToast("Error de red: ${t.message}")
                }
            })
        } else {
            showToast("Session token not found")
        }
    }

    private fun setupBarChart(annualAttendance: List<AnnualAttendance>) {

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

        annualAttendance.forEachIndexed { index, attendance ->

            val entries = mutableListOf<BarEntry>()

            entries.add(BarEntry(index.toFloat(), attendance.annual_attendances.toFloat()))
            years.add(attendance.year.toString())

            val dataSet = BarDataSet(entries, attendance.year.toString())
            dataSet.colors = listOf(colors[index % colors.size])
            dataSets.add(dataSet)
        }

        val barData = BarData(dataSets)
        barData.barWidth = 0.9f // Ajustar el ancho de las barras

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