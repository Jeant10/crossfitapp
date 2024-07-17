package com.jeantituana2024.tesis.client

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.ProfileActivity
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.admin.HomeFragment
import com.jeantituana2024.tesis.admin.members.MembersFragment
import com.jeantituana2024.tesis.admin.plans.PlansFragment
import com.jeantituana2024.tesis.admin.users.UsersFragment
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.client.attendances.AttendancesClientFragment
import com.jeantituana2024.tesis.client.membership.MembershipFragment
import com.jeantituana2024.tesis.client.nextpays.NextPaysFragment
import com.jeantituana2024.tesis.client.payments.PaysClientFragment
import com.jeantituana2024.tesis.client.plans.PlansClientFragment
import com.jeantituana2024.tesis.databinding.ActivityDashboardClientBinding
import com.jeantituana2024.tesis.models.ErrorResponse
import com.jeantituana2024.tesis.models.GenericResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import com.jeantituana2024.tesis.storage.UserPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class DashboardClientActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var userPreferences: UserPreferences
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var progressDialog: ProgressDialog

    private lateinit var toggle: ActionBarDrawerToggle
    private val profileLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Usuario añadido exitosamente, actualizar la lista
            updateDrawerHeader()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard_client)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)

        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        tokenPreferences = TokenPreferences(this)
        userPreferences = UserPreferences(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Habilitar el botón de regreso en el Toolbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.bringToFront()
        navigationView.setNavigationItemSelectedListener(this)

        updateDrawerHeader()

        toggle = ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open_nav,R.string.close_nav)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        if(savedInstanceState==null){
            openFragment(HomeClientFragment())
            navigationView.setCheckedItem(R.id.nav_home)
        }

    }
    private fun updateDrawerHeader() {
        val headerView: View = navigationView.getHeaderView(0)
        val imageView = headerView.findViewById<ShapeableImageView>(R.id.profileTv)
        val nameTextView = headerView.findViewById<TextView>(R.id.nameEt)
        val emailTextView = headerView.findViewById<TextView>(R.id.emailEt)

        val user = userPreferences.getUser()
        user?.let {

            Glide.with(this)
                .load(it.image)
                .placeholder(R.drawable.ic_person_gray) // Placeholder image
                .error(R.drawable.ic_person_gray) // Error image
                .diskCacheStrategy(DiskCacheStrategy.ALL) // Caching strategy
                .into(imageView)

            nameTextView.text = it.name
            emailTextView.text = it.email
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_home -> {
                openFragment(HomeClientFragment())
                supportActionBar?.title = "Inicio"
                // Update bottom navigation to home
                Log.d("Navigation", "Home selected")
            }

            R.id.nav_membership -> {
                openFragment(MembershipFragment())
                supportActionBar?.title = "Membresia"
                Log.d("Navigation", "member selected")
            }
            R.id.nav_attendances -> {
                openFragment(AttendancesClientFragment())
                supportActionBar?.title = "Asistencias"
                Log.d("Navigation", "attendances selected")
            }

            R.id.nav_pays -> {
                openFragment(PaysClientFragment())
                supportActionBar?.title = "Pagos"
                Log.d("Navigation", "pays selected")
            }

            R.id.nav_pays_next -> {
                openFragment(NextPaysFragment())
                supportActionBar?.title = "Proximos Pagos"
                Log.d("Navigation", "pays selected")
            }

            R.id.nav_plans -> {
                openFragment(PlansClientFragment())
                supportActionBar?.title = "Planes"
                Log.d("Navigation", "plans selected")
            }
            R.id.nav_profile -> {
                navigateToProfile()
                Log.d("Navigation", "profile")
            }
            R.id.nav_logout -> {
                logout()
                Log.d("Navigation", "logout selected")
            }
            R.id.nav_about -> {
                showAboutDialog()
                Log.d("Navigation", "About")
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    private fun openFragment(fragment: Fragment){
        val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container,fragment)
        transaction.commit()
    }
    private fun showAboutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Acerca de Crossfit App")
        builder.setMessage(
            "La Crossfit App es una aplicación móvil diseñada " +
                    "para la gestion operativa de usuarios, membresias, planes, asistencias y ganancias," +
                    "con el unico fin de proporcionar una herramienta eficiente " +
                    "y accesible para administrar y analizar datos financieros " +
                    "de un Centro de Crossfit")
        builder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }


    override fun onBackPressed() {
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START)
        }else{
            super.onBackPressed()
        }

    }

    private fun logout(){

        progressDialog.setMessage("Cerrando sesion...")
        progressDialog.show()

        val token = tokenPreferences.getToken()

        if(token!=null){

            val call = RetrofitClient.instance.logout("Bearer $token")
            call.enqueue(object : Callback<GenericResponse> {
                override fun onResponse(call: Call<GenericResponse>, response: Response<GenericResponse>) {

                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        val logoutResponse = response.body()

                        logoutResponse?.let{
                            // Mostrar mensaje de éxito
                            showToast("Sesion cerrada correctamente")

                            // Borrar el token y la información del usuario de las preferencias
                            tokenPreferences.clearToken()
                            userPreferences.clearUserData()

                            navigateToLogin()

                        }

                    }
                    else{

                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            progressDialog.dismiss()
                            handleErrorResponse(it)
                        }

                    }
                }

                override fun onFailure(call: Call<GenericResponse>, t: Throwable) {
                    progressDialog.dismiss()
                    navigateToLogin()
                    showToast("Error de conexión ${t.message}")
                }

            })
        }
        else {
            progressDialog.dismiss()
            showToast("No se encontró el token de sesión")
            navigateToLogin()
        }
    }

    private fun handleErrorResponse(errorBody: String) {
        val gson = Gson()
        val errorResponseType = object : TypeToken<SingleErrorResponse>() {}.type
        val errorResponse: SingleErrorResponse? = gson.fromJson(errorBody, errorResponseType)

        errorResponse?.let {
            when (it.error) {
                "Invalid token" -> {
                    showToast("Sesion Expirada")
                    navigateToLogin()
                }
                else -> {
                    showToast("Error: ${it.error}")
                    navigateToLogin()
                }
            }
        }
    }

    private fun navigateToLogin() {
        val intent = Intent(this@DashboardClientActivity, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun navigateToProfile() {
        val intent = Intent(this@DashboardClientActivity, ProfileActivity::class.java)
        profileLauncher.launch(intent)
    }
    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}