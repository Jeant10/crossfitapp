package com.jeantituana2024.tesis.admin.plans

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.ActivityAddPlanBinding
import com.jeantituana2024.tesis.models.ErrorDetail
import com.jeantituana2024.tesis.models.ErrorResponse
import com.jeantituana2024.tesis.models.PlanCreateRequest
import com.jeantituana2024.tesis.models.PlanResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddPlanActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPlanBinding
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPlanBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere Porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        tokenPreferences = TokenPreferences(this)


        // Configurar el Toolbar desde el binding
        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Crear Plan"

        // Habilitar el botón de regreso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Configurar el comportamiento del botón de regreso
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            getData()
        }

    }

    private var name = ""
    private var description = ""
    private var price = ""
    private var duration = "0"

    private fun getData() {

        name = binding.nameEt.text.toString().trim()
        description = binding.descriptionEt.text.toString().trim()
        price = binding.priceEt.text.toString().trim()
        duration = binding.durationEt.text.toString().trim()

        registerPlan()

    }
 
    private fun registerPlan() {

        progressDialog.setMessage("Creando Plan...")
        progressDialog.show()

        val priceNum: Double? = if (price.isNotEmpty()) {
            try {
                price.toDouble()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                null // Maneja el error y devuelve null
            }
        } else {
            null // Devuelve null si la cadena está vacía
        }

        val durationNum: Int? = if (duration.isNotEmpty()) {
            try {
                duration.toInt()
            } catch (e: NumberFormatException) {
                e.printStackTrace()
                null // Maneja el error y devuelve null
            }
        } else {
            null // Devuelve null si la cadena está vacía
        }

        if (priceNum != null && durationNum != null) {

            val token = tokenPreferences.getToken()

            val planData = PlanCreateRequest(name,description,priceNum,durationNum)

            val call = RetrofitClient.instance.createPlan("Bearer $token", planData)


            call.enqueue(object: Callback<PlanResponse>{
                override fun onResponse(call: Call<PlanResponse>, response: Response<PlanResponse>) {
                    if (response.isSuccessful) {

                        progressDialog.dismiss()

                        val registerResponse = response.body()

                        registerResponse?.let {
                            if (it.success == "Plan created successfully") {
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

                override fun onFailure(p0: Call<PlanResponse>, t: Throwable) {
                    progressDialog.dismiss()
                    showToast("Error de conexión: ${t.message}")
                }

            })
        }
        else {
            progressDialog.dismiss()
            showToast("Por favor, ingresa todos los campos")
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

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

}