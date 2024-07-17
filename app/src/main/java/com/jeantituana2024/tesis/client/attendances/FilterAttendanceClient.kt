package com.jeantituana2024.tesis.client.attendances

import android.widget.Filter
import com.jeantituana2024.tesis.models.Attendance

class FilterAttendanceClient: Filter {

    private var filterList: ArrayList<Attendance>

    private var adapterAttendanceClient: AdapterAttendanceClient

    constructor(filterList: ArrayList<Attendance>, adapterAttendanceClient: AdapterAttendanceClient): super(){
        this.filterList = filterList
        this.adapterAttendanceClient = adapterAttendanceClient
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
        adapterAttendanceClient.attendanceClientArrayList = results?.values as ArrayList<Attendance>
        adapterAttendanceClient.notifyDataSetChanged()
    }
}