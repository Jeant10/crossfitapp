package com.jeantituana2024.tesis.kpis

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.models.DailyAttendance
import com.jeantituana2024.tesis.models.DailyAttendancesResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AttendancesDayActivity : AppCompatActivity() {

    private lateinit var stackedLineChart: LineChart
    private lateinit var progressBar: ProgressBar
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendances_day)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar el Toolbar
        supportActionBar?.title = "Asistencias Por Día"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        stackedLineChart = findViewById(R.id.lineChart)
        progressBar = findViewById(R.id.progressBar)
        tokenPreferences = TokenPreferences(this)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        loadDailyAttendances()
    }

    private fun loadDailyAttendances() {
        val token = tokenPreferences.getToken()

        if (token != null) {
            progressBar.visibility = View.VISIBLE

            val call = RetrofitClient.instance.getDailyAttendances("Bearer $token")

            call.enqueue(object : Callback<DailyAttendancesResponse> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onResponse(call: Call<DailyAttendancesResponse>, response: Response<DailyAttendancesResponse>) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val dailyAttendances = response.body()?.daily_attendance?.values?.toList() ?: emptyList()
                        if (dailyAttendances.isNotEmpty()) {
                            stackedLineChart.visibility = View.VISIBLE
                            setupStackedLineChart(dailyAttendances)
                        } else {
                            showToast("No se encontraron datos de asistencias diarias.")
                        }
                    } else {
                        showToast("Error al obtener datos: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<DailyAttendancesResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    showToast("Error de red: ${t.message}")
                }
            })
        } else {
            showToast("Session token not found")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupStackedLineChart(dailyAttendances: List<DailyAttendance>) {
        // Ordenar las asistencias por día
        val sortedAttendances = dailyAttendances.sortedBy { it.day }

        // Agrupar asistencias por año
        val attendanceByYear = sortedAttendances.groupBy { it.day.substring(0, 4) }

        val dataSets = mutableListOf<ILineDataSet>()
        val colors = getColors()

        for ((index, yearAndAttendances) in attendanceByYear.entries.withIndex()) {
            val (year, attendances) = yearAndAttendances
            val entries = attendances.map { attendance ->
                val day = attendance.day.substring(0, 10) // Ignorar la parte de tiempo
                val dateParts = day.split("-")
                val dayOfYear = LocalDate.of(dateParts[0].toInt(), dateParts[1].toInt(), dateParts[2].toInt()).dayOfYear
                Entry(dayOfYear.toFloat(), attendance.daily_attendances.toFloat())
            }

            val dataSet = LineDataSet(entries, year)
            dataSet.color = colors[index % colors.size]
//            dataSet.setDrawFilled(true) // Opción para llenar el área bajo la línea
            dataSet.setDrawCircles(true)
            dataSet.lineWidth = 2f
            dataSet.circleRadius = 4f
            dataSet.valueTextSize = 10f

            dataSets.add(dataSet)
        }
        val lineData = LineData(dataSets)
        stackedLineChart.data = lineData

        // Configuración del eje X para mostrar los días
        val xAxis = stackedLineChart.xAxis
        xAxis.valueFormatter = DayOfYearAxisValueFormatter()
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f // Asegura que cada valor del eje X se muestre

        stackedLineChart.axisLeft.axisMinimum = 0f
        stackedLineChart.axisRight.axisMinimum = 0f

        // Hacer que los nombres de los días aparezcan al hacer zoom
        stackedLineChart.setVisibleXRangeMaximum(30f)
        stackedLineChart.setScaleEnabled(true)

        stackedLineChart.animateY(1000)
        stackedLineChart.invalidate()
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
            // Agrega más colores si es necesario
        )
    }

    private class DayOfYearAxisValueFormatter : ValueFormatter() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun getFormattedValue(value: Float): String {
            val dayOfYear = value.toInt()
            val date = LocalDate.ofYearDay(2020, dayOfYear) // Año 2020 como referencia
            return date.format(DateTimeFormatter.ofPattern("MMM dd"))
        }
    }
}

