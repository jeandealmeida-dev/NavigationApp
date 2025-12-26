package com.jeandealmeida_dev.billortest.chat.ui

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeandealmeida_dev.billortest.chat.domain.ChatException
import com.jeandealmeida_dev.billortest.chat.domain.model.ChatMessage
import com.jeandealmeida_dev.billortest.chat.ui.adapter.ChatMessageAdapter
import com.jeandealmeida_dev.billortest.chat.ui.viewmodel.ChatViewModel
import com.jeandealmeida_dev.billortest.chat.ui.viewmodel.ChatViewState
import com.jeandealmeida_dev.billortest.commons.audio.AudioPlayer
import com.jeandealmeida_dev.billortest.commons.ui.extensions.getDeviceID
import com.jeandealmeida_dev.billortest.commons.ui.handler.AudioRecordHandler
import com.jeandealmeida_dev.billortest.feature.chat.R
import com.jeandealmeida_dev.billortest.feature.chat.databinding.FragmentChatBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * Fragment for displaying and sending chat messages
 */
@AndroidEntryPoint
class ChatFragment : Fragment() {

    private val viewModel: ChatViewModel by viewModels()
    private var adapter: ChatMessageAdapter? = null
    private var audioPlayer: AudioPlayer? = null

    private var viewBinding: FragmentChatBinding? = null

    // Listeners
    private var newMessageCounterListener: (() -> Unit)? = null

    @Inject
    lateinit var recordAudioHandler: AudioRecordHandler

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = FragmentChatBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: as a demo app (no auth), we are considering MEI as an userID
        viewModel.setCurrentUser(
            userId = requireContext().getDeviceID()
        )

        setupAudioRecordHandler()
        setupAudioPlayer()
        setupRecyclerView()
        setupMessageView()
        setupSendButton()
        setupRecordButton()
        observeViewModel()

        viewModel.start()
    }

    private fun setupAudioRecordHandler() {
        lifecycle.addObserver(recordAudioHandler)
        recordAudioHandler.setCallback(object : AudioRecordHandler.AudioRecordCallback {
            override fun onPermissionNeeded() {
                recordAudioHandler.requestPermission()
            }

            override fun onRecording(secondsElapsed: Int) {
                lifecycleScope.launch(Dispatchers.Main) {
                    updateAudioRecordTimer(secondsElapsed)
                }
            }

            override fun onFailure(exception: Exception) {
                exception.printStackTrace()
            }
        })

    }

    private fun setupAudioPlayer() {
        audioPlayer = AudioPlayer(requireContext()).apply {
            setListener(object : AudioPlayer.AudioPlayerListener {
                override fun onPlaybackStateChanged(isPlaying: Boolean, audioUrl: String) {
                    adapter?.updatePlaybackState(audioUrl, isPlaying)
                }

                override fun onProgressUpdate(currentPosition: Long, duration: Long, audioUrl: String) {
                    if (duration > 0) {
                        val progress = ((currentPosition.toFloat() / duration.toFloat()) * 100).toInt()
                        val currentTimeFormatted = formatMillisToTime(currentPosition)
                        adapter?.updateProgress(audioUrl, progress, currentTimeFormatted)
                    }
                }

                override fun onPlaybackComplete(audioUrl: String) {
                    adapter?.resetAudioUI(audioUrl)
                }
            })
        }
    }

    private fun formatMillisToTime(millis: Long): String {
        val totalSeconds = (millis / 1000).toInt()
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%d:%02d", minutes, seconds)
    }

    private fun setupRecyclerView() {
        adapter = ChatMessageAdapter(viewModel.getCurrentUserId()) {
            audioPlayer?.playAudio(url = it)
        }

        viewBinding?.recyclerMessages?.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // Start from bottom
            }
            adapter = this@ChatFragment.adapter
        }
    }

    private fun setupMessageView() {
        viewBinding?.apply {
            editMessage.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(p0: Editable?) {

                }

                override fun beforeTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                }

                override fun onTextChanged(
                    p0: CharSequence?,
                    p1: Int,
                    p2: Int,
                    p3: Int
                ) {
                    val text = p0.toString()

                    buttonSend.isInvisible = text.isBlank()
                    buttonAudio.isInvisible = text.isNotBlank()

                    if (text.isNotBlank()) {
                        viewModel.sendTypingStatus()
                    }
                }
            })
        }
    }

    private fun setupSendButton() {
        viewBinding?.apply {
            buttonSend.setOnClickListener {
                val messageText = editMessage.text.toString()
                if (messageText.isNotBlank()) {
                    viewModel.sendMessage(messageText)
                    editMessage.setText("")
                }
            }
        }
    }

    private fun setupRecordButton() {
        viewBinding?.apply {
            buttonAudio.setOnClickListener {
                if (recordAudioHandler.isRecording()) {
                    val pair = recordAudioHandler.stopRecording()
                    textRecordingTime.visibility = View.GONE
                    viewModel.sendAudioMessage(pair.first, pair.second)
                } else {
                    recordAudioHandler.startAudioRecord()
                }
            }
        }
    }

    private fun updateAudioRecordTimer(secondsElapsed: Int) {
        val minutes = secondsElapsed / 60
        val seconds = secondsElapsed % 60

        viewBinding?.apply {
            textRecordingTime.text =
                String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
            textRecordingTime.visibility = View.VISIBLE
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { viewState ->
                    render(viewState)
                }
            }
        }
    }

    private fun render(state: ChatViewState) {
        when (state) {
            is ChatViewState.Idle -> {}
            is ChatViewState.Loading -> showLoading(true)
            is ChatViewState.Messages -> {
                showLoading(false)
                showMessages(state.messages)
            }

            is ChatViewState.NewMessage -> {
                newMessageCounterListener?.invoke()
                newMessage(state.message)
            }

            is ChatViewState.Error -> {
                showLoading(false)
                handleError(state.exception)
            }

            is ChatViewState.Typing -> {
                showUserTyping(state.usersTyping)
            }

            is ChatViewState.MessageSent -> {
                // Message sent successfully, show a confirmation
            }

            else -> {}
        }
    }

    private fun showLoading(isLoading: Boolean) {
        viewBinding?.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            buttonSend.isEnabled = !isLoading
        }
    }

    private fun showMessages(messages: List<ChatMessage>) {
        adapter?.submitList(messages) {
            if (messages.isNotEmpty()) {
                viewBinding?.recyclerMessages?.smoothScrollToPosition(messages.size - 1)
            }
        }
    }

    private fun showUserTyping(users: List<String>) {
        viewBinding?.apply {
            textUserTyping.isVisible = users.isNotEmpty()
            textUserTyping.text = getString(R.string.typing).format(users.joinToString(","))
        }
    }

    private fun newMessage(message: ChatMessage) {
        val currentAdapter = adapter ?: return

        val history = currentAdapter.currentList.toMutableList()
        val updated = history + message

        adapter?.submitList(updated) {
            viewBinding?.recyclerMessages?.smoothScrollToPosition(updated.size - 1)
        }
    }

    private fun handleError(exception: ChatException) {
        when (exception) {
            is ChatException.EmptyMessageException -> {
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
            }

            else -> {
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                Log.e("ChatFragment", "Error: ${exception.message}")
                exception.printStackTrace()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        audioPlayer?.release()
        audioPlayer = null
        newMessageCounterListener = null
    }

    fun setNewMessageCounterListener(function: () -> Unit) {
        newMessageCounterListener = function
    }
}
