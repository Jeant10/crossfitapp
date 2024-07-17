package com.jeantituana2024.tesis.kpis

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.models.ActiveMembersResponse
import com.jeantituana2024.tesis.models.InactiveMembersResponse
import com.jeantituana2024.tesis.models.TotalMembersResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MembersKpiActivity : AppCompatActivity() {

    private lateinit var barChart: BarChart
    private lateinit var progressBar: ProgressBar
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_members_kpi)

        toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Configurar el Toolbar
        supportActionBar?.title = "Miembros"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        barChart = findViewById(R.id.barChart)
        progressBar = findViewById(R.id.progressBar)
        tokenPreferences = TokenPreferences(this)

        toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        loadMembersKPI()
    }

    private fun loadMembersKPI() {
        val token = tokenPreferences.getToken()
        if (token != null) {
            progressBar.visibility = View.VISIBLE
            // Make the API calls sequentially
            loadTotalMembers(token)
        } else {
            showToast("Session token not found")
        }
    }

    private fun loadTotalMembers(token: String) {
        val callTotal = RetrofitClient.instance.getTotalMembers("Bearer $token")
        callTotal.enqueue(object : Callback<TotalMembersResponse> {
            override fun onResponse(call: Call<TotalMembersResponse>, response: Response<TotalMembersResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val totalMembers = response.body()?.total_membership?.values?.firstOrNull()?.total_memberships ?: 0
                    loadActiveMembers(token, totalMembers)
                } else {
                    showToast("Error al obtener datos: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<TotalMembersResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showToast("Error de red: ${t.message}")
            }
        })
    }

    private fun loadActiveMembers(token: String, totalMembers: Int) {
        val callActive = RetrofitClient.instance.getActiveMembers("Bearer $token")
        callActive.enqueue(object : Callback<ActiveMembersResponse> {
            override fun onResponse(call: Call<ActiveMembersResponse>, response: Response<ActiveMembersResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val activeMembers = response.body()?.active_membership?.values?.firstOrNull()?.active_memberships ?: 0
                    loadInactiveMembers(token, totalMembers, activeMembers)
                } else {
                    showToast("Error al obtener datos: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ActiveMembersResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showToast("Error de red: ${t.message}")
            }
        })
    }

    private fun loadInactiveMembers(token: String, totalMembers: Int, activeMembers: Int) {
        val callInactive = RetrofitClient.instance.getInactiveMembers("Bearer $token")
        callInactive.enqueue(object : Callback<InactiveMembersResponse> {
            override fun onResponse(call: Call<InactiveMembersResponse>, response: Response<InactiveMembersResponse>) {
                progressBar.visibility = View.GONE
                if (response.isSuccessful) {
                    val inactiveMembers = response.body()?.inactive_membership?.values?.firstOrNull()?.inactive_memberships ?: 0
                    setupBarChart(totalMembers, activeMembers, inactiveMembers)
                } else {
                    showToast("Error al obtener datos: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<InactiveMembersResponse>, t: Throwable) {
                progressBar.visibility = View.GONE
                showToast("Error de red: ${t.message}")
            }
        })
    }

    private fun setupBarChart(totalMembers: Int, activeMembers: Int, inactiveMembers: Int) {
        val entries = mutableListOf<BarEntry>()
        entries.add(BarEntry(0f, totalMembers.toFloat(), "Total Members"))
        entries.add(BarEntry(1f, activeMembers.toFloat(), "Active Members"))
        entries.add(BarEntry(2f, inactiveMembers.toFloat(), "Inactive Members"))

        val dataSet = BarDataSet(entries, "Miembros")
        dataSet.setColors(
            getColor(R.color.color1),
            getColor(R.color.color2),
            getColor(R.color.color3)
        )
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.xAxis.apply {
            granularity = 1f
            valueFormatter = LabelFormatter(arrayOf("Total", "Activos", "Inactivos"))
            position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        }
        barChart.axisRight.isEnabled = false
        barChart.axisLeft.axisMinimum = 0f
        barChart.invalidate() // Refrescar el gráfico
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    class LabelFormatter(private val labels: Array<String>) : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return labels.getOrElse(value.toInt()) { "" }
        }
    }
}