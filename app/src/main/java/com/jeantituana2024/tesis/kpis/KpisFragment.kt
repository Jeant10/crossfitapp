package com.jeantituana2024.tesis.kpis

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.jeantituana2024.tesis.databinding.FragmentKpisBinding
import com.jeantituana2024.tesis.storage.TokenPreferences
import com.jeantituana2024.tesis.storage.UserPreferences

class KpisFragment : Fragment() {

    private lateinit var binding: FragmentKpisBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var tokenPreferences: TokenPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentKpisBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userPreferences = UserPreferences(requireContext())
        tokenPreferences = TokenPreferences(requireContext())

        setupUI()
    }

    private fun setupUI() {

        binding.attendaceAnual.setOnClickListener {
            val intent = Intent(requireContext(), AttendancesAnualActivity::class.java)
            startActivity(intent)
        }

        binding.attendaceMonth.setOnClickListener {
            val intent = Intent(requireContext(), AttendancesMonthActivity::class.java)
            startActivity(intent)
        }

        binding.attendaceDay.setOnClickListener {
            val intent = Intent(requireContext(), AttendancesDayActivity::class.java)
            startActivity(intent)
        }

        binding.attendaceGender.setOnClickListener {
            val intent = Intent(requireContext(), AttendancesByGenderActivity::class.java)
            startActivity(intent)
        }

        binding.earningsAnual.setOnClickListener {
            val intent = Intent(requireContext(), EarningsAnualActivity::class.java)
            startActivity(intent)
        }

        binding.earningsMonth.setOnClickListener {
            val intent = Intent(requireContext(), EarningsMonthActivity::class.java)
            startActivity(intent)
        }

        binding.earningsforPlan.setOnClickListener {
            val intent = Intent(requireContext(), EarningsByPlanActivity::class.java)
            startActivity(intent)
        }

        binding.members.setOnClickListener {
            val intent = Intent(requireContext(), MembersKpiActivity::class.java)
            startActivity(intent)
        }
    }
}