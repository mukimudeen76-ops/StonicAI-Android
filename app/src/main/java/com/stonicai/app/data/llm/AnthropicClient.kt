package com.stonicai.app.data.llm

import com.stonicai.app.data.Models
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class AnthropicClient(private val apiKey: String) : LlmClient {

    override fun stream(
        model: Models.Model,
        apiKey: String,
        systemPrompt: String,
        history: List<LlmClient.ChatMessage>
    ): Flow<String> = flow {
        val messages = JSONArray()
        history.forEach { m ->
            val role = if (m.role == LlmClient.ChatMessage.Role.USER) "user" else "assistant"
            messages.put(
                JSONObject()
                    .put("role", role)
                    .put("content", m.content)
            )
        }

        val payload = JSONObject()
            .put("model", model.apiModel)
            .put("max_tokens", 4096)
            .put("stream", true)
            .put("system", systemPrompt)
            .put("messages", messages)
            .toString()

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header("x-api-key", this@AnthropicClient.apiKey)
            .header("anthropic-version", "2023-06-01")
            .header("Accept", "text/event-stream")
            .jsonBody(payload)
            .build()

        sseFlow(request).collect { data ->
            val obj = JSONObject(data)
            when (obj.optString("type")) {
                "content_block_delta" -> {
                    val delta = obj.optJSONObject("delta") ?: return@collect
                    val piece = delta.optString("text")
                    if (!piece.isNullOrEmpty()) emit(piece)
                }
                "error" -> {
                    val err = obj.optJSONObject("error")?.optString("message") ?: "Anthropic error"
                    throw RuntimeException(err)
                }
            }
        }
    }
}
