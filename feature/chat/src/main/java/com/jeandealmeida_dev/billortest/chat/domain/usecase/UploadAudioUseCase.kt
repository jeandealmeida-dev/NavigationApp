package com.jeandealmeida_dev.billortest.chat.domain.usecase

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.jeandealmeida_dev.billortest.chat.domain.ChatException
import kotlinx.coroutines.tasks.await
import java.io.File
import java.util.UUID
import javax.inject.Inject

/**
 * Use case for uploading audio files to Firebase Storage
 */
class UploadAudioUseCase @Inject constructor() {

    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    /**
     * Upload audio file to Firebase Storage
     * @param audioFile The audio file to upload
     * @return Single that emits the download URL of the uploaded file
     */
    suspend operator fun invoke(audioFile: File): String {
        try {
            if (!audioFile.exists() || audioFile.length() == 0L) {
                throw ChatException.AudioSentException("Arquivo de áudio inválido ou vazio")
            }

            val fileName = "audio_${UUID.randomUUID()}.m4a"
            val audioRef = storageRef.child("audios/$fileName")

            // Upload audio
            val uploadTask = audioRef.putFile(Uri.fromFile(audioFile))

            val snapshot = uploadTask.await()

            // Get audio url
            val downloadUri = snapshot.metadata?.reference?.downloadUrl?.await()

            return downloadUri.toString()
        } catch (exception: Exception) {
            throw ChatException.AudioSentException(exception.message ?: "Erro ao enviar")
        }
    }
}
