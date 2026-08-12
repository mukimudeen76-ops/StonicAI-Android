package com.stonicai.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "stonic_settings")

data class ApiKeys(
    val openai: String = "",
    val anthropic: String = "",
    val google: String = "",
    val groq: String = ""
)

data class StonicSettings(
    val selectedModelId: String = Models.DEFAULT.id,
    val systemPrompt: String = "You are Stonic, a helpful, concise, and powerful AI assistant running natively on the user's Android phone. Reply in Markdown. Match the user's language. Always be accurate and honest.",
    val ttsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val keys: ApiKeys = ApiKeys()
)

object Models {
    data class Model(
        val id: String,
        val displayName: String,
        val provider: String,
        val apiModel: String
    )

    val ALL: List<Model> = listOf(
        Model("gpt-4o", "GPT-4o", "openai", "gpt-4o"),
        Model("gpt-4o-mini", "GPT-4o Mini", "openai", "gpt-4o-mini"),
        Model("claude-3-5-sonnet", "Claude 3.5 Sonnet", "anthropic", "claude-3-5-sonnet-latest"),
        Model("claude-3-haiku", "Claude 3 Haiku", "anthropic", "claude-3-haiku-20240307"),
        Model("gemini-1.5-pro", "Gemini 1.5 Pro", "google", "gemini-1.5-pro"),
        Model("gemini-1.5-flash", "Gemini 1.5 Flash", "google", "gemini-1.5-flash"),
        Model("llama-3.1-70b", "Llama 3.1 70B (Groq)", "groq", "llama-3.1-70b-versatile"),
        Model("llama-3.1-8b", "Llama 3.1 8B (Groq)", "groq", "llama-3.1-8b-instant")
    )
    val DEFAULT = ALL.first { it.id == "gpt-4o-mini" }
    fun byId(id: String?) = ALL.firstOrNull { it.id == id } ?: DEFAULT
}

class SettingsRepository(private val context: Context) {

    private object Keys {
        val MODEL = stringPreferencesKey("model")
        val SYS = stringPreferencesKey("system")
        val TTS = stringPreferencesKey("tts")
        val HAP = stringPreferencesKey("haptics")
        val KEY_OPENAI = stringPreferencesKey("key_openai")
        val KEY_ANTHROPIC = stringPreferencesKey("key_anthropic")
        val KEY_GOOGLE = stringPreferencesKey("key_google")
        val KEY_GROQ = stringPreferencesKey("key_groq")
        val ONBOARDED = stringPreferencesKey("onboarded")
    }

    val settings: Flow<StonicSettings> = context.dataStore.data.map { p ->
        StonicSettings(
            selectedModelId = p[Keys.MODEL] ?: Models.DEFAULT.id,
            systemPrompt = p[Keys.SYS] ?: "",
            ttsEnabled = (p[Keys.TTS] ?: "1") == "1",
            hapticsEnabled = (p[Keys.HAP] ?: "1") == "1",
            keys = ApiKeys(
                openai = p[Keys.KEY_OPENAI] ?: "",
                anthropic = p[Keys.KEY_ANTHROPIC] ?: "",
                google = p[Keys.KEY_GOOGLE] ?: "",
                groq = p[Keys.KEY_GROQ] ?: ""
            )
        )
    }

    val isOnboarded: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDED] == "1" }

    suspend fun setOnboarded() {
        context.dataStore.edit { it[Keys.ONBOARDED] = "1" }
    }

    suspend fun save(
        modelId: String? = null,
        systemPrompt: String? = null,
        tts: Boolean? = null,
        haptics: Boolean? = null,
        openai: String? = null,
        anthropic: String? = null,
        google: String? = null,
        groq: String? = null
    ) {
        context.dataStore.edit { p ->
            modelId?.let { p[Keys.MODEL] = it }
            systemPrompt?.let { p[Keys.SYS] = it }
            tts?.let { p[Keys.TTS] = if (it) "1" else "0" }
            haptics?.let { p[Keys.HAP] = if (it) "1" else "0" }
            openai?.let { p[Keys.KEY_OPENAI] = it.trim() }
            anthropic?.let { p[Keys.KEY_ANTHROPIC] = it.trim() }
            google?.let { p[Keys.KEY_GOOGLE] = it.trim() }
            groq?.let { p[Keys.KEY_GROQ] = it.trim() }
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { it.clear() }
    }
}
