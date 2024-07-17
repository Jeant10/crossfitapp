package com.jeantituana2024.tesis.client.payments

import android.widget.Filter
import com.jeantituana2024.tesis.models.Payment

class FilterPayClient: Filter {

    private var filterList: ArrayList<Payment>

    private var adapterPayClient: AdapterPayClient

    constructor(filterList: ArrayList<Payment>, adapterPayClient: AdapterPayClient): super(){
        this.filterList = filterList
        this.adapterPayClient = adapterPayClient
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results = FilterResults()

        if(!constraint.isNullOrEmpty()){

            constraint = constraint.toString().uppercase()

            val filteredModels: ArrayList<Payment> = ArrayList()

            for(i in 0 until filterList.size){
                if(filterList[i].date.uppercase().contains(constraint) ||
                    filterList[i].Member.plan.name.uppercase().contains(constraint)){
                    filteredModels.add(filterList[i])
                }
            }

            results.count = filteredModels.size
            results.values = filteredModels
        }
        else{
            results.count = filterList.size
            results.values = filterList
        }

        return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
        adapterPayClient.payClientArrayList = results?.values as ArrayList<Payment>
        adapterPayClient.notifyDataSetChanged()
    }

}