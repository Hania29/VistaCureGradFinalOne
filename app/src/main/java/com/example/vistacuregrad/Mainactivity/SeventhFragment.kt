package com.example.vistacuregrad.Mainactivity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.InputType
import android.text.TextUtils
import android.util.Log
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
import com.example.vistacuregrad.Viewmodel.LoginViewModel
import com.example.vistacuregrad.Viewmodel.LoginViewModelFactory
import com.example.vistacuregrad.network.RetrofitClient

class SeventhFragment : Fragment() {

    private lateinit var sharedPreferences: SharedPreferences
    private var isPasswordVisible = false
    private lateinit var progressBar: ProgressBar

    private val loginViewModel: LoginViewModel by viewModels {
        LoginViewModelFactory(AuthRepository(RetrofitClient.apiService), requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_seventh, container, false)

        sharedPreferences = requireActivity().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        val etUserName0: EditText = view.findViewById(R.id.etUserName0)
        val etPassword: EditText = view.findViewById(R.id.etPassword)
        val ivTogglePassword: ImageView = view.findViewById(R.id.hidddenEye)
        val btnSignUp: Button = view.findViewById(R.id.btnSignUp)
        val btnForgotPassword: Button = view.findViewById(R.id.btnForgotPassword)
        val btnBack: Button = view.findViewById(R.id.btnBack)
        val btnLogIn: Button = view.findViewById(R.id.btnLogIn)
        progressBar = view.findViewById(R.id.progressBar)

        ivTogglePassword.setOnClickListener {
            isPasswordVisible = togglePasswordVisibility(etPassword, ivTogglePassword, isPasswordVisible)
        }

        btnSignUp.setOnClickListener {
            findNavController().navigate(R.id.action_seventhFragment_to_fourthFragment)
        }

        btnForgotPassword.setOnClickListener {
            findNavController().navigate(R.id.action_seventhFragment_to_fifthFragment)
        }

        btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        btnLogIn.setOnClickListener {
            val enteredUsername = etUserName0.text.toString().trim()
            val enteredPassword = etPassword.text.toString().trim()

            if (validateFields(etUserName0, etPassword)) {
                progressBar.visibility = View.VISIBLE
                loginViewModel.loginUser(enteredUsername, enteredPassword)
            }
        }

        loginViewModel.loginResponse.observe(viewLifecycleOwner, Observer { response ->
            progressBar.visibility = View.GONE
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    if (body.status == "Success" && !body.tempToken.isNullOrEmpty()) {
                        sharedPreferences.edit()
                            .putString("TEMP_TOKEN", body.tempToken)
                            .apply()

                        Log.d("LoginFragment", "Login successful. Token saved. Navigating to OTP screen...")
                        findNavController().navigate(R.id.action_seventhFragment_to_sixthFragment)
                    } else {
                        Toast.makeText(requireContext(), "Login failed: ${body.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                        Log.e("LoginFragment", "Login failed: ${body.message}")
                    }
                } ?: Log.e("LoginFragment", "Response body is null")
            } else {
                Toast.makeText(requireContext(), "Server error: ${response.message()}", Toast.LENGTH_SHORT).show()
                Log.e("LoginFragment", "Login API call failed: ${response.message()}")
            }
        })

        return view
    }

    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView, isVisible: Boolean): Boolean {
        return if (isVisible) {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            imageView.setImageResource(R.drawable.hidden11)
            false
        } else {
            editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            imageView.setImageResource(R.drawable.visabile)
            true
        }.also { editText.setSelection(editText.text.length) }
    }

    private fun validateFields(username: EditText, password: EditText): Boolean {
        return when {
            TextUtils.isEmpty(username.text.toString().trim()) -> {
                username.error = "Username cannot be empty"
                username.requestFocus()
                false
            }
            TextUtils.isEmpty(password.text.toString().trim()) -> {
                password.error = "Password cannot be empty"
                password.requestFocus()
                false
            }
            else -> true
        }
    }
}
