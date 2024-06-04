package com.jeantituana2024.tesis

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.jeantituana2024.tesis.databinding.ActivityProfileEditBinding

class ProfileEditActivity : AppCompatActivity() {

    private lateinit var binding:ActivityProfileEditBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private var imageUri: Uri?=null

    private lateinit var progressDialog: ProgressDialog

    private val CAMERA_REQUEST_CODE = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        loadUserInfo()

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.profileTv.setOnClickListener {
            checkPermission()
        }

        binding.updateBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private fun validateData() {

        name = binding.nameEt.text.toString().trim()

        if(name.isEmpty()){
            Toast.makeText(this,"Ingresa tu nombre...!", Toast.LENGTH_SHORT).show()
        }
        else{

            if(imageUri==null){
                updateProfile("")
            }
            else{
                uploadImage()
            }
        }
    }

    private fun uploadImage() {
        progressDialog.setMessage("Subiendo imagen de perfil")
        progressDialog.show()

        val filePathAndName = "ProfileImages/"+firebaseAuth.uid

        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"
                updateProfile(uploadedImageUrl)
            }
            .addOnFailureListener{e->
                progressDialog.dismiss()
                Toast.makeText(this,"Fallido al subir la imagen debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfile(uploadedImageUrl: String) {
        progressDialog.setMessage("Actualizando perfil")

        val hashmap: HashMap<String,Any> = HashMap()
        hashmap["name"] = "${name}"
        if(imageUri != null){
            hashmap["profileImage"] = uploadedImageUrl
        }

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(firebaseAuth.uid!!)
            .updateChildren(hashmap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Perfil actualizado", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this,"Fallido al actualizar perfil debido a ${e.message}", Toast.LENGTH_SHORT).show()

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

                binding.profileTv.setImageURI(imageUri)

            }else{
                Toast.makeText(this,"Cancelado", Toast.LENGTH_SHORT).show()
            }
        }
    )

    private fun loadUserInfo() {
        val ref = FirebaseDatabase.getInstance().getReference("Users")

        ref.child(firebaseAuth.uid!!)
            .addValueEventListener(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    val name = "${snapshot.child("name").value}"
                    val profileImage = "${snapshot.child("profileImage").value}"

                    binding.nameEt.setText(name)

                    //image

                    try {
                        Glide.with(this@ProfileEditActivity)
                            .load(profileImage)
                            .placeholder(R.drawable.ic_person_gray)
                            .into(binding.profileTv)

                    }catch (e:Exception){

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }
}