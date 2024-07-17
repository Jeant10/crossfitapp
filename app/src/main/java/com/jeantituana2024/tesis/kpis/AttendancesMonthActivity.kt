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
import com.jeantituana2024.tesis.models.MonthlyAttendance
import com.jeantituana2024.tesis.models.MonthlyAttendancesResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AttendancesMonthActivity : AppCompatActivity() {

    private lateinit var stackedAreaChart: LineChart
    private lateinit var progressBar: ProgressBar
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendances_month)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar el Toolbar
        supportActionBar?.title = "Asistencias Mensuales"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        stackedAreaChart = findViewById(R.id.stackedAreaChart)
        progressBar = findViewById(R.id.progressBar)
        tokenPreferences = TokenPreferences(this)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        loadMonthlyAttendances()
    }

    private fun loadMonthlyAttendances() {
        val token = tokenPreferences.getToken()

        if (token != null) {
            progressBar.visibility = View.VISIBLE

            val call = RetrofitClient.instance.getMonthlyAttendances("Bearer $token")

            call.enqueue(object : Callback<MonthlyAttendancesResponse> {
                override fun onResponse(call: Call<MonthlyAttendancesResponse>, response: Response<MonthlyAttendancesResponse>) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val monthlyAttendances = response.body()?.monthly_attendance?.values?.toList() ?: emptyList()
                        if (monthlyAttendances.isNotEmpty()) {
                            stackedAreaChart.visibility = View.VISIBLE
                            setupAreaChart(monthlyAttendances)
                        } else {
                            showToast("No se encontraron datos de asistencias mensuales.")
                        }
                    } else {
                        showToast("Error al obtener datos: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<MonthlyAttendancesResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    showToast("Error de red: ${t.message}")
                }
            })
        } else {
            showToast("Session token not found")
        }
    }

    private fun setupAreaChart(monthlyAttendances: List<MonthlyAttendance>) {
        val years = monthlyAttendances.map { it.year }.distinct().sorted()

        val dataSets = mutableListOf<ILineDataSet>()
        val colors = getColors()

        // Configurar nombres de meses estáticos
        val months = listOf("Ene", "Feb", "Mar", "Abr", "May", "Jun", "Jul", "Ago", "Sep", "Oct", "Nov", "Dic")

        years.forEachIndexed { index, year ->
            val entries = mutableListOf<Entry>()

            months.forEachIndexed { monthIndex, monthName ->
                val attendanceForMonth = monthlyAttendances.find { it.year == year && it.month == monthIndex + 1 }
                val attendanceValue = attendanceForMonth?.monthly_attendances?.toFloat() ?: 0f
                entries.add(Entry(monthIndex.toFloat(), attendanceValue))
            }

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