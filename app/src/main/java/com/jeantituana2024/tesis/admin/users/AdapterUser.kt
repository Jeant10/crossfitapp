package com.jeantituana2024.tesis.admin.users

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.RowUserBinding
import com.jeantituana2024.tesis.models.EditUserResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.models.UserModel
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class AdapterUser: RecyclerView.Adapter<AdapterUser.HolderUser>, Filterable{

    private val context:Context

    var userArrayList: ArrayList<UserModel>
    private var filterList: ArrayList<UserModel>
    private var editUserLauncher: ActivityResultLauncher<Intent>? = null
    private var filter: FilterUser?=null
    private lateinit var progressDialog: ProgressDialog
    private lateinit var tokenPreferences: TokenPreferences

    constructor(context: Context, userArrayList: ArrayList<UserModel>, editUserLauncher: ActivityResultLauncher<Intent>){
        this.context = context
        this.userArrayList = userArrayList
        this.filterList = userArrayList
        this.editUserLauncher = editUserLauncher
    }

    private lateinit var binding:RowUserBinding

    inner class HolderUser(itemView:View): RecyclerView.ViewHolder(itemView){
        var imageTv: ImageView = binding.imageTv
        var nameTv: TextView = binding.nameTv
        var emailTv: TextView = binding.emailTv
        var typeuserTv: TextView = binding.typeuserTv
        var switch: Switch = binding.switchStd

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderUser {
        tokenPreferences = TokenPreferences(context)
        progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)
        binding = RowUserBinding.inflate(LayoutInflater.from(context), parent,false)
        return HolderUser(binding.root)
    }

    override fun getItemCount(): Int {
        return userArrayList.size
    }

    override fun onBindViewHolder(holder: HolderUser, position: Int) {

        if(userArrayList.isNotEmpty()){
            // Ordenar la lista por nombre
            userArrayList.sortBy { it.name }
        }

        val model = userArrayList[position]
        val id = model.id
        val name = model.name
        val email = model.email
        val role = model.role
        val image = model.image
        val status = model.status

        holder.nameTv.text = name
        holder.emailTv.text = email
        holder.typeuserTv.text = role

        Glide.with(context)
            .load(image)
            .placeholder(R.drawable.ic_person_gray) // Placeholder image
            .error(R.drawable.ic_person_gray) // Error image
            .diskCacheStrategy(DiskCacheStrategy.ALL) // Caching strategy
            .into(holder.imageTv)

        // Configurar el Switch según el estado del usuario
        holder.switch.apply {
            setOnCheckedChangeListener(null) // Elimina cualquier listener anterior
            isChecked = model.status // Actualiza el estado del Switch
            setOnCheckedChangeListener { _, isChecked ->
                // Actualiza el estado del usuario al cambiar el Switch
                changeStateUser(model, holder.switch)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, UserEditActivity::class.java)
            intent.putExtra("userId",id)
            editUserLauncher?.launch(intent)
        }

    }


    private fun changeStateUser(model: UserModel,switch: Switch) {

        progressDialog.setMessage("Cambiando estado...")
        progressDialog.show()

        val token = tokenPreferences.getToken()
        val uid = model.id

        if(token!=null){
            val call = RetrofitClient.instance.deleteUser("Bearer $token", uid)

            call.enqueue(object : Callback<EditUserResponse> {
                override fun onResponse(call: Call<EditUserResponse>, response: Response<EditUserResponse>) {

                    if (response.isSuccessful) {

                        progressDialog.dismiss()

                        val userResponse = response.body()
                        userResponse?.let { res ->
                            val message = res.success ?: "Unknown response"

                            // Actualiza el estado del modelo basado en la respuesta
                            when (message) {
                                "User activated successfully" -> {
                                    showToast("Usuario Activado")
                                    model.status = true
                                    switch.isChecked = true
                                }
                                "User desactivated successfully" -> {
                                    showToast("Usuario Desactivado")
                                    model.status = false
                                    switch.isChecked = false
                                }
                                else -> {
                                    showToast("Unexpected response")
                                    switch.isChecked = model.status // Revertir el estado del Switch
                                }
                            }
                            notifyDataSetChanged() // Notificar los cambios a la UI
                        } ?: run {
                            showToast("Error: La respuesta del servidor es nula")
                            switch.isChecked = model.status // Revertir el estado del Switch
                        }

                    } else {
                        progressDialog.dismiss()
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            handleErrorResponse(it)
                        }
                        switch.isChecked = model.status
                    }
                }

                override fun onFailure(call: Call<EditUserResponse>, t: Throwable) {
                    progressDialog.dismiss()
                    showToast("Error de red: ${t.message}")
                    switch.isChecked = model.status
                }
            })
        }else {
            progressDialog.dismiss()
            showToast("No se encontró el token de sesión")
            switch.isChecked = model.status
        }

    }

    private fun handleErrorResponse(errorBody: String) {
        val gson = Gson()
        val errorResponseType = object : TypeToken<SingleErrorResponse>() {}.type
        val errorResponse: SingleErrorResponse? = gson.fromJson(errorBody, errorResponseType)

        errorResponse?.let {
            when (it.error) {
                "Invalid token" -> {
                    showSessionExpiredAlert()
                }
                else -> {
                    showToast("Error: ${it.error}")
                }
            }
        }
    }

    // Función para mostrar una alerta de sesión expirada y redirigir al LoginActivity
    private fun showSessionExpiredAlert() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Sesión Expirada")
        builder.setMessage("Vuelve a iniciar sesión")
        builder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss() // Cerrar el diálogo
            // Redirigir a LoginActivity
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
        builder.setCancelable(false) // Prevenir el cierre del diálogo usando el botón de atrás
        builder.show()
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterUser(filterList,this)
        }
        return filter as FilterUser
    }

        private fun showToast(message: String) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
        }
}