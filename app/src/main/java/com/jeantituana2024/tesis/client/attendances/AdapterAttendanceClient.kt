package com.jeantituana2024.tesis.client.attendances

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
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.attendance.FilterAttendance
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.RowAttendanceBinding
import com.jeantituana2024.tesis.databinding.RowAttendanceClientBinding
import com.jeantituana2024.tesis.models.Attendance
import com.jeantituana2024.tesis.models.AttendanceResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AdapterAttendanceClient: RecyclerView.Adapter<AdapterAttendanceClient.HolderAttendanceClient>,
    Filterable {

    private val context: Context

    var attendanceClientArrayList: ArrayList<Attendance>
    private var filterList: ArrayList<Attendance>

    private var filter: FilterAttendanceClient?=null

    constructor(context: Context, attendanceClientArrayList: ArrayList<Attendance>){
        this.context = context
        this.attendanceClientArrayList = attendanceClientArrayList
        this.filterList = attendanceClientArrayList
    }

    private lateinit var binding: RowAttendanceClientBinding

    inner class HolderAttendanceClient(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvDate: TextView = binding.tvDate
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAttendanceClient {
        binding = RowAttendanceClientBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAttendanceClient(binding.root)
    }

    override fun getItemCount(): Int {
        return attendanceClientArrayList.size
    }

    override fun onBindViewHolder(holder: HolderAttendanceClient, position: Int) {
        if(attendanceClientArrayList.isNotEmpty()){
            // Ordenar la lista por fecha de manera descendente
            attendanceClientArrayList.sortByDescending { formatDateString(it.date) }
        }

        val model = attendanceClientArrayList[position]
        val date = model.date
        // Formatear la fecha antes de asignarla al EditText
        val formattedDate = formatDateString(date)
        holder.tvDate.text = formattedDate

    }


    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterAttendanceClient(filterList, this)
        }
        return filter as FilterAttendanceClient
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

}