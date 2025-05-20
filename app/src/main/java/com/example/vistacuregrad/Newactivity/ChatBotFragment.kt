
package com.example.vistacuregrad

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vistacuregrad.Newactivity.Adapter.ChatAdapter
import com.example.vistacuregrad.databinding.FragmentChatBotBinding
import com.example.vistacuregrad.model.ChatHistoryItem
import com.example.vistacuregrad.model.ChatRequest
import com.example.vistacuregrad.network.RetrofitClient
import kotlinx.coroutines.launch

// ViewModel to hold chat state
class ChatViewModel : ViewModel() {
    var chatHistory: MutableList<ChatHistoryItem> = mutableListOf()
    var welcomeHidden: Boolean = false
    var isHistoryLoaded: Boolean = false
}

class ChatBotFragment : Fragment(R.layout.fragment_chat_bot) {

    private var _binding: FragmentChatBotBinding? = null
    private val binding get() = _binding!!

    private lateinit var chatAdapter: ChatAdapter
    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Restore state from ViewModel or savedInstanceState
        savedInstanceState?.let {
            viewModel.welcomeHidden = it.getBoolean("welcomeHidden", false)
            viewModel.isHistoryLoaded = it.getBoolean("isHistoryLoaded", false)
            // Restore chat history from saved state
            val inputTexts = it.getStringArrayList("inputTexts") ?: arrayListOf()
            val responseTexts = it.getStringArrayList("responseTexts") ?: arrayListOf()
            val timestamps = it.getStringArrayList("timestamps") ?: arrayListOf()

            // Reconstruct chatHistory list
            viewModel.chatHistory.clear()
            for (i in inputTexts.indices) {
                viewModel.chatHistory.add(
                    ChatHistoryItem(
                        inputText = inputTexts[i],
                        responseText = responseTexts.getOrNull(i) ?: "",
                        timestamp = timestamps.getOrNull(i) ?: ""
                    )
                )
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean("welcomeHidden", viewModel.welcomeHidden)
        outState.putBoolean("isHistoryLoaded", viewModel.isHistoryLoaded)
        // Save chat history as separate lists of strings
        outState.putStringArrayList("inputTexts", ArrayList(viewModel.chatHistory.map { it.inputText }))
        outState.putStringArrayList("responseTexts", ArrayList(viewModel.chatHistory.map { it.responseText }))
        outState.putStringArrayList("timestamps", ArrayList(viewModel.chatHistory.map { it.timestamp }))
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentChatBotBinding.bind(view)

        // Setup chat adapter and recycler view
        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true // Puts the last item (newest) at the bottom
        layoutManager.reverseLayout = false // Keeps the order from top (oldest) to bottom (newest)
        binding.rvChat.layoutManager = layoutManager
        chatAdapter = ChatAdapter(viewModel.chatHistory)
        binding.rvChat.adapter = chatAdapter

        // Restore UI state
        updateUIVisibility()

        // Load history only if not already loaded
        if (!viewModel.isHistoryLoaded) {
            loadChatHistory()
        } else {
            chatAdapter.updateData(viewModel.chatHistory)
            binding.rvChat.smoothScrollToPosition(viewModel.chatHistory.size - 1)
        }

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

        // Welcome message and center eye hiding when typing starts
        binding.etMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!viewModel.welcomeHidden && !s.isNullOrBlank()) {
                    viewModel.welcomeHidden = true
                    updateUIVisibility()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Send button click
        binding.btnEye.setOnClickListener {
            val message = binding.etMessage.text.toString().trim()
            if (message.isNotEmpty()) {
                viewModel.welcomeHidden = true
                updateUIVisibility()
                sendMessage(message)
                binding.etMessage.text.clear()
            } else {
                Toast.makeText(requireContext(), "Please enter a message", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUIVisibility() {
        if (viewModel.welcomeHidden) {
            binding.welcomeContainer.visibility = View.GONE
            binding.eyeTop.visibility = View.VISIBLE
            binding.rvChat.visibility = View.VISIBLE
        } else {
            binding.welcomeContainer.visibility = View.VISIBLE
            binding.eyeTop.visibility  = View.GONE
            binding.rvChat.visibility = View.GONE
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
        val token = sharedPreferences.getString("ORIGINAL_TOKEN", null)
        Log.d("ChatBotFragment", "Retrieved token: $token")
        return token
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
                    viewModel.chatHistory.clear()
                    // Reverse the API response if newest messages are first
                    viewModel.chatHistory.addAll(response.chatHistory.reversed())
                    chatAdapter.updateData(viewModel.chatHistory)
                    binding.rvChat.smoothScrollToPosition(viewModel.chatHistory.size - 1)
                    viewModel.isHistoryLoaded = true
                    // Do not hide welcome_container here; let it stay until user interacts
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
        Log.d("ChatBotFragment", "Token: $token, Message: $message")
        if (token == null) {
            Toast.makeText(requireContext(), "You need to login first", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                Log.d("ChatBotFragment", "Sending message to API...")
                val response = RetrofitClient.apiService.askChatbot("Bearer $token", ChatRequest(message))
                Log.d("ChatBotFragment", "API Response: status=${response.status}, response=${response.response}, message=${response.message}")
                if (response.status == "Success" && response.response != null) {
                    val newItem = ChatHistoryItem(
                        inputText = message,
                        responseText = response.response,
                        timestamp = ""
                    )
                    viewModel.chatHistory.add(newItem)
                    chatAdapter.updateData(viewModel.chatHistory)
                    binding.rvChat.smoothScrollToPosition(viewModel.chatHistory.size - 1)
                    updateUIVisibility()
                } else {
                    Toast.makeText(requireContext(), "Couldn't send message: ${response.message}", Toast.LENGTH_SHORT).show()
                    Log.e("ChatBotFragment", "API failed: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("ChatBotFragment", "Exception during API call: ${e.message}", e)
                Toast.makeText(requireContext(), "Couldn't send message: ${e.message}", Toast.LENGTH_SHORT).show()
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
