package com.jeantituana2024.tesis.client.membership

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jeantituana2024.tesis.R
import com.jeantituana2024.tesis.api.RetrofitClient
import com.jeantituana2024.tesis.auth.LoginActivity
import com.jeantituana2024.tesis.databinding.FragmentMembershipBinding
import com.jeantituana2024.tesis.models.MemberResponse
import com.jeantituana2024.tesis.models.SingleErrorResponse
import com.jeantituana2024.tesis.storage.TokenPreferences
import com.jeantituana2024.tesis.storage.UserPreferences
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MembershipFragment : Fragment() {

    private lateinit var binding: FragmentMembershipBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var tokenPreferences: TokenPreferences
    // Mantén una referencia a la llamada Retrofit
    private var retrofitCall: Call<MemberResponse>? = null
    private var memberId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMembershipBinding.inflate(inflater,container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        tokenPreferences = TokenPreferences(requireContext())

        setupUI()
    }

    private fun setupUI() {

        loadMember()

    }


    private fun loadMember() {

        val token = tokenPreferences.getToken()

        if (token != null) {

            // Mostrar el ProgressBar antes de iniciar la solicitud
            binding.progressBar.visibility = View.VISIBLE

            retrofitCall = RetrofitClient.instance.getMemberUser("Bearer $token")

            retrofitCall?.enqueue(object: Callback<MemberResponse> {
                override fun onResponse(p0: Call<MemberResponse>, response: Response<MemberResponse>) {
                    // Verifica si el Fragment está adjunto antes de proceder
                    if (!isAdded) {
                        return // Salir si el Fragment no está adjunto
                    }

                    // Ocultar el ProgressBar después de recibir la respuesta
                    binding.progressBar.visibility = View.GONE

                    if (response.isSuccessful) {

                        val memberResponse = response.body()?.member

                        if (memberResponse != null) {

                            binding.tvName.text = getString(R.string.member_name, memberResponse.user.name, memberResponse.user.lastname)
                            binding.tvIdentification.text = memberResponse.user.identification
                            binding.tvEmail.text = memberResponse.email
                            val formattedBornDate = formatDateString(memberResponse.user.bornDate)
                            binding.tvBornDate.text = formattedBornDate
                            val formattedDate = formatDateString(memberResponse.inscriptionDate)
                            binding.tvInscriptionDate.text = formattedDate
                            binding.tvPhone.text = memberResponse.user.phone
                            binding.tvEmergencyPhone.text = memberResponse.user.emergencyPhone
                            binding.tvDirection.text = memberResponse.user.direction
                            binding.tvGender.text = memberResponse.user.gender
                            binding.tvNationality.text = memberResponse.user.nacionality

                        }
                        else {

                            binding.membership.visibility = View.GONE
                            binding.emptyView.text = getString(R.string.membership_no_membership)
                            binding.emptyView.visibility = View.VISIBLE
                        }
                    }
                    else{
                        val errorResponse = response.errorBody()?.string()
                        errorResponse?.let {
                            handleErrorResponse(it)
                        }
                    }
                }

                override fun onFailure(p0: Call<MemberResponse>, p1: Throwable) {
                    if (!isAdded) {
                        return // Salir si el Fragment no está adjunto
                    }

                    binding.progressBar.visibility = View.GONE

                    showToast("Error de conexión: ${p1.message}")
                }

            })
        }
        else {
            if (isAdded) {
                showToast("No se encontró el token de sesión")
            }
        }
    }

    private fun handleErrorResponse(errorBody: String) {

        // Ocultar el ProgressBar después de recibir la respuesta
        binding.progressBar.visibility = View.GONE

        val gson = Gson()
        val errorResponseType = object : TypeToken<SingleErrorResponse>() {}.type
        val errorResponse: SingleErrorResponse? = gson.fromJson(errorBody, errorResponseType)

        errorResponse?.let {
            when (it.error) {
                "No membership found for this user" ->{
                    // Mostrar emptyView si no hay membresía
                    binding.membership.visibility = View.GONE
                    binding.emptyView.text = getString(R.string.membership_no_membership)
                    binding.emptyView.visibility = View.VISIBLE
                }
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

    private fun formatDateString(dateString: String): String {
        return try {
            // Define el formato original de la fecha
            val originalFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            originalFormat.timeZone = TimeZone.getTimeZone("UTC") // Asegurar que sea interpretado como UTC

            // Parsear la fecha en el formato original
            val date = originalFormat.parse(dateString)

            // Define el nuevo formato de la fecha
            val desiredFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            desiredFormat.timeZone = TimeZone.getTimeZone("UTC") // Mantener la salida en UTC
            // Formatear la fecha al nuevo formato y devolverla
            desiredFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            dateString // En caso de error, devolver el string original
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