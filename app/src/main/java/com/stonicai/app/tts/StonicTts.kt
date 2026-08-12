package com.stonicai.app.tts

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import java.util.Locale

data class StonicVoice(
    val name: String,
    val language: String,
    val quality: String,
    val isNetwork: Boolean
)

class StonicTts(context: Context) {

    private var ready = false
    private var pending: String? = null
    private var tts: TextToSpeech? = null
    private var volume = 1.0f
    private var rate = 1.05f
    private var pitch = 1.0f
    private var preferredVoice: String? = null
    private var onReady: ((List<StonicVoice>) -> Unit)? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val t = tts ?: return@TextToSpeech
                t.language = Locale.getDefault()
                applyParams()
                ready = true
                pending?.let { speak(it); pending = null }
                onReady?.invoke(availableVoices())
            }
        }
    }

    fun setOnVoicesReady(cb: (List<StonicVoice>) -> Unit) {
        onReady = cb
        if (ready) cb(availableVoices())
    }

    fun applySettings(volume: Float, rate: Float, pitch: Float, voiceName: String?) {
        this.volume = volume.coerceIn(0f, 1.5f)
        this.rate = rate.coerceIn(0.5f, 2.0f)
        this.pitch = pitch.coerceIn(0.5f, 2.0f)
        this.preferredVoice = voiceName
        if (ready) applyParams()
    }

    private fun applyParams() {
        val t = tts ?: return
        t.setSpeechRate(rate)
        t.setPitch(pitch)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            val v = preferredVoice
                ?.let { name -> t.voices?.firstOrNull { it.name == name } }
                ?: t.defaultVoice
            if (v != null) runCatching { t.voice = v }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            t.setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
            )
        }
    }

    fun availableVoices(): List<StonicVoice> {
        val t = tts ?: return emptyList()
        val all = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            t.voices?.toList() ?: emptyList()
        } else emptyList()
        return all
            .filter { it.locale.language.isNotBlank() }
            .map {
                StonicVoice(
                    name = it.name,
                    language = it.locale.displayLanguage,
                    quality = when {
                        it.quality == Voice.QUALITY_VERY_HIGH -> "Very High"
                        it.quality == Voice.QUALITY_HIGH -> "High"
                        else -> "Normal"
                    },
                    isNetwork = it.isNetworkConnectionRequired
                )
            }
            .distinctBy { it.name }
            .sortedWith(compareBy({ it.language }, { it.name }))
    }

    fun speak(text: String) {
        val t = tts ?: return
        if (!ready) { pending = text; return }
        val clean = text
            .replace(Regex("```[\\s\\S]*?```"), " code block. ")
            .replace(Regex("[#*_>`~\\[\\]\\(\\)]"), " ")
            .take(4000)
        runCatching {
            @Suppress("DEPRECATION")
            t.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "stonic-${System.nanoTime()}")
        }
    }

    fun stop() = runCatching { tts?.stop() }

    fun shutdown() {
        runCatching { tts?.stop() }
        runCatching { tts?.shutdown() }
        tts = null
        ready = false
    }
}
