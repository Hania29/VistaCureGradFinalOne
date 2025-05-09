package com.example.vistacuregrad

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.Viewmodel.ChatBotViewModel
import com.example.vistacuregrad.Viewmodel.ChatBotViewModelFactory
import com.example.vistacuregrad.databinding.FragmentChatBotBinding
import com.example.vistacuregrad.network.RetrofitClient

class ChatBotFragment : Fragment(R.layout.fragment_chat_bot) {

    private var _binding: FragmentChatBotBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatBotViewModel

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBotBinding.bind(view)

        val repository = AuthRepository(RetrofitClient.apiService)
        val factory = ChatBotViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[ChatBotViewModel::class.java]

        binding.bottomNavigationView.itemIconTintList = null

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            handleBottomNavigation(item)
            true
        }

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Get token from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("ORIGINAL_TOKEN", null)

        // Fetch chat history when fragment is created (if token is available)
        if (!token.isNullOrBlank()) {
            viewModel.fetchChatHistory(token)
        }

        binding.btnEye.setOnClickListener {
            val message = binding.etMessage.text.toString()
            val token = sharedPreferences.getString("ORIGINAL_TOKEN", null)
            if (message.isNotBlank() && !token.isNullOrBlank()) {
                viewModel.sendMessage(token, message)
                binding.etMessage.text.clear()
            } else {
                Toast.makeText(requireContext(), "Please enter a message and ensure you are logged in", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.chatResponse.observe(viewLifecycleOwner, Observer { response ->
            if (response.status == "Success") {
                binding.tvChatMessage.text = response.response
                // Optionally, refresh chat history after sending a message
                val token = sharedPreferences.getString("ORIGINAL_TOKEN", null)
                if (!token.isNullOrBlank()) {
                    viewModel.fetchChatHistory(token)
                }
            } else {
                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.chatHistory.observe(viewLifecycleOwner, Observer { response ->
            if (response.status == "Success" && response.chatHistory != null) {
                // For now, just show a Toast with the number of history items
                Toast.makeText(requireContext(), "History loaded: ${response.chatHistory.size} items", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
            }
        })

        viewModel.loading.observe(viewLifecycleOwner, Observer { isLoading ->
            // Optionally show/hide a progress bar
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })
    }

    private fun handleBottomNavigation(item: MenuItem) {
        val navController = findNavController()
        when (item.itemId) {
            R.id.homeFragment -> {
                navController.navigate(R.id.homeFragment)
                binding.bottomNavigationView.menu.findItem(R.id.homeFragment).isChecked = true
            }
            R.id.chatBotFragment -> {
                binding.bottomNavigationView.menu.findItem(R.id.chatBotFragment).isChecked = true
            }
            R.id.historyFragment -> {
                navController.navigate(R.id.action_chatBotFragment_to_historyFragment)
                binding.bottomNavigationView.menu.findItem(R.id.historyFragment).isChecked = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigationView.menu.findItem(R.id.chatBotFragment).isChecked = true
    }
}