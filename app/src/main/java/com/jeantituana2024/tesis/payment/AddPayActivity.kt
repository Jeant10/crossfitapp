package com.jeantituana2024.tesis.payment

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
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.ActivityAddPayBinding
import com.jeantituana2024.tesis.models.ErrorDetail
import com.jeantituana2024.tesis.models.ErrorResponse
import com.jeantituana2024.tesis.models.Member
import com.jeantituana2024.tesis.models.MembersResponse
import com.jeantituana2024.tesis.models.PaymentRequest
import com.jeantituana2024.tesis.models.PaymentResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AddPayActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddPayBinding
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var progressDialog: ProgressDialog
    private lateinit var memberArrayList: ArrayList<Member>
    private var selectedMemberId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddPayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere Porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        tokenPreferences = TokenPreferences(this)

        loadMembers()

        // Configurar el Toolbar desde el binding
        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Crear Pago"

        // Habilitar el botón de regreso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Configurar el comportamiento del botón de regreso
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.dateEt.setOnClickListener {
            showDatePickerDialog()
        }

        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }
    private var date = ""
    private var typePay = ""

    private fun validateData() {

        date = binding.dateEt.text.toString().trim()
        typePay = binding.spinner.selectedItem.toString().trim()

        registerPay()
    }

    private fun registerPay() {

        progressDialog.setMessage("Creando pago...")
        progressDialog.show()

        val token = tokenPreferences.getToken()
        val memberId = selectedMemberId.toString()

        val payData = PaymentRequest(date,typePay)

        val call = RetrofitClient.instance.createPayment("Bearer $token", memberId, payData)

        call.enqueue(object : Callback<PaymentResponse>{
            override fun onResponse(p0: Call<PaymentResponse>, response: Response<PaymentResponse>) {
                if (response.isSuccessful) {

                    progressDialog.dismiss()

                    val registerResponse = response.body()

                    registerResponse?.let {
                        if (it.success == "Pay created successfully") {
                            // Redirigir a LoginActivity
                            showToast("Registro exitoso")
                            setResult(Activity.RESULT_OK)
                        }
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

            override fun onFailure(p0: Call<PaymentResponse>, t: Throwable) {
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

    private fun setupMemberSpinner() {
        // Obtener los nombres de los miembros para mostrar en el Spinner
        val memberNames = memberArrayList.map { getString(R.string.member_name, it.user.name, it.user.lastname) }

        // Configurar el Adapter para el Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, memberNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.memberSpinner.adapter = adapter

        // Manejar la selección del usuario
        binding.memberSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                // Obtener el member seleccionado
                val selectedMember = memberArrayList[position]
                selectedMemberId = selectedMember.id // Guardar el ID del member seleccionado
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // No hacer nada si no se selecciona nada
            }
        }
    }

    private fun handleValidationErrors(errors: List<ErrorDetail>) {

        val errorMessages = errors.joinToString(separator = "\n") { error ->
            when (error.path[0]) {
                "date" -> "${error.path[0]}: ${error.message}"
                "payment_type" -> "${error.path[0]}: ${error.message}"
                else -> "${error.path[0]}: ${error.message}"
            }
        }
        showToast(errorMessages)
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay)
                binding.dateEt.setText(formattedDate)
            },
            year, month, day
        )
        datePickerDialog.show()
    }

    private fun loadMembers(){
        memberArrayList = ArrayList()
        val token = tokenPreferences.getToken()

        if(token!=null){

            val call = RetrofitClient.instance.getMembers("Bearer $token")

            call.enqueue(object: Callback<MembersResponse>{
                override fun onResponse(call: Call<MembersResponse>, response: Response<MembersResponse>) {
                    if(response.isSuccessful){

                        memberArrayList.clear()

                        val membersList = response.body()?.members

                        membersList?.let{ members ->
                            memberArrayList.addAll(members)
                        }

                        Log.d("Members",memberArrayList.toString())

                        setupMemberSpinner()

                    }else{
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            handleErrorResponse(it)
                        }
                    }
                }

                override fun onFailure(call: Call<MembersResponse>, t: Throwable) {
                    t.printStackTrace()
                    showToast("Network error: ${t.message}")
                }

            })
        }else {
            showToast("Session token not found")
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