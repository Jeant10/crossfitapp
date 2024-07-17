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
import com.jeantituana2024.tesis.storage.TokenPreferences
import com.jeantituana2024.tesis.storage.UserPreferences
import retrofit2.Callback
import retrofit2.Response

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

        loadUser()

        // Configurar el Toolbar desde el binding
        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Editar Perfil"

        // Habilitar el botón de regreso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)


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


    private fun loadUser() {
        val user = userPreferences.getUser()
        user?.let {

            userId = it.id

            binding.nameEt.setText(it.name)

            //image

            Glide.with(this)
                .load(it.image)
                .placeholder(R.drawable.ic_person_gray) // Placeholder image
                .error(R.drawable.ic_person_gray) // Error image
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Caching strategy
                .into(binding.profileTv)
        }
    }


    private var name = ""

    private fun validateData() {

        name = binding.nameEt.text.toString().trim()

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
                updateRequest = EditProfileWithImageRequest(name, image)
                RetrofitClient.instance.editProfileWithImage("Bearer $token", updateRequest as EditProfileWithImageRequest)
            }
            else -> {

                updateRequest = EditProfileRequest(name)
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
                        userPreferences.saveUser(updateResponse.user)
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
                "image" -> "${error.message}"
                "name" -> "${error.message}"
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