package com.jeantituana2024.tesis.client.plans

import android.widget.Filter
import com.jeantituana2024.tesis.models.Plan

class FilterPlanClient: Filter {

    private var filterList: ArrayList<Plan>

    private var adapterPlanClient: AdapterPlanClient

    constructor(filterList: ArrayList<Plan>, adapterPlanClient: AdapterPlanClient):super(){
        this.filterList = filterList
        this.adapterPlanClient = adapterPlanClient
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
        adapterPlanClient.planClientArrayList = results?.values as ArrayList<Plan>

        adapterPlanClient.notifyDataSetChanged()
    }
}