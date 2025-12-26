package com.jeandealmeida_dev.billortest

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.transition.TransitionManager
import com.jeandealmeida_dev.billortest.app.databinding.ActivityMapChatBinding
import com.jeandealmeida_dev.billortest.chat.ui.ChatFragment
import com.jeandealmeida_dev.billortest.map.ui.MapFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapChatBinding

    private var isChatExpanded = false
    private val mapFragment: MapFragment by lazy {
        MapFragment()
    }
    private val chatFragment: ChatFragment by lazy {
        ChatFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupFragments()
        setupClickListeners()
    }

    private fun setupFragments() {
        chatFragment.setNewMessageCounterListener {
            if (isChatExpanded) {
                binding.textChatCounter.visibility = View.GONE
                binding.textChatCounter.text = "0"
            } else {
                val update = binding.textChatCounter.text.toString().toInt()
                binding.textChatCounter.visibility = View.VISIBLE
                binding.textChatCounter.text = (update + 1).toString()
            }
        }
        supportFragmentManager.beginTransaction()
            .replace(binding.mapContainer.id, mapFragment)
            .commit()
    }

    private fun setupClickListeners() {
        binding.fabChat.setOnClickListener {
            binding.textChatCounter.visibility = View.GONE
            binding.textChatCounter.text = "0"
            expandChat()
        }

        binding.fabCloseChat.setOnClickListener {
            minimizeChat()
        }
    }

    private fun expandChat() {
        if (isChatExpanded) return

        isChatExpanded = true
        supportFragmentManager.beginTransaction()
            .replace(binding.chatContainer.id, chatFragment)
            .commit()

        TransitionManager.beginDelayedTransition(binding.rootLayout)

        binding.chatContainer.visibility = View.VISIBLE
        binding.fabCloseChat.visibility = View.VISIBLE
        binding.fabChat.visibility = View.GONE

        updateConstraintsForExpandedChat()
    }

    private fun minimizeChat() {
        if (!isChatExpanded) return

        isChatExpanded = false

        TransitionManager.beginDelayedTransition(binding.rootLayout)

        binding.chatContainer.visibility = View.GONE
        binding.fabCloseChat.visibility = View.GONE

        binding.fabChat.visibility = View.VISIBLE

        updateConstraintsForMinimizedChat()
    }

    private fun updateConstraintsForExpandedChat() {
        binding.rootLayout.requestLayout()
    }

    private fun updateConstraintsForMinimizedChat() {
        binding.rootLayout.requestLayout()
    }

    override fun onBackPressed() {
        if (isChatExpanded) {
            minimizeChat()
        } else {
            super.onBackPressed()
        }
    }
}
