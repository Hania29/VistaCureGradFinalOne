package com.example.vistacuregrad.Mainactivity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
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
    private var isResponseHandled = false

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

        Log.d("UserProfile", "UI initialized: " +
                "etFirstName=${etFirstName != null}, " +
                "etLastName=${etLastName != null}, " +
                "etDateOfBirth=${etDateOfBirth != null}, " +
                "etHeight=${etHeight != null}, " +
                "etWeight=${etWeight != null}, " +
                "rgGender=${rgGender != null}")

        // Initialize SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE)

        // Load saved user data
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

            Log.d("UserProfile", "Next clicked: " +
                    "firstName=$firstName, " +
                    "lastName=$lastName, " +
                    "dateOfBirth=$dateOfBirth, " +
                    "heightStr=$heightStr, " +
                    "weightStr=$weightStr")

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

            isResponseHandled = false
            viewModel.createUserProfile(request)

            // Save to SharedPreferences and verify
            saveUserProfile(firstName, lastName, dateOfBirth, selectedGenderId, heightStr, weightStr)
            verifySharedPreferences() // Debug step
        }

        // Observe API response
        viewModel.profileResponse.observe(viewLifecycleOwner) { response ->
            Log.d("UserProfile", "Response received: isSuccessful=${response.isSuccessful}")
            if (!isResponseHandled && response.isSuccessful) {
                isResponseHandled = true
                showToast("Profile created successfully!")

                // Save data again after successful API response
                saveUserProfile(
                    etFirstName.text.toString().trim(),
                    etLastName.text.toString().trim(),
                    etDateOfBirth.text.toString().trim(),
                    rgGender.checkedRadioButtonId,
                    etHeight.text.toString().trim(),
                    etWeight.text.toString().trim()
                )
                verifySharedPreferences() // Debug step

                Log.d("UserProfile", "Navigating to medical history")
                findNavController().navigate(R.id.action_userProfile_to_medicalHistory)
            } else if (!response.isSuccessful) {
                showToast("Failed: ${response.errorBody()?.string() ?: "Unknown error"}")
                Log.e("UserProfile", "API call failed: ${response.errorBody()?.string()}")
            }
        }

        btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }

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

    private fun isValidDate(date: String): Boolean {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        dateFormat.isLenient = false
        return try {
            dateFormat.parse(date) != null
        } catch (e: Exception) {
            false
        }
    }

    // In UserProfile.kt, update saveUserProfile to include more debug info:
    private fun saveUserProfile(
        firstName: String,
        lastName: String,
        dateOfBirth: String,
        genderId: Int,
        height: String,
        weight: String
    ) {
        val editor = sharedPreferences.edit()
        editor.putString("FirstName", firstName)
        editor.putString("LastName", lastName)
        editor.putString("DateOfBirth", dateOfBirth)
        editor.putInt("GenderId", genderId)
        editor.putString("Height", height)
        editor.putString("Weight", weight)
        val success = editor.commit()
        Log.d("UserProfile", "Saved to SharedPreferences: " +
                "FirstName=$firstName, " +
                "LastName=$lastName, " +
                "DateOfBirth=$dateOfBirth, " +
                "GenderId=$genderId, " +
                "Height=$height, " +
                "Weight=$weight, " +
                "CommitSuccess=$success")
    }

    private fun loadUserProfile(
        etFirstName: EditText,
        etLastName: EditText,
        etDateOfBirth: EditText,
        rgGender: RadioGroup,
        etHeight: EditText,
        etWeight: EditText
    ) {
        val firstName = sharedPreferences.getString("FirstName", "") ?: ""
        val lastName = sharedPreferences.getString("LastName", "") ?: ""
        val dateOfBirth = sharedPreferences.getString("DateOfBirth", "") ?: ""
        val height = sharedPreferences.getString("Height", "") ?: ""
        val weight = sharedPreferences.getString("Weight", "") ?: ""
        val genderId = sharedPreferences.getInt("GenderId", -1)

        etFirstName.setText(firstName)
        etLastName.setText(lastName)
        etDateOfBirth.setText(dateOfBirth)
        etHeight.setText(height)
        etWeight.setText(weight)
        if (genderId != -1) {
            rgGender.check(genderId)
        }

        Log.d("UserProfile", "Loaded from SharedPreferences: " +
                "FirstName=$firstName, " +
                "LastName=$lastName, " +
                "DateOfBirth=$dateOfBirth, " +
                "GenderId=$genderId, " +
                "Height=$height, " +
                "Weight=$weight")
    }

    private fun verifySharedPreferences() {
        val firstName = sharedPreferences.getString("FirstName", "N/A") ?: "N/A"
        val lastName = sharedPreferences.getString("LastName", "N/A") ?: "N/A"
        val dateOfBirth = sharedPreferences.getString("DateOfBirth", "N/A") ?: "N/A"
        val height = sharedPreferences.getString("Height", "N/A") ?: "N/A"
        val weight = sharedPreferences.getString("Weight", "N/A") ?: "N/A"
        val genderId = sharedPreferences.getInt("GenderId", -1)
        Log.d("UserProfile", "Verified SharedPreferences contents: " +
                "FirstName=$firstName, " +
                "LastName=$lastName, " +
                "DateOfBirth=$dateOfBirth, " +
                "GenderId=$genderId, " +
                "Height=$height, " +
                "Weight=$weight")
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}