package com.jeandealmeida_dev.billortest.chat.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import com.jeandealmeida_dev.billortest.chat.domain.model.MessageType
import com.jeandealmeida_dev.billortest.commons.ui.extensions.formatInSeconds
import com.jeandealmeida_dev.billortest.feature.chat.R
import com.jeandealmeida_dev.billortest.feature.chat.databinding.ItemMessageBinding

/**
 * Adapter for displaying chat messages in RecyclerView with unified layout
 */
class ChatMessageAdapter(
    private val currentUserId: String,
    private val onPlayAudioClick: (String) -> Unit
) : ListAdapter<ChatMessage, ChatMessageAdapter.MessageViewHolder>(ChatMessageDiffCallback()) {

    private var currentPlayingUrl: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val binding = ItemMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        val isPlaying = message.audioUrl == currentPlayingUrl
        holder.bind(message, currentUserId, isPlaying, onPlayAudioClick)
    }

    /**
     * Update the play/pause state for an audio message
     */
    fun updatePlaybackState(audioUrl: String, isPlaying: Boolean) {
        currentPlayingUrl = if (isPlaying) audioUrl else null
        notifyDataSetChanged()
    }

    /**
     * Update the progress for a specific audio message
     */
    fun updateProgress(audioUrl: String, progress: Int, currentTime: String) {
        val position = currentList.indexOfFirst { it.audioUrl == audioUrl }
        if (position != -1) {
            // Update specific item
            notifyItemChanged(position, AudioProgressPayload(progress, currentTime))
        }
    }

    /**
     * Reset audio UI when playback completes
     */
    fun resetAudioUI(audioUrl: String) {
        currentPlayingUrl = null
        val position = currentList.indexOfFirst { it.audioUrl == audioUrl }
        if (position != -1) {
            notifyItemChanged(position, AudioResetPayload)
        }
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads)
        } else {
            val message = getItem(position)
            for (payload in payloads) {
                when (payload) {
                    is AudioProgressPayload -> holder.updateProgress(payload.progress, payload.currentTime)
                    is AudioResetPayload -> holder.resetAudio(message)
                }
            }
        }
    }

    data class AudioProgressPayload(val progress: Int, val currentTime: String)
    object AudioResetPayload

    /**
     * Unified ViewHolder for both sent and received messages
     */
    class MessageViewHolder(private val binding: ItemMessageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(message: ChatMessage, currentUserId: String, isPlaying: Boolean, onPlayAudioClick: (String) -> Unit) {
            val isMine = message.isMine(currentUserId)
            
            // Configure message alignment and background color
            val layoutParams = binding.cardMessage.layoutParams as ConstraintLayout.LayoutParams
            if (isMine) {
                // Sent message - align to right with green background
                layoutParams.startToStart = ConstraintLayout.LayoutParams.UNSET
                layoutParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.horizontalBias = 1.0f
                binding.cardMessage.setCardBackgroundColor(
                    binding.root.context.getColor(R.color.sent_message_background)
                )
            } else {
                // Received message - align to left with white background
                layoutParams.startToStart = ConstraintLayout.LayoutParams.PARENT_ID
                layoutParams.endToEnd = ConstraintLayout.LayoutParams.UNSET
                layoutParams.horizontalBias = 0.0f
                binding.cardMessage.setCardBackgroundColor(
                    binding.root.context.getColor(R.color.received_message_background)
                )
            }
            binding.cardMessage.layoutParams = layoutParams
            
            // Show/hide sender name (only for received messages)
            if (isMine) {
                binding.textMessageName.visibility = View.GONE
            } else {
                binding.textMessageName.visibility = View.VISIBLE
                binding.textMessageName.text = message.userName
            }
            
            // Handle message type (text or audio)
            when (message.messageType) {
                MessageType.TEXT -> {
                    binding.textMessageBody.visibility = View.VISIBLE
                    binding.groupAudioPlayer.visibility = View.GONE

                    binding.textMessageBody.text = message.message
                }
                
                MessageType.AUDIO -> {
                    binding.groupAudioPlayer.visibility = View.VISIBLE
                    binding.textMessageBody.visibility = View.GONE

                    binding.textAudioDuration.text = message.audioDuration.formatInSeconds() ?: "0:00"
                    binding.progressAudio.progress = 0
                    updatePlayPauseButton(isPlaying)

                    binding.buttonPlayPauseAudio.setOnClickListener {
                        message.audioUrl?.let { url -> onPlayAudioClick(url) }
                    }
                }
            }
            
            // Set timestamp
            binding.textMessageTime.text = message.getFormattedTime()
        }

        fun updateProgress(progress: Int, currentTime: String) {
            binding.progressAudio.progress = progress
            binding.textAudioDuration.text = currentTime
        }

        fun resetAudio(message: ChatMessage) {
            binding.textAudioDuration.text = message.audioDuration.formatInSeconds() ?: "0:00"
            binding.progressAudio.progress = 0
            updatePlayPauseButton(false)
        }

        private fun updatePlayPauseButton(isPlaying: Boolean) {
            if (isPlaying) {
                binding.buttonPlayPauseAudio.setImageResource(R.drawable.ic_pause_circle_filled)
                binding.buttonPlayPauseAudio.contentDescription = 
                    binding.root.context.getString(R.string.pause_audio)
            } else {
                binding.buttonPlayPauseAudio.setImageResource(R.drawable.ic_play_circle_filled)
                binding.buttonPlayPauseAudio.contentDescription = 
                    binding.root.context.getString(R.string.play_audio)
            }
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

