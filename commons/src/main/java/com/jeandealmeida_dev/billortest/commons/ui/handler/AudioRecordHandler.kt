package com.jeandealmeida_dev.billortest.commons.ui.handler

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.IOException
import java.util.Timer
import java.util.TimerTask
import javax.inject.Inject

class AudioRecordHandler @Inject constructor(
    @ApplicationContext private val context: Context,
) : DefaultLifecycleObserver {

    private var mediaRecorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var startTime: Long = 0

    private var callback: AudioRecordCallback? = null
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    private var timer: Timer? = null
    private var secondsElapsed = 0

    fun startAudioRecord() {
        if (hasPermission()) {
            recording()
        } else {
            callback?.onPermissionNeeded()
        }
    }

    fun isRecording() : Boolean {
        return mediaRecorder != null
    }

    fun stopRecording(): Pair<File?, Int> {
        stopTimer()
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

    fun cancelRecording() {
        stopTimer()
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

        audioFile?.delete()
        audioFile = null
    }

    fun setCallback(callback: AudioRecordCallback) {
        this.callback = callback
    }

    fun requestPermission() {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun recording(): File? {
        try {
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
                startTimer()
            }

            return audioFile
        } catch (e: IOException) {
            e.printStackTrace()
            releaseRecorder()
            return null
        }
    }

    private fun startTimer() {
        callback?.onRecording(0)
        secondsElapsed = 0
        timer = Timer()
        timer?.schedule(object : TimerTask() {
            override fun run() {
                secondsElapsed++
                callback?.onRecording(secondsElapsed)
            }
        }, 1000, 1000)
    }

    private fun stopTimer() {
        timer?.cancel()
        timer = null
        secondsElapsed = 0
    }

    private fun releaseRecorder() {
        stopTimer()
        mediaRecorder?.release()
        mediaRecorder = null
    }

    override fun onCreate(owner: LifecycleOwner) {
        val registry = (owner as? Fragment)?.requireActivity()?.activityResultRegistry ?: return

        permissionLauncher = registry.register(
            "record_key_${owner.hashCode()}",
            owner,
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                recording()
            } else {
                callback?.onFailure(Exception("Permissão de áudio negada"))
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        releaseRecorder()
        callback = null
        permissionLauncher.unregister()
    }

    interface AudioRecordCallback {
        fun onPermissionNeeded()
        fun onRecording(secondsElapsed: Int)
        fun onFailure(exception: Exception)
    }
}