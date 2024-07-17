package com.jeantituana2024.tesis.admin.plans

import android.widget.Filter
import com.jeantituana2024.tesis.models.Plan

class FilterPlan: Filter{

    private var filterList: ArrayList<Plan>

    private var adapterPlan: AdapterPlan

    constructor(filterList: ArrayList<Plan>, adapterPlan: AdapterPlan):super(){
        this.filterList = filterList
        this.adapterPlan = adapterPlan
    }
    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results = FilterResults()

        if(!constraint.isNullOrEmpty()){

            constraint = constraint.toString().uppercase()
            val filteredModels:ArrayList<Plan> = ArrayList()

            for (i in 0 until filterList.size){
                if(filterList[i].name.uppercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }

            results.count = filteredModels.size
            results.values = filteredModels
        }else{
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapterPlan.planArrayList = results?.values as ArrayList<Plan>

        adapterPlan.notifyDataSetChanged()
    }
}