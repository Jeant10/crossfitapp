package com.jeantituana2024.tesis.auth

import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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
import java.util.Calendar

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var progressDialog: ProgressDialog
    private var selectedDate: Calendar = Calendar.getInstance() // Variable para almacenar la fecha seleccionada
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        setupGenderSelection()

        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        binding.bornDateEt.setOnClickListener {
            showDatePickerDialog()
        }

        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }

    private var identification = ""
    private var name = ""
    private var lastname = ""
    private var email = ""
    private var password=""
    private var telefono = ""
    private var emergencyPhone = ""
    private var dateBorn = ""
    private var direction = ""
    private var gender = ""
    private var nationality = ""


    private fun validateData(){

        identification = binding.identificationEt.text.toString().trim()
        name = binding.nameEt.text.toString().trim()
        lastname = binding.lastnameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        telefono = binding.phoneEt.text.toString().trim()
        emergencyPhone = binding.emergencyPhoneEt.text.toString().trim()
        dateBorn = binding.bornDateEt.text.toString().trim()
        direction = binding.directionEt.text.toString().trim()
        gender = getSelectedGender()
        nationality = binding.spinner.selectedItem.toString().trim()

        registerUser()

    }

    private fun registerUser() {

        progressDialog.setMessage("Creando Cuenta...")
        progressDialog.show()

        val userData = RegisterRequest(identification, name, lastname, password, email, telefono,emergencyPhone,dateBorn,direction,gender,nationality)
        val call = RetrofitClient.instance.register(userData)

        call.enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {

                    progressDialog.dismiss()
                    val registerResponse = response.body()

                    registerResponse?.let {
                        if (it.success == "Successfully Registered") {
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

    private fun setupGenderSelection() {
        binding.genderRg.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                com.jeantituana2024.tesis.R.id.maleRb -> Log.d("Género seleccionado","M")
                com.jeantituana2024.tesis.R.id.femaleRb -> Log.d("Género seleccionado","F")
            }
        }
    }
    private fun handleValidationErrors(errors: List<ErrorDetail>) {

        val errorMessages = errors.joinToString(separator = "\n") { error ->
            when (error.path[0]) {
                "identification" -> "${error.path[0]}: ${error.message}"
                "name" -> "${error.path[0]}: ${error.message}"
                "lastname" -> "${error.path[0]}: ${error.message}"
                "email" -> "${error.path[0]}: ${error.message}"
                "password" -> "${error.path[0]}: ${error.message}"
                "phone" -> "${error.path[0]}: ${error.message}"
                "emergencyPhone" -> "${error.path[0]}: ${error.message}"
                "bornDate" -> "${error.message}"
                "direction" -> "${error.path[0]}: ${error.message}"
                "gender" -> "${error.path[0]}: ${error.message}"
                "nacionality" -> "${error.path[0]}: ${error.message}"
                else -> "${error.path[0]}: ${error.message}"
            }
        }
        showToast(errorMessages)
    }

    private fun showDatePickerDialog() {
        val year = selectedDate.get(Calendar.YEAR)
        val month = selectedDate.get(Calendar.MONTH)
        val day = selectedDate.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.bornDateEt.setText(formattedDate)

                // Actualiza selectedDate con la nueva fecha seleccionada
                selectedDate.set(selectedYear, selectedMonth, selectedDay)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun getSelectedGender(): String {
        return when (binding.genderRg.checkedRadioButtonId) {
            com.jeantituana2024.tesis.R.id.maleRb -> "M"
            com.jeantituana2024.tesis.R.id.femaleRb -> "F"
            else -> ""
        }
    }
    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }


}