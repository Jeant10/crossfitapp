package com.jeantituana2024.tesis.admin.users

import android.R
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.ActivityAddUserBinding
import com.jeantituana2024.tesis.models.ErrorDetail
import com.jeantituana2024.tesis.models.ErrorResponse
import com.jeantituana2024.tesis.models.Plan
import com.jeantituana2024.tesis.models.PlansResponse
import com.jeantituana2024.tesis.models.RegisterResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.models.UserRequest
import com.jeantituana2024.tesis.models.UserResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar


class AddUserActivity : AppCompatActivity() {

    private lateinit var binding:ActivityAddUserBinding
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var progressDialog: ProgressDialog
    private var selectedDate: Calendar = Calendar.getInstance() // Variable para almacenar la fecha seleccionada
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

        setupGenderSelection()

        // Configurar el comportamiento del botón de regreso
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.bornDateEt.setOnClickListener {
            showDatePickerDialog()
        }

        binding.submitBtn.setOnClickListener {
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
    private var rol=""
    private var nationality = ""

    private fun validateData() {

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
        rol = binding.roleSpinner.selectedItem.toString().trim()

        registerUser()

    }

    private fun registerUser() {

        progressDialog.setMessage("Creando Usuario...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        val userData = UserRequest(identification, name, lastname, password, email, telefono, emergencyPhone,
            dateBorn,direction,gender,rol,nationality)

        val call = RetrofitClient.instance.createUser("Bearer $token", userData)

        call.enqueue(object : Callback<UserResponse> {
            override fun onResponse(call: Call<UserResponse>, response: Response<UserResponse>) {
                if (response.isSuccessful) {
                    progressDialog.dismiss()

                    val registerResponse = response.body()

                    registerResponse?.let {
                        if (it.success == "Create User Successfully") {
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

            override fun onFailure(call: Call<UserResponse>, t: Throwable) {
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
                "borndate" -> "${error.path[0]}: ${error.message}"
                "direction" -> "${error.path[0]}: ${error.message}"
                "gender" -> "${error.path[0]}: ${error.message}"
                "role" -> "${error.path[0]}: ${error.message}"
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