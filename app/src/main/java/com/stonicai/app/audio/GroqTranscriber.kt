package com.stonicai.app.audio

import com.stonicai.app.data.llm.Http
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File

/**
 * Transcribes an audio file via Groq's Whisper endpoint.
 * Optional — if no Groq key is configured, the app falls back to on-device
 * Android SpeechRecognizer (free, no API key).
 */
class GroqTranscriber(private val apiKey: String) {

    suspend fun transcribe(file: File): String? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) return@withContext null

        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("model", "whisper-large-v3")
            .addFormDataPart("response_format", "json")
            .addFormDataPart(
                "file", file.name,
                file.asRequestBody("audio/mp4".toMediaType())
            )
            .build()

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/audio/transcriptions")
            .header("Authorization", "Bearer $apiKey")
            .post(body)
            .build()

        Http.client.newCall(request).execute().use { resp ->
            val text = resp.body?.string().orEmpty()
            if (!resp.isSuccessful) return@use null
            JSONObject(text).optString("text").takeIf { it.isNotBlank() }
        }
    }
}
