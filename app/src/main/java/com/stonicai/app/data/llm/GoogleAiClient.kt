package com.stonicai.app.data.llm

import com.stonicai.app.data.Models
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

/**
 * Uses Google's `streamGenerateContent` SSE endpoint. The endpoint responds with
 * a stream of JSON arrays (one per chunk), each containing candidates with text.
 */
class GoogleAiClient(private val apiKey: String) : LlmClient {

    override fun stream(
        model: Models.Model,
        apiKey: String,
        systemPrompt: String,
        history: List<LlmClient.ChatMessage>
    ): Flow<String> = flow {
        val contents = JSONArray()
        if (systemPrompt.isNotBlank()) {
            contents.put(
                JSONObject().put("role", "user")
                    .put("parts", JSONArray().put(JSONObject().put("text", systemPrompt)))
            )
            contents.put(
                JSONObject().put("role", "model")
                    .put("parts", JSONArray().put(JSONObject().put("text", "Understood.")))
            )
        }
        history.forEach { m ->
            val role = if (m.role == LlmClient.ChatMessage.Role.USER) "user" else "model"
            contents.put(
                JSONObject().put("role", role)
                    .put("parts", JSONArray().put(JSONObject().put("text", m.content)))
            )
        }

        val payload = JSONObject().put("contents", contents).toString()

        val url = ("https://generativelanguage.googleapis.com/v1beta/models/" +
            "${model.apiModel}:streamGenerateContent")
            .toHttpUrl().newBuilder()
            .addQueryParameter("alt", "sse")
            .addQueryParameter("key", this@GoogleAiClient.apiKey)
            .build()

        val request = Request.Builder()
            .url(url)
            .header("Accept", "text/event-stream")
            .jsonBody(payload)
            .build()

        sseFlow(request).collect { data ->
            // Each SSE `data` is a JSON array with one GenerateContentResponse.
            val arr = JSONArray(data)
            for (i in 0 until arr.length()) {
                val item = arr.getJSONObject(i)
                val candidates = item.optJSONArray("candidates") ?: continue
                if (candidates.length() == 0) continue
                val content = candidates.getJSONObject(0).optJSONObject("content") ?: continue
                val parts = content.optJSONArray("parts") ?: continue
                for (j in 0 until parts.length()) {
                    val text = parts.getJSONObject(j).optString("text")
                    if (!text.isNullOrEmpty()) emit(text)
                }
            }
        }
    }
}
