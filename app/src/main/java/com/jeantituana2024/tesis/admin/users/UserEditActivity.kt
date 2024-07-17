package com.jeantituana2024.tesis.admin.users

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.ActivityUserEditBinding
import com.jeantituana2024.tesis.models.EditProfileRequest
import com.jeantituana2024.tesis.models.EditProfileWithImageRequest
import com.jeantituana2024.tesis.models.EditUserRequest
import com.jeantituana2024.tesis.models.EditUserResponse
import com.jeantituana2024.tesis.models.EditUserWithImageRequest
import com.jeantituana2024.tesis.models.ErrorDetail
import com.jeantituana2024.tesis.models.ErrorResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class UserEditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserEditBinding
    private lateinit var progressDialog: ProgressDialog
    private lateinit var tokenPreferences: TokenPreferences
    private val CAMERA_REQUEST_CODE = 0

    private var userUid = ""
    private var userName = ""
    private var imageUri: Uri?=null
    private var rol=""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        tokenPreferences = TokenPreferences(this)

        val intent = intent
        userUid = intent.getStringExtra("userId")!!

        loadUser(userUid)

        // Configurar el Toolbar desde el binding
        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Editar Usuario"

        // Habilitar el botón de regreso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


        // Configurar el comportamiento del botón de regreso
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.userTv.setOnClickListener {
            checkPermission()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }

    private fun loadUser(userUid: String) {

        progressDialog.setMessage("Cargando...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        if (token != null) {

            val call = RetrofitClient.instance.viewUser("Bearer $token", userUid)

            call.enqueue(object: Callback<EditUserResponse>{
                override fun onResponse(
                    p0: Call<EditUserResponse>,
                    response: Response<EditUserResponse>
                ) {
                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        val userResponse = response.body()?.user
                        if (userResponse != null) {

                            binding.nameEt.setText(userResponse.name)

                            val role = userResponse.role

                            // Obtener la posición del rol en el Spinner
                            val roles = resources.getStringArray(R.array.options)
                            val rolePosition = roles.indexOf(role)

                            //luego añadir trainer
                            // Verificar si el rol está en la lista y establecer la selección
                            if (rolePosition >= 0) {
                                binding.spinner.setSelection(rolePosition)
                            } else {
                                showToast("El rol del usuario no es válido")
                            }

                            try {
                                Glide.with(this@UserEditActivity)
                                    .load(userResponse.image)
                                    .placeholder(R.drawable.ic_person_gray)
                                    .into(binding.userTv)

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

                override fun onFailure(p0: Call<EditUserResponse>, p1: Throwable) {
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
                "image" -> "${error.message}"
                "role" -> "${error.message}"
                "name" -> "${error.message}"
                else -> "${error.path[0]}: ${error.message}"
            }
        }
        showToast(errorMessages)
    }


    private fun validateData() {

        userName = binding.nameEt.text.toString().trim()
        rol = binding.spinner.selectedItem.toString().trim()

        if(userName.isEmpty()){
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

    private fun showImageAttachMenu() {
        val popupMenu = PopupMenu(this, binding.userTv)
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
        ActivityResultCallback<ActivityResult>{ result ->
            if(result.resultCode == Activity.RESULT_OK){
                //val data = result.data
                //imageUri = data!!.data
                Log.d("image","${imageUri}")
                binding.userTv.setImageURI(imageUri)
            }else{
                Toast.makeText(this,"Cancelado", Toast.LENGTH_SHORT).show()
            }
        }

    )

    private fun uploadImage() {

        progressDialog.setMessage("Subiendo imagen de perfil")
        progressDialog.show()

        val filePathAndName = "ProfileImages/$userUid"

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

    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult>{result ->
            if(result.resultCode == RESULT_OK){
                val data = result.data
                imageUri = data!!.data
                Log.d("image","${imageUri}")
                binding.userTv.setImageURI(imageUri)

            }else{
                Toast.makeText(this,"Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )

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

    private fun updateUser(image: String) {
        progressDialog.setMessage("Actualizando usuario...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        if (token != null) {

            val updateRequest: Any // Declarar como Any o una interfaz común

            val call = when {
                imageUri != null -> {
                    updateRequest = EditUserWithImageRequest(userName, rol, image)
                    RetrofitClient.instance.editUserWithImage("Bearer $token", userUid, updateRequest)
                }
                else -> {

                    updateRequest = EditUserRequest(userName, rol)
                    RetrofitClient.instance.editUser("Bearer $token", userUid, updateRequest)
                }
            }

            call.enqueue(object: Callback<EditUserResponse>{
                override fun onResponse(
                    p0: Call<EditUserResponse>,
                    response: Response<EditUserResponse>
                ) {
                    if (response.isSuccessful) {

                        progressDialog.dismiss()
                        val updateResponse = response.body()

                        if (updateResponse?.success != null) {
                            // Mostrar mensaje de éxito
                            showToast("Usuario Actualizado")
                            setResult(Activity.RESULT_OK)
                        } else {
                            showToast("Error desconocido")
                        }

                    }else {

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

                override fun onFailure(p0: Call<EditUserResponse>, p1: Throwable) {
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

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}