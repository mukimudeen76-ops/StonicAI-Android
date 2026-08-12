package com.stonicai.app.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class StonicTts(context: Context) {

    private var ready = false
    private var pending: String? = null

    private val tts = TextToSpeech(context.applicationContext) { status ->
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale.getDefault()
            tts.setSpeechRate(1.05f)
            tts.setPitch(1.0f)
            ready = true
            pending?.let { speak(it); pending = null }
        }
    }

    fun speak(text: String) {
        if (!ready) {
            pending = text
            return
        }
        val clean = text
            .replace(Regex("```[\\s\\S]*?```"), " code block omitted. ")
            .replace(Regex("[#*_>`~\\[\\]\\(\\)]"), " ")
            .take(3000)
        @Suppress("DEPRECATION")
        tts.speak(clean, TextToSpeech.QUEUE_FLUSH, null, "stonic-${System.nanoTime()}")
    }

    fun stop() {
        runCatching { tts.stop() }
    }

    fun shutdown() {
        runCatching { tts.stop() }
        runCatching { tts.shutdown() }
    }
}
