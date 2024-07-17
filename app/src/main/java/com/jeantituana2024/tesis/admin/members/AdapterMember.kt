package com.jeantituana2024.tesis.admin.members

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.admin.plans.AdapterPlan
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.RowMemberBinding
import com.jeantituana2024.tesis.models.Member
import com.jeantituana2024.tesis.models.MemberResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AdapterMember: RecyclerView.Adapter<AdapterMember.HolderMember>, Filterable{

    private val context: Context
    private lateinit var tokenPreferences: TokenPreferences

    var memberArrayList: ArrayList<Member>
    private var filterList: ArrayList<Member>
    private var editMemberLauncher: ActivityResultLauncher<Intent>? = null
    private lateinit var progressDialog: ProgressDialog
    private var filter: FilterMember?=null

    constructor(context: Context, memberArrayList: ArrayList<Member>, editMemberLauncher: ActivityResultLauncher<Intent>){
        this.context = context
        this.memberArrayList = memberArrayList
        this.filterList = memberArrayList
        this.editMemberLauncher = editMemberLauncher
    }

    private lateinit var binding: RowMemberBinding

    inner class HolderMember(itemView: View): RecyclerView.ViewHolder(itemView){

        var tvName: TextView = binding.tvName
        var tvEmail: TextView = binding.tvEmail
        var tvincriptionDate: TextView = binding.tvinscriptionDate
        val tvGender: TextView = binding.tvGender
        var tvPhone: TextView = binding.tvPhone
        var tvPlan: TextView = binding.tvPlan
        val switch: Switch = binding.switchStd
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderMember {
        tokenPreferences = TokenPreferences(context)
        progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)
        binding = RowMemberBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderMember(binding.root)
    }

    override fun getItemCount(): Int {
        return memberArrayList.size
    }

    override fun onBindViewHolder(holder: HolderMember, position: Int) {

        if(memberArrayList.isNotEmpty()){
            // Ordenar la lista por nombre
            memberArrayList.sortBy { it.name }
        }

        val model = memberArrayList[position]
        val id = model.id.toString()
        val name = model.name
        val lastname = model.lastname
        val email = model.email
        val inscriptionDate = model.inscriptionDate
        val gender = model.gender
        val phone = model.phone
        val plan = model.plan.name
        val status = model.status

        val memberName = context.getString(R.string.member_name, name, lastname)
        holder.tvName.text = memberName
        holder.tvEmail.text = email
        // Formatear la fecha antes de asignarla al EditText
        val formattedDate = formatDateString(inscriptionDate)
        holder.tvincriptionDate.text = formattedDate
        holder.tvGender.text = gender
        holder.tvPhone.text = phone
        holder.tvPlan.text = plan

        // Configurar el Switch según el estado del usuario
        holder.switch.apply {
            setOnCheckedChangeListener(null) // Elimina cualquier listener anterior
            isChecked = model.status // Actualiza el estado del Switch
            setOnCheckedChangeListener { _, isChecked ->
                // Actualiza el estado del usuario al cambiar el Switch
                changeStateMember(model, holder.switch)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MemberEditActivity::class.java)
            intent.putExtra("memberId",id)
            editMemberLauncher?.launch(intent)
        }

    }


    private fun changeStateMember(model: Member,switch: Switch) {
        progressDialog.setMessage("Cambiando estado...")
        progressDialog.show()

        val token = tokenPreferences.getToken()
        val memberUid = model.id.toString()

        if(token!=null){

            val call = RetrofitClient.instance.deleteMember("Bearer $token", memberUid)

            call.enqueue(object : Callback<MemberResponse>{
                override fun onResponse(p0: Call<MemberResponse>, response: Response<MemberResponse>) {
                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        val memberResponse = response.body()

                        memberResponse?.let { res ->

                            val message = res.success ?: "Unknown response"

                            // Actualiza el estado del modelo basado en la respuesta
                            when (message) {
                                "Member activated successfully" -> {
                                    showToast("Miembro Activado")
                                    model.status = true
                                    switch.isChecked = true
                                }
                                "Member desactivated successfully" -> {
                                    showToast("Miembro Desactivado")
                                    model.status = false
                                    switch.isChecked = false
                                }
                                else -> {
                                    showToast("Inesperado Error")
                                    switch.isChecked = model.status // Revertir el estado del Switch
                                }
                            }
                            notifyDataSetChanged() // Notificar los cambios a la UI
                        } ?: run {
                            // Manejo del caso en que userResponse sea nulo
                            showToast("Error: La respuesta del servidor es nula")
                            switch.isChecked = model.status
                        }

                    }
                    else {
                        progressDialog.dismiss()
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            handleErrorResponse(it)
                        }
                        switch.isChecked = model.status
                    }
                }

                override fun onFailure(p0: Call<MemberResponse>, t: Throwable) {
                    progressDialog.dismiss()
                    showToast("Error de red: ${t.message}")
                    switch.isChecked = model.status
                }

            })
        }
        else {
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

    private fun formatDateString(dateString: String): String {
        return try {
            // Define el formato original de la fecha
            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            originalFormat.timeZone = TimeZone.getTimeZone("UTC") // Asegurar que sea interpretado como UTC

            // Parsear la fecha en el formato original
            val date = originalFormat.parse(dateString)

            // Define el nuevo formato de la fecha
            val desiredFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            desiredFormat.timeZone = TimeZone.getTimeZone("UTC") // Mantener la salida en UTC
            // Formatear la fecha al nuevo formato y devolverla
            desiredFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            dateString // En caso de error, devolver el string original
        }
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterMember(filterList, this)
        }
        return filter as FilterMember
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

}