package com.jeantituana2024.tesis.auth

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.databinding.ActivityForgotPasswordBinding

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityForgotPasswordBinding

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        binding.backBtn.setOnClickListener {
            onBackPressed()
        }
        binding.submitBtn.setOnClickListener{
            validateData()
        }
    }

    private var email=""
    private fun validateData() {

        email = binding.emailEt.text.toString().trim()

        if(email.isEmpty()){
            Toast.makeText(this,"Ingresa tu correo electrónico...!", Toast.LENGTH_SHORT).show()
        }
        else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this,"Correo electrónico no valido...!", Toast.LENGTH_SHORT).show()
        }
        else{
            recoverPassword()
        }
    }

    private fun recoverPassword() {

        progressDialog.setMessage("Envíando instrucciones para la recuperación de la contraseña al ${email}")
        progressDialog.show()

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener {
                progressDialog.dismiss()
                Toast.makeText(this,"Instrucciones para restablecer la contraseña enviadas a \n${email}",Toast.LENGTH_SHORT).show();
            }
            .addOnFailureListener {e->
                progressDialog.dismiss()
                Toast.makeText(this,"No se pudo enviar debido a ${e.message}",Toast.LENGTH_SHORT).show();
            }
    }
}