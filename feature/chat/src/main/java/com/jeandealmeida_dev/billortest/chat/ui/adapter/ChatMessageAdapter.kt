package com.jeandealmeida_dev.billortest.chat.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import com.jeandealmeida_dev.billortest.feature.chat.R

/**
 * Adapter for displaying chat messages in RecyclerView
 */
class ChatMessageAdapter(
    private val currentUserId: String
) : ListAdapter<ChatMessage, RecyclerView.ViewHolder>(ChatMessageDiffCallback()) {
    
    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
    }
    
    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.isMine(currentUserId)) {
            VIEW_TYPE_MESSAGE_SENT
        } else {
            VIEW_TYPE_MESSAGE_RECEIVED
        }
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_sent, parent, false)
            SentMessageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_message_received, parent, false)
            ReceivedMessageViewHolder(view)
        }
    }
    
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }
    
    /**
     * ViewHolder for sent messages (current user)
     */
    class SentMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.text_message_body)
        private val timeTextView: TextView = itemView.findViewById(R.id.text_message_time)
        
        fun bind(message: ChatMessage) {
            messageTextView.text = message.message
            timeTextView.text = message.getFormattedTime()
        }
    }
    
    /**
     * ViewHolder for received messages (other users)
     */
    class ReceivedMessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageTextView: TextView = itemView.findViewById(R.id.text_message_body)
        private val nameTextView: TextView = itemView.findViewById(R.id.text_message_name)
        private val timeTextView: TextView = itemView.findViewById(R.id.text_message_time)
        
        fun bind(message: ChatMessage) {
            messageTextView.text = message.message
            nameTextView.text = message.userName
            timeTextView.text = message.getFormattedTime()
        }
    }
    
    /**
     * DiffUtil callback for efficient list updates
     */
    class ChatMessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}
