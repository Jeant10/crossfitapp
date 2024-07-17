package com.jeantituana2024.tesis.payment

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
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.ActivityPayEditBinding
import com.jeantituana2024.tesis.models.ErrorDetail
import com.jeantituana2024.tesis.models.ErrorResponse
import com.jeantituana2024.tesis.models.GetPaymentResponse
import com.jeantituana2024.tesis.models.PaymentRequest
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PayEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPayEditBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var tokenPreferences: TokenPreferences

    private var selectedDate: Calendar = Calendar.getInstance() // Variable para almacenar la fecha seleccionada
    private var memberId = ""
    private var payId = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPayEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere Porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        tokenPreferences = TokenPreferences(this)

        val intent = intent
        memberId = intent.getStringExtra("memberId")!!
        payId = intent.getStringExtra("payId")!!

        loadPay()

        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Editar Pago"

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

        binding.updateBtn.setOnClickListener {
            getData()
        }
    }

    private fun loadPay() {

        progressDialog.setMessage("Cargando...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        if (token != null) {

            val call = RetrofitClient.instance.getPayment("Bearer $token", memberId, payId)

            call.enqueue(object: Callback<GetPaymentResponse>{
                override fun onResponse(
                    p0: Call<GetPaymentResponse>,
                    response: Response<GetPaymentResponse>
                ) {
                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        val payResponse = response.body()?.pay

                        if (payResponse != null) {

                            val formattedDate = formatDateString(payResponse.date)
                            binding.dateEt.setText(formattedDate)

                            // Actualiza la variable selectedDate
                            updateSelectedDate(formattedDate)
                            // Configurar nacionalidad
                            val typePayResponse = payResponse.payment_type
                            val typePays = resources.getStringArray(R.array.typePay)
                            val typePayPosition = typePays.indexOf(typePayResponse)
                            if (typePayPosition >= 0) {
                                binding.spinner.setSelection(typePayPosition)
                            } else {
                                showToast("El metodo de pago no es válida")
                            }
                        }
                        else {
                            showToast("No se encontró el pago")
                        }
                    }
                    else{
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            progressDialog.dismiss()
                            handleErrorResponse(it)
                        }
                    }
                }

                override fun onFailure(p0: Call<GetPaymentResponse>, p1: Throwable) {
                    progressDialog.dismiss()
                    showToast("Error de conexión: ${p1.message}")
                }

            })
        }
        else {
            progressDialog.dismiss()
            showToast("No se encontró el token de sesión")
        }
    }

    private var date = ""
    private var typePay = ""
    
    private fun getData() {

        date = binding.dateEt.text.toString().trim()
        typePay = binding.spinner.selectedItem.toString().trim()

        updatePay()
    }

    private fun updatePay() {
        progressDialog.setMessage("Actualizando pago...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        if (token != null) {

            val editRequest = PaymentRequest(date,typePay)

            val call = RetrofitClient.instance.updatePayment("Bearer $token",memberId, payId, editRequest)

            call.enqueue(object: Callback<GetPaymentResponse>{
                override fun onResponse(
                    p0: Call<GetPaymentResponse>,
                    response: Response<GetPaymentResponse>
                ) {
                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        val updateResponse = response.body()
                        if (updateResponse?.success != null) {
                            // Mostrar mensaje de éxito
                            showToast("Pago Actualizado")
                            setResult(Activity.RESULT_OK)
                        } else {
                            showToast("Error desconocido")
                        }

                    }
                    else{
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

                override fun onFailure(p0: Call<GetPaymentResponse>, p1: Throwable) {
                    progressDialog.dismiss()
                    showToast("Error de conexión: ${p1.message}")
                }

            })
        }
        else {
            progressDialog.dismiss()
            showToast("No se encontró el token de sesión")
        }
    }

    // Método para actualizar la variable selectedDate
    private fun updateSelectedDate(dateString: String) {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = format.parse(dateString)
            selectedDate.time = date
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun formatDateString(dateString: String): String {
        return try {
            // Define el formato original de la fecha
            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            originalFormat.timeZone = TimeZone.getTimeZone("UTC") // Asegurar que sea interpretado como UTC

            // Parsear la fecha en el formato original
            val date = originalFormat.parse(dateString)

            // Define el nuevo formato de la fecha
            val desiredFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            desiredFormat.timeZone = TimeZone.getTimeZone("UTC") // Mantener la salida en UTC
            // Formatear la fecha al nuevo formato y devolverla
            desiredFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            dateString // En caso de error, devolver el string original
        }
    }

    private fun handleErrorResponse(errorBody: String) {
        val gson = Gson()
        val errorResponseType = object : TypeToken<SingleErrorResponse>() {}.type
        val errorResponse: SingleErrorResponse? = gson.fromJson(errorBody, errorResponseType)

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

    private fun showDatePickerDialog() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.dateEt.setText(formattedDate)

                // Actualiza selectedDate con la nueva fecha seleccionada
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun handleValidationErrors(errors: List<ErrorDetail>) {

        val errorMessages = errors.joinToString(separator = "\n") { error ->
            when (error.path[0]) {
                "date" -> "${error.message}"
                "payment_type" -> "${error.message}"
                else -> "${error.path[0]}: ${error.message}"
            }
        }
        showToast(errorMessages)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}