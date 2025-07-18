package com.example.vistacuregrad.Mainactivity

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.vistacuregrad.R
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.Viewmodel.RegisterViewModel
import com.example.vistacuregrad.Viewmodel.RegisterViewModelFactory
import com.example.vistacuregrad.network.RetrofitClient

class FourthFragment : Fragment() {

    private val registerViewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory(AuthRepository(RetrofitClient.apiService))
    }

    private var isPasswordVisible = false
    private lateinit var progressBar: ProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_fourth, container, false)

        val etUserName = view.findViewById<EditText>(R.id.etUserName)
        val etEmail = view.findViewById<EditText>(R.id.etEmailAddressSecondActivity)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val ivTogglePassword = view.findViewById<ImageView>(R.id.hiddenEye2)
        val btnSignUp = view.findViewById<Button>(R.id.btnSignUpSecondActivity)
        val btnLogIn = view.findViewById<Button>(R.id.btnLogInSecondActivity)
        val btnBack = view.findViewById<Button>(R.id.btnBack)
        progressBar = view.findViewById(R.id.progressBar)

        ivTogglePassword.setOnClickListener {
            isPasswordVisible =
                togglePasswordVisibility(etPassword, ivTogglePassword, isPasswordVisible)
        }

        btnSignUp.setOnClickListener {
            val username = etUserName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validateFields(etUserName, etEmail, etPassword)) {
                progressBar.visibility = View.VISIBLE
                registerViewModel.registerUser(username, password, email)
            }
        }

        registerViewModel.registerResponse.observe(viewLifecycleOwner, Observer { response ->
            progressBar.visibility = View.GONE
            if (response.isSuccessful) {
                // Clear any old user data when a new user registers
                clearOldUserData()
                Toast.makeText(requireContext(), "Registration Successful", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_fourthFragment_to_seventhFragment)
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Registration Failed"
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        })

        btnLogIn.setOnClickListener {
            findNavController().navigate(R.id.action_fourthFragment_to_seventhFragment)
        }

        btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return view
    }

    private fun togglePasswordVisibility(
        editText: EditText,
        imageView: ImageView,
        isVisible: Boolean
    ): Boolean {
        if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(R.drawable.hidden11)
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imageView.setImageResource(R.drawable.visabile)
        }
        editText.setSelection(editText.text.length)
        return !isVisible
    }

    private fun validateFields(
        etUserName: EditText,
        etEmail: EditText,
        etPassword: EditText
    ): Boolean {
        val userName = etUserName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        if (TextUtils.isEmpty(userName)) {
            etUserName.error = "Username is required"
            etUserName.requestFocus()
            return false
        }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = "Enter a valid email address"
            etEmail.requestFocus()
            return false
        }

        if (password.length < 6 || !password.matches(".*[A-Z].*".toRegex()) ||
            !password.matches(".*\\d.*".toRegex()) || !password.matches(".*[@#\$%&!].*".toRegex())
        ) {
            etPassword.error = "Password must contain 6+ chars, 1 uppercase, 1 number & 1 special char"
            etPassword.requestFocus()
            return false
        }

        return true
    }

    private fun clearOldUserData() {
        // Clear user profile data
        val userProfilePrefs = requireContext().getSharedPreferences("UserProfilePrefs", Context.MODE_PRIVATE)
        userProfilePrefs.edit().clear().apply()

        // Clear medical history data
        val medicalHistoryPrefs = requireContext().getSharedPreferences("MedicalHistoryPrefs", Context.MODE_PRIVATE)
        medicalHistoryPrefs.edit().clear().apply()

        // Clear app preferences
        val appPrefs = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        appPrefs.edit().clear().apply()

        Log.d("FourthFragment", "Cleared old user data for new registration")
    }
}