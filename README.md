# Stonic AI — Native Android

A full, standalone Android port of the Stonic AI assistant, written in Kotlin + Jetpack Compose. It does **not** require the desktop app or any bridge — all AI runs directly on the phone against provider APIs (OpenAI, Anthropic, Google Gemini, Groq).

## ✨ Features

- **Stonic terminal UI** — same dark `#05070A` / cyan `#00E5FF` design language as the desktop app.
- **Multi-model chat** with streaming responses:
  - GPT-4o / GPT-4o Mini (OpenAI)
  - Claude 3.5 Sonnet / Claude 3 Haiku (Anthropic)
  - Gemini 1.5 Pro / Flash (Google AI Studio)
  - Llama 3.1 70B / 8B (Groq — free tier)
- **Voice input** via on-device Android `SpeechRecognizer` (no API key needed).
- **Text-to-speech** — Stonic reads responses aloud using Android TTS.
- **Markdown rendering** — tables, code blocks (syntax highlight), lists, links.
- **Haptics**, safe-area / edge-to-edge layout, Material 3.
- **On-device secure storage** — API keys kept in Jetpack DataStore (Preferences), never leave the device except direct calls to the chosen provider.
- **Onboarding flow** + Settings screen.

## 📂 Project structure

```
StonicAI/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
└── app/
    ├── build.gradle.kts
    ├── proguard-rules.pro
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/                      (themes, adaptive icon, strings)
        └── java/com/stonicai/app/
            ├── MainActivity.kt
            ├── StonicApp.kt
            ├── nav/                 (Navigation Compose graph)
            ├── data/
            │   ├── SettingsRepository.kt   (DataStore + Models registry)
            │   ├── ChatModels.kt
            │   └── llm/                    (SSE streaming clients)
            │       ├── LlmClient.kt
            │       ├── Sse.kt
            │       ├── OpenAiClient.kt       (also Groq)
            │       ├── AnthropicClient.kt
            │       └── GoogleAiClient.kt
            ├── audio/
            │   ├── VoiceRecorder.kt
            │   ├── GroqTranscriber.kt
            │   └── OnDeviceSpeech.kt
            ├── tts/StonicTts.kt
            └── ui/
                ├── theme/
                ├── components/MarkdownText.kt
                ├── chat/
                │   ├── ChatScreen.kt
                │   └── ChatViewModel.kt
                └── settings/
                    ├── SettingsScreen.kt
                    └── SettingsViewModel.kt
```

## 🚀 Build the APK

This is a standard Android Gradle project. You do **not** need to install anything globally if you use Android Studio:

1. Open the `StonicAI/` folder in **Android Studio** (Hedgehog or newer).
2. Let Gradle sync (it will download AGP 8.5.2, Kotlin 1.9.24, Compose BOM 2024.06).
3. Connect an Android 8.0+ (API 26+) device with USB debugging, or start an emulator.
4. Press **Run ▶** — or build a signed APK via **Build → Build Bundle(s)/APK(s) → Build APK(s)**.

Command-line build (requires a local Android SDK + JDK 17):

```bash
cd StonicAI
./gradlew :app:assembleDebug
# APK output:
# app/build/outputs/apk/debug/app-debug.apk
```

Install on a connected phone:

```bash
./gradlew :app:installDebug
```

## 🔑 API keys (free options available)

On first launch you'll see the **Setup** screen. Add any one key:

| Provider | Get a key | Models |
|---|---|---|
| **Google AI Studio** | https://aistudio.google.com/apikey (free) | Gemini 1.5 Pro / Flash |
| **Groq** | https://console.groq.com/keys (free tier) | Llama 3.1 70B / 8B |
| OpenAI | https://platform.openai.com/api-keys | GPT-4o, GPT-4o Mini |
| Anthropic | https://console.anthropic.com/ | Claude 3.5 Sonnet, Haiku |

Keys are sent from your phone directly to the provider using HTTPS — there is no middleman server.

## 🧠 Architecture notes

- The LLM layer is a small `LlmClient` interface with one implementation per provider. Each uses OkHttp's `EventSources` to consume **Server-Sent Events** and emit streaming text via a Kotlin `Flow`, which the ViewModel collects and appends to the active message.
- The chat history sent to the API is the list of completed messages; the empty "streaming placeholder" is dropped before the request.
- Voice uses `SpeechRecognizer` (Google/on-device) for free transcription. The `VoiceRecorder` + `GroqTranscriber` classes are also included if you prefer Whisper-quality transcription using a Groq key.
- `StonicTts` is an app-wide singleton that strips markdown before speaking.

## ⚠️ What is intentionally different from the desktop build

The Windows desktop app ships with Electron, Playwright-driven browser automation, `robotjs` mouse/keyboard control, `screenshot-desktop`, an embedded Python runtime, Porcupine wake-word, MediaPipe, and bundled Windows `.exe` skill binaries. These components:

- are Windows-native and cannot run on Android,
- rely on APIs Android forbids for third-party apps (controlling other apps, global mouse/keyboard injection, reading other apps' screens without Accessibility permission).

This port therefore reproduces everything that is meaningful on a phone — the AI, the voice, the UI, the terminal-style conversation, model switching, Markdown, TTS — and is structured so Android-native skills (sharing, intents, opening apps, notifications, camera) can be added under `audio/`, `tts/`, and a future `skills/` package.

## 🧭 Roadmap / next steps (drop-in ready)

- Image attachments (vision models — GPT-4o, Gemini, Claude) using the photo picker.
- Chat history persistence (Room database).
- Android-native wake word ("Hey Stonic") via Porcupine mobile.
- Home-screen widget + quick-tile.
- Push-to-talk lockscreen action.
- Accessibility-service based on-device automation for supported apps.

## License

Original Stonic AI © Stonic AI. This Android port is provided for personal use with the desktop product you own.
