package com.example.vistacuregrad.Mainactivity

import android.os.Bundle
import android.text.TextUtils
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.vistacuregrad.R
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.network.RetrofitClient
import com.example.vistacuregrad.viewmodel.AuthViewModel
import com.example.vistacuregrad.viewmodel.AuthViewModelFactory
import kotlinx.coroutines.launch

class FifthFragment : Fragment() {

    private lateinit var emailField: EditText
    private lateinit var progressBar: ProgressBar
    private var savedEmail: String? = null

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(AuthRepository(RetrofitClient.apiService))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_fifth, container, false)

        emailField = view.findViewById(R.id.etEmailAddressThirdActivity)
        progressBar = view.findViewById(R.id.progressBarForgot)

        savedInstanceState?.getString("email")?.let {
            emailField.setText(it)
        }

        val btnBack: Button = view.findViewById(R.id.btnBack)
        val btnConfirm: Button = view.findViewById(R.id.btnConfirmMail)

        btnConfirm.setOnClickListener {
            handleForgotPassword()
        }

        btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        observeForgotPasswordResponse()

        return view
    }

    private fun handleForgotPassword() {
        val emailText = emailField.text.toString().trim()

        if (!isValidEmail(emailText)) {
            emailField.error = "Enter a valid email"
            emailField.requestFocus()
            return
        }

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            authViewModel.forgotPassword(emailText)
        }
    }

    private fun observeForgotPasswordResponse() {
        authViewModel.forgotPasswordResponse.observe(viewLifecycleOwner, Observer { response ->
            progressBar.visibility = View.GONE
            if (response.isSuccessful && response.body() != null) {
                Toast.makeText(requireContext(), response.body()?.message, Toast.LENGTH_LONG).show()
                findNavController().navigate(R.id.action_fifthFragment_to_resetPass)
            } else {
                Toast.makeText(requireContext(), "Error: ${response.message()}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun isValidEmail(email: String): Boolean {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("email", emailField.text.toString())
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.getString("email")?.let {
            emailField.setText(it)
        }
    }
}
