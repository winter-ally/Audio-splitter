# 🔊 Audio Splitter — Dual Audio Router

A native Android app that simultaneously routes **different audio streams to two separate output devices** — one to a Bluetooth device and one to the phone speaker — at the same time.

## 💡 The Idea

Most phones only allow audio to play through one output at a time. This app breaks that limitation by splitting and routing two independent audio streams to two different devices simultaneously.

**Use cases:**
- Play music on a Bluetooth speaker while navigation audio plays on the phone speaker
- Route different content to different listeners without headphone splitters
- Any scenario requiring independent dual audio outputs

## 📱 Features

- 🎧 Simultaneous Bluetooth + phone speaker audio output
- 🎵 Independent audio streams on each device
- 🎚️ Per-stream playback control using ExoPlayer (Media3)
- 📡 Bluetooth audio routing via AndroidX MediaRouter
- Simple, clean Material Design UI

## 🛠️ Built With

- **Language:** Kotlin
- **UI:** Android Views + ViewBinding + Material Components
- **Audio Playback:** [AndroidX Media3 (ExoPlayer)](https://developer.android.com/media/media3) v1.2.1
- **Audio Routing:** [AndroidX MediaRouter](https://developer.android.com/guide/topics/media/mediarouter) v1.6.0-alpha05
- **Async:** Kotlin Coroutines
- **Min SDK:** Android 10 (API 29)
- **Target SDK:** Android 14 (API 34)
- **Build System:** Gradle (Kotlin DSL)
- **AI-assisted development:** Google Antigravity

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2023.1.1) or newer
- Android device or emulator running **Android 10 (API 29) or higher**
- A paired Bluetooth audio device (for dual-output testing)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/AudioSplitterMVP.git
   cd AudioSplitterMVP
   ```

2. **Open in Android Studio**
   - File → Open → select the `AudioSplitterMVP` folder

3. **Build & Run**
   - Connect your Android device or start an emulator
   - Click **Run ▶** or use:
   ```bash
   ./gradlew assembleDebug
   ```

### Permissions Required

The app requests the following permissions at runtime:
- `BLUETOOTH` / `BLUETOOTH_CONNECT` / `BLUETOOTH_SCAN` — for Bluetooth device discovery and routing
- `MODIFY_AUDIO_SETTINGS` — for audio output routing

## 📂 Project Structure

```
AudioSplitterMVP/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/audiosplitter/   # Kotlin source files
│   │   ├── res/                               # Layouts, drawables, strings
│   │   └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

## 📸 Demo

[(https://youtube.com/shorts/IFkL8tHfSOE?si=b0dc6dpigK70cwd_)]

## 👤 Author

Thigala abhilash
- GitHub: [abhilashthigala7@gmail.com](https://github.com/winter-ally/Audio-splitter)
- LinkedIn: [Thigala Abhilash]( www.linkedin.com/in/thigala-abhilash-808b053b2)

## 📄 License

All rights reserved © 2026 [Thigala Abhilash]
