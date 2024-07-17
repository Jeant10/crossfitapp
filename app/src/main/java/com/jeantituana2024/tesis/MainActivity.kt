package com.jeantituana2024.tesis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.ActivityMainBinding
import com.jeantituana2024.tesis.models.RegisterRequest
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.startBtn.setOnClickListener {
            startActivity(Intent(this,LoginActivity::class.java))
        }

    }
}