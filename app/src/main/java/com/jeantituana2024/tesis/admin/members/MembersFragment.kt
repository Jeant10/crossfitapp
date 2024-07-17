package com.jeantituana2024.tesis.admin.members

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.FragmentMembersBinding
import com.jeantituana2024.tesis.models.Member
import com.jeantituana2024.tesis.models.MembersResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import com.jeantituana2024.tesis.storage.UserPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MembersFragment : Fragment() {

    private lateinit var binding: FragmentMembersBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var tokenPreferences: TokenPreferences
    private lateinit var adapterMember: AdapterMember
    private lateinit var memberArrayList: ArrayList<Member>

    // Mantén una referencia a la llamada Retrofit
    private var retrofitCall: Call<MembersResponse>? = null

    // Paso 1: Registrar el ActivityResultLauncher
    private val addMemberLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Usuario añadido exitosamente, actualizar la lista
            loadMembers()
        }
    }

    private val editMemberLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Usuario añadido exitosamente, actualizar la lista
            loadMembers()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMembersBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        tokenPreferences = TokenPreferences(requireContext())

        setupUI()
    }

    private fun setupUI() {

        loadMembers()

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                try {
                    adapterMember.filter.filter(s)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.fabAddMember.setOnClickListener {
            val intent = Intent(requireContext(), AddMemberActivity::class.java)
            addMemberLauncher.launch(intent)
        }

    }

    private fun loadMembers(){
        memberArrayList = ArrayList()
        val token = tokenPreferences.getToken()

        if(token!=null){

            // Mostrar el ProgressBar antes de iniciar la solicitud
            binding.progressBar.visibility = View.VISIBLE

            retrofitCall = RetrofitClient.instance.getMembers("Bearer $token")

            retrofitCall?.enqueue(object: Callback<MembersResponse>{
                override fun onResponse(call: Call<MembersResponse>, response: Response<MembersResponse>) {

                    // Verifica si el Fragment está adjunto antes de proceder
                    if (!isAdded) {
                        return // Salir si el Fragment no está adjunto
                    }

                    // Ocultar el ProgressBar después de recibir la respuesta
                    binding.progressBar.visibility = View.GONE

                    if(response.isSuccessful){

                        memberArrayList.clear()

                        val membersList = response.body()?.members

                        membersList?.let{ members ->
                            memberArrayList.addAll(members)

                        }

                        Log.d("Members",memberArrayList.toString())

                        adapterMember = AdapterMember(requireContext(), memberArrayList, editMemberLauncher)
                        binding.membersRv.adapter = adapterMember

                        // Verifica si la lista está vacía y ajusta la visibilidad del TextView
                        if (memberArrayList.isEmpty()) {
                            binding.emptyView.visibility = View.VISIBLE
                            binding.membersRv.visibility = View.GONE
                        } else {
                            binding.emptyView.visibility = View.GONE
                            binding.membersRv.visibility = View.VISIBLE
                        }

                    }else{
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            handleErrorResponse(it)
                        }
                    }
                }

                override fun onFailure(call: Call<MembersResponse>, t: Throwable) {

                    if (!isAdded) {
                        return // Salir si el Fragment no está adjunto
                    }

                    binding.progressBar.visibility = View.GONE
                    t.printStackTrace()
                    showToast("Error de red: ${t.message}")
                }

            })
        }else {
            if (isAdded) {
                showToast("No se encontró el token de sesión")
            }
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
                    if (isAdded) {
                        showToast("Error: ${it.error}")
                    }
                }
            }
        }
    }

    // Función para mostrar una alerta de sesión expirada y redirigir al LoginActivity
    private fun showSessionExpiredAlert() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Sesión Expirada")
        builder.setMessage("Vuelve a iniciar sesión")
        builder.setPositiveButton("Aceptar") { dialog, _ ->
            dialog.dismiss() // Cerrar el diálogo
            // Redirigir a LoginActivity
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
        builder.setCancelable(false) // Prevenir el cierre del diálogo usando el botón de atrás
        builder.show()
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Cancela la llamada Retrofit si está en progreso
        retrofitCall?.cancel()
    }
}