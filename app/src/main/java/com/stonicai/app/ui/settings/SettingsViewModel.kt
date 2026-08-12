package com.stonicai.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stonicai.app.data.SettingsRepository
import com.stonicai.app.data.StonicSettings
import com.stonicai.app.ui.chat.ServiceLocator
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo: SettingsRepository = ServiceLocator.repo(app)

    val settings: StateFlow<StonicSettings> = repo.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, StonicSettings())

    fun save(
        modelId: String? = null,
        personaId: String? = null,
        systemPrompt: String? = null,
        tts: Boolean? = null,
        haptics: Boolean? = null,
        expert: Boolean? = null,
        openai: String? = null,
        anthropic: String? = null,
        google: String? = null,
        groq: String? = null
    ) {
        viewModelScope.launch {
            repo.save(
                modelId = modelId,
                personaId = personaId,
                systemPrompt = systemPrompt,
                tts = tts,
                haptics = haptics,
                expert = expert,
                openai = openai,
                anthropic = anthropic,
                google = google,
                groq = groq
            )
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch { repo.setOnboarded() }
    }

    fun resetAll() {
        viewModelScope.launch { repo.clearAll() }
    }
}
