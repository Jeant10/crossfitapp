package com.jeantituana2024.tesis.client

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.jeantituana2024.tesis.MainActivity
import com.jeantituana2024.tesis.ProfileActivity
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.databinding.ActivityDashboardClientBinding

class DashboardClientActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardClientBinding

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardClientBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()

        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            checkUser()
        }

        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else{
            val email = firebaseUser.email

            binding.subTitleTv.text = email
        }
    }
}