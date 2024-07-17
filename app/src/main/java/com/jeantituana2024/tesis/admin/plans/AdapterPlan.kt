package com.jeantituana2024.tesis.admin.plans

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.admin.users.AdapterUser
import com.jeantituana2024.tesis.admin.users.FilterUser
import com.jeantituana2024.tesis.admin.users.UserEditActivity
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.RowPlanBinding
import com.jeantituana2024.tesis.models.EditUserResponse
import com.jeantituana2024.tesis.models.Plan
import com.jeantituana2024.tesis.models.PlanResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.models.UserModel
import com.jeantituana2024.tesis.storage.TokenPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Collections

class AdapterPlan: RecyclerView.Adapter<AdapterPlan.HolderPlan>, Filterable{

    private val context: Context
    //lists
    var planArrayList: ArrayList<Plan>
    private var filterList: ArrayList<Plan>
    private lateinit var tokenPreferences: TokenPreferences
    private var editPlanLauncher: ActivityResultLauncher<Intent>? = null

    //filter
    private var filter: FilterPlan?=null

    constructor(context: Context, planArrayList: ArrayList<Plan>, editPlanLauncher: ActivityResultLauncher<Intent>){
        this.context = context
        this.planArrayList = planArrayList
        this.filterList = planArrayList
        this.editPlanLauncher = editPlanLauncher
    }

    private lateinit var binding:RowPlanBinding

    inner class HolderPlan(itemView:View): RecyclerView.ViewHolder(itemView){
        var nameTv: TextView = binding.nameTv
        var descriptionTv: TextView = binding.descriptionTv
        val priceTv: TextView = binding.priceTv
        val durationTv: TextView = binding.durationTv
        val deleteBtn: ImageButton = binding.deleteBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderPlan {
        tokenPreferences = TokenPreferences(context)
        binding = RowPlanBinding.inflate(LayoutInflater.from(context),parent,false)
        return HolderPlan(binding.root)
    }

    override fun getItemCount(): Int {
        return planArrayList.size
    }

    override fun onBindViewHolder(holder: HolderPlan, position: Int) {

        if (planArrayList.isNotEmpty()){
            // Ordenar la lista de planes según el criterio deseado
            planArrayList.sortWith(compareBy { it.name })
        }

        val model = planArrayList[position]
        val id = model.id.toString()
        val name = model.name
        val description = model.description
        val duration = model.duration
        val price = model.price

        holder.nameTv.text = name
        holder.descriptionTv.text = description
        holder.priceTv.text = price
        holder.durationTv.text = duration.toString()

        holder.deleteBtn.setOnClickListener {
            showEliminatePlanAlert(model)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, PlanEditActivity::class.java)
            intent.putExtra("planId",id)
            editPlanLauncher?.launch(intent)
        }
    }

    private fun showEliminatePlanAlert(model: Plan) {
        val alertDialog = AlertDialog.Builder(context)
            .setTitle("Eliminar Plan")
            .setMessage("¿Seguro quieres eliminar este plan?")
            .setPositiveButton("Aceptar") { _, _ ->
                deletePlan(model)
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        alertDialog.show()
    }

    private fun deletePlan(model: Plan) {

        val token = tokenPreferences.getToken()
        val planId = model.id.toString()

        if(token!=null){
            val call = RetrofitClient.instance.deletePlan("Bearer $token", planId)

            call.enqueue(object: Callback<PlanResponse> {
                override fun onResponse(call: Call<PlanResponse>, response: Response<PlanResponse>) {
                    if (response.isSuccessful) {
                        val userResponse = response.body()

                        userResponse?.let {
                            if (it.success != null) {
                                planArrayList.remove(model)
                                notifyDataSetChanged()
                                showToast("Plan eliminado exitosamente")
                            }
                        }

                    } else {
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            handleErrorResponse(it)
                        }
                    }
                }

                override fun onFailure(call: Call<PlanResponse>, t: Throwable) {
                    showToast("Error de red: ${t.message}")
                }
            })
        }else {
            showToast("No se encontró el token de sesión")
        }

    }

    private fun handleErrorResponse(errorBody: String) {
        val gson = Gson()
        val errorResponseType = object : TypeToken<SingleErrorResponse>() {}.type
        val errorResponse: SingleErrorResponse? = gson.fromJson(errorBody, errorResponseType)

        errorResponse?.let {
            when (it.error) {
                "Invalid token" -> {
                    showSessionExpiredAlert()
                }
                else -> {
                    showToast("Error: ${it.error}")
                }
            }
        }
    }

    // Función para mostrar una alerta de sesión expirada y redirigir al LoginActivity
    private fun showSessionExpiredAlert() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Sesión Expirada")
        builder.setMessage("Vuelve a iniciar sesión")
        builder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss() // Cerrar el diálogo
            // Redirigir a LoginActivity
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
        builder.setCancelable(false) // Prevenir el cierre del diálogo usando el botón de atrás
        builder.show()
    }

    override fun getFilter(): Filter {
        if(filter == null){
            filter = FilterPlan(filterList,this)
        }
        return filter as FilterPlan
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

}