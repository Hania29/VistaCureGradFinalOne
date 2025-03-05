package com.example.vistacuregrad

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.vistacuregrad.databinding.FragmentMedicalDrawerBinding
import com.example.vistacuregrad.model.MedicalHistoryData
import com.example.vistacuregrad.model.MedicalHistoryRequest
import com.example.vistacuregrad.viewmodel.MedicalHistoryLogViewModel
import com.example.vistacuregrad.viewmodel.MedicalHistoryLogViewModelFactory
import java.util.regex.Pattern

class MedicalDrawerFragment : Fragment(R.layout.fragment_medical_drawer) {

    private var _binding: FragmentMedicalDrawerBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    private val viewModel: MedicalHistoryLogViewModel by viewModels {
        val repository = (requireActivity().application as MyApplication).repository
        MedicalHistoryLogViewModelFactory(repository, requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMedicalDrawerBinding.inflate(inflater, container, false)
        sharedPreferences = requireContext().getSharedPreferences("MedicalHistoryPrefs", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load saved data
        loadMedicalHistory()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnNext.setOnClickListener {
            if (validateFields()) {
                val request = MedicalHistoryRequest(
                    allergies = binding.Allergies.text.toString().trim(),
                    chronicConditions = binding.chronicConditions.text.toString().trim(),
                    medications = binding.medication.text.toString().trim(),
                    surgeries = binding.surgeries.text.toString().trim(),
                    familyHistory = binding.familyhistory.text.toString().trim(),
                    lastCheckupDate = binding.lastcheckup.text.toString().trim()
                )

                viewModel.updateMedicalHistory(request)
                // Save to SharedPreferences
                saveMedicalHistory(
                    binding.Allergies.text.toString().trim(),
                    binding.chronicConditions.text.toString().trim(),
                    binding.medication.text.toString().trim(),
                    binding.surgeries.text.toString().trim(),
                    binding.familyhistory.text.toString().trim(),
                    binding.lastcheckup.text.toString().trim()
                )
            } else {
                Toast.makeText(context, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe the medical history log response
        viewModel.medicalHistoryLogResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                val medicalHistory = response.body()?.medicalHistory
                medicalHistory?.let {
                    updateUIWithMedicalHistory(it)
                    // Save fetched data to SharedPreferences
                    saveMedicalHistory(
                        it.allergies,
                        it.chronicConditions,
                        it.medications,
                        it.surgeries,
                        it.familyHistory,
                        it.lastCheckupDate
                    )
                }
            } else {
                Toast.makeText(context, "Failed to fetch medical history.", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe the update response
        viewModel.updateMedicalHistoryResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                Toast.makeText(context, "Medical history updated successfully.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to update medical history.", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.getMedicalHistory()
    }

    private fun saveMedicalHistory(
        allergies: String,
        chronicConditions: String,
        medications: String,
        surgeries: String,
        familyHistory: String,
        lastCheckupDate: String
    ) {
        val editor = sharedPreferences.edit()
        editor.putString("Allergies", allergies)
        editor.putString("ChronicConditions", chronicConditions)
        editor.putString("Medications", medications)
        editor.putString("Surgeries", surgeries)
        editor.putString("FamilyHistory", familyHistory)
        editor.putString("LastCheckupDate", lastCheckupDate)
        editor.apply()
    }

    private fun loadMedicalHistory() {
        binding.Allergies.setText(sharedPreferences.getString("Allergies", ""))
        binding.chronicConditions.setText(sharedPreferences.getString("ChronicConditions", ""))
        binding.medication.setText(sharedPreferences.getString("Medications", ""))
        binding.surgeries.setText(sharedPreferences.getString("Surgeries", ""))
        binding.familyhistory.setText(sharedPreferences.getString("FamilyHistory", ""))
        binding.lastcheckup.setText(sharedPreferences.getString("LastCheckupDate", ""))
    }

    private fun updateUIWithMedicalHistory(medicalHistory: MedicalHistoryData) {
        binding.Allergies.setText(medicalHistory.allergies)
        binding.chronicConditions.setText(medicalHistory.chronicConditions)
        binding.medication.setText(medicalHistory.medications)
        binding.surgeries.setText(medicalHistory.surgeries)
        binding.familyhistory.setText(medicalHistory.familyHistory)
        binding.lastcheckup.setText(medicalHistory.lastCheckupDate)
    }

    private fun validateFields(): Boolean {
        val allergies = binding.Allergies.text.toString().trim()
        val chronicConditions = binding.chronicConditions.text.toString().trim()
        val medication = binding.medication.text.toString().trim()
        val surgeries = binding.surgeries.text.toString().trim()
        val familyHistory = binding.familyhistory.text.toString().trim()
        val lastCheckup = binding.lastcheckup.text.toString().trim()

        var isValid = true

        if (allergies.isEmpty()) {
            binding.Allergies.error = "This field is required"
            isValid = false
        } else if (!Pattern.matches("^[a-zA-Z\\s]+$", allergies)) {
            binding.Allergies.error = "Allergies should only contain letters and spaces"
            isValid = false
        }

        if (chronicConditions.isEmpty()) {
            binding.chronicConditions.error = "This field is required"
            isValid = false
        } else if (!Pattern.matches("^[a-zA-Z\\s]+$", chronicConditions)) {
            binding.chronicConditions.error = "Chronic conditions should only contain letters and spaces"
            isValid = false
        }

        if (medication.isEmpty()) {
            binding.medication.error = "This field is required"
            isValid = false
        } else if (!Pattern.matches("^[a-zA-Z\\s]+$", medication)) {
            binding.medication.error = "Medication should only contain letters and spaces"
            isValid = false
        }

        if (surgeries.isEmpty()) {
            binding.surgeries.error = "This field is required"
            isValid = false
        } else if (!Pattern.matches("^[a-zA-Z\\s]+$", surgeries)) {
            binding.surgeries.error = "Surgeries should only contain letters and spaces"
            isValid = false
        }

        if (familyHistory.isEmpty()) {
            binding.familyhistory.error = "This field is required"
            isValid = false
        } else if (!Pattern.matches("^[a-zA-Z\\s]+$", familyHistory)) {
            binding.familyhistory.error = "Family history should only contain letters and spaces"
            isValid = false
        }

        val datePattern = Pattern.compile("^\\d{2}/\\d{2}/\\d{4}$")
        if (lastCheckup.isEmpty()) {
            binding.lastcheckup.error = "This field is required"
            isValid = false
        } else if (!datePattern.matcher(lastCheckup).matches()) {
            binding.lastcheckup.error = "Invalid date format. Use MM/DD/YYYY"
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}