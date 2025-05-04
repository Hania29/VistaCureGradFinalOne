package com.example.vistacuregrad

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.vistacuregrad.Newactivity.Adapter.HistoryAdapter
import com.example.vistacuregrad.Repository.AuthRepository
import com.example.vistacuregrad.Viewmodel.HistoryViewModel
import com.example.vistacuregrad.Viewmodel.HistoryViewModelFactory
import com.example.vistacuregrad.databinding.FragmentHistoryBinding
import com.example.vistacuregrad.network.RetrofitClient

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: HistoryViewModel
    private val adapter = HistoryAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentHistoryBinding.bind(view)

        val repository = AuthRepository(RetrofitClient.apiService)
        val factory = HistoryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HistoryViewModel::class.java]

        // Setup RecyclerView
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())

        viewModel.historyItems.observe(viewLifecycleOwner) { items ->
            Log.d("HistoryFragment", "Observed items: ${items.size}")
            adapter.submitList(items)
        }

        // Get token from SharedPreferences
        val sharedPreferences = requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val token = sharedPreferences.getString("ORIGINAL_TOKEN", null)
        if (token.isNullOrEmpty()) {
            Toast.makeText(requireContext(), "Authentication token not found!", Toast.LENGTH_LONG).show()
        } else {
            viewModel.fetchHistory(token)
        }

        binding.bottomNavigationView.itemIconTintList = null

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            handleBottomNavigation(item)
            true
        }
    }

    private fun handleBottomNavigation(item: MenuItem) {
        val navController = findNavController()
        when (item.itemId) {
            R.id.homeFragment -> {
                navController.navigate(R.id.homeFragment)
            }
            R.id.chatBotFragment -> {
                navController.navigate(R.id.action_historyFragment_to_chatBotFragment)
            }
            R.id.historyFragment -> {
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
        binding.bottomNavigationView.menu.findItem(R.id.historyFragment).isChecked = true
    }
}