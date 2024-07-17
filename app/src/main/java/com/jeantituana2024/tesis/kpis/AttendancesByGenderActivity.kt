package com.jeantituana2024.tesis.kpis

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.PieData
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.models.AttendancesByGenderResponse
import com.jeantituana2024.tesis.models.GenderAttendance
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AttendancesByGenderActivity : AppCompatActivity() {

    private lateinit var pieChart: PieChart
    private lateinit var progressBar: ProgressBar
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_attendances_by_gender)

        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar el Toolbar
        supportActionBar?.title = "Asistencias Por Genero"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        pieChart = findViewById(R.id.pieChart)
        progressBar = findViewById(R.id.progressBar)
        tokenPreferences = TokenPreferences(this)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        loadAttendancesByGender()
    }

    private fun loadAttendancesByGender() {
        val token = tokenPreferences.getToken()

        if (token != null) {
            progressBar.visibility = View.VISIBLE

            val call = RetrofitClient.instance.getAttendancesByGender("Bearer $token")

            call.enqueue(object : Callback<AttendancesByGenderResponse> {
                override fun onResponse(call: Call<AttendancesByGenderResponse>, response: Response<AttendancesByGenderResponse>) {
                    progressBar.visibility = View.GONE

                    if (response.isSuccessful) {
                        val attendancesByGender = response.body()?.attendaces_by_gender?.values?.toList() ?: emptyList()
                        if (attendancesByGender.isNotEmpty()) {
                            pieChart.visibility = View.VISIBLE
                            setupPieChart(attendancesByGender)
                        } else {
                            showToast("No se encontraron datos de asistencias por género.")
                        }
                    } else {
                        showToast("Error al obtener datos: ${response.message()}")
                    }
                }

                override fun onFailure(call: Call<AttendancesByGenderResponse>, t: Throwable) {
                    progressBar.visibility = View.GONE
                    showToast("Error de red: ${t.message}")
                }
            })
        } else {
            showToast("Session token not found")
        }
    }

    private fun setupPieChart(attendancesByGender: List<GenderAttendance>) {
        val entries = mutableListOf<PieEntry>()

        attendancesByGender.forEach { attendance ->
            entries.add(PieEntry(attendance.total_attendances.toFloat(), getGenderLabel(attendance.gender)))
        }

        val dataSet = PieDataSet(entries, "")

        // Aplicar los colores
        val colors = listOf(
            getColor(R.color.color1),
            getColor(R.color.color2),
            getColor(R.color.color3),
            getColor(R.color.color4),
            getColor(R.color.color5)
        )
        dataSet.colors = colors

        val pieData = PieData(dataSet)
        pieData.setValueTextSize(12f)

        pieChart.apply {
            data = pieData
            description.isEnabled = false
            isDrawHoleEnabled = true
            setEntryLabelTextSize(14f)
            setEntryLabelColor(getColor(R.color.black))
            animateY(1000)
            invalidate() // Refrescar el gráfico
        }
    }

    private fun getGenderLabel(gender: String): String {
        return when (gender) {
            "M" -> "Masculino"
            "F" -> "Femenino"
            else -> "Otro"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}