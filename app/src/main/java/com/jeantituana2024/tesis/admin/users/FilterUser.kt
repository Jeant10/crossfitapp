package com.jeantituana2024.tesis.admin.users

import android.widget.Filter
import com.jeantituana2024.tesis.models.UserModel

class FilterUser: Filter {

    private var filterList: ArrayList<UserModel>

    private var adapterUser: AdapterUser

    constructor(filterList: ArrayList<UserModel>, adapterUser: AdapterUser):super(){
        this.filterList = filterList
        this.adapterUser = adapterUser
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results = FilterResults()

        if(!constraint.isNullOrEmpty()){

            constraint = constraint.toString().uppercase()
            val filteredModels:ArrayList<UserModel> = ArrayList()

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

        adapterUser.userArrayList = results?.values as ArrayList<UserModel>

        adapterUser.notifyDataSetChanged()
    }
}