package com.example.vistacuregrad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.vistacuregrad.databinding.FragmentMedicalDrawerBinding
import com.example.vistacuregrad.model.MedicalHistoryRequest
import com.example.vistacuregrad.viewmodel.MedicalHistoryLogViewModel
import com.example.vistacuregrad.viewmodel.MedicalHistoryLogViewModelFactory
import java.util.regex.Pattern

class MedicalDrawerFragment : Fragment(R.layout.fragment_medical_drawer) {

    private var _binding: FragmentMedicalDrawerBinding? = null
    private val binding get() = _binding!!

    // Initialize ViewModel using the factory
    private val viewModel: MedicalHistoryLogViewModel by viewModels {
        val repository = (requireActivity().application as MyApplication).repository
        MedicalHistoryLogViewModelFactory(repository, requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMedicalDrawerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set up click listeners
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnNext.setOnClickListener {
            if (validateFields()) {
                // Create a MedicalHistoryRequest object from the input fields
                val request = MedicalHistoryRequest(
                    allergies = binding.Allergies.text.toString().trim(),
                    chronicConditions = binding.chronicConditions.text.toString().trim(),
                    medications = binding.medication.text.toString().trim(),
                    surgeries = binding.surgeries.text.toString().trim(),
                    familyHistory = binding.familyhistory.text.toString().trim(),
                    lastCheckupDate = binding.lastcheckup.text.toString().trim()
                )

                // Call the ViewModel to update medical history
                viewModel.updateMedicalHistory(request)
            } else {
                Toast.makeText(context, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
            }
        }

        /// Observe the medical history log response
        viewModel.medicalHistoryLogResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                val medicalHistory = response.body()?.medicalHistory
                // Update UI with medical history data
                medicalHistory?.let {
                    binding.Allergies.setText(it.allergies)
                    binding.chronicConditions.setText(it.chronicConditions)
                    binding.medication.setText(it.medications)
                    binding.surgeries.setText(it.surgeries)
                    binding.familyhistory.setText(it.familyHistory)
                    binding.lastcheckup.setText(it.lastCheckupDate)
                }
            } else {
                // Handle error
                Toast.makeText(context, "Failed to fetch medical history.", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe the update medical history response
        viewModel.updateMedicalHistoryResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                // Handle successful update
                Toast.makeText(context, "Medical history updated successfully.", Toast.LENGTH_SHORT).show()
            } else {
                // Handle error
                Toast.makeText(context, "Failed to update medical history.", Toast.LENGTH_SHORT).show()
            }
        }

        // Fetch medical history when the fragment is created
        viewModel.getMedicalHistory()
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