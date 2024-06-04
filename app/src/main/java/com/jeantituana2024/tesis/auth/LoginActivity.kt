package com.jeantituana2024.tesis.auth

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.admin.DashboardAdminActivity
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.client.DashboardClientActivity
import com.jeantituana2024.tesis.databinding.ActivityLoginBinding
import com.jeantituana2024.tesis.models.LoginResponse
import com.jeantituana2024.tesis.storage.SharedPrefManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.noAccountTv.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }

        binding.loginBtn.setOnClickListener {
            validateData()
        }

        binding.forgotTv.setOnClickListener {
            startActivity(Intent(this,ForgotPasswordActivity::class.java))
        }
    }

    private var email=""
    private var password=""

    private fun validateData() {
        email = binding.emailEt.text.toString().trim()
        password = binding.passwordEt.text.toString().trim()


        if(email.isEmpty()){
            Toast.makeText(this,"Ingresa tu correo electrónico...!", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Correo electrónico no valido...!", Toast.LENGTH_SHORT).show()
        }
        else if(password.isEmpty()){
            Toast.makeText(this,"Ingresa tu contraseña...!", Toast.LENGTH_SHORT).show()
        }
        else{
            loginUser()
//            RetrofitClient.instance.userLogin(email,password)
//                .enqueue(object: Callback<LoginResponse>{
//                    override fun onResponse(p0: Call<LoginResponse>, response: Response<LoginResponse>) {
//                        if(!response.body()?.error!!){
//                            SharedPrefManager.getInstance(applicationContext)?.saveUser(response.body()?.user!!)
//
//                            val intent = Intent(this@LoginActivity, DashboardClientActivity::class.java)
//                            startActivity(intent)
//
//                        }else{
//                            Toast.makeText(applicationContext, response.body()?.message,Toast.LENGTH_SHORT).show()
//                        }
//                    }
//
//                    override fun onFailure(p0: Call<LoginResponse>, p1: Throwable) {
//                        Toast.makeText(applicationContext,p1.message,Toast.LENGTH_LONG).show()
//
//                    }
//
//                })
        }

    }

    private fun loginUser() {
        progressDialog.setMessage("Iniciando sesión...")
        progressDialog.show()

        firebaseAuth.signInWithEmailAndPassword(email,password)
            .addOnSuccessListener {
                checkUser()
            }
            .addOnFailureListener { e->
                progressDialog.dismiss()
                Toast.makeText(this,"Fallido al iniciar sesion debido a ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun checkUser(){
        progressDialog.setMessage("Comprobando usuario...")

        val firebaseUser = firebaseAuth.currentUser!!

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{

                override fun onDataChange(snapshot: DataSnapshot) {
                    progressDialog.dismiss()

                    val userType = ""+snapshot.child("userType").value
                    val userState = ""+snapshot.child("state").value

                    if(userType == "user" && userState == "true"){
                        startActivity(Intent(this@LoginActivity, DashboardClientActivity::class.java))
                        finish()
                    }
                    else if(userType == "admin" && userState == "true"){
                        startActivity(Intent(this@LoginActivity, DashboardAdminActivity::class.java))
                        finish()
                    }
                    else if(userState == "false"){
                        firebaseAuth.signOut()
                        Toast.makeText(this@LoginActivity,"El usuario esta inhabilitado, \n" +
                                "Solicite ayuda a un administrador",Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, LoginActivity::class.java))
                        finish()
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    progressDialog.dismiss()
                }
            })
    }
}