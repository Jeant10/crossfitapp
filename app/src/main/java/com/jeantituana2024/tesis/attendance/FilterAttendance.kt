package com.jeantituana2024.tesis.attendance

import android.widget.Filter
import com.jeantituana2024.tesis.models.Attendance
import com.jeantituana2024.tesis.models.Member

class FilterAttendance: Filter {

    private var filterList: ArrayList<Attendance>

    private var adapterAttendance: AdapterAttendance

    constructor(filterList: ArrayList<Attendance>, adapterAttendance: AdapterAttendance): super(){
        this.filterList = filterList
        this.adapterAttendance = adapterAttendance
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results = FilterResults()

        if(!constraint.isNullOrEmpty()){

            constraint = constraint.toString().uppercase()

            val filteredModels: ArrayList<Attendance> = ArrayList()

            for(i in 0 until filterList.size){
                if(filterList[i].date.uppercase().contains(constraint)){
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
        adapterAttendance.attendanceArrayList = results?.values as ArrayList<Attendance>
        adapterAttendance.notifyDataSetChanged()
    }
}