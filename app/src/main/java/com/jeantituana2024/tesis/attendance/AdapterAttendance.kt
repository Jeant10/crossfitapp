package com.jeantituana2024.tesis.attendance

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.RowAttendanceBinding
import com.jeantituana2024.tesis.models.Attendance
import com.jeantituana2024.tesis.models.AttendanceResponse
import com.jeantituana2024.tesis.models.Member
import com.jeantituana2024.tesis.models.Plan
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AdapterAttendance: RecyclerView.Adapter<AdapterAttendance.HolderAttendance>,
    Filterable {

    private val context: Context
    private lateinit var tokenPreferences: TokenPreferences

    var attendanceArrayList: ArrayList<Attendance>
    private var filterList: ArrayList<Attendance>

    private var filter: FilterAttendance?=null

    constructor(context: Context, attendanceArrayList: ArrayList<Attendance>){
        this.context = context
        this.attendanceArrayList = attendanceArrayList
        this.filterList = attendanceArrayList
    }

    private lateinit var binding: RowAttendanceBinding

    inner class HolderAttendance(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvDate: TextView = binding.tvDate
        val deleteBtn: ImageButton = binding.deleteBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAttendance {
        tokenPreferences = TokenPreferences(context)
        binding = RowAttendanceBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAttendance(binding.root)
    }

    override fun getItemCount(): Int {
        return attendanceArrayList.size
    }

    override fun onBindViewHolder(holder: HolderAttendance, position: Int) {
        if(attendanceArrayList.isNotEmpty()){
            // Ordenar la lista por fecha de manera descendente
            attendanceArrayList.sortByDescending { formatDateString(it.date) }
        }

        val model = attendanceArrayList[position]
        val date = model.date
        // Formatear la fecha antes de asignarla al EditText
        val formattedDate = formatDateString(date)
        holder.tvDate.text = formattedDate

        holder.deleteBtn.setOnClickListener {
            showEliminatePlanAlert(model)
        }

    }

    private fun showEliminatePlanAlert(model: Attendance) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Eliminar Asistencia")
            .setMessage("¿Seguro quieres eliminar esta asistencia?")
            .setPositiveButton("Aceptar") { _, _ ->
                deleteAttendance(model)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun deleteAttendance(model: Attendance) {
        val token = tokenPreferences.getToken()
        val memberId = model.memberId.toString()
        val attendanceId = model.id.toString()

        if(token!=null){

            val call = RetrofitClient.instance.deleteAttendance("Bearer $token", memberId, attendanceId)

            call.enqueue(object: Callback<AttendanceResponse>{
                override fun onResponse(
                    p0: Call<AttendanceResponse>,
                    response: Response<AttendanceResponse>
                ) {
                    if (response.isSuccessful) {
                        val attendanceResponse = response.body()

                        attendanceResponse?.let {
                            if(it.success!=null){
                                attendanceArrayList.remove(model)
                                notifyDataSetChanged()
                                showToast("Asistencia eliminada exitosamente")
                            }
                        }
                    }
                    else{
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            handleErrorResponse(it)
                        }
                    }
                }

                override fun onFailure(p0: Call<AttendanceResponse>, t: Throwable) {
                    showToast("Error de red: ${t.message}")

                }

            })
        }else {
            showToast("No se encontró el token de sesión")
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
            filter = FilterAttendance(filterList, this)
        }
        return filter as FilterAttendance
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

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}