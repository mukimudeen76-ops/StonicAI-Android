package com.stonicai.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stonicai.app.data.ChatMessage
import com.stonicai.app.data.MessageStatus
import com.stonicai.app.data.Models
import com.stonicai.app.data.Sender
import com.stonicai.app.data.SettingsRepository
import com.stonicai.app.data.StonicSettings
import com.stonicai.app.data.llm.LlmClient
import com.stonicai.app.data.llm.LlmClients
import com.stonicai.app.data.llm.MissingApiKeyException
import com.stonicai.app.tts.StonicTts
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChatViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = ServiceLocator.repo(app)
    private val tts = ServiceLocator.tts(app)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _settings = MutableStateFlow(StonicSettings())
    val settings: StateFlow<StonicSettings> = _settings.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private var streamJob: Job? = null

    init {
        viewModelScope.launch {
            repo.settings.collect { _settings.value = it }
        }
    }

    fun send(text: String) {
        val content = text.trim()
        if (content.isEmpty() || _isStreaming.value) return

        val userMsg = ChatMessage(sender = Sender.USER, text = content, status = MessageStatus.DONE)
        val aiId = java.util.UUID.randomUUID().toString()
        val aiMsg = ChatMessage(id = aiId, sender = Sender.AI, text = "", status = MessageStatus.STREAMING)
        _messages.value = _messages.value + userMsg + aiMsg
        _isStreaming.value = true

        streamJob = viewModelScope.launch {
            val s = _settings.value
            val model = Models.byId(s.selectedModelId)
            try {
                if (!hasKeyFor(model)) throw MissingApiKeyException(model.provider)

                val history = buildHistoryForApi()
                val client = LlmClients.forModel(model, s.keys)
                val system = s.systemPrompt.ifBlank {
                    "You are Stonic, a helpful AI assistant on Android. Reply concisely in Markdown, matching the user's language."
                }

                val sb = StringBuilder()
                client.stream(model, s.keys.openai, system, history).collect { piece ->
                    sb.append(piece)
                    updateAi(aiId) { it.copy(text = sb.toString()) }
                }

                updateAi(aiId) { it.copy(status = MessageStatus.DONE) }
                if (s.ttsEnabled) tts.speak(sb.toString())
            } catch (e: MissingApiKeyException) {
                updateAi(aiId) {
                    it.copy(
                        status = MessageStatus.ERROR,
                        text = "⚠️ ${e.message}\n\nOpen **Settings → API Keys** and add your key for ${model.provider}. " +
                            "You can use any of: OpenAI, Anthropic, Google AI Studio, or Groq (free tier)."
                    )
                }
            } catch (e: Exception) {
                updateAi(aiId) {
                    it.copy(
                        status = MessageStatus.ERROR,
                        text = "⚠️ Request failed: ${e.message ?: e.javaClass.simpleName}"
                    )
                }
            } finally {
                _isStreaming.value = false
            }
        }
    }

    fun stop() {
        streamJob?.cancel()
        _isStreaming.value = false
        _messages.value = _messages.value.map {
            if (it.status == MessageStatus.STREAMING) it.copy(status = MessageStatus.DONE) else it
        }
    }

    fun selectModel(id: String) {
        viewModelScope.launch { repo.save(modelId = id) }
    }

    fun clear() {
        streamJob?.cancel()
        _isStreaming.value = false
        _messages.value = emptyList()
    }

    fun speakLast() {
        val last = _messages.value.lastOrNull { it.sender == Sender.AI && it.text.isNotBlank() } ?: return
        tts.speak(last.text)
    }

    fun stopSpeaking() = tts.stop()

    private fun hasKeyFor(model: Models.Model): Boolean {
        val k = _settings.value.keys
        return when (model.provider) {
            "openai" -> k.openai.isNotBlank()
            "anthropic" -> k.anthropic.isNotBlank()
            "google" -> k.google.isNotBlank()
            "groq" -> k.groq.isNotBlank()
            else -> false
        }
    }

    private fun buildHistoryForApi(): List<LlmClient.ChatMessage> {
        return _messages.value
            .filter { it.sender == Sender.USER || it.status == MessageStatus.DONE }
            .dropLast(1) // drop the empty streaming AI placeholder
            .map {
                LlmClient.ChatMessage(
                    role = if (it.sender == Sender.USER) LlmClient.ChatMessage.Role.USER
                    else LlmClient.ChatMessage.Role.ASSISTANT,
                    content = it.text
                )
            }
    }

    private fun updateAi(id: String, block: (ChatMessage) -> ChatMessage) {
        _messages.value = _messages.value.map { if (it.id == id) block(it) else it }
    }

    override fun onCleared() {
        super.onCleared()
        tts.shutdown()
    }
}

/**
 * Tiny service locator to avoid pulling in a full DI framework.
 * Repo and TTS are app-scoped singletons.
 */
object ServiceLocator {
    @Volatile private var repo: SettingsRepository? = null
    @Volatile private var tts: StonicTts? = null

    fun repo(app: Application): SettingsRepository =
        repo ?: synchronized(this) { repo ?: SettingsRepository(app).also { repo = it } }

    fun tts(app: Application): StonicTts =
        tts ?: synchronized(this) { tts ?: StonicTts(app).also { tts = it } }
}
