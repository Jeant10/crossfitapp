package com.jeantituana2024.tesis

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jeantituana2024.tesis.databinding.ActivityProfileBinding

import android.text.format.DateFormat
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.jeantituana2024.tesis.admin.DashboardAdminActivity
import com.jeantituana2024.tesis.storage.UserPreferences
import java.util.Calendar
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val editProfileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            loadUserProfile()
        }
    }

    private lateinit var userPreferences: UserPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)

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
        val user = userPreferences.getUser()

        user?.let {
            binding.nameTv.text = it.name
            binding.emailTv.text = it.email
            binding.idTv.text = it.id
            binding.accountTypeTv.text = it.role

            Glide.with(this)
                .load(it.image)
                .placeholder(R.drawable.ic_person_gray) // Placeholder image
                .error(R.drawable.ic_person_gray) // Error image
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Caching strategy
                .into(binding.profileTv)
        }
    }
}