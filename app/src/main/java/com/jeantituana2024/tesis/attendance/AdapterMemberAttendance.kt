package com.jeantituana2024.tesis.attendance

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Switch
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.admin.members.AdapterMember
import com.jeantituana2024.tesis.admin.members.FilterMember
import com.jeantituana2024.tesis.admin.members.MemberEditActivity
import com.jeantituana2024.tesis.databinding.RowAttendanceMemberBinding
import com.jeantituana2024.tesis.databinding.RowMemberBinding
import com.jeantituana2024.tesis.models.Member
import com.jeantituana2024.tesis.storage.TokenPreferences

class AdapterMemberAttendance: RecyclerView.Adapter<AdapterMemberAttendance.HolderMemberAttendance>, Filterable {

    private val context: Context
    private lateinit var tokenPreferences: TokenPreferences

    var memberArrayList: ArrayList<Member>
    private var filterList: ArrayList<Member>
    private var memberAttendancesLauncher: ActivityResultLauncher<Intent>? = null

    private var filter: FilterMemberAttendance?=null

    constructor(context: Context, memberArrayList: ArrayList<Member>, memberAttendancesLauncher: ActivityResultLauncher<Intent>){
        this.context = context
        this.memberArrayList = memberArrayList
        this.filterList = memberArrayList
        this.memberAttendancesLauncher = memberAttendancesLauncher
    }

    private lateinit var binding: RowAttendanceMemberBinding

    inner class HolderMemberAttendance(itemView: View): RecyclerView.ViewHolder(itemView){
        var tvName: TextView = binding.tvName
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderMemberAttendance {
        tokenPreferences = TokenPreferences(context)
        binding = RowAttendanceMemberBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderMemberAttendance(binding.root)
    }

    override fun onBindViewHolder(holder: HolderMemberAttendance, position: Int) {

        if(memberArrayList.isNotEmpty()){
            // Ordenar la lista por nombre
            memberArrayList.sortBy { it.name }
        }

        val model = memberArrayList[position]
        val id = model.id.toString()
        val name = model.name
        val lastname = model.lastname

        val memberName = context.getString(R.string.member_name, name, lastname)
        holder.tvName.text = memberName

        holder.itemView.setOnClickListener {
            val intent = Intent(context, MemberAttendancesActivity::class.java)
            intent.putExtra("memberId",id)
            memberAttendancesLauncher?.launch(intent)
        }
    }

    override fun getItemCount(): Int {
        return memberArrayList.size
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterMemberAttendance(filterList, this)
        }
        return filter as FilterMemberAttendance
    }

}