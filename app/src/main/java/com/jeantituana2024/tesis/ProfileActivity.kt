package com.jeantituana2024.tesis

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.jeantituana2024.tesis.databinding.ActivityProfileBinding
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.models.EditUserResponse
import com.jeantituana2024.tesis.models.ProfileResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import com.jeantituana2024.tesis.storage.UserPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone


class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var tokenPreferences: TokenPreferences

    private val editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadUserProfile()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        tokenPreferences = TokenPreferences(this)

        // Configurar el Toolbar desde el binding
        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Perfil"

        // Habilitar el botón de regreso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        loadUserProfile()

        // Configurar el comportamiento del botón de regreso
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.profileEditBtn.setOnClickListener {
            val intent = Intent(this, ProfileEditActivity::class.java)
            editProfileLauncher.launch(intent)

        }
    }

    override fun onBackPressed() {
        val resultIntent = Intent()
        setResult(Activity.RESULT_OK, resultIntent)
        super.onBackPressed()
    }

    private fun loadUserProfile() {

        progressDialog.setMessage("Cargando...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        if (token != null) {

            val call = RetrofitClient.instance.viewProfile("Bearer $token")

            call.enqueue(object: Callback<ProfileResponse> {
                override fun onResponse(
                    p0: Call<ProfileResponse>,
                    response: Response<ProfileResponse>
                ) {
                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        val userResponse = response.body()?.user
                        if (userResponse != null) {

                            val userName = getString(R.string.member_name, userResponse.name, userResponse.lastname)
                            binding.nameTv.text = userName
                            binding.idTv.text = userResponse.id
                            binding.identificationTv.text = userResponse.identification
                            binding.emailTv.text = userResponse.email
                            binding.phoneTv.text = userResponse.phone
                            binding.emergencyPhoneTv.text = userResponse.emergencyPhone
                            // Formatear la fecha antes de asignarla al EditText
                            val formattedDate = formatDateString(userResponse.bornDate)
                            binding.bornDateTv.text = formattedDate
                            binding.directionTv.text = userResponse.direction
                            binding.genderTv.text = userResponse.gender
                            binding.nacionalityTv.text = userResponse.nacionality
                            binding.statusTv.text = if (userResponse.status) "Activo" else "Inactivo"
                            binding.roleTv.text = userResponse.role
                            // Formatear la fecha antes de asignarla al EditText
                            val formattedDateEmail = formatDateString(userResponse.emailVerified)
                            binding.emailVerifiedTv.text = formattedDateEmail

                            try {
                                Glide.with(this@ProfileActivity)
                                    .load(userResponse.image)
                                    .placeholder(R.drawable.ic_person_gray)
                                    .into(binding.profileTv)

                            }catch (e:Exception){

                            }

                        } else {
                            showToast("No se encontró el usuario")
                        }
                    }
                    else {
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            progressDialog.dismiss()
                            handleErrorResponse(it)
                        }
                    }
                }

                override fun onFailure(p0: Call<ProfileResponse>, p1: Throwable) {
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
    private fun handleErrorResponse(errorBody: String) {
        val gson = Gson()
        val errorResponseType = object : TypeToken<SingleErrorResponse>() {}.type
        val errorResponse: SingleErrorResponse? = gson.fromJson(errorBody, errorResponseType)

        errorResponse?.let {
            when (it.error) {
                "Session not found"-> {
                    showUnVerifiedEmail()
                }
                "Invalid token" -> {
                    showSessionExpiredAlert()
                }
                else -> {
                    showToast("Error: ${it.error}")
                }
            }
        }
    }

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

    private fun showUnVerifiedEmail() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Correo Actualizado")
        builder.setMessage("Vuelve a verificar tu correo")
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