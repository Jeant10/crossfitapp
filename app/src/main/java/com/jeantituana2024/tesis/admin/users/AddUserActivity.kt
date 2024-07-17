package com.jeantituana2024.tesis.admin.users

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
import com.jeantituana2024.tesis.databinding.ActivityAddUserBinding
import com.jeantituana2024.tesis.models.CreateUser
import com.jeantituana2024.tesis.models.ErrorDetail
import com.jeantituana2024.tesis.models.ErrorResponse
import com.jeantituana2024.tesis.models.RegisterResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AddUserActivity : AppCompatActivity() {

    private lateinit var binding:ActivityAddUserBinding
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddUserBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere Porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        tokenPreferences = TokenPreferences(this)

        // Configurar el Toolbar desde el binding
        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Crear Usuario"

        // Habilitar el botón de regreso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Configurar el comportamiento del botón de regreso
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private var name = ""
    private var email=""
    private var password=""

    private fun validateData() {

        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        val cPassword = binding.cPasswordEt.text.toString().trim()

        if(password != cPassword){
            showToast("Password doesn't match...!")
        }
        else{
            registerUser()
        }

    }

    private fun registerUser() {

        progressDialog.setMessage("Creando Usuario...")
        progressDialog.show()

        val userData = CreateUser(name, email, password)
        val token = tokenPreferences.getToken()

        val call = RetrofitClient.instance.createUser("Bearer $token", userData)

        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    progressDialog.dismiss()

                    val registerResponse = response.body()

                    registerResponse?.let {
                        if (it.success == "Create User Successfully") {
                            // Redirigir a LoginActivity
                            showToast("Registro exitoso")
                            setResult(Activity.RESULT_OK)
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

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
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
                "email" -> "${error.message}"
                "password" -> "Password: ${error.message}"
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