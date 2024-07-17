package com.jeantituana2024.tesis.auth

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.admin.DashboardAdminActivity
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.client.DashboardClientActivity
import com.jeantituana2024.tesis.coach.DashboardCoachActivity
import com.jeantituana2024.tesis.databinding.ActivityLoginBinding
import com.jeantituana2024.tesis.models.ErrorDetail
import com.jeantituana2024.tesis.models.ErrorResponse
import com.jeantituana2024.tesis.models.LoginRequest
import com.jeantituana2024.tesis.models.LoginResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import com.jeantituana2024.tesis.storage.UserPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var progressDialog: ProgressDialog

    private lateinit var userPreferences: UserPreferences

    private lateinit var tokenPreferences: TokenPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        userPreferences = UserPreferences(this)
        tokenPreferences = TokenPreferences(this)

        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
             LoginUser()
        }

        binding.forgotTv.setOnClickListener {
            startActivity(Intent(this,ForgotPasswordActivity::class.java))
        }
    }

    private var email=""
    private var password=""
    private fun LoginUser(){

        progressDialog.setMessage("Iniciando Sesion...")
        progressDialog.show()

        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()

        val userData = LoginRequest(email,password)
        val call = RetrofitClient.instance.login(userData)

        call.enqueue(object : Callback<LoginResponse>{
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {

                    progressDialog.dismiss()

                    val loginResponse = response.body()
                    loginResponse?.let {
                        when (it.success) {
                            "Login Sucessfully" -> {
                                // Guardar datos del usuario en SharedPreferences
                                userPreferences.saveUser(it.user)
                                Log.d("user","${it.user}")
                                tokenPreferences.saveToken(it.token)

                                // Redirigir a la actividad de Dashboard
                                // Redirigir según el rol del usuario
                                when (it.user.role) {
                                    "ADMIN" -> {
                                        startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                                    }
                                    "USER" -> {
                                        startActivity(Intent(this@LoginActivity, DashboardClientActivity::class.java))
                                    }
                                    "TRAINER" -> {
                                        startActivity(Intent(this@LoginActivity, DashboardCoachActivity::class.java))
                                    }
                                    else -> {
                                        Toast.makeText(this@LoginActivity, "Rol desconocido", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                finish()
                            }
                            "Confirmation email sent" -> {
                                // Mostrar mensaje de confirmación de correo
                                showToast("Email de confirmación enviado. Por favor, verifica tu correo.")
                            }
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

            override fun onFailure(p0: Call<LoginResponse>, e: Throwable) {
                progressDialog.dismiss()
                showToast("Error al procesar la respuesta del servidor ${e.message}")
            }

        })

    }

    private fun handleValidationErrors(errors: List<ErrorDetail>) {

        val errorMessages = errors.joinToString(separator = "\n") { error ->
            when (error.path[0]) {
                "email" -> "${error.message}"
                "password" -> "Password ${error.message}"
                else -> "${error.path[0]}: ${error.message}"
            }
        }
        showToast(errorMessages)
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

}