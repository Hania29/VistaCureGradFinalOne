package com.example.vistacuregrad.Newactivity.Adapter

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vistacuregrad.databinding.ItemHistoryBinding
import com.example.vistacuregrad.model.HistoryItem

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {
    private var items = listOf<HistoryItem>()

    fun submitList(newItems: List<HistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    inner class HistoryViewHolder(val binding: ItemHistoryBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val item = items[position]
        // Decode base64 image
        val imageBytes = Base64.decode(item.image.substringAfter(","), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        holder.binding.imageView.setImageBitmap(bitmap)
        holder.binding.diseaseName.text = "Disease: ${item.diseaseName}"
        holder.binding.probability.text = "Probability: ${item.probability}"
        holder.binding.predictionDate.text = item.predictionDate
    }

    override fun getItemCount() = items.size
}