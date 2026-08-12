package com.stonicai.app.data.llm

import com.stonicai.app.data.Models
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

/**
 * Streaming client for OpenAI-compatible Chat Completions endpoints.
 * Works for OpenAI and Groq (same wire format).
 */
class OpenAiClient(
    private val endpoint: String,
    private val apiKey: String
) : LlmClient {

    override fun stream(
        model: Models.Model,
        @Suppress("UNUSED_PARAMETER") ignoredKey: String,
        systemPrompt: String,
        history: List<LlmClient.ChatMessage>
    ): Flow<String> = flow {
        val messages = JSONArray()
        if (systemPrompt.isNotBlank()) {
            messages.put(JSONObject().put("role", "system").put("content", systemPrompt))
        }
        history.forEach { m ->
            val role = when (m.role) {
                LlmClient.ChatMessage.Role.USER -> "user"
                LlmClient.ChatMessage.Role.ASSISTANT -> "assistant"
                LlmClient.ChatMessage.Role.SYSTEM -> "system"
            }
            messages.put(JSONObject().put("role", role).put("content", m.content))
        }

        val payload = JSONObject()
            .put("model", model.apiModel)
            .put("stream", true)
            .put("temperature", 0.7)
            .put("messages", messages)
            .toString()

        val request = Request.Builder()
            .url(endpoint)
            .header("Authorization", "Bearer $apiKey")
            .header("Accept", "text/event-stream")
            .jsonBody(payload)
            .build()

        sseFlow(request).collect { data ->
            val obj = JSONObject(data)
            val choices = obj.optJSONArray("choices") ?: return@collect
            if (choices.length() == 0) return@collect
            val delta = choices.getJSONObject(0).optJSONObject("delta") ?: return@collect
            val piece = delta.optString("content")
            if (!piece.isNullOrEmpty()) emit(piece)
        }
    }
}
