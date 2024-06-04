package com.jeantituana2024.tesis

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.jeantituana2024.tesis.admin.DashboardAdminActivity
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.client.DashboardClientActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        firebaseAuth = FirebaseAuth.getInstance()

        Handler().postDelayed(Runnable {
            checkUser()
        },2000) // 2 seconds splash
    }

    private fun checkUser() {
        val firebaseUser = firebaseAuth.currentUser
        if(firebaseUser == null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        else{

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseUser.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(snapshot: DataSnapshot) {

                        val userType = ""+snapshot.child("userType").value
                        val userState = ""+snapshot.child("state").value

                        if(userType == "user" && userState == "true"){
                            startActivity(Intent(this@SplashActivity, DashboardClientActivity::class.java))
                            finish()
                        }
                        else if(userType == "admin"&& userState == "true"){
                            startActivity(Intent(this@SplashActivity, DashboardAdminActivity::class.java))
                            finish()
                        }
                        else if(userState == "false"){
                            firebaseAuth.signOut()
                            Toast.makeText(this@SplashActivity,"El usuario esta inhabilitado, \n" +
                                    "Solicite ayuda a un administrador",Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                            finish()
                        }

                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }
}