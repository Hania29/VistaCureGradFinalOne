// File: app/src/main/java/com/example/vistacuregrad/Mainactivity/SixthFragment.kt
package com.example.vistacuregrad.Mainactivity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.vistacuregrad.R
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.Viewmodel.OtpViewModel
import com.example.vistacuregrad.Viewmodel.OtpViewModelFactory
import com.example.vistacuregrad.model.ProfileHistoryCheckResponse // Import
import com.example.vistacuregrad.network.RetrofitClient
import com.example.vistacuregrad.Newactivity.NewActivity


class SixthFragment : Fragment() {

    private lateinit var otpDigit1: EditText
    private lateinit var otpDigit2: EditText
    private lateinit var otpDigit3: EditText
    private lateinit var otpDigit4: EditText
    private lateinit var otpDigit5: EditText
    private lateinit var otpDigit6: EditText
    private lateinit var progressBar: ProgressBar
    private lateinit var btnOTP: Button

    private val otpViewModel: OtpViewModel by viewModels {
        OtpViewModelFactory(requireActivity().application, AuthRepository(RetrofitClient.apiService))
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_sixth, container, false)

        otpDigit1 = view.findViewById(R.id.otpDigit1)
        otpDigit2 = view.findViewById(R.id.otpDigit2)
        otpDigit3 = view.findViewById(R.id.otpDigit3)
        otpDigit4 = view.findViewById(R.id.otpDigit4)
        otpDigit5 = view.findViewById(R.id.otpDigit5)
        otpDigit6 = view.findViewById(R.id.otpDigit6)
        progressBar = view.findViewById(R.id.progressBar)
        btnOTP = view.findViewById(R.id.btnOTP)

        val btnBack = view.findViewById<Button>(R.id.btnBack)

        otpDigit1.addTextChangedListener(createOtpTextWatcher(otpDigit2))
        otpDigit2.addTextChangedListener(createOtpTextWatcher(otpDigit3, otpDigit1))
        otpDigit3.addTextChangedListener(createOtpTextWatcher(otpDigit4, otpDigit2))
        otpDigit4.addTextChangedListener(createOtpTextWatcher(otpDigit5, otpDigit3))
        otpDigit5.addTextChangedListener(createOtpTextWatcher(otpDigit6, otpDigit4))
        otpDigit6.addTextChangedListener(createOtpTextWatcher(null, otpDigit5))

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        btnOTP.setOnClickListener {
            val otpCode = getOtpCode()
            if (validateOtpFields(otpCode)) {
                btnOTP.isEnabled = false // Prevent multiple clicks
                verifyOtp(otpCode)
            }
        }

        // *** Observe the profileHistoryCheckResult LiveData for navigation decision ***
        observeProfileHistoryCheckResult()

        // Optional: Observe otpResponse for initial OTP verification message
        observeOtpResponse()

        return view
    }

    private fun getOtpCode(): String {
        return otpDigit1.text.toString() +
                otpDigit2.text.toString() +
                otpDigit3.text.toString() +
                otpDigit4.text.toString() +
                otpDigit5.text.toString() +
                otpDigit6.text.toString()
    }

    private fun verifyOtp(otpCode: String) {
        progressBar.visibility = View.VISIBLE
        // Calling verifyOtp in ViewModel triggers the profile check internally now
        otpViewModel.verifyOtp(otpCode)
    }

    // Observer for initial OTP verification message
    private fun observeOtpResponse() {
        otpViewModel.otpResponse.observe(viewLifecycleOwner, Observer { response ->
            // Progress bar and button re-enabling handled by profileHistoryCheckResult observer
            response?.let {
                if (it.isSuccessful && it.body() != null) {
                    val otpResponse = it.body()
                    if (otpResponse?.status.equals("Success", ignoreCase = true)) {
                        Toast.makeText(requireContext(), "OTP Verified Successfully! Checking profile...", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(requireContext(), otpResponse?.message ?: "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    response.errorBody()?.string()?.let { errorMessage ->
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                    } ?: Toast.makeText(requireContext(), "Failed to verify OTP. Try again.", Toast.LENGTH_SHORT).show()
                }
            } ?: run {
                Toast.makeText(requireContext(), "An error occurred. Please try again.", Toast.LENGTH_SHORT).show()
            }
        })
    }


    // *** Observer for handling the profile and medical history check result and navigation ***
    private fun observeProfileHistoryCheckResult() {
        otpViewModel.profileHistoryCheckResult.observe(viewLifecycleOwner, Observer { result ->
            progressBar.visibility = View.GONE // Hide progress bar once check is complete
            btnOTP.isEnabled = true // Re-enable button

            result.onSuccess { data: ProfileHistoryCheckResponse ->
                Log.d("SixthFragment", "Profile check result: UserProfileExists=${data.UserProfileExists}, MedicalHistoryExists=${data.MedicalHistoryExists}")
                if (data.UserProfileExists && data.MedicalHistoryExists) {
                    // User has profile and medical history, navigate to new activity and finish current activity
                    Toast.makeText(requireContext(), "Profile and history found. Redirecting...", Toast.LENGTH_SHORT).show()
                    navigateToNewActivity()
                } else {
                    // User needs to complete profile/medical history, navigate to that flow
                    Toast.makeText(requireContext(), "Profile/history not found. Complete your profile.", Toast.LENGTH_SHORT).show()
                    navigateToUserProfileAndMedicalHistory()
                }
            }.onFailure { exception ->
                // Handle the error (e.g., show a Toast)
                Log.e("SixthFragment", "Error during profile check: ${exception.message}")
                Toast.makeText(context, "Error checking user profile: ${exception.message}", Toast.LENGTH_LONG).show()
                // Keep user on OTP screen on error, or decide on a different error flow
            }
        })
    }


    private fun createOtpTextWatcher(next: EditText?, previous: EditText? = null): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrEmpty() && s.length == 1) {
                    next?.requestFocus()
                } else if (s.isNullOrEmpty() && before == 1) {
                    previous?.requestFocus()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        }
    }

    private fun validateOtpFields(otpCode: String): Boolean {
        if (otpCode.length != 6) {
            Toast.makeText(requireContext(), "Please enter a valid 6-digit OTP.", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // Function to navigate to the "new activity" and finish the current activity
    private fun navigateToNewActivity() {
        val intent = Intent(activity, com.example.vistacuregrad.Newactivity.NewActivity::class.java)
        startActivity(intent)
        activity?.finish() // Finish the current Activity (the one hosting this fragment)
    }

    // Function to navigate to the user profile / medical history flow using Navigation Component
    private fun navigateToUserProfileAndMedicalHistory() {
        findNavController().navigate(R.id.action_sixthFragment_to_userProfile) // *** Use your actual navigation action ID ***
    }
}