package com.example.vistacuregrad.Mainactivity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.vistacuregrad.R
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.model.UserProfileRequest
import com.example.vistacuregrad.network.RetrofitClient
import com.example.vistacuregrad.viewmodel.UserProfileViewModel
import com.example.vistacuregrad.viewmodel.UserProfileViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class UserProfile : Fragment() {

    private lateinit var viewModel: UserProfileViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_user_profile, container, false)

        // Initialize UI elements
        val btnNext: Button = view.findViewById(R.id.btnNext)
        val btnBack: Button = view.findViewById(R.id.btnBack)
        val rgGender: RadioGroup = view.findViewById(R.id.rgGender)
        val etFirstName: EditText = view.findViewById(R.id.firstname)
        val etLastName: EditText = view.findViewById(R.id.Lastname)
        val etDateOfBirth: EditText = view.findViewById(R.id.Dateofbirth)
        val etHeight: EditText = view.findViewById(R.id.etHeight)
        val etWeight: EditText = view.findViewById(R.id.etWeight)

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE)

        // Load saved user data (if available)
        loadUserProfile(etFirstName, etLastName, etDateOfBirth, rgGender, etHeight, etWeight)

        // Initialize ViewModel
        val apiService = RetrofitClient.apiService
        val repository = AuthRepository(apiService)
        val factory = UserProfileViewModelFactory(repository, requireContext())
        viewModel = ViewModelProvider(this, factory)[UserProfileViewModel::class.java]

        btnNext.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val dateOfBirth = etDateOfBirth.text.toString().trim()
            val heightStr = etHeight.text.toString().trim()
            val weightStr = etWeight.text.toString().trim()

            if (!validateInputs(firstName, lastName, dateOfBirth, heightStr, weightStr, rgGender)) {
                return@setOnClickListener
            }

            val selectedGenderId = rgGender.checkedRadioButtonId
            val selectedGender = view.findViewById<RadioButton>(selectedGenderId).text.toString()

            val height = heightStr.toDouble()
            val weight = weightStr.toDouble()

            val request = UserProfileRequest(
                firstName = firstName,
                lastName = lastName,
                dateOfBirth = dateOfBirth,
                height = height,
                weight = weight,
                gender = selectedGender
            )

            viewModel.createUserProfile(request)
        }

        // Observe API response
        viewModel.profileResponse.observe(viewLifecycleOwner) { response ->
            if (response.isSuccessful) {
                showToast("Profile created successfully!")

                // Save data in SharedPreferences after successful response
                saveUserProfile(
                    etFirstName.text.toString(),
                    etLastName.text.toString(),
                    etDateOfBirth.text.toString(),
                    rgGender.checkedRadioButtonId,
                    etHeight.text.toString(),
                    etWeight.text.toString()
                )

                findNavController().navigate(R.id.action_userProfile_to_medicalHistory)
            } else {
                showToast("Failed: ${response.errorBody()?.string() ?: "Unknown error"}")
            }
        }

        btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }

    // Function to validate inputs
    private fun validateInputs(
        firstName: String, lastName: String, dateOfBirth: String,
        heightStr: String, weightStr: String, rgGender: RadioGroup
    ): Boolean {
        if (firstName.isEmpty() || lastName.isEmpty() || dateOfBirth.isEmpty() ||
            heightStr.isEmpty() || weightStr.isEmpty()) {
            showToast("Please fill all fields")
            return false
        }

        if (!isValidDate(dateOfBirth)) {
            showToast("Enter a valid date (dd/MM/yyyy)")
            return false
        }

        if (rgGender.checkedRadioButtonId == -1) {
            showToast("Please select your gender")
            return false
        }

        val height = heightStr.toDoubleOrNull()
        val weight = weightStr.toDoubleOrNull()

        if (height == null || height <= 0 || weight == null || weight <= 0) {
            showToast("Invalid height or weight")
            return false
        }

        return true
    }

    // Function to validate the date format
    private fun isValidDate(date: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        return try {
            dateFormat.parse(date) != null
        } catch (e: Exception) {
            false
        }
    }

    // Function to save user profile data in SharedPreferences
    private fun saveUserProfile(
        firstName: String, lastName: String, dateOfBirth: String,
        genderId: Int, height: String, weight: String
    ) {
        val editor = sharedPreferences.edit()
        editor.putString("FirstName", firstName)
        editor.putString("LastName", lastName)
        editor.putString("DateOfBirth", dateOfBirth)
        editor.putInt("GenderId", genderId)
        editor.putString("Height", height)
        editor.putString("Weight", weight)
        editor.apply() // Asynchronous save
    }

    // Function to load user profile data from SharedPreferences
    private fun loadUserProfile(
        etFirstName: EditText, etLastName: EditText, etDateOfBirth: EditText,
        rgGender: RadioGroup, etHeight: EditText, etWeight: EditText
    ) {
        etFirstName.setText(sharedPreferences.getString("FirstName", ""))
        etLastName.setText(sharedPreferences.getString("LastName", ""))
        etDateOfBirth.setText(sharedPreferences.getString("DateOfBirth", ""))
        etHeight.setText(sharedPreferences.getString("Height", ""))
        etWeight.setText(sharedPreferences.getString("Weight", ""))

        val genderId = sharedPreferences.getInt("GenderId", -1)
        if (genderId != -1) {
            rgGender.check(genderId)
        }
    }

    // Function to show Toast messages
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}
