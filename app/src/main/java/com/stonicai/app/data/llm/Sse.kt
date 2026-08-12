package com.stonicai.app.data.llm

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.IOException
import java.util.concurrent.TimeUnit

internal object Http {
    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .callTimeout(180, TimeUnit.SECONDS)
        .build()
}

internal fun Request.Builder.jsonBody(body: String): Request.Builder {
    val mediaType = "application/json; charset=utf-8".toMediaType()
    return this.post(body.toRequestBody(mediaType))
}

internal fun sseFlow(request: Request): Flow<String> = callbackFlow {
    val factory = EventSources.createFactory(Http.client)
    var source: EventSource? = null

    val listener = object : EventSourceListener() {
        override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
            if (data == "[DONE]") {
                close()
                return
            }
            trySend(data)
        }

        override fun onClosed(eventSource: EventSource) {
            close()
        }

        override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
            val code = response?.code
            val body = response?.body?.string()
            val err = t ?: IOException(
                buildString {
                    append("HTTP ").append(code ?: "?")
                    if (!body.isNullOrBlank()) append(" — ").append(body.take(500))
                }
            )
            close(err)
        }
    }

    source = factory.newEventSource(request, listener)

    awaitClose {
        source?.cancel()
    }
}.flowOn(Dispatchers.IO)
