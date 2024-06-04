package com.jeantituana2024.tesis.auth

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.client.DashboardClientActivity
import com.jeantituana2024.tesis.databinding.ActivityRegisterBinding
import com.jeantituana2024.tesis.models.DefaultResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener{
            onBackPressed()
        }

        binding.registerBtn.setOnClickListener {
            validateData()
        }
    }

    private var name = ""
    private var email = ""
    private var password = ""

    private fun validateData(){

        name = binding.nameEt.text.toString().trim()
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()
        val cPassword = binding.cPasswordEt.text.toString().trim()

        if(name.isEmpty()){
            Toast.makeText(this,"Ingresa tu nombre...!", Toast.LENGTH_SHORT).show()
        }
        else if(email.isEmpty()){
            Toast.makeText(this,"Ingresa tu correo electrónico...!", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Correo electrónico no válido...!", Toast.LENGTH_SHORT).show()
        }
        else if(password.isEmpty()){
            Toast.makeText(this,"Introducir la contraseña...!", Toast.LENGTH_SHORT).show()
        }
        else if(cPassword.isEmpty()){
            Toast.makeText(this,"Confirmar Contraseña...!", Toast.LENGTH_SHORT).show()
        }
        else if(password != cPassword){
            Toast.makeText(this,"La contraseñas no coinciden...!", Toast.LENGTH_SHORT).show()
        }
        else{
            createUserAccount()
//            RetrofitClient.instance.createUser(
//                name,
//                email,
//                password
//                )
//                .enqueue(object: Callback<DefaultResponse>{
//                    override fun onResponse(
//                        p0: Call<DefaultResponse>,
//                        response: Response<DefaultResponse>
//                    ) {
//
//                        Toast.makeText(applicationContext, response.body()?.message,Toast.LENGTH_SHORT).show()
//                    }
//
//                    override fun onFailure(p0: Call<DefaultResponse>, p1: Throwable) {
//                        Toast.makeText(applicationContext,p1.message,Toast.LENGTH_LONG).show()
//                    }
//
//                })
        }


    }

    private fun createUserAccount() {
        progressDialog.setMessage("Creando cuenta...")
        progressDialog.show()

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                updateUserInfo()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this,"Fallido al crear la cuenta debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }

    }

    private fun updateUserInfo(){
        progressDialog.setMessage("Guardando información del usuario...")

        //timestamp
        val timestamp = System.currentTimeMillis()

        //get currente user uid, since user is registered so we can get now
        val uid = firebaseAuth.uid

        val hashMap: HashMap<String, Any?> = HashMap()

        hashMap["uid"] = uid
        hashMap["email"] = email
        hashMap["name"] = name
        hashMap["profileImage"] = ""
        hashMap["userType"] = "user"
        hashMap["timestamp"] = timestamp
        hashMap["state"] = true

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Cuenta creada...", Toast.LENGTH_SHORT).show()
                //since user account is created to start dashboard of user
                startActivity(Intent(this@RegisterActivity,DashboardClientActivity::class.java))
                finish()
            }

            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this,"Fallido al guardar la informacion del usuario debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}