package com.stonicai.app.data

import java.util.UUID

enum class Sender { USER, AI }
enum class MessageStatus { SENDING, STREAMING, DONE, ERROR }

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: Sender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val status: MessageStatus = MessageStatus.DONE
)
