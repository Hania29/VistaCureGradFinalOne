package com.example.vistacuregrad.Mainactivity

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

    // Initialize ViewModel using Factory
    private val registerViewModel: RegisterViewModel by viewModels {
        RegisterViewModelFactory(AuthRepository(RetrofitClient.apiService))
    }

    private var isPasswordVisible = false

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

        // Toggle password visibility
        ivTogglePassword.setOnClickListener {
            isPasswordVisible =
                togglePasswordVisibility(etPassword, ivTogglePassword, isPasswordVisible)
        }

        btnSignUp.setOnClickListener {
            val username = etUserName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            Log.i("FourthFragment username", etUserName.text.toString().trim())
            Log.i("FourthFragment email", etEmail.text.toString().trim())
            Log.i("FourthFragment password", etPassword.text.toString().trim())
            if (validateFields(etUserName, etEmail, etPassword)) {
               registerViewModel.registerUser(username,password,email)
            }
        }

        // Observe registration response
        registerViewModel.registerResponse.observe(viewLifecycleOwner, Observer { response ->
            if (response.isSuccessful) {
                Toast.makeText(requireContext(), "Registration Successful", Toast.LENGTH_SHORT)
                    .show()
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
            editText.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
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
            etPassword.error =
                "Password must contain 6+ chars, 1 uppercase, 1 number & 1 special char"
            etPassword.requestFocus()
            return false
        }

        return true
    }
}
