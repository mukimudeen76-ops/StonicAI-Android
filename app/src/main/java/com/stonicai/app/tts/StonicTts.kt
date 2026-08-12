package com.stonicai.app.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class StonicTts(context: Context) {

    private var ready: Boolean = false
    private var pending: String? = null
    private var tts: TextToSpeech? = null

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.getDefault()
                tts?.setSpeechRate(1.05f)
                tts?.setPitch(1.0f)
                ready = true
                pending?.let { speak(it); pending = null }
            }
        }
    }

    fun speak(text: String) {
        val t = tts ?: return
        if (!ready) {
            pending = text
            return
        }
        val clean: String = text
            .replace(Regex("```[\\s\\S]*?```"), " code block omitted. ")
            .replace(Regex("[#*_>`~\\[\\]\\(\\)]"), " ")
            .take(3000)
        @Suppress("DEPRECATION")
        t.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "stonic-${System.nanoTime()}")
    }

    fun stop() {
        runCatching { tts?.stop() }
    }

    fun shutdown() {
        runCatching { tts?.stop() }
        runCatching { tts?.shutdown() }
        tts = null
        ready = false
    }
}
