package com.example.vistacuregrad

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vistacuregrad.Newactivity.Adapter.ChatAdapter
import com.example.vistacuregrad.databinding.FragmentChatBotBinding
import com.example.vistacuregrad.model.ChatHistoryItem
import com.example.vistacuregrad.model.ChatRequest
import com.example.vistacuregrad.network.RetrofitClient
import kotlinx.coroutines.launch

class ChatBotFragment : Fragment(R.layout.fragment_chat_bot) {

    private var _binding: FragmentChatBotBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter
    private var chatHistory: MutableList<ChatHistoryItem> = mutableListOf()
    private var welcomeHidden = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBotBinding.bind(view)

        // Setup chat adapter and recycler view
        chatAdapter = ChatAdapter(chatHistory)
        binding.rvChat.adapter = chatAdapter
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())

        // Load history
        loadChatHistory()

        // Bottom navigation icon fix and listener
        binding.bottomNavigationView.itemIconTintList = null
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            handleBottomNavigation(item)
            true
        }

        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Welcome message hiding
        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!welcomeHidden && !s.isNullOrBlank()) {
                    binding.tvChatMessage.visibility = View.GONE
                    welcomeHidden = true
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Send button click
        binding.btnEye.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                binding.tvChatMessage.visibility = View.GONE
                welcomeHidden = true
                sendMessage(message)
                binding.etMessage.text.clear()
            } else {
                Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleBottomNavigation(item: MenuItem) {
        val navController = findNavController()
        when (item.itemId) {
            R.id.homeFragment -> {
                navController.navigate(R.id.homeFragment)
            }
            R.id.chatBotFragment -> {
                // Stay here
            }
            R.id.historyFragment -> {
                navController.navigate(R.id.action_chatBotFragment_to_historyFragment)
            }
        }
    }

    private fun getToken(): String? {
        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("ORIGINAL_TOKEN", null)
    }

    private fun loadChatHistory() {
        val token = getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "You need to login first", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getChatHistory("Bearer $token")
                if (response.status == "Success" && response.chatHistory != null) {
                    chatHistory.clear()
                    chatHistory.addAll(response.chatHistory)
                    chatAdapter.updateData(chatHistory)
                    binding.rvChat.scrollToPosition(chatHistory.size - 1)
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to load chat history", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendMessage(message: String) {
        val token = getToken()
        if (token == null) {
            Toast.makeText(requireContext(), "You need to login first", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.askChatbot("Bearer $token", ChatRequest(message))
                if (response.status == "Success" && response.response != null) {
                    val newItem = ChatHistoryItem(
                        inputText = message,
                        responseText = response.response,
                        timestamp = ""
                    )
                    chatHistory.add(newItem)
                    chatAdapter.updateData(chatHistory)
                    binding.rvChat.scrollToPosition(chatHistory.size - 1)
                } else {
                    Toast.makeText(requireContext(), response.message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Failed to send message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.bottomNavigationView.menu.findItem(R.id.chatBotFragment).isChecked = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
