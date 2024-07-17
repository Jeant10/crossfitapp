package com.jeantituana2024.tesis.coach

import android.widget.Filter
import com.jeantituana2024.tesis.models.AttendanceCoach

class FilterAttendanceCoach: Filter {

    private var filterList: ArrayList<AttendanceCoach>

    private var adapterAttendanceCoach: AdapterAttendanceCoach

    constructor(filterList: ArrayList<AttendanceCoach>, adapterAttendanceCoach: AdapterAttendanceCoach): super(){
        this.filterList = filterList
        this.adapterAttendanceCoach = adapterAttendanceCoach
    }

    override fun performFiltering(constraint: CharSequence?): FilterResults {

        var constraint = constraint
        val results = FilterResults()

        if(!constraint.isNullOrEmpty()){

            constraint = constraint.toString().uppercase()

            val filteredModels: ArrayList<AttendanceCoach> = ArrayList()

            for(i in 0 until filterList.size){
                if(filterList[i].date.uppercase().contains(constraint) ||
                    filterList[i].Member.name.uppercase().contains(constraint) ||
                    filterList[i].Member.lastname.uppercase().contains(constraint)){
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
        adapterAttendanceCoach.attendanceCoachArrayList = results?.values as ArrayList<AttendanceCoach>
        adapterAttendanceCoach.notifyDataSetChanged()
    }

}