package com.jeantituana2024.tesis.admin.members

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.admin.users.AdapterUser
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.ActivityAddMemberBinding
import com.jeantituana2024.tesis.models.ErrorDetail
import com.jeantituana2024.tesis.models.ErrorResponse
import com.jeantituana2024.tesis.models.Member
import com.jeantituana2024.tesis.models.MemberRequest
import com.jeantituana2024.tesis.models.MemberResponse
import com.jeantituana2024.tesis.models.MembersResponse
import com.jeantituana2024.tesis.models.Plan
import com.jeantituana2024.tesis.models.PlansResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.models.UserModel
import com.jeantituana2024.tesis.models.UsersResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AddMemberActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddMemberBinding
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var progressDialog: ProgressDialog
    private lateinit var planArrayList: ArrayList<Plan>
    private var selectedPlanId: Int = 0
    private lateinit var userArrayList: ArrayList<UserModel>
    private var selectedUserEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMemberBinding.inflate(layoutInflater)
        setContentView(binding.root)


        progressDialog = ProgressDialog(this)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere Porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        tokenPreferences = TokenPreferences(this)

        loadPlans()

        loadUsers()
        // Configurar el Toolbar desde el binding
        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Crear Miembro"

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


    private fun validateData() {

        registerMember()
    }

    private fun registerMember() {

        progressDialog.setMessage("Creando miembro...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        val memberData = MemberRequest(selectedUserEmail, selectedPlanId)

        val call = RetrofitClient.instance.createMember("Bearer $token", memberData)

        call.enqueue(object : Callback<MemberResponse>{
            override fun onResponse(p0: Call<MemberResponse>, response: Response<MemberResponse>) {
                if (response.isSuccessful) {

                    progressDialog.dismiss()

                    val registerResponse = response.body()

                    registerResponse?.let {
                        if (it.success == "Member created successfully") {
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

            override fun onFailure(p0: Call<MemberResponse>, t: Throwable) {
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

    private fun setupUserSpinner() {
        // Obtener los nombres de los miembros para mostrar en el Spinner
        val userEmails = userArrayList.map { it.email }

        // Configurar el Adapter para el Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, userEmails)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.userSpinner.adapter = adapter

        // Manejar la selección del usuario
        binding.userSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

                val selectedUser = userArrayList[position]
                selectedUserEmail = selectedUser.email
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada si no se selecciona nada
            }
        }
    }

    private fun setupPlanSpinner() {
        // Obtener los nombres de los planes para mostrar en el Spinner
        val planNames = planArrayList.map { it.name }

        // Configurar el Adapter para el Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, planNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.planSpinner.adapter = adapter

        // Manejar la selección del usuario
        binding.planSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Obtener el plan seleccionado
                val selectedPlan = planArrayList[position]
                selectedPlanId = selectedPlan.id // Guardar el ID del plan seleccionado
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada si no se selecciona nada
            }
        }
    }


    private fun handleValidationErrors(errors: List<ErrorDetail>) {

        val errorMessages = errors.joinToString(separator = "\n") { error ->
            when (error.path[0]) {
                "email" -> "${error.path[0]}: ${error.message}"
                "planId" -> "${error.path[0]}: ${error.message}"
                else -> "${error.path[0]}: ${error.message}"
            }
        }
        showToast(errorMessages)
    }


    private fun loadPlans() {
        planArrayList = ArrayList()
        val token = tokenPreferences.getToken()

        if (token != null) {
            val call = RetrofitClient.instance.getPlans("Bearer $token")

            call.enqueue(object : Callback<PlansResponse> {
                override fun onResponse(call: Call<PlansResponse>, response: Response<PlansResponse>) {
                    if (response.isSuccessful) {
                        planArrayList.clear()

                        val usersMap = response.body()?.plan
                        if (usersMap != null) {
                            planArrayList.addAll(usersMap.values)
                        }

                        Log.d("Planes", "${{planArrayList}}")

                        setupPlanSpinner()

                    } else {
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            handleErrorResponse(it)
                        }
                    }
                }

                override fun onFailure(call: Call<PlansResponse>, t: Throwable) {
                    t.printStackTrace()
                    showToast("Network error: ${t.message}")
                }
            })
        } else {
            showToast("Session token not found")
        }
    }

    private fun loadUsers() {
        userArrayList = ArrayList()
        val token = tokenPreferences.getToken()

        if (token != null) {

            val call = RetrofitClient.instance.getUsers("Bearer $token")

            call.enqueue(object : Callback<UsersResponse> {
                override fun onResponse(call: Call<UsersResponse>, response: Response<UsersResponse>) {

                    if (response.isSuccessful) {


                        userArrayList.clear()

                        val usersMap = response.body()?.user
                        if (usersMap != null) {
                            userArrayList.addAll(usersMap.values)
                        }

                        Log.d("Users",userArrayList.toString())

                        setupUserSpinner()

                    } else {
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            handleErrorResponse(it)
                        }
                    }
                }

                override fun onFailure(call: Call<UsersResponse>, t: Throwable) {

                    t.printStackTrace()
                    showToast("Error de red: ${t.message}")
                }
            })
        } else {
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

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}