package com.jeantituana2024.tesis.payment

import android.widget.Filter
import com.jeantituana2024.tesis.models.Payment

class FilterPay: Filter {

    private var filterList: ArrayList<Payment>

    private var adapterPay: AdapterPay

    constructor(filterList: ArrayList<Payment>, adapterPay: AdapterPay): super(){
        this.filterList = filterList
        this.adapterPay = adapterPay
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results = FilterResults()

        if(!constraint.isNullOrEmpty()){

            constraint = constraint.toString().uppercase()

            val filteredModels: ArrayList<Payment> = ArrayList()

            for(i in 0 until filterList.size){
                if(filterList[i].Member.user.name.uppercase().contains(constraint) ||
                    filterList[i].Member.user.lastname.uppercase().contains(constraint) ||
                    filterList[i].date.uppercase().contains(constraint) ||
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
        adapterPay.payArrayList = results?.values as ArrayList<Payment>
        adapterPay.notifyDataSetChanged()
    }

}