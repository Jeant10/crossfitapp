package com.jeantituana2024.tesis.auth

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.models.ResetPasswordRequest
import com.jeantituana2024.tesis.databinding.ActivityForgotPasswordBinding
import com.jeantituana2024.tesis.models.GenericResponse
import com.jeantituana2024.tesis.models.LoginResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.submitBtn.setOnClickListener{
            validateData()
        }
    }

    private var email=""
    private fun validateData() {

        email = binding.emailEt.text.toString().trim()

        if(email.isEmpty()){
            Toast.makeText(this,"Email requerido..!", Toast.LENGTH_SHORT).show()
        }
        else{
            resetPassword()
        }
    }

    private fun resetPassword(){

        progressDialog.setMessage("Enviando...")
        progressDialog.show()

        val resetPasswordRequest = ResetPasswordRequest(email)
        val call = RetrofitClient.instance.resetPassword(resetPasswordRequest)
        call.enqueue(object : Callback<GenericResponse>{
            override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {
                if (response.isSuccessful) {

                    progressDialog.dismiss()

                    val resetResponse = response.body()
                    resetResponse?.let {
                        if (it.success == "Reset email sent!") {
                            // Mostrar mensaje de éxito
                            showToast("Email de restablecimiento enviado. Por favor, verifica tu correo.")
                        }
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

            override fun onFailure(p0: Call<GenericResponse>, e: Throwable) {
                progressDialog.dismiss()
                showToast("Error al procesar la respuesta del servidor ${e.message}")
            }

        })
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }

    private fun handleErrorResponse(errorBody: String) {
        val gson = Gson()
        val errorResponseType = object : TypeToken<SingleErrorResponse>() {}.type
        val errorResponse: SingleErrorResponse? = gson.fromJson(errorBody, errorResponseType)

        errorResponse?.let {
            when (it.error) {
                "Invalid email" -> {
                    Toast.makeText(this, "El correo electrónico no es válido", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Toast.makeText(this, "Error: ${it.error}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}