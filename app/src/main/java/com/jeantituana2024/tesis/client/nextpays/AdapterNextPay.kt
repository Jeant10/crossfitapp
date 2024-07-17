package com.jeantituana2024.tesis.client.nextpays

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jeantituana2024.tesis.client.payments.AdapterPayClient
import com.jeantituana2024.tesis.client.payments.FilterPayClient
import com.jeantituana2024.tesis.databinding.RowPayClientBinding
import com.jeantituana2024.tesis.databinding.RowPayNextBinding
import com.jeantituana2024.tesis.models.PayInfo
import com.jeantituana2024.tesis.models.Payment
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class AdapterNextPay: RecyclerView.Adapter<AdapterNextPay.HolderPayNext>, Filterable {

    private val context: Context

    var payNextArrayList: ArrayList<PayInfo>
    private var filterList: ArrayList<PayInfo>
    private var filter: FilterNextPay?=null

    constructor(context: Context, payNextArrayList: ArrayList<PayInfo>){
        this.context = context
        this.payNextArrayList = payNextArrayList
        this.filterList = payNextArrayList
    }

    private lateinit var binding: RowPayNextBinding

    inner class HolderPayNext(itemView: View): RecyclerView.ViewHolder(itemView){

        var tvDate: TextView = binding.tvDate
        var tvPlan: TextView = binding.tvPlan

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdapterNextPay.HolderPayNext {
        binding = RowPayNextBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderPayNext(binding.root)

    }

    override fun getItemCount(): Int {
        return payNextArrayList.size
    }

    override fun onBindViewHolder(holder: AdapterNextPay.HolderPayNext, position: Int) {
        if (payNextArrayList.isNotEmpty()) {
            // Ordenar la lista por fecha (de más reciente a más antigua)
            payNextArrayList.sortByDescending { formatDateString(it.next_payment_date) }
        }

        val model = payNextArrayList[position]
        val id = model.id.toString()
        val date = model.next_payment_date
        val plan = model.plan_name

        // Formatear la fecha antes de asignarla al EditText
        val formattedDate = formatDateString(date)
        holder.tvDate.text = formattedDate
        holder.tvPlan.text = plan

    }


    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterNextPay(filterList, this)
        }
        return filter as FilterNextPay
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