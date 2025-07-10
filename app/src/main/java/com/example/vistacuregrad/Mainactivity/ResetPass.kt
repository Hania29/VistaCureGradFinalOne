package com.example.vistacuregrad.Mainactivity

import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.vistacuregrad.R
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.network.RetrofitClient
import com.example.vistacuregrad.viewmodel.ResetPasswordViewModel
import com.example.vistacuregrad.viewmodel.ResetPasswordViewModelFactory
import kotlinx.coroutines.launch
import org.json.JSONObject

class ResetPass : Fragment() {

    private lateinit var etPass: EditText
    private lateinit var etConfirmPass: EditText
    private lateinit var etEmailAddress: EditText
    private lateinit var btnDone: Button
    private lateinit var btnBack: Button
    private lateinit var ivTogglePass: ImageView
    private lateinit var ivToggleConfirmPass: ImageView

    private var token: String? = null
    private var email: String? = null
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false
    private var isResetting = false

    private val resetPasswordViewModel: ResetPasswordViewModel by viewModels {
        ResetPasswordViewModelFactory(AuthRepository(RetrofitClient.apiService))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        token = resetPasswordViewModel.getResetToken(requireContext()) // Get token from SharedPreferences


        if (token.isNullOrEmpty() || email.isNullOrEmpty()) {
            Log.e("ResetPassFragment", "Missing token or email in arguments")
            showToast("Invalid reset data. Please try again.")
        } else {
            Log.d("ResetPassFragment", "Token and email received.")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_reset_pass, container, false)

        etPass = view.findViewById(R.id.etPass)
        etConfirmPass = view.findViewById(R.id.etConfirmPass)
        etEmailAddress = view.findViewById(R.id.etemailaddress)
        ivTogglePass = view.findViewById(R.id.ivTogglePass)
        ivToggleConfirmPass = view.findViewById(R.id.ivToggleConfirmPass)
        btnDone = view.findViewById(R.id.btnDone)
        btnBack = view.findViewById(R.id.btnBack)

        etEmailAddress.setText(email ?: "")

        ivTogglePass.setOnClickListener {
            isPasswordVisible = togglePasswordVisibility(etPass, ivTogglePass, isPasswordVisible)
        }
        ivToggleConfirmPass.setOnClickListener {
            isConfirmPasswordVisible = togglePasswordVisibility(etConfirmPass, ivToggleConfirmPass, isConfirmPasswordVisible)
        }

        btnDone.setOnClickListener {
            if (isResetting) return@setOnClickListener

            val password = etPass.text.toString().trim()
            val confirmPassword = etConfirmPass.text.toString().trim()
            val emailInput = etEmailAddress.text.toString().trim().lowercase()

            if (validateFields(password, confirmPassword, emailInput)) {
                if (token.isNullOrEmpty()) {
                    showToast("Missing reset token")
                    return@setOnClickListener
                }

                isResetting = true
                btnDone.isEnabled = false
                btnDone.alpha = 0.5f

                lifecycleScope.launch {
                    Log.d("ResetPassFragment", "Resetting password for $emailInput")
                    resetPasswordViewModel.resetPassword(password, confirmPassword, token!!, emailInput)
                }
            }
        }

        resetPasswordViewModel.resetPasswordResponse.observe(viewLifecycleOwner) { response ->
            lifecycleScope.launch {
                isResetting = false
                btnDone.isEnabled = true
                btnDone.alpha = 1.0f

                if (response.isSuccessful && response.body() != null) {
                    showToast(response.body()?.message ?: "Password reset successfully!")
                    Log.d("ResetPassFragment", "Password Reset Successful")
                    findNavController().navigate(R.id.action_resetPass_to_seventhFragment)
                } else {
                    val errorMessage = parseError(response)
                    Log.e("ResetPassFragment", "API Error: $errorMessage")
                    showToast(errorMessage)
                }
            }
        }

        btnBack.setOnClickListener {
            if (!isResetting) {
                findNavController().navigateUp()
            }
        }

        return view
    }

    private fun togglePasswordVisibility(editText: EditText, imageView: ImageView, isVisible: Boolean): Boolean {
        editText.inputType = if (isVisible) {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        }
        imageView.setImageResource(if (isVisible) R.drawable.hidden11 else R.drawable.visabile)
        editText.setSelection(editText.text.length)
        return !isVisible
    }

    private fun validateFields(password: String, confirmPassword: String, email: String): Boolean {
        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmailAddress.error = "Enter a valid email"
            etEmailAddress.requestFocus()
            return false
        }
        if (password.length !in 8..20 || !password.matches(".*[A-Z].*".toRegex()) ||
            !password.matches(".*\\d.*".toRegex()) || !password.matches(".*[!@#\$%^&*()_+\\-=].*".toRegex())
        ) {
            etPass.error = "Password must be 8-20 chars, include 1 uppercase, 1 number, and 1 special char"
            etPass.requestFocus()
            return false
        }
        if (password != confirmPassword) {
            etConfirmPass.error = "Passwords do not match"
            etConfirmPass.requestFocus()
            return false
        }
        return true
    }

    private fun parseError(response: retrofit2.Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string() ?: "{}"
            Log.e("ResetPassFragment", "Full Error Response: $errorBody")

            JSONObject(errorBody).optString("message", "Unknown error occurred").let { message ->
                when {
                    message.contains("token", ignoreCase = true) -> {
                        "Invalid or expired token. Please request a new password reset link."
                    }
                    message.contains("expired", ignoreCase = true) -> {
                        "This password reset link has expired. Please request a new one."
                    }
                    message.contains("invalid", ignoreCase = true) -> {
                        "Invalid request. Please try again or request a new link."
                    }
                    else -> message.ifEmpty { "An unknown error occurred. Please try again." }
                }
            }
        } catch (e: Exception) {
            Log.e("ResetPassFragment", "Error parsing error response", e)
            "Error: ${response.message()}"
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resetPasswordViewModel.resetPasswordResponse.removeObservers(viewLifecycleOwner)
    }
}
