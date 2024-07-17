package com.jeantituana2024.tesis.attendance

import android.widget.Filter
import com.jeantituana2024.tesis.admin.members.AdapterMember
import com.jeantituana2024.tesis.models.Member

class FilterMemberAttendance: Filter {

    private var filterList: ArrayList<Member>

    private var adapterMemberAttendance: AdapterMemberAttendance

    constructor(filterList: ArrayList<Member>, adapterMemberAttendance: AdapterMemberAttendance): super(){
        this.filterList = filterList
        this.adapterMemberAttendance = adapterMemberAttendance
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results = FilterResults()

        if(!constraint.isNullOrEmpty()){

            constraint = constraint.toString().uppercase()

            val filteredModels: ArrayList<Member> = ArrayList()

            for(i in 0 until filterList.size){
                if(filterList[i].name.uppercase().contains(constraint)
                    ||
                    filterList[i].lastname.uppercase().contains(constraint)){
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
        adapterMemberAttendance.memberArrayList = results?.values as ArrayList<Member>
        adapterMemberAttendance.notifyDataSetChanged()
    }

}