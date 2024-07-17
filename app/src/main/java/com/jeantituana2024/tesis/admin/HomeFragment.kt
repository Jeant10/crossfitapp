package com.jeantituana2024.tesis.admin

import android.animation.ValueAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.databinding.FragmentHomeBinding
import com.jeantituana2024.tesis.storage.TokenPreferences
import com.jeantituana2024.tesis.storage.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var tokenPreferences: TokenPreferences

    private lateinit var attendanceNumber: TextView
    private lateinit var earningsNumber: TextView
    private lateinit var membersActiveNumber: TextView
    private lateinit var membersInactiveNumber: TextView
    private lateinit var membersNumber: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        tokenPreferences = TokenPreferences(requireContext())

        // Obtén la referencia al TextView
        attendanceNumber = binding.attendanceNumber
        earningsNumber = binding.earningsNumber
        membersActiveNumber = binding.membersActiveNumber
        membersInactiveNumber = binding.membersInactiveNumber
        membersNumber = binding.membersNumber

        setupUI()
    }

    private fun setupUI() {

        // Llama a la función para cargar datos de la API y animar el número
        fetchAndAnimateTotalAttendances()
        fetchAndAnimateEarnings()
        fetchAndAnimateActiveMembers()
        fetchAndAnimateInactiveMembers()
        fetchAndAnimateMembers()
    }

    private fun fetchAndAnimateTotalAttendances() {
        // Llamar a la API en un hilo de background

        val token = tokenPreferences.getToken()

        // Llama a la API en un hilo de background usando coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getTotalAttendances("Bearer $token")
                val totalAttendances = response.total_attendance["0"]?.total_attendances ?: 0

                // Cambia a la hebra principal para actualizar la UI
                withContext(Dispatchers.Main) {
                    // Muestra el TextView con la animación
                    animateNumber(attendanceNumber, 0, totalAttendances)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Manejo de errores
            }
        }
    }

    private fun fetchAndAnimateEarnings(){
        // Llamar a la API en un hilo de background

        val token = tokenPreferences.getToken()

        // Llama a la API en un hilo de background usando coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getTotalEarnings("Bearer $token")
                val totalEarnings = response.total_earning["0"]?.total_earnings ?: "0"

                // Cambia a la hebra principal para actualizar la UI
                withContext(Dispatchers.Main) {
                    // Muestra el TextView con la animación
                    animateNumber(earningsNumber, 0, totalEarnings.toInt())
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Manejo de errores
            }
        }

    }

    private fun fetchAndAnimateActiveMembers() {

        val token = tokenPreferences.getToken()

        // Llama a la API en un hilo de background usando coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getActiveMembersNumber("Bearer $token")
                val activeMembers = response.active_membership["0"]?.active_memberships ?: 0

                // Cambia a la hebra principal para actualizar la UI
                withContext(Dispatchers.Main) {
                    // Muestra el TextView con la animación
                    animateNumber(membersActiveNumber, 0, activeMembers)
                }
            } catch (e: Exception) {
                // Maneja los errores apropiadamente
            }
        }
    }

    private fun fetchAndAnimateInactiveMembers() {

        val token = tokenPreferences.getToken()

        // Llama a la API en un hilo de background usando coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.instance.getInactiveMembersNumber("Bearer $token")
                val inactiveMembers = response.inactive_membership["0"]?.inactive_memberships ?: 0

                // Cambia a la hebra principal para actualizar la UI
                withContext(Dispatchers.Main) {
                    // Muestra el TextView con la animación
                    animateNumber(membersInactiveNumber, 0, inactiveMembers)
                }
            } catch (e: Exception) {
                // Maneja los errores apropiadamente
            }
        }
    }
    private fun fetchAndAnimateMembers() {

        val token = tokenPreferences.getToken()

        // Llama a la API en un hilo de background usando coroutines
        CoroutineScope(Dispatchers.IO).launch {
            try {

                val response = RetrofitClient.instance.getTotalMembersNumber("Bearer $token")
                val membersNum = response.total_membership["0"]?.total_memberships ?: 0

                // Cambia a la hebra principal para actualizar la UI
                withContext(Dispatchers.Main) {
                    // Muestra el TextView con la animación
                    animateNumber(membersNumber, 0, membersNum)
                }
            } catch (e: Exception) {
                // Maneja los errores apropiadamente
            }
        }
    }


    private fun animateNumber(textView: TextView, start: Int, end: Int) {
        val animator = ValueAnimator.ofInt(start, end)
        animator.duration = 2000 // Duración de la animación en milisegundos
        animator.addUpdateListener { animation ->
            textView.text = animation.animatedValue.toString()
        }
        animator.start()
    }

}