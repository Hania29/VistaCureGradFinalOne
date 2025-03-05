package com.example.vistacuregrad

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.vistacuregrad.databinding.FragmentUserDrawerBinding
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.model.UserProfileLogData
import com.example.vistacuregrad.viewmodel.UserProfileLogViewModel
import com.example.vistacuregrad.viewmodel.UserProfileLogViewModelFactory
import com.example.vistacuregrad.model.UserProfileLogRequest
import com.example.vistacuregrad.network.RetrofitClient
import java.util.regex.Pattern

class UserDrawerFragment : Fragment(R.layout.fragment_user_drawer) {

    private var _binding: FragmentUserDrawerBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private val repository = AuthRepository(RetrofitClient.apiService)
    private val viewModel: UserProfileLogViewModel by viewModels {
        UserProfileLogViewModelFactory(repository, requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserDrawerBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Load saved user data
        loadUserProfile()

        // Fetch user profile
        viewModel.getUserProfileLog()

        // Observe the response
        viewModel.profileLogResponse.observe(viewLifecycleOwner, Observer { response ->
            if (response.isSuccessful) {
                val profileData = response.body()?.data
                if (profileData != null && profileData.firstName != null) { // Check for non-null data
                    updateUIWithProfileData(profileData)
                    saveUserProfile(
                        profileData.firstName,
                        profileData.lastName ?: "",
                        profileData.dateOfBirth ?: "",
                        binding.Radiogender.checkedRadioButtonId,
                        profileData.height.toString(),
                        profileData.weight.toString()
                    )
                    Log.d("UserDrawerFragment", "Fetched user data: $profileData")
                } else {
                    Log.e("UserDrawerFragment", "Response successful but data is null or incomplete")
                }
            } else {
                Log.e("UserDrawerFragment", "Failed to fetch profile: ${response.errorBody()?.string()}")
                Toast.makeText(context, "Failed to fetch profile", Toast.LENGTH_SHORT).show()
            }
        })

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnNext.setOnClickListener {
            if (validateFields()) {
                val updateRequest = UserProfileLogRequest(
                    firstName = binding.firstnameUser.text.toString().trim(),
                    lastName = binding.lastnameUser.text.toString().trim(),
                    dateOfBirth = binding.editTextText7.text.toString().trim(),
                    height = binding.editTextText9.text.toString().trim().toDoubleOrNull(),
                    weight = binding.editTextText8.text.toString().trim().toDoubleOrNull(),
                    gender = getSelectedGender()
                )

                viewModel.updateUserProfileLog(updateRequest)

                // Save to SharedPreferences when updating
                saveUserProfile(
                    binding.firstnameUser.text.toString().trim(),
                    binding.lastnameUser.text.toString().trim(),
                    binding.editTextText7.text.toString().trim(),
                    binding.Radiogender.checkedRadioButtonId,
                    binding.editTextText9.text.toString().trim(),
                    binding.editTextText8.text.toString().trim()
                )

                Toast.makeText(context, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
            }
        }
    }

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
        Log.d("UserDrawerFragment", "Saved to SharedPreferences: " +
                "FirstName=$firstName, LastName=$lastName, DateOfBirth=$dateOfBirth, " +
                "GenderId=$genderId, Height=$height, Weight=$weight, CommitSuccess=$success")
    }

    private fun loadUserProfile() {
        val firstName = sharedPreferences.getString("FirstName", "") ?: ""
        val lastName = sharedPreferences.getString("LastName", "") ?: ""
        val dateOfBirth = sharedPreferences.getString("DateOfBirth", "") ?: ""
        val weight = sharedPreferences.getString("Weight", "") ?: ""
        val height = sharedPreferences.getString("Height", "") ?: ""
        val genderId = sharedPreferences.getInt("GenderId", -1)

        binding.firstnameUser.setText(firstName)
        binding.lastnameUser.setText(lastName)
        binding.editTextText7.setText(dateOfBirth)
        binding.editTextText8.setText(weight)
        binding.editTextText9.setText(height)
        if (genderId != -1) {
            binding.Radiogender.check(genderId)
        }

        Log.d("UserDrawerFragment", "Loaded from SharedPreferences: " +
                "FirstName=$firstName, LastName=$lastName, DateOfBirth=$dateOfBirth, " +
                "Weight=$weight, Height=$height, GenderId=$genderId")
    }

    private fun updateUIWithProfileData(profileData: UserProfileLogData?) {
        profileData?.let {
            binding.firstnameUser.setText(it.firstName ?: "")
            binding.lastnameUser.setText(it.lastName ?: "")
            binding.editTextText7.setText(it.dateOfBirth ?: "")
            binding.editTextText8.setText(it.weight?.toString() ?: "")
            binding.editTextText9.setText(it.height?.toString() ?: "")
            setSelectedGender(it.gender)
        }
    }

    private fun getSelectedGender(): String? {
        return when (binding.Radiogender.checkedRadioButtonId) {
            R.id.rbMale -> "Male"
            R.id.rbFemale -> "Female"
            else -> null
        }
    }

    private fun setSelectedGender(gender: String?) {
        when (gender) {
            "Male" -> binding.Radiogender.check(R.id.rbMale)
            "Female" -> binding.Radiogender.check(R.id.rbFemale)
        }
    }

    private fun validateFields(): Boolean {
        val firstname = binding.firstnameUser.text.toString().trim()
        val lastname = binding.lastnameUser.text.toString().trim()
        val birthday = binding.editTextText7.text.toString().trim()
        val weight = binding.editTextText8.text.toString().trim()
        val height = binding.editTextText9.text.toString().trim()

        var isValid = true

        if (firstname.isEmpty()) {
            binding.firstnameUser.error = "This field is required"
            isValid = false
        } else if (!Pattern.matches("^[a-zA-Z]+$", firstname)) {
            binding.firstnameUser.error = "First name should only contain letters"
            isValid = false
        }

        if (lastname.isEmpty()) {
            binding.lastnameUser.error = "This field is required"
            isValid = false
        } else if (!Pattern.matches("^[a-zA-Z]+$", lastname)) {
            binding.lastnameUser.error = "Last name should only contain letters"
            isValid = false
        }

        val datePattern = Pattern.compile("^\\d{1,2}/\\d{1,2}/\\d{4}$")
        if (birthday.isEmpty()) {
            binding.editTextText7.error = "This field is required"
            isValid = false
        } else if (!datePattern.matcher(birthday).matches()) {
            binding.editTextText7.error = "Invalid date format. Use MM/DD/YYYY"
            isValid = false
        }

        if (weight.isEmpty()) {
            binding.editTextText8.error = "This field is required"
            isValid = false
        } else if (!weight.matches("\\d+".toRegex())) {
            binding.editTextText8.error = "Weight should be a number"
            isValid = false
        }

        if (height.isEmpty()) {
            binding.editTextText9.error = "This field is required"
            isValid = false
        } else if (!height.matches("\\d+".toRegex())) {
            binding.editTextText9.error = "Height should be a number"
            isValid = false
        }

        if (binding.Radiogender.checkedRadioButtonId == -1) {
            Toast.makeText(context, "Please select a gender", Toast.LENGTH_SHORT).show()
            isValid = false
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}