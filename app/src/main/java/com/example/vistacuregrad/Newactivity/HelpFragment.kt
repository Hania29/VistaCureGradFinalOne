package com.example.vistacuregrad

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vistacuregrad.Newactivity.Adapter.TermsAdapter
import com.example.vistacuregrad.Newactivity.Model.TermsItem
import com.example.vistacuregrad.databinding.FragmentHelpBinding

class HelpFragment : Fragment(R.layout.fragment_help) {

    private var _binding: FragmentHelpBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHelpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Terms list
        val termsList = listOf(
            TermsItem(
                "• Terms and Conditions",
                "VistaCure is an AI-powered app for eye issue detection. By using this app, you agree to use the services only for lawful and personal health-related purposes."
            ),
            TermsItem(
                "• Privacy Policy",
                "We respect your privacy. VistaCure may collect limited data like eye images and detection results for analysis and improvement. Your data is stored securely and never shared without your consent."
            ),
            TermsItem(
                "• AI Limitations",
                "VistaCure includes an AI chatbot designed to provide supportive health information. It does not replace a professional medical consultation."
            ),
            TermsItem(
                "• User Responsibility",
                "You are responsible for the accuracy of the data you upload. Uploading unrelated, misleading, or harmful content is strictly prohibited."
            ),
            TermsItem(
                "• Governing Law",
                "These terms are governed by the applicable health and data laws in your region. By continuing to use VistaCure, you agree to comply with all relevant regulations."
            )
        )

        // Setup RecyclerView
        binding.recyclerViewTerms.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTerms.adapter = TermsAdapter(termsList)

        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Agree button navigates to HomeFragment

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
