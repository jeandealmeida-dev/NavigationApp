package com.jeandealmeida_dev.billortest.commons.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

class AudioPlayer(context: Context) {

    interface AudioPlayerListener {
        fun onPlaybackStateChanged(isPlaying: Boolean, audioUrl: String)
        fun onProgressUpdate(currentPosition: Long, duration: Long, audioUrl: String)
        fun onPlaybackComplete(audioUrl: String)
    }

    private val exoPlayer = ExoPlayer.Builder(context).build()
    private var listener: AudioPlayerListener? = null
    private var currentAudioUrl: String? = null
    private val progressHandler = Handler(Looper.getMainLooper())
    private var progressRunnable: Runnable? = null

    init {
        exoPlayer.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                currentAudioUrl?.let { url ->
                    listener?.onPlaybackStateChanged(isPlaying, url)
                }
                
                if (isPlaying) {
                    startProgressUpdates()
                } else {
                    stopProgressUpdates()
                }
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    currentAudioUrl?.let { url ->
                        listener?.onPlaybackComplete(url)
                    }
                    stopProgressUpdates()
                }
            }
        })
    }

    fun setListener(listener: AudioPlayerListener?) {
        this.listener = listener
    }

    fun playAudio(url: String) {
        if (currentAudioUrl == url && exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else if (currentAudioUrl == url && !exoPlayer.isPlaying) {
            exoPlayer.play()
        } else {
            currentAudioUrl = url
            val mediaItem = MediaItem.fromUri(url)
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.prepare()
            exoPlayer.play()
        }
    }

    fun release() {
        stopProgressUpdates()
        exoPlayer.release()
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressRunnable = object : Runnable {
            override fun run() {
                if (exoPlayer.isPlaying) {
                    val currentPosition = exoPlayer.currentPosition
                    val duration = exoPlayer.duration
                    currentAudioUrl?.let { url ->
                        listener?.onProgressUpdate(currentPosition, duration, url)
                    }
                    progressHandler.postDelayed(this, 100)
                }
            }
        }
        progressHandler.post(progressRunnable!!)
    }

    private fun stopProgressUpdates() {
        progressRunnable?.let {
            progressHandler.removeCallbacks(it)
        }
        progressRunnable = null
    }
}