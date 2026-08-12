package com.stonicai.app.data.llm

import com.stonicai.app.data.ApiKeys
import com.stonicai.app.data.Models.Model
import kotlinx.coroutines.flow.Flow

interface LlmClient {
    /**
     * Streams partial assistant text for the given conversation.
     * Implementations must cancel promptly when the collector's coroutine is cancelled.
     */
    fun stream(
        model: Model,
        @Suppress("UNUSED_PARAMETER") apiKey: String,
        systemPrompt: String,
        history: List<ChatMessage>
    ): Flow<String>

    data class ChatMessage(val role: Role, val content: String) {
        enum class Role { USER, ASSISTANT, SYSTEM }
    }
}

class MissingApiKeyException(provider: String) :
    RuntimeException("No API key configured for $provider. Add it in Settings → API Keys.")

object LlmClients {
    fun forModel(model: Model, keys: ApiKeys): LlmClient {
        val key = when (model.provider) {
            "openai" -> keys.openai
            "anthropic" -> keys.anthropic
            "google" -> keys.google
            "groq" -> keys.groq
            else -> ""
        }
        if (key.isBlank()) throw MissingApiKeyException(model.provider)
        return when (model.provider) {
            "openai" -> OpenAiClient("https://api.openai.com/v1/chat/completions", key)
            "groq" -> OpenAiClient("https://api.groq.com/openai/v1/chat/completions", key)
            "anthropic" -> AnthropicClient(key)
            "google" -> GoogleAiClient(key)
            else -> throw IllegalArgumentException("Unsupported provider: ${model.provider}")
        }
    }
}
