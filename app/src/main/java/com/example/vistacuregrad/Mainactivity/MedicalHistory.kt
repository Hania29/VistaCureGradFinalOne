package com.example.vistacuregrad.Mainactivity

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.vistacuregrad.R
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.model.MedicalHistoryRequest
import com.example.vistacuregrad.network.RetrofitClient
import com.example.vistacuregrad.viewmodel.MedicalHistoryViewModel
import com.example.vistacuregrad.viewmodel.MedicalHistoryViewModelFactory
import com.example.vistacuregrad.Newactivity.NewActivity
import java.text.SimpleDateFormat
import java.util.*

class MedicalHistory : Fragment() {

    private lateinit var viewModel: MedicalHistoryViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_medical_history, container, false)

        // Initialize UI elements
        val btnDone: Button = view.findViewById(R.id.btndone)
        val btnBack: Button = view.findViewById(R.id.btnBack)
        val etAllergies: EditText = view.findViewById(R.id.etAllergies)
        val etChronicConditions: EditText = view.findViewById(R.id.etchronicconditions)
        val etMedications: EditText = view.findViewById(R.id.etmedications)
        val etSurgeries: EditText = view.findViewById(R.id.etsurgeries)
        val etFamilyHistory: EditText = view.findViewById(R.id.etfamilyhistory)
        val etCheckupDate: EditText = view.findViewById(R.id.etcheckupdate)

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MedicalHistoryPrefs", Context.MODE_PRIVATE)

        // Load saved medical history data
        loadMedicalHistory(
            etAllergies, etChronicConditions, etMedications,
            etSurgeries, etFamilyHistory, etCheckupDate
        )

        // Initialize ViewModel
        val apiService = RetrofitClient.apiService
        val repository = AuthRepository(apiService)
        val factory = MedicalHistoryViewModelFactory(repository, requireContext())
        viewModel = ViewModelProvider(this, factory)[MedicalHistoryViewModel::class.java]

        btnDone.setOnClickListener {
            val allergies = etAllergies.text.toString().trim()
            val chronicConditions = etChronicConditions.text.toString().trim()
            val medications = etMedications.text.toString().trim()
            val surgeries = etSurgeries.text.toString().trim()
            val familyHistory = etFamilyHistory.text.toString().trim()
            val checkupDate = etCheckupDate.text.toString().trim()

            // Validate user inputs
            if (allergies.isEmpty() || chronicConditions.isEmpty() || medications.isEmpty() ||
                surgeries.isEmpty() || familyHistory.isEmpty() || checkupDate.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validate Date format
            if (!isValidDate(checkupDate)) {
                etCheckupDate.error = "Enter a valid date (dd/MM/yyyy)"
                etCheckupDate.requestFocus()
                return@setOnClickListener
            }

            val request = MedicalHistoryRequest(
                allergies = allergies,
                chronicConditions = chronicConditions,
                medications = medications,
                surgeries = surgeries,
                familyHistory = familyHistory,
                lastCheckupDate = checkupDate
            )

            viewModel.createMedicalHistory(request)
        }

        // Observe API response
        viewModel.medicalHistoryResponse.observe(viewLifecycleOwner, Observer { response ->
            if (response.isSuccessful) {
                Toast.makeText(requireContext(), "Medical history created successfully!", Toast.LENGTH_SHORT).show()

                // Save data to SharedPreferences
                saveMedicalHistory(
                    etAllergies.text.toString(),
                    etChronicConditions.text.toString(),
                    etMedications.text.toString(),
                    etSurgeries.text.toString(),
                    etFamilyHistory.text.toString(),
                    etCheckupDate.text.toString()
                )

                // Navigate to NewActivity
                val intent = Intent(requireActivity(), NewActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
            }
        })

        btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
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

    private fun loadMedicalHistory(
        etAllergies: EditText,
        etChronicConditions: EditText,
        etMedications: EditText,
        etSurgeries: EditText,
        etFamilyHistory: EditText,
        etCheckupDate: EditText
    ) {
        etAllergies.setText(sharedPreferences.getString("Allergies", ""))
        etChronicConditions.setText(sharedPreferences.getString("ChronicConditions", ""))
        etMedications.setText(sharedPreferences.getString("Medications", ""))
        etSurgeries.setText(sharedPreferences.getString("Surgeries", ""))
        etFamilyHistory.setText(sharedPreferences.getString("FamilyHistory", ""))
        etCheckupDate.setText(sharedPreferences.getString("LastCheckupDate", ""))
    }

    private fun isValidDate(date: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        return try {
            dateFormat.parse(date) != null
        } catch (e: Exception) {
            false
        }
    }
}