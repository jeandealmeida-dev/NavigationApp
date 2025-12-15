package com.jeandealmeida_dev.billortest

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.transition.TransitionManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jeandealmeida_dev.billortest.app.R
import com.jeandealmeida_dev.billortest.chat.ui.ChatFragment
import com.jeandealmeida_dev.billortest.map.ui.MapFragment
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity for tablets that integrates maps and chat on the same screen.
 * 
 * Features:
 * - Map fills the screen initially
 * - FAB to open chat (when minimized)
 * - Chat appears as a column on the right side when opened (tablets)
 * - Chat overlays the map on phones
 * - Close button to minimize chat back to FAB
 */
@AndroidEntryPoint
class MapChatActivity : AppCompatActivity() {

    private lateinit var rootLayout: ConstraintLayout
    private lateinit var mapContainer: View
    private lateinit var chatContainer: View
    private lateinit var fabChat: FloatingActionButton
    private lateinit var fabCloseChat: FloatingActionButton
    
    private var isChatExpanded = false
    private var mapFragment: MapFragment? = null
    private var chatFragment: ChatFragment? = null

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
    }

    private fun setupFragments() {
        // Add MapFragment if not already added
//        if (savedInstanceState == null) {
            mapFragment = MapFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.map_container, mapFragment!!)
                .commit()
//        } else {
//            mapFragment = supportFragmentManager.findFragmentById(R.id.map_container) as? MapFragment
        //}
    }

    private fun setupClickListeners() {
        fabChat.setOnClickListener {
            expandChat()
        }

        fabCloseChat.setOnClickListener {
            minimizeChat()
        }
    }

    private fun expandChat() {
        if (isChatExpanded) return

        isChatExpanded = true

        // Add ChatFragment if not already added
        if (chatFragment == null) {
            chatFragment = ChatFragment()
            supportFragmentManager.beginTransaction()
                .replace(R.id.chat_container, chatFragment!!)
                .commit()
        }

        // Animate the transition
        TransitionManager.beginDelayedTransition(rootLayout)

        // Show chat container and close button
        chatContainer.visibility = View.VISIBLE
        fabCloseChat.visibility = View.VISIBLE

        // Hide the FAB
        fabChat.visibility = View.GONE

        // Update constraints for tablet layout
        updateConstraintsForExpandedChat()
    }

    private fun minimizeChat() {
        if (!isChatExpanded) return

        isChatExpanded = false

        // Animate the transition
        TransitionManager.beginDelayedTransition(rootLayout)

        // Hide chat container and close button
        chatContainer.visibility = View.GONE
        fabCloseChat.visibility = View.GONE

        // Show the FAB
        fabChat.visibility = View.VISIBLE

        // Update constraints for minimized state
        updateConstraintsForMinimizedChat()
    }

    private fun updateConstraintsForExpandedChat() {
        // On tablets (w600dp), the constraint changes are handled by the layout
        // The chat_container already has width_percent="0.35" and proper constraints
        // On phones, chat overlays the entire screen (handled by layout)
        
        // Request layout update
        rootLayout.requestLayout()
    }

    private fun updateConstraintsForMinimizedChat() {
        // On minimized state, map takes full width
        // The layout handles this automatically when chat_container is GONE
        
        // Request layout update
        rootLayout.requestLayout()
    }

    override fun onBackPressed() {
        if (isChatExpanded) {
            // If chat is expanded, minimize it instead of closing the activity
            minimizeChat()
        } else {
            super.onBackPressed()
        }
    }
}
