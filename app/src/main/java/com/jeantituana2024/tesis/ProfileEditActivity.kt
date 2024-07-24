package com.jeantituana2024.tesis

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import retrofit2.Call
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.ActivityProfileEditBinding
import com.jeantituana2024.tesis.models.EditProfileRequest
import com.jeantituana2024.tesis.models.EditProfileResponse
import com.jeantituana2024.tesis.models.EditProfileWithImageRequest
import com.jeantituana2024.tesis.models.ErrorDetail
import com.jeantituana2024.tesis.models.ErrorResponse
import com.jeantituana2024.tesis.models.ProfileResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.models.UserLogin
import com.jeantituana2024.tesis.storage.TokenPreferences
import com.jeantituana2024.tesis.storage.UserPreferences
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding:ActivityProfileEditBinding

    private var imageUri: Uri?=null

    private lateinit var progressDialog: ProgressDialog

    private lateinit var userPreferences: UserPreferences

    private lateinit var tokenPreferences: TokenPreferences

    private val CAMERA_REQUEST_CODE = 0

    private var userId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        userPreferences = UserPreferences(this)
        tokenPreferences = TokenPreferences(this)

        loadUserProfile()

        // Configurar el Toolbar desde el binding
        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Editar Perfil"

        // Habilitar el botón de regreso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        setupGenderSelection()

        // Configurar el comportamiento del botón de regreso
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.profileTv.setOnClickListener {
            checkPermission()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }
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

                            binding.nameEt.setText(userResponse.name)
                            binding.lastnameEt.setText(userResponse.lastname)
                            binding.emailEt.setText(userResponse.email)
                            binding.passwordEt.setText(userResponse.password)
                            binding.phoneEt.setText(userResponse.phone)
                            binding.emergencyPhoneEt.setText(userResponse.emergencyPhone)
                            // Formatear la fecha antes de asignarla al EditText
                            val formattedDate = formatDateString(userResponse.bornDate)
                            binding.bornDateEt.setText(formattedDate)
                            binding.directionEt.setText(userResponse.direction)
                            val genderResponse = userResponse.gender
                            binding.genderRg.check(if (genderResponse == "M") R.id.maleRb else R.id.femaleRb)
                            // Configurar nacionalidad
                            val nationalityResponse = userResponse.nacionality
                            val nationalities = resources.getStringArray(R.array.nationality)
                            val nationalityPosition = nationalities.indexOf(nationalityResponse)
                            if (nationalityPosition >= 0) {
                                binding.spinner.setSelection(nationalityPosition)
                            } else {
                                showToast("La nacionalidad del miembro no es válida")
                            }

                            try {
                                Glide.with(this@ProfileEditActivity)
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

    private var name = ""
    private var lastname = ""
    private var email = ""
    private var password=""
    private var telefono = ""
    private var emergencyPhone = ""
    private var direction = ""
    private var gender = ""
    private var nationality = ""

    private fun validateData() {

        name = binding.nameEt.text.toString().trim()
        lastname = binding.lastnameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        telefono = binding.phoneEt.text.toString().trim()
        emergencyPhone = binding.emergencyPhoneEt.text.toString().trim()
        direction = binding.directionEt.text.toString().trim()
        gender = getSelectedGender()
        nationality = binding.spinner.selectedItem.toString().trim()

        if(name.isEmpty()){
            showToast("Ingresa tu nombre...!")
        }
        else{

            if(imageUri==null){
                updateUser("")
            }
            else{
                uploadImage()
            }
        }
    }

    private fun updateUser(image: String) {

        progressDialog.setMessage("Actualizando Perfil...")
        progressDialog.show()

        val token = tokenPreferences.getToken()
        val updateRequest: Any // Declarar como Any o una interfaz común

        val call = when {
            imageUri != null -> {
                updateRequest = EditProfileWithImageRequest(name, lastname, password, email, telefono, emergencyPhone, direction, gender, nationality, image)
                RetrofitClient.instance.editProfileWithImage("Bearer $token", updateRequest as EditProfileWithImageRequest)
            }
            else -> {

                updateRequest = EditProfileRequest(name, lastname, password, email, telefono, emergencyPhone, direction, gender, nationality)
                RetrofitClient.instance.editProfile("Bearer $token", updateRequest as EditProfileRequest)
            }
        }

        call.enqueue(object: Callback<EditProfileResponse>{
            override fun onResponse(call: Call<EditProfileResponse>, response: Response<EditProfileResponse>) {

                if (response.isSuccessful) {

                    progressDialog.dismiss()
                    val updateResponse = response.body()

                    if (updateResponse?.success != null) {
                        showToast("Perfil Actualizado")
                        updateResponse.user.let {
                            val user = UserLogin(it.id,it.email,it.name,it.role,it.image)
                            userPreferences.saveUser(user)
                        }
                        setResult(Activity.RESULT_OK)
                    } else {
                        showToast("Error desconocido")
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

            override fun onFailure(call: Call<EditProfileResponse>, t: Throwable) {
                progressDialog.dismiss()
                showToast("Fallido al actualizar Perfil debido a: ${t.message}")
            }

        })
    }


    private fun handleValidationErrors(errors: List<ErrorDetail>) {

        val errorMessages = errors.joinToString(separator = "\n") { error ->
            when (error.path[0]) {
                "image" -> "${error.path[0]}: ${error.message}"
                "name" -> "${error.path[0]}: ${error.message}"
                "lastname" -> "${error.path[0]}: ${error.message}"
                "email" -> "${error.path[0]}: ${error.message}"
                "password" -> "${error.path[0]}: ${error.message}"
                "phone" -> "${error.path[0]}: ${error.message}"
                "emergencyPhone" -> "${error.path[0]}: ${error.message}"
                "direction" -> "${error.path[0]}: ${error.message}"
                "gender" -> "${error.path[0]}: ${error.message}"
                "nacionality" -> "${error.path[0]}: ${error.message}"
                else -> "${error.path[0]}: ${error.message}"
            }
        }
        showToast(errorMessages)
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

    private fun uploadImage() {

        progressDialog.setMessage("Subiendo imagen de perfil")
        progressDialog.show()

        val filePathAndName = "ProfileImages/$userId"

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"
                updateUser(uploadedImageUrl)
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this,"Fallido al subir la imagen debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun showImageAttachMenu() {
        val popupMenu = PopupMenu(this, binding.profileTv)
        popupMenu.menu.add(Menu.NONE,0,0,"Camera")
        popupMenu.menu.add(Menu.NONE,1,1,"Gallery")
        popupMenu.show()

        popupMenu.setOnMenuItemClickListener { item ->
            val id = item.itemId
            if(id==0){
                pickImageCamera()
            }
            else if (id==1){
                pickImageGallery()
            }

            true
        }
    }

    private fun pickImageCamera() {
        val values = ContentValues()

        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION,"Tem_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->
            if(result.resultCode == Activity.RESULT_OK){
                //val data = result.data
                //imageUri = data!!.data
                Log.d("image","${imageUri}")
                binding.profileTv.setImageURI(imageUri)
            }else{
                Toast.makeText(this,"Cancelado", Toast.LENGTH_SHORT).show()
            }
        }

    )

    private fun checkPermission(){

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            //El permiso no está aceptado.
            requestCameraPermission()
        } else {
            //El permiso está aceptado.
            showImageAttachMenu()
        }
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this,"Permiso Rechazado Anteriormente, \n ir ajustes para habilitarlo", Toast.LENGTH_SHORT).show()
        } else {
            //Es la primera vez, así que le pedimos que acepte el permiso.
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                CAMERA_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            CAMERA_REQUEST_CODE->{
                if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    showImageAttachMenu()
                }
            }
            else->{
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }

    }


    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->
            if(result.resultCode == Activity.RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                Log.d("image","${imageUri}")
                binding.profileTv.setImageURI(imageUri)

            }else{
                Toast.makeText(this,"Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )

}