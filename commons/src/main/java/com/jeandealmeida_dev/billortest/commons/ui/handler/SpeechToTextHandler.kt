package com.jeandealmeida_dev.billortest.commons.ui.handler

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import javax.inject.Inject

class SpeechToTextHandler @Inject constructor(
    @ApplicationContext private val context: Context,
) : DefaultLifecycleObserver {

    private var callback: SpeechToTextCallback? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    fun setCallback(callback: SpeechToTextCallback) {
        this.callback = callback
    }

    fun startSpeechFlow() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            callback?.onFailure(Exception("Reconhecimento de voz não disponível neste dispositivo"))
            return
        }

        if (hasPermission()) {
            startListeningInternal()
        } else {
            callback?.onPermissionNeeded()
        }
    }

    fun requestPermission() {
        permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startListeningInternal() {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(recognitionListener)
            }
        }

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }

        speechRecognizer?.startListening(intent)
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                callback?.onSuccess(matches[0])
            }
        }

        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Erro de áudio"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Permissões insuficientes"
                SpeechRecognizer.ERROR_NETWORK -> "Erro de rede"
                SpeechRecognizer.ERROR_NO_MATCH -> "Nenhuma fala reconhecida"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Serviço ocupado"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Tempo de fala esgotado"
                else -> "Erro desconhecido: $error"
            }
            callback?.onFailure(Exception(errorMessage))
        }

        override fun onReadyForSpeech(params: Bundle?) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() {}
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    override fun onCreate(owner: LifecycleOwner) {
        val registry = (owner as? Fragment)?.requireActivity()?.activityResultRegistry ?: return

        permissionLauncher = registry.register(
            "speech_key_${owner.hashCode()}",
            owner,
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                startListeningInternal()
            } else {
                callback?.onFailure(Exception("Permissão de áudio negada"))
            }
        }
    }

    override fun onDestroy(owner: LifecycleOwner) {
        speechRecognizer?.destroy()
        speechRecognizer = null
        callback = null
    }

    interface SpeechToTextCallback {
        fun onPermissionNeeded()
        fun onSuccess(recognizedText: String)
        fun onFailure(exception: Exception)
    }
}