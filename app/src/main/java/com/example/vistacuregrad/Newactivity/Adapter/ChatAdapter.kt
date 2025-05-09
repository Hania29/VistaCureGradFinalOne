package com.example.vistacuregrad.Newactivity.Adapter

// ChatAdapter.kt
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.vistacuregrad.R
import com.example.vistacuregrad.model.ChatHistoryItem

class ChatAdapter(private var items: List<ChatHistoryItem>) :
    RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    class ChatViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userMessage: TextView = view.findViewById(R.id.tv_user_message)
        val botResponse: TextView = view.findViewById(R.id.tv_bot_response)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val item = items[position]
        holder.userMessage.text = item.inputText
        holder.botResponse.text = item.responseText
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<ChatHistoryItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}