package com.jeantituana2024.tesis.admin.plans

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.ActivityPlanEditBinding
import com.jeantituana2024.tesis.models.ErrorDetail
import com.jeantituana2024.tesis.models.ErrorResponse
import com.jeantituana2024.tesis.models.PlanCreateRequest
import com.jeantituana2024.tesis.models.PlanResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlanEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlanEditBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var tokenPreferences: TokenPreferences

    private var planId = ""
    private var planName = ""
    private var planDescription = ""
    private var planPrice = ""
    private var planDuration = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlanEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere Porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        tokenPreferences = TokenPreferences(this)

        val intent = intent
        planId = intent.getStringExtra("planId")!!

        loadPlan(planId)

        // Configurar el Toolbar desde el binding
        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Editar Plan"

        // Habilitar el botón de regreso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        // Configurar el comportamiento del botón de regreso
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.updateBtn.setOnClickListener {
            getData()
        }

    }

    private fun getData() {

        planName = binding.nameEt.text.toString().trim()
        planDescription = binding.descriptionEt.text.toString().trim()
        planPrice = binding.priceEt.text.toString().trim()
        planDuration = binding.durationEt.text.toString().trim()

        updatePlan()
    }

    private fun loadPlan(planId: String) {
        progressDialog.setMessage("Cargando...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        if (token != null) {

            val call = RetrofitClient.instance.viewPlan("Bearer $token", planId)

            call.enqueue(object: Callback<PlanResponse>{
                override fun onResponse(p0: Call<PlanResponse>, response: Response<PlanResponse>) {

                    if (response.isSuccessful) {
                        progressDialog.dismiss()

                        val planResponse = response.body()?.plan

                        if (planResponse != null) {
                            binding.nameEt.setText(planResponse.name)
                            binding.descriptionEt.setText(planResponse.description)
                            binding.priceEt.setText(planResponse.price)
                            binding.durationEt.setText(planResponse.duration.toString())

                        }
                        else {
                            showToast("No se encontró el plan")
                        }
                    }
                    else {
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            progressDialog.dismiss()
                            handleErrorResponse(it)
                        }

                    }
                }

                override fun onFailure(p0: Call<PlanResponse>, p1: Throwable) {
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

    private fun handleValidationErrors(errors: List<ErrorDetail>) {

        val errorMessages = errors.joinToString(separator = "\n") { error ->
            when (error.path[0]) {
                "name" -> "${error.path[0]}: ${error.message}"
                "description" -> "${error.path[0]}: ${error.message}"
                "price" -> "${error.path[0]}: ${error.message}"
                "duration" -> "${error.path[0]}: ${error.message}"
                else -> "${error.path[0]}: ${error.message}"
            }
        }
        showToast(errorMessages)
    }

    private fun updatePlan() {
        progressDialog.setMessage("Actualizando plan...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        if (token != null) {

            val planPriceNumber: Double? = if (planPrice.isNotEmpty()) {
                try {
                    planPrice.toDouble()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    null
                }
            } else {
                null
            }

            val planDurationNumber: Int? = if (planDuration.isNotEmpty()) {
                try {
                    planDuration.toInt()
                } catch (e: NumberFormatException) {
                    e.printStackTrace()
                    null
                }
            } else {
                null
            }


            if (planPriceNumber != null && planDurationNumber != null) {

                val editRequest = PlanCreateRequest(planName,planDescription,planPriceNumber,planDurationNumber)

                val call = RetrofitClient.instance.updatePlan("Bearer $token", planId, editRequest)

                call.enqueue(object: Callback<PlanResponse>{
                    override fun onResponse(p0: Call<PlanResponse>, response: Response<PlanResponse>) {
                        if (response.isSuccessful) {
                            progressDialog.dismiss()
                            val updateResponse = response.body()

                            if (updateResponse?.success != null) {
                                // Mostrar mensaje de éxito
                                showToast("Plan Actualizado")
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

                    override fun onFailure(p0: Call<PlanResponse>, p1: Throwable) {
                        progressDialog.dismiss()
                        showToast("Error de conexión: ${p1.message}")
                    }

                })
            }
            else {
                progressDialog.dismiss()
                showToast("Por favor, ingresa todos los campos")
            }
        }
        else {
            progressDialog.dismiss()
            showToast("No se encontró el token de sesión")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

}