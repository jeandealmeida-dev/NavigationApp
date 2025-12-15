package com.jeandealmeida_dev.billortest.chat.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jeandealmeida_dev.billortest.chat.ui.adapter.ChatMessageAdapter
import com.jeandealmeida_dev.billortest.chat.ui.viewmodel.ChatViewModel
import com.jeandealmeida_dev.billortest.feature.chat.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Fragment for displaying and sending chat messages
 */
@AndroidEntryPoint
class ChatFragment : Fragment() {
    
    private val viewModel: ChatViewModel by viewModels()
    private lateinit var adapter: ChatMessageAdapter
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var progressBar: ProgressBar
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)
        
        // Initialize views
        recyclerView = view.findViewById(R.id.recycler_messages)
        messageEditText = view.findViewById(R.id.edit_message)
        sendButton = view.findViewById(R.id.button_send)
        progressBar = view.findViewById(R.id.progress_bar)
        
        return view
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupSendButton()
        observeViewModel()
    }
    
    private fun setupRecyclerView() {
        adapter = ChatMessageAdapter(viewModel.getCurrentUserId())
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // Start from bottom
            }
            adapter = this@ChatFragment.adapter
        }
    }
    
    private fun setupSendButton() {
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString()
            if (messageText.isNotBlank()) {
                viewModel.sendMessage(messageText)
                messageEditText.setText("")
            }
        }
    }
    
    private fun observeViewModel() {
        // Observe messages
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.submitList(messages) {
                // Scroll to bottom when new messages arrive
                if (messages.isNotEmpty()) {
                    recyclerView.smoothScrollToPosition(messages.size - 1)
                }
            }
        }
        
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            sendButton.isEnabled = !isLoading
        }
        
        // Observe errors
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }
        
        // Observe message sent confirmation
        viewModel.messageSent.observe(viewLifecycleOwner) { sent ->
            if (sent == true) {
                // Message sent successfully, maybe show a confirmation
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        // ViewModel will handle cleanup in onCleared()
    }
}
