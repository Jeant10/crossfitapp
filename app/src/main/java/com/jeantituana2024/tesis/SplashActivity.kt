package com.jeantituana2024.tesis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.admin.DashboardAdminActivity
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.client.DashboardClientActivity
import com.jeantituana2024.tesis.coach.DashboardCoachActivity
import com.jeantituana2024.tesis.models.CheckSessionResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import com.jeantituana2024.tesis.storage.UserPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashActivity : AppCompatActivity() {

    private lateinit var userPreferences: UserPreferences
    private lateinit var tokenPreferences: TokenPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        userPreferences = UserPreferences(this)
        tokenPreferences = TokenPreferences(this)

        Handler().postDelayed(Runnable {
            checkSession()
        },2000) // 2 seconds splash
    }

    private fun checkSession() {

        val token = tokenPreferences.getToken()

        if (token != null) {

            val call = RetrofitClient.instance.checkSession("Bearer $token")

            call.enqueue(object: Callback<CheckSessionResponse>{
                override fun onResponse(
                    p0: Call<CheckSessionResponse>,
                    response: Response<CheckSessionResponse>
                ) {
                    if (response.isSuccessful) {

                        val checkSessionResponse = response.body()

                        checkSessionResponse?.let {
                            if (it.success == "Valid session") {
                                // Token válido, manejar la redirección según el rol del usuario
                                it.user?.let { user ->
                                    when (user.role) {
                                        "ADMIN" -> {
                                            startActivity(Intent(this@SplashActivity, DashboardAdminActivity::class.java))
                                        }
                                        "CUSTOMER" -> {
                                            startActivity(Intent(this@SplashActivity, DashboardClientActivity::class.java))
                                        }
                                        "TRAINER" -> {
                                            startActivity(Intent(this@SplashActivity, DashboardCoachActivity::class.java))
                                        }
                                        else -> {
                                            // Manejar el caso de un rol desconocido
                                            showToast("Rol desconocido. Por favor, inicie sesión de nuevo.")
                                            clearPreferencesAndLogout()
                                        }
                                    }
                                    finish()
                                }
                            }
                            else {
                                showToast("Error desconocido")
                                clearPreferencesAndLogout()
                            }
                        }
                    }
                    else {
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            handleErrorResponse(it)
                        }
                    }
                }

                override fun onFailure(p0: Call<CheckSessionResponse>, t: Throwable) {
                    showToast("Error de red: ${t.message}")
                    clearPreferencesAndLogout()
                }

            })

        } else {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    private fun handleErrorResponse(errorBody: String) {
        val gson = Gson()
        val errorResponseType = object : TypeToken<SingleErrorResponse>() {}.type
        val errorResponse: SingleErrorResponse? = gson.fromJson(errorBody, errorResponseType)

        errorResponse?.let {
            when (it.error) {
                "Invalid token" -> {
                    showToast("Sesion Expirada")
                    clearPreferencesAndLogout()
                }
                else -> {
                    showToast("Error: ${it.error}")
                    clearPreferencesAndLogout()
                }
            }
        }
    }

    private fun clearPreferencesAndLogout() {
        tokenPreferences.clearToken()
        userPreferences.clearUserData()
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

}