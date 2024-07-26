package com.jeantituana2024.tesis.auth

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.databinding.ActivityChangePasswordBinding
import com.jeantituana2024.tesis.storage.TokenPreferences

class ChangePasswordActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChangePasswordBinding
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var progressDialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChangePasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere Porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        tokenPreferences = TokenPreferences(this)

        // Configurar el Toolbar desde el binding
        setSupportActionBar(binding.toolbar)

        // Establecer el título del Toolbar
        supportActionBar?.title = "Cambiar Contraseña"

        // Habilitar el botón de regreso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        // Configurar el comportamiento del botón de regreso
        binding.toolbar.setNavigationOnClickListener {
            onBackPressed()
        }

        binding.changeBtn.setOnClickListener {
            validateData()
        }
    }


    private var password=""
    private var newpassword = ""


    private fun validateData() {

        password = binding.passwordEt.text.toString().trim()
        newpassword = binding.passwordNewEt.text.toString().trim()

        changePassword()
    }

    private fun changePassword() {

        progressDialog.setMessage("Cambiando contraseña...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        progressDialog.dismiss()
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}