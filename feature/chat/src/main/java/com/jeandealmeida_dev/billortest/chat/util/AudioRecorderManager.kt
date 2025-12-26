package com.jeandealmeida_dev.billortest.chat.util

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File
import java.io.IOException
import javax.inject.Inject

/**
 * Manager class for handling audio recording
 */
class AudioRecorderManager @Inject constructor(
    private val context: Context
) {
    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var startTime: Long = 0
    
    /**
     * Start recording audio
     * @return The file where audio is being recorded
     */
    fun startRecording(): File? {
        try {
            // Create audio file
            val audioDir = File(context.cacheDir, "audio")
            if (!audioDir.exists()) {
                audioDir.mkdirs()
            }
            
            audioFile = File(audioDir, "audio_${System.currentTimeMillis()}.m4a")
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }
            
            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(audioFile?.absolutePath)
                
                prepare()
                start()
                startTime = System.currentTimeMillis()
            }
            
            return audioFile
        } catch (e: IOException) {
            e.printStackTrace()
            releaseRecorder()
            return null
        }
    }
    
    /**
     * Stop recording and return the recorded file
     * @return Pair of audio file and duration in seconds
     */
    fun stopRecording(): Pair<File?, Int> {
        val duration = ((System.currentTimeMillis() - startTime) / 1000).toInt()
        
        try {
            mediaRecorder?.apply {
                stop()
                reset()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            releaseRecorder()
        }
        
        return Pair(audioFile, duration)
    }
    
    /**
     * Cancel recording and delete the file
     */
    fun cancelRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                reset()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            releaseRecorder()
        }
        
        // Delete the audio file
        audioFile?.delete()
        audioFile = null
    }
    
    /**
     * Check if currently recording
     */
    fun isRecording(): Boolean {
        return mediaRecorder != null
    }
    
    /**
     * Get current recording duration in seconds
     */
    fun getRecordingDuration(): Int {
        return if (startTime > 0) {
            ((System.currentTimeMillis() - startTime) / 1000).toInt()
        } else {
            0
        }
    }
    
    private fun releaseRecorder() {
        mediaRecorder?.release()
        mediaRecorder = null
    }
}
