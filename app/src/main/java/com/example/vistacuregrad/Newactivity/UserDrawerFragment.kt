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
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferences = requireActivity().getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE)

        // Load saved user data (if available)
        loadUserProfile()

        // Fetch user profile when the fragment is created
        viewModel.getUserProfileLog()

        // Observe the response
        viewModel.profileLogResponse.observe(viewLifecycleOwner, Observer { response ->
            if (response.isSuccessful) {
                val profileData = response.body()?.data
                if (profileData != null) {
                    updateUIWithProfileData(profileData)
                    Log.d("UserDrawerFragment", "Fetched user data: $profileData")
                } else {
                    Log.e("UserDrawerFragment", "Response successful but data is null")
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
                Toast.makeText(context, "Profile updated successfully.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Please fill all fields correctly.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadUserProfile() {
        binding.firstnameUser.setText(sharedPreferences.getString("FirstName", ""))
        binding.lastnameUser.setText(sharedPreferences.getString("LastName", ""))
        binding.editTextText7.setText(sharedPreferences.getString("DateOfBirth", ""))
        binding.editTextText8.setText(sharedPreferences.getString("Weight", ""))
        binding.editTextText9.setText(sharedPreferences.getString("Height", ""))

        val genderId = sharedPreferences.getInt("GenderId", -1)
        if (genderId != -1) {
            binding.Radiogender.check(genderId)
        }
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