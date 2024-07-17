package com.jeantituana2024.tesis.coach

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.databinding.RowAttendanceCoachBinding

import com.jeantituana2024.tesis.models.AttendanceCoach
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AdapterAttendanceCoach: RecyclerView.Adapter<AdapterAttendanceCoach.HolderAttendanceCoach>,
    Filterable {

    private val context: Context

    var attendanceCoachArrayList: ArrayList<AttendanceCoach>
    private var filterList: ArrayList<AttendanceCoach>

    private var filter: FilterAttendanceCoach?=null

    constructor(context: Context, attendanceCoachArrayList: ArrayList<AttendanceCoach>){
        this.context = context
        this.attendanceCoachArrayList = attendanceCoachArrayList
        this.filterList = attendanceCoachArrayList
    }

    private lateinit var binding: RowAttendanceCoachBinding

    inner class HolderAttendanceCoach(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvName: TextView = binding.tvName
        var tvDate: TextView = binding.tvDate
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderAttendanceCoach {
        binding = RowAttendanceCoachBinding.inflate(LayoutInflater.from(context), parent, false)
        return HolderAttendanceCoach(binding.root)
    }

    override fun getItemCount(): Int {
        return attendanceCoachArrayList.size
    }

    override fun onBindViewHolder(holder: HolderAttendanceCoach, position: Int) {
        if(attendanceCoachArrayList.isNotEmpty()){
            // Ordenar la lista por fecha de manera descendente
            attendanceCoachArrayList.sortByDescending { formatDateString(it.date) }
        }

        val model = attendanceCoachArrayList[position]
        val date = model.date
        val name = model.Member.name
        val lastname = model.Member.lastname

        val memberName = context.getString(R.string.member_name, name, lastname)

        // Formatear la fecha antes de asignarla al EditText
        val formattedDate = formatDateString(date)
        holder.tvDate.text = formattedDate
        holder.tvName.text = memberName

    }


    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterAttendanceCoach(filterList, this)
        }
        return filter as FilterAttendanceCoach
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