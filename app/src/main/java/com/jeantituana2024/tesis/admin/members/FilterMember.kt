package com.jeantituana2024.tesis.admin.members

import android.widget.Filter
import com.jeantituana2024.tesis.models.Member

class FilterMember: Filter {

    private var filterList: ArrayList<Member>

    private var adapterMember: AdapterMember

    constructor(filterList: ArrayList<Member>, adapterMember: AdapterMember): super(){
        this.filterList = filterList
        this.adapterMember = adapterMember
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results = FilterResults()

        if(!constraint.isNullOrEmpty()){

            constraint = constraint.toString().uppercase()

            val filteredModels: ArrayList<Member> = ArrayList()

            for(i in 0 until filterList.size){
                if(filterList[i].user.name.uppercase().contains(constraint) ||
                    filterList[i].user.lastname.uppercase().contains(constraint)){
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
        adapterMember.memberArrayList = results?.values as ArrayList<Member>
        adapterMember.notifyDataSetChanged()
    }


}