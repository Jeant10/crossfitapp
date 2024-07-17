package com.jeantituana2024.tesis.client.payments


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jeantituana2024.tesis.databinding.RowPayClientBinding
import com.jeantituana2024.tesis.models.Payment

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AdapterPayClient: RecyclerView.Adapter<AdapterPayClient.HolderPayClient>, Filterable {

    private val context: Context

    var payClientArrayList: ArrayList<Payment>
    private var filterList: ArrayList<Payment>
    private var filter: FilterPayClient?=null

    constructor(context: Context, payClientArrayList: ArrayList<Payment>){
        this.context = context
        this.payClientArrayList = payClientArrayList
        this.filterList = payClientArrayList
    }

    private lateinit var binding: RowPayClientBinding

    inner class HolderPayClient(itemView: View): RecyclerView.ViewHolder(itemView){

        var tvDate: TextView = binding.tvDate
        var tvPlan: TextView = binding.tvPlan
        var tvPay: TextView = binding.tvPay

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPayClient {
        binding = RowPayClientBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderPayClient(binding.root)

    }

    override fun getItemCount(): Int {
        return payClientArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPayClient, position: Int) {
        if (payClientArrayList.isNotEmpty()) {
            // Ordenar la lista por fecha (de más reciente a más antigua)
            payClientArrayList.sortByDescending { formatDateString(it.date) }
        }

        val model = payClientArrayList[position]
        val id = model.id.toString()
        val date = model.date
        val plan = model.Member.plan.name
        val pay = model.payment_type

        // Formatear la fecha antes de asignarla al EditText
        val formattedDate = formatDateString(date)
        holder.tvDate.text = formattedDate
        holder.tvPlan.text = plan
        holder.tvPay.text = pay

    }


    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPayClient(filterList, this)
        }
        return filter as FilterPayClient
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