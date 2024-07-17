package com.jeantituana2024.tesis.attendance

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.ActivityMemberAttendancesBinding
import com.jeantituana2024.tesis.models.Attendance
import com.jeantituana2024.tesis.models.AttendancesResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import com.jeantituana2024.tesis.storage.UserPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MemberAttendancesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMemberAttendancesBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var adapterAttendance: AdapterAttendance
    private lateinit var attendanceArrayList: ArrayList<Attendance>

    private var memberId = ""

    // Paso 1: Registrar el ActivityResultLauncher
    private val addAttendanceLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Usuario añadido exitosamente, actualizar la lista
            loadAttendances(memberId)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMemberAttendancesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        tokenPreferences = TokenPreferences(this)

        setupUI()
    }

    private fun setupUI() {

        val intent = intent
        memberId = intent.getStringExtra("memberId")!!

        loadAttendances(memberId)

        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Asistencias del Miembro"

        // Habilitar el botón de regreso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Configurar el comportamiento del botón de regreso
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterAttendance.filter.filter(s)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.fabAddAttendance.setOnClickListener {
            val intent = Intent(this, AddAttendanceActivity::class.java)
            intent.putExtra("memberId",memberId)
            addAttendanceLauncher.launch(intent)
        }
    }

    private fun loadAttendances(memberId: String) {

        attendanceArrayList = ArrayList()

        val token = tokenPreferences.getToken()

        if(token!=null){

            // Mostrar el ProgressBar antes de iniciar la solicitud
            binding.progressBar.visibility = View.VISIBLE

            val call = RetrofitClient.instance.getMemberAttendances("Bearer $token", memberId)

            call.enqueue(object: Callback<AttendancesResponse>{
                override fun onResponse(
                    p0: Call<AttendancesResponse>,
                    response: Response<AttendancesResponse>
                ) {

                    // Ocultar el ProgressBar después de recibir la respuesta
                    binding.progressBar.visibility = View.GONE

                    if(response.isSuccessful){

                        attendanceArrayList.clear()

                        val attendancesList = response.body()?.attendance

                        attendancesList?.let {attendance ->
                            attendanceArrayList.addAll(attendance.values)
                        }
                        Log.d("Asistencias",attendanceArrayList.toString())

                        adapterAttendance = AdapterAttendance(this@MemberAttendancesActivity, attendanceArrayList)
                        binding.membersRv.adapter = adapterAttendance

                        setResult(Activity.RESULT_OK)

                        // Verifica si la lista está vacía y ajusta la visibilidad del TextView
                        if (attendanceArrayList.isEmpty()) {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.membersRv.visibility = View.GONE
                        } else {
                            binding.emptyView.visibility = View.GONE
                            binding.membersRv.visibility = View.VISIBLE
                        }


                    }else{
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            handleErrorResponse(it)
                        }
                    }
                }

                override fun onFailure(p0: Call<AttendancesResponse>, t: Throwable) {

                    binding.progressBar.visibility = View.GONE

                    t.printStackTrace()
                    showToast("Error de red: ${t.message}")
                }

            })
        }else {
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

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}