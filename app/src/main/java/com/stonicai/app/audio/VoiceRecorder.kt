package com.stonicai.app.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Records mic audio to an M4A (AAC) file. Stops and returns the file when asked.
 */
class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var target: File? = null

    fun hasPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED

    suspend fun start(): File = withContext(Dispatchers.IO) {
        val out = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        target = out

        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION") MediaRecorder()
        }
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setAudioSamplingRate(44_100)
        r.setAudioEncodingBitRate(96_000)
        r.setOutputFile(out.absolutePath)
        r.prepare()
        r.start()
        recorder = r
        out
    }

    fun stop(): File? {
        val out = target
        try {
            recorder?.stop()
        } catch (_: Throwable) { /* recording too short */ }
        recorder?.release()
        recorder = null
        return out?.takeIf { it.exists() && it.length() > 0 }
    }

    fun cancel() {
        try { recorder?.stop() } catch (_: Throwable) {}
        recorder?.release()
        recorder = null
        target?.delete()
        target = null
    }
}
