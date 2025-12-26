package com.jeandealmeida_dev.billortest

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.transition.TransitionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jeandealmeida_dev.billortest.app.R
import com.jeandealmeida_dev.billortest.chat.ui.ChatFragment
import com.jeandealmeida_dev.billortest.map.ui.MapFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MapChatActivity : AppCompatActivity() {

    private lateinit var rootLayout: ConstraintLayout
    private lateinit var mapContainer: View
    private lateinit var chatContainer: View
    private lateinit var fabChat: FloatingActionButton
    private lateinit var fabCloseChat: FloatingActionButton
    private lateinit var textChatCounter: TextView

    private var isChatExpanded = false
    private var mapFragment: MapFragment? = null
    private val chatFragment: ChatFragment by lazy {
        ChatFragment().also {
            it.setNewMessageCounterListener {
                if (isChatExpanded) {
                    textChatCounter.visibility = View.GONE
                    textChatCounter.text = "0"
                } else {
                    val update = textChatCounter.text.toString().toInt()
                    textChatCounter.visibility = View.VISIBLE
                    textChatCounter.text = (update + 1).toString()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map_chat)

        initViews()
        setupFragments()
        setupClickListeners()
    }

    private fun initViews() {
        rootLayout = findViewById(R.id.root_layout)
        mapContainer = findViewById(R.id.map_container)
        chatContainer = findViewById(R.id.chat_container)
        fabChat = findViewById(R.id.fab_chat)
        fabCloseChat = findViewById(R.id.fab_close_chat)
        textChatCounter = findViewById(R.id.text_chat_counter)
    }

    private fun setupFragments() {
        mapFragment = MapFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.map_container, mapFragment!!)
            .commit()
    }

    private fun setupClickListeners() {
        fabChat.setOnClickListener {
            textChatCounter.visibility = View.GONE
            textChatCounter.text = "0"
            expandChat()
        }

        fabCloseChat.setOnClickListener {
            minimizeChat()
        }
    }

    private fun expandChat() {
        if (isChatExpanded) return

        isChatExpanded = true
        supportFragmentManager.beginTransaction()
            .replace(R.id.chat_container, chatFragment)
            .commit()

        TransitionManager.beginDelayedTransition(rootLayout)

        chatContainer.visibility = View.VISIBLE
        fabCloseChat.visibility = View.VISIBLE
        fabChat.visibility = View.GONE

        updateConstraintsForExpandedChat()
    }

    private fun minimizeChat() {
        if (!isChatExpanded) return

        isChatExpanded = false

        TransitionManager.beginDelayedTransition(rootLayout)

        chatContainer.visibility = View.GONE
        fabCloseChat.visibility = View.GONE

        fabChat.visibility = View.VISIBLE

        updateConstraintsForMinimizedChat()
    }

    private fun updateConstraintsForExpandedChat() {
        rootLayout.requestLayout()
    }

    private fun updateConstraintsForMinimizedChat() {
        rootLayout.requestLayout()
    }

    override fun onBackPressed() {
        if (isChatExpanded) {
            minimizeChat()
        } else {
            super.onBackPressed()
        }
    }
}
