package com.stonicai.app.ui.chat

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stonicai.app.data.ChatMessage
import com.stonicai.app.data.MessageStatus
import com.stonicai.app.data.Models
import com.stonicai.app.data.Sender
import com.stonicai.app.data.SettingsRepository
import com.stonicai.app.data.StonicAgent
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
    private val agent = StonicAgent(app)

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _settings = MutableStateFlow(StonicSettings())
    val settings: StateFlow<StonicSettings> = _settings.asStateFlow()

    private val _isStreaming = MutableStateFlow(false)
    val isStreaming: StateFlow<Boolean> = _isStreaming.asStateFlow()

    private var streamJob: Job? = null

    init {
        viewModelScope.launch {
            repo.settings.collect { s ->
                _settings.value = s
                tts.applySettings(s.ttsVolume, s.ttsRate, s.ttsPitch, s.ttsVoice.ifBlank { null })
            }
        }
    }

    fun send(text: String) {
        val content = text.trim()
        if (content.isEmpty() || _isStreaming.value) return
        viewModelScope.launch {
            val local = agent.handleIfDeviceCommand(content)
            if (local != null) {
                appendUser(content)
                appendAi(local.reply, MessageStatus.DONE)
                if (_settings.value.ttsEnabled) tts.speak(local.reply)
                return@launch
            }
            runLlm(content)
        }
    }

    private fun runLlm(content: String) {
        appendUser(content)
        val aiId = java.util.UUID.randomUUID().toString()
        _messages.value = _messages.value +
            ChatMessage(id = aiId, sender = Sender.AI, text = "", status = MessageStatus.STREAMING)
        _isStreaming.value = true

        streamJob = viewModelScope.launch {
            val s = _settings.value
            val model = Models.byId(s.selectedModelId)
            try {
                if (!hasKeyFor(model)) throw MissingApiKeyException(model.provider)
                val history = buildHistoryForApi()
                val client = LlmClients.forModel(model, s.keys)
                val sb = StringBuilder()
                client.stream(model, "", s.effectiveSystemPrompt, history).collect { piece ->
                    sb.append(piece)
                    updateAi(aiId) { it.copy(text = sb.toString()) }
                }
                updateAi(aiId) { it.copy(status = MessageStatus.DONE) }
                if (s.ttsEnabled) tts.speak(sb.toString())
            } catch (e: MissingApiKeyException) {
                updateAi(aiId) {
                    it.copy(
                        status = MessageStatus.ERROR,
                        text = "⚠️ ${e.message}\n\nOpen **Settings → Model & Keys** and add a key for ${model.provider}. Free options: Google AI Studio and Groq."
                    )
                }
            } catch (e: Exception) {
                updateAi(aiId) {
                    it.copy(status = MessageStatus.ERROR,
                        text = "⚠️ Request failed: ${e.message ?: e.javaClass.simpleName}")
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
        tts.stop()
    }

    fun clear() {
        streamJob?.cancel()
        _isStreaming.value = false
        _messages.value = emptyList()
    }

    fun selectModel(id: String) { viewModelScope.launch { repo.save(modelId = id) } }
    fun setPersona(id: String) { viewModelScope.launch { repo.save(personaId = id) } }
    fun setExpert(on: Boolean) { viewModelScope.launch { repo.save(expert = on) } }
    fun setTtsEnabled(on: Boolean) { viewModelScope.launch { repo.save(tts = on) } }
    fun stopSpeaking() = tts.stop()

    private fun appendUser(text: String) {
        _messages.value = _messages.value +
            ChatMessage(sender = Sender.USER, text = text, status = MessageStatus.DONE)
    }

    private fun appendAi(text: String, status: MessageStatus) {
        _messages.value = _messages.value +
            ChatMessage(sender = Sender.AI, text = text, status = status)
    }

    private fun hasKeyFor(model: Models.Model): Boolean = when (model.provider) {
        "openai" -> _settings.value.keys.openai.isNotBlank()
        "anthropic" -> _settings.value.keys.anthropic.isNotBlank()
        "google" -> _settings.value.keys.google.isNotBlank()
        "groq" -> _settings.value.keys.groq.isNotBlank()
        else -> false
    }

    private fun buildHistoryForApi(): List<LlmClient.ChatMessage> =
        _messages.value
            .filter { it.sender == Sender.USER || it.status == MessageStatus.DONE }
            .dropLast(1)
            .map {
                LlmClient.ChatMessage(
                    role = if (it.sender == Sender.USER) LlmClient.ChatMessage.Role.USER
                    else LlmClient.ChatMessage.Role.ASSISTANT,
                    content = it.text
                )
            }

    private fun updateAi(id: String, block: (ChatMessage) -> ChatMessage) {
        _messages.value = _messages.value.map { if (it.id == id) block(it) else it }
    }

    override fun onCleared() {
        super.onCleared()
        tts.shutdown()
    }
}

object ServiceLocator {
    @Volatile private var repo: SettingsRepository? = null
    @Volatile private var tts: StonicTts? = null

    fun repo(app: Application): SettingsRepository =
        repo ?: synchronized(this) { repo ?: SettingsRepository(app).also { repo = it } }

    fun tts(app: Application): StonicTts =
        tts ?: synchronized(this) { tts ?: StonicTts(app).also { tts = it } }
}
