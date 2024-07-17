package com.jeantituana2024.tesis.client.plans

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jeantituana2024.tesis.databinding.RowPlanClientBinding
import com.jeantituana2024.tesis.models.Plan


class AdapterPlanClient: RecyclerView.Adapter<AdapterPlanClient.HolderPlanClient>, Filterable {

    private val context: Context
    //lists
    var planClientArrayList: ArrayList<Plan>
    private var filterList: ArrayList<Plan>

    //filter
    private var filter: FilterPlanClient?=null

    constructor(context: Context, planClientArrayList: ArrayList<Plan>){
        this.context = context
        this.planClientArrayList = planClientArrayList
        this.filterList = planClientArrayList
    }

    private lateinit var binding: RowPlanClientBinding

    inner class HolderPlanClient(itemView: View): RecyclerView.ViewHolder(itemView){
        var nameTv: TextView = binding.nameTv
        var descriptionTv: TextView = binding.descriptionTv
        val priceTv: TextView = binding.priceTv
        val durationTv: TextView = binding.durationTv
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPlanClient {
        binding = RowPlanClientBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderPlanClient(binding.root)
    }

    override fun getItemCount(): Int {
        return planClientArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPlanClient, position: Int) {
        if (planClientArrayList.isNotEmpty()){
            // Ordenar la lista de planes según el criterio deseado
            planClientArrayList.sortWith(compareBy { it.name })
        }

        val model = planClientArrayList[position]
        val id = model.id.toString()
        val name = model.name
        val description = model.description
        val duration = model.duration
        val price = model.price

        holder.nameTv.text = name
        holder.descriptionTv.text = description
        holder.priceTv.text = price
        holder.durationTv.text = duration.toString()

    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPlanClient(filterList,this)
        }
        return filter as FilterPlanClient
    }

}