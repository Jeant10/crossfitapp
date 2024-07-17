package com.jeantituana2024.tesis.auth

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.databinding.ActivityRegisterBinding
import com.jeantituana2024.tesis.models.RegisterRequest
import com.jeantituana2024.tesis.models.ErrorResponse

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.models.ErrorDetail
import com.jeantituana2024.tesis.models.RegisterResponse
import androidx.annotation.VisibleForTesting

// Marca el método con @VisibleForTesting para que sea accesible solo para pruebas
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    @VisibleForTesting
    internal lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""


    private fun validateData(){

        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        if (password != cPassword) {
            showToast("Las contraseñas no coinciden")
        }else{
            registerUser()
        }

    }

    @VisibleForTesting
    internal fun registerUser() {

        progressDialog.setMessage("Creando Cuenta...")
        progressDialog.show()

        val userData = RegisterRequest(name, email, password)
        val call = RetrofitClient.instance.register(userData)

        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {

                    progressDialog.dismiss()
                    val registerResponse = response.body()

                    registerResponse?.let {
                        if (it.success == "Successfully Register") {
                            // Redirigir a LoginActivity
                            showToast("Registro exitoso")
                            startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
                            finish()
                        }
                    }


                } else {

                    response.errorBody()?.let { errorBody ->
                        try {
                            progressDialog.dismiss()
                            val gson = Gson()
                            val errorResponseType = object : TypeToken<ErrorResponse>() {}.type
                            val errorResponse: ErrorResponse? = gson.fromJson(errorBody.charStream(), errorResponseType)

                            if (errorResponse?.details != null && errorResponse.details.isNotEmpty()) {
                                handleValidationErrors(errorResponse.details)
                            } else {
                                showToast(errorResponse?.error ?: "Error desconocido")
                            }

                        } catch (e: Exception) {

                            e.printStackTrace()
                            showToast("Error al procesar la respuesta del servidor.")

                        }
                    }
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                progressDialog.dismiss()
                showToast("Error de conexión: ${t.message}")
            }
        })
    }
    private fun handleValidationErrors(errors: List<ErrorDetail>) {

        val errorMessages = errors.joinToString(separator = "\n") { error ->
            when (error.path[0]) {
                "email" -> "${error.message}"
                "password" -> "${error.message}"
                "name" -> "${error.message}"
                else -> "${error.path[0]}: ${error.message}"
            }
        }
        showToast(errorMessages)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }


}