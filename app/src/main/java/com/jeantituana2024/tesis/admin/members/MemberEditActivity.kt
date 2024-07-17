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
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.ActivityMemberEditBinding
import com.jeantituana2024.tesis.models.ErrorDetail
import com.jeantituana2024.tesis.models.ErrorResponse
import com.jeantituana2024.tesis.models.MemberResponse
import com.jeantituana2024.tesis.models.MemberUpdateRequest
import com.jeantituana2024.tesis.models.Plan
import com.jeantituana2024.tesis.models.PlansResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class MemberEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemberEditBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var planArrayList: ArrayList<Plan>
    private var selectedPlanId: Int = 0
    private var selectedDate: Calendar = Calendar.getInstance() // Variable para almacenar la fecha seleccionada
    private var memberId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemberEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere Porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        tokenPreferences = TokenPreferences(this)

        loadPlans()

        val intent = intent
        memberId = intent.getStringExtra("memberId")!!

        loadMember(memberId)

        // Configurar el Toolbar desde el binding
        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Editar Miembro"

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

        binding.updateBtn.setOnClickListener {
            getData()
        }
    }

    private var identification = ""
    private var name = ""
    private var apellido = ""
    private var telefono = ""
    private var emergencyPhone = ""
    private var dateBorn = ""
    private var direction = ""
    private var gender = ""
    private var nationality = ""

    private fun getData() {

        identification = binding.identificationEt.text.toString().trim()
        name = binding.nameEt.text.toString().trim()
        apellido = binding.lastnameEt.text.toString().trim()
        telefono = binding.phoneEt.text.toString().trim()
        emergencyPhone = binding.emergencyPhoneEt.text.toString().trim()
        dateBorn = binding.bornDateEt.text.toString().trim()
        direction = binding.directionEt.text.toString().trim()
        gender = getSelectedGender()
        nationality = binding.spinner.selectedItem.toString().trim()

        updateMember()

    }

    private fun loadMember(memberId: String) {
        progressDialog.setMessage("Cargando...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        if (token != null) {

            val call = RetrofitClient.instance.viewMember("Bearer $token", memberId)

            call.enqueue(object: Callback<MemberResponse>{
                override fun onResponse(p0: Call<MemberResponse>, response: Response<MemberResponse>) {
                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        val memberResponse = response.body()?.member

                        if (memberResponse != null) {

                            binding.identificationEt.setText(memberResponse.identification)
                            binding.nameEt.setText(memberResponse.name)
                            binding.lastnameEt.setText(memberResponse.lastname)
                            binding.phoneEt.setText(memberResponse.phone)
                            binding.emergencyPhoneEt.setText(memberResponse.emergencyPhone)
                            // Formatear la fecha antes de asignarla al EditText
                            val formattedDate = formatDateString(memberResponse.bornDate)
                            binding.bornDateEt.setText(formattedDate)
                            // Actualiza la variable selectedDate
                            updateSelectedDate(formattedDate)

                            binding.directionEt.setText(memberResponse.direction)


                            val genderResponse = memberResponse.gender
                            binding.genderRg.check(if (genderResponse == "M") R.id.maleRb else R.id.femaleRb)

                            val planIdResponse = memberResponse.planId
                            // Configurar plan
                            val planPosition = getPlanIndex(planIdResponse)
                            if (planPosition >= 0) {
                                binding.planSpinner.setSelection(planPosition)
                            } else {
                                showToast("El plan del miembro no es válido")
                            }

                            // Configurar nacionalidad
                            val nationalityResponse = memberResponse.nacionality
                            val nationalities = resources.getStringArray(R.array.nationality)
                            val nationalityPosition = nationalities.indexOf(nationalityResponse)
                            if (nationalityPosition >= 0) {
                                binding.spinner.setSelection(nationalityPosition)
                            } else {
                                showToast("La nacionalidad del miembro no es válida")
                            }

                        }
                        else {
                            showToast("No se encontró el miembro")
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

                override fun onFailure(p0: Call<MemberResponse>, p1: Throwable) {
                    progressDialog.dismiss()
                    showToast("Error de conexión: ${p1.message}")
                }

            })
        }
        else {
            progressDialog.dismiss()
            showToast("No se encontró el token de sesión")
        }
    }


    private fun updateMember() {
        progressDialog.setMessage("Actualizando miembro...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        if (token != null) {

            val editRequest = MemberUpdateRequest(identification,name,apellido,telefono,emergencyPhone,dateBorn,direction,gender,nationality,selectedPlanId)
            val call = RetrofitClient.instance.updateMember("Bearer $token", memberId, editRequest)

            call.enqueue(object: Callback<MemberResponse>{
                override fun onResponse(p0: Call<MemberResponse>, response: Response<MemberResponse>) {
                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        val updateResponse = response.body()

                        if (updateResponse?.success != null) {
                            // Mostrar mensaje de éxito
                            showToast("Miembro Actualizado")
                            setResult(Activity.RESULT_OK)
                        } else {
                            showToast("Error desconocido")
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

                override fun onFailure(p0: Call<MemberResponse>, p1: Throwable) {
                    progressDialog.dismiss()
                    showToast("Error de conexión: ${p1.message}")
                }

            })


        }
        else {
            progressDialog.dismiss()
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
                "name" -> "Name: ${error.message}"
                "lastname" -> "Last Name: ${error.message}"
                "phone" -> "Phone: ${error.message}"
                "emergency_phone" -> "Emergency Phone: ${error.message}"
                "born_date" -> "Enter a Date"
                "direction" -> "Direction: ${error.message}"
                "gender" -> "Choose a gender: M or F"
                "nacionality" -> "${error.message}"
                "planId" -> "${error.message}"
                else -> "${error.path[0]}: ${error.message}"
            }
        }
        showToast(errorMessages)
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

    // Método para actualizar la variable selectedDate
    private fun updateSelectedDate(dateString: String) {
        try {
            val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = format.parse(dateString)
            selectedDate.time = date
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

    private fun setupGenderSelection() {
        binding.genderRg.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.maleRb -> Log.d("Género seleccionado","M")
                R.id.femaleRb -> Log.d("Género seleccionado","F")
            }
        }
    }

    private fun getSelectedGender(): String {
        return when (binding.genderRg.checkedRadioButtonId) {
            R.id.maleRb -> "M"
            R.id.femaleRb -> "F"
            else -> ""
        }
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

    private fun getPlanIndex(planId: Int): Int {
        for (i in planArrayList.indices) {
            if (planArrayList[i].id == planId) {
                return i
            }
        }
        return -1 // Si no se encuentra el plan
    }

    private fun formatDateString(dateString: String): String {
        return try {
            // Define el formato original de la fecha
            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            originalFormat.timeZone = TimeZone.getTimeZone("UTC") // Asegurar que sea interpretado como UTC

            // Parsear la fecha en el formato original
            val date = originalFormat.parse(dateString)

            // Define el nuevo formato de la fecha
            val desiredFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            desiredFormat.timeZone = TimeZone.getTimeZone("UTC") // Mantener la salida en UTC
            // Formatear la fecha al nuevo formato y devolverla
            desiredFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            dateString // En caso de error, devolver el string original
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}