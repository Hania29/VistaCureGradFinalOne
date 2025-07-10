package com.example.vistacuregrad.Newactivity.Adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.vistacuregrad.Newactivity.Model.TermsItem
import com.example.vistacuregrad.databinding.ItemTermsBinding

class TermsAdapter(private val items: List<TermsItem>) :
    RecyclerView.Adapter<TermsAdapter.TermsViewHolder>() {

    inner class TermsViewHolder(private val binding: ItemTermsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TermsItem) {
            binding.tvTitle.text = item.title
            binding.tvContent.text = item.content
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TermsViewHolder {
        val binding = ItemTermsBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TermsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TermsViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size
}
