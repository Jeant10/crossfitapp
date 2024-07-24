package com.jeantituana2024.tesis.attendance

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.ActivityAddAttendanceBinding
import com.jeantituana2024.tesis.models.AttendanceRequest
import com.jeantituana2024.tesis.models.AttendanceResponse
import com.jeantituana2024.tesis.models.ErrorDetail
import com.jeantituana2024.tesis.models.ErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AddAttendanceActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddAttendanceBinding
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var progressDialog: ProgressDialog
    private var date = ""
    private var memberId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddAttendanceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        tokenPreferences = TokenPreferences(this)

        val intent = intent
        memberId = intent.getStringExtra("memberId")!!

        setSupportActionBar(binding.toolbar)
        // Establecer el título del Toolbar
        supportActionBar?.title = "Crear Asistencia"

        // Habilitar el botón de regreso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Configurar el comportamiento del botón de regreso
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.dateEt.setOnClickListener {
            showDatePickerDialog()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private fun validateData() {
        date = binding.dateEt.text.toString().trim()

        registerAttendance()
    }

    private fun registerAttendance() {
        progressDialog.setMessage("Creando asistencia...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        val attendanceData = AttendanceRequest(date)

        val call = RetrofitClient.instance.createAttendance("Bearer $token", memberId, attendanceData)

        call.enqueue(object : Callback<AttendanceResponse>{
            override fun onResponse(
                p0: Call<AttendanceResponse>,
                response: Response<AttendanceResponse>
            ) {
                if (response.isSuccessful) {
                    progressDialog.dismiss()

                    val registerResponse = response.body()

                    registerResponse?.let {
                        if (it.success == "Attendance created successfully") {
                            // Redirigir a LoginActivity
                            showToast("Registro exitoso")
                            setResult(Activity.RESULT_OK)
                        }
                    }
                }else{
                    response.errorBody()?.let { errorBody ->
                        try {
                            progressDialog.dismiss()
                            val gson = Gson()
                            val errorResponseType = object : TypeToken<ErrorResponse>() {}.type
                            val errorResponse: ErrorResponse? = gson.fromJson(errorBody.charStream(), errorResponseType)

                            if (errorResponse?.details != null && errorResponse.details.isNotEmpty()) {
                                handleValidationErrors(errorResponse.details)
                            } else {
                                errorResponse?.let {
                                    when (it.error) {
                                        "Invalid token" -> {
                                            showSessionExpiredAlert()
                                        }
                                        else -> {
                                            showToast("Error: ${it.error}")
                                        }
                                    }
                                }
                            }

                        } catch (e: Exception) {
                            progressDialog.dismiss()
                            e.printStackTrace()
                            showToast("Error al procesar la respuesta del servidor.")

                        }
                    }
                }
            }

            override fun onFailure(p0: Call<AttendanceResponse>, t: Throwable) {
                progressDialog.dismiss()
                showToast("Error de conexión: ${t.message}")
            }

        })
    }

    // Función para mostrar una alerta de sesión expirada y redirigir al LoginActivity
    private fun showSessionExpiredAlert() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Sesión Expirada")
        builder.setMessage("Vuelve a iniciar sesión")
        builder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss() // Cerrar el diálogo
            // Redirigir a LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        builder.setCancelable(false) // Prevenir el cierre del diálogo usando el botón de atrás
        builder.show()
    }

    private fun handleValidationErrors(errors: List<ErrorDetail>) {

        val errorMessages = errors.joinToString(separator = "\n") { error ->
            when (error.path[0]) {
                "date" -> "${error.path[0]}: ${error.message}"
                else -> "${error.path[0]}: ${error.message}"
            }
        }
        showToast(errorMessages)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.dateEt.setText(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}