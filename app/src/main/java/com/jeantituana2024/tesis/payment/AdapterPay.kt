package com.jeantituana2024.tesis.payment

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.util.Log
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
import com.jeantituana2024.tesis.admin.members.AdapterMember
import com.jeantituana2024.tesis.admin.members.FilterMember
import com.jeantituana2024.tesis.admin.members.MemberEditActivity
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.RowPayBinding
import com.jeantituana2024.tesis.models.DeletePaymentResponse
import com.jeantituana2024.tesis.models.Member
import com.jeantituana2024.tesis.models.Payment
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AdapterPay: RecyclerView.Adapter<AdapterPay.HolderPay>, Filterable {

    private val context: Context
    private lateinit var tokenPreferences: TokenPreferences

    var payArrayList: ArrayList<Payment>
    private var filterList: ArrayList<Payment>
    private var editPayLauncher: ActivityResultLauncher<Intent>? = null
    private lateinit var progressDialog: ProgressDialog
    private var filter: FilterPay?=null

    constructor(context: Context, payArrayList: ArrayList<Payment>, editPayLauncher: ActivityResultLauncher<Intent>){
        this.context = context
        this.payArrayList = payArrayList
        this.filterList = payArrayList
        this.editPayLauncher = editPayLauncher
    }

    private lateinit var binding: RowPayBinding

    inner class HolderPay(itemView: View): RecyclerView.ViewHolder(itemView){

        var tvName: TextView = binding.tvName
        var tvDate: TextView = binding.tvDate
        var tvPlan: TextView = binding.tvPlan
        var tvPay: TextView = binding.tvPay
        val switch: Switch = binding.switchStd

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPay {
        tokenPreferences = TokenPreferences(context)
        progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Espere porfavor")
        progressDialog.setCanceledOnTouchOutside(false)
        binding = RowPayBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderPay(binding.root)

    }

    override fun getItemCount(): Int {
        return payArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPay, position: Int) {

        if (payArrayList.isNotEmpty()) {
            // Ordenar la lista por fecha (de más reciente a más antigua)
            payArrayList.sortByDescending { formatDateString(it.date) }
        }

        val model = payArrayList[position]
        val id = model.id.toString()
        val memberId = model.memberId.toString()
        val name = model.Member.name
        val lastname = model.Member.lastname
        val date = model.date
        val plan = model.Member.plan.name
        val pay = model.payment_type
        val status = model.status

        val memberName = context.getString(R.string.member_name, name, lastname)
        holder.tvName.text = memberName
        // Formatear la fecha antes de asignarla al EditText
        val formattedDate = formatDateString(date)
        holder.tvDate.text = formattedDate
        holder.tvPlan.text = plan
        holder.tvPay.text = pay

        // Configurar el Switch según el estado del usuario
        holder.switch.apply {
            setOnCheckedChangeListener(null) // Elimina cualquier listener anterior
            isChecked = model.status // Actualiza el estado del Switch
            setOnCheckedChangeListener { _, isChecked ->
                // Actualiza el estado del usuario al cambiar el Switch
                changeStatePay(model, holder.switch)
            }
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PayEditActivity::class.java)
            intent.putExtra("memberId",memberId)
            intent.putExtra("payId",id)
            editPayLauncher?.launch(intent)
        }
    }

    private fun changeStatePay(model: Payment,switch: Switch) {

        progressDialog.setMessage("Cambiando estado...")
        progressDialog.show()

        val token = tokenPreferences.getToken()
        val payId = model.id.toString()
        val memberId = model.memberId.toString()

        if(token!=null){

            val call = RetrofitClient.instance.deletePayment("Bearer $token", memberId, payId)

            call.enqueue(object : Callback<DeletePaymentResponse>{
                override fun onResponse(
                    p0: Call<DeletePaymentResponse>,
                    response: Response<DeletePaymentResponse>
                ) {
                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        val payResponse = response.body()

                        payResponse?.let { res ->
                            val message = res.success ?: "Unknown response"

                            // Actualiza el estado del modelo basado en la respuesta
                            when (message) {
                                "Pay activated successfully" -> {
                                    showToast("Pago Activado")
                                    model.status = true
                                    switch.isChecked = true
                                }
                                "Pay desactivated successfully" -> {
                                    showToast("Pago Desactivado")
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

                override fun onFailure(p0: Call<DeletePaymentResponse>, t: Throwable) {
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

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPay(filterList, this)
        }
        return filter as FilterPay
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

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}