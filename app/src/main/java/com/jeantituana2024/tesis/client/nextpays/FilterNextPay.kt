package com.jeantituana2024.tesis.client.nextpays

import android.widget.Filter
import com.jeantituana2024.tesis.client.payments.AdapterPayClient
import com.jeantituana2024.tesis.models.PayInfo
import com.jeantituana2024.tesis.models.Payment

class FilterNextPay: Filter {

    private var filterList: ArrayList<PayInfo>

    private var adapterPayNext: AdapterNextPay

    constructor(filterList: ArrayList<PayInfo>, adapterPayNext: AdapterNextPay): super(){
        this.filterList = filterList
        this.adapterPayNext = adapterPayNext
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results = FilterResults()

        if(!constraint.isNullOrEmpty()){

            constraint = constraint.toString().uppercase()

            val filteredModels: ArrayList<PayInfo> = ArrayList()

            for(i in 0 until filterList.size){
                if(filterList[i].next_payment_date.uppercase().contains(constraint)||
                    filterList[i].plan_name.uppercase().contains(constraint)){
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
        adapterPayNext.payNextArrayList = results?.values as ArrayList<PayInfo>
        adapterPayNext.notifyDataSetChanged()
    }

}