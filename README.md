# AI components for [Stream Android Chat SDK](https://getstream.io/tutorials/android-chat/)

<p align="center">
  <a href="https://github.com/GetStream/stream-chat-android-ai/actions/workflows/ci.yml">
    <img alt="CI" src="https://github.com/GetStream/stream-chat-android-ai/actions/workflows/ci.yml/badge.svg" />
  </a>
  <a href="https://developer.android.com/about/versions/marshmallow">
    <img alt="API-23" src="https://img.shields.io/badge/API-23%2B-brightgreen.svg?style=flat"/>
  </a>
  <a href="https://github.com/GetStream/stream-chat-android-ai/releases">
    <img alt="release" src="https://img.shields.io/github/v/release/GetStream/stream-chat-android-ai" />
  </a>
  <a href="https://central.sonatype.com/repository/maven-snapshots/io/getstream/stream-chat-android-ai-compose/">
    <img alt="snapshot" src="https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fio%2Fgetstream%2Fstream-chat-android-ai-compose%2Fmaven-metadata.xml&strategy=latestProperty&label=snapshot" />
  </a>
</p>

<div align="center">

![stream-chat-android-ai-compose](https://img.shields.io/badge/stream--chat--android--ai--compose-.01%20MB-lightgreen)

</div>

This official repository for Stream Chat's UI components is designed specifically for AI-first
applications written in Jetpack Compose. When paired with our real-time [Chat API](https://getstream.io/chat/), it makes
integrating with and rendering responses from LLM providers such as ChatGPT, Gemini, Anthropic or
any custom backend easier by providing rich out-of-the-box components able to render Markdown,
code blocks, tables, thinking indicators, images, etc.

To start, this library includes the following components which assist with this task:

**StreamingText** - a composable that progressively reveals text content word-by-word with smooth
animation, perfect for displaying AI-generated responses in real-time, similar to ChatGPT.

**AITypingIndicator** - a component that displays animated typing indicators with optional
labels, able to show different states of the LLM (thinking, checking external sources, etc).

**ChatComposer** - a complete chat input component with text input, attachment support, voice input,
and send/stop buttons. It manages message composition state and provides a polished UI with
automatic keyboard handling and visual fade gradients.

**SpeechToTextButton** - a composable button that provides speech-to-text functionality with
waveform visualization, automatic permission handling, and customizable UI components.

This repository also includes a sample app that demonstrates how to use these components in a
AI chat application.

Our team plans to keep iterating and adding more components over time. If there's a component
you use every day in your apps and would like to see added, please open an issue and we will try
to add it 😎.

## 📦 Installation

Add the dependency to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.getstream:stream-chat-android-ai-compose:$version")
}
```

The library provides UI components that work independently. For state management and backend
integration, refer to the sample app which demonstrates how to integrate these components with
Stream Chat SDK and AI providers.

### Snapshot Releases

To use snapshot releases, you need to add the Sonatype snapshot repository to your `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://central.sonatype.com/repository/maven-snapshots") }
    }
}
```

Find the latest snapshot version in the badge above, or check the [Maven Central snapshot repository](https://central.sonatype.com/repository/maven-snapshots/io/getstream/stream-chat-android-ai-compose/) for available versions.

## 🚀 Usage

### AITypingIndicator

`AITypingIndicator` is a composable that displays an animated typing indicator with an optional
label. By default, it shows three animated dots that sequentially highlight.

**Basic Usage:**

```kotlin
import io.getstream.chat.android.ai.compose.ui.component.AITypingIndicator

@Composable
fun MyScreen() {
    AITypingIndicator(
        label = { Text("Thinking") }
    )
}
```

**Customization:**

```kotlin
AITypingIndicator(
    modifier = Modifier.padding(16.dp),
    label = { Text("Processing...") },
    indicator = { 
        // Custom indicator composable
        CircularProgressIndicator()
    }
)
```

**Parameters:**
- `modifier`: Modifier to be applied to the root Row container
- `label`: Optional composable label to display before the indicator (defaults to empty)
- `indicator`: Composable indicator to display (defaults to `AnimatedDots`)

### StreamingText

`StreamingText` progressively reveals text content word-by-word with smooth animation, perfect
for displaying AI-generated responses. By default it renders the streamed text using the same
markdown formatter as the sample app, so you can drop it directly into chat bubbles without any
extra setup.

**Basic Usage:**

```kotlin
import io.getstream.chat.android.ai.compose.ui.component.StreamingText

@Composable
fun AssistantMessage(
    text: String,
    isGenerating: Boolean
) {
    StreamingText(
        text = text,
        animate = isGenerating,
    )
}
```

**Customization:**

```kotlin
StreamingText(
    text = fullText,
    animate = true,
    chunkDelayMs = 50, // Adjust animation speed
) { displayedText ->
    Text(
        text = displayedText,
        style = MaterialTheme.typography.bodyLarge
    )
}
```

**Behavior:**
- When `animate` is `true`: Progressively reveals text word-by-word
- When `animate` is `false`: Displays full text immediately
- Automatically handles continuation (if new text starts with previous text, continues from
  current position)
- Resets animation for completely new text

**Parameters:**
- `text`: The full text content to display
- `animate`: Whether to animate the text reveal (default: `true`)
- `chunkDelayMs`: Delay in milliseconds between each chunk reveal (default: `30`)
- `content`: Optional composable that receives the animated text as `displayedText`. Defaults to
  the library's markdown renderer used across the sample app.

### ChatComposer

`ChatComposer` is a complete chat input component that provides text input, attachment support,
voice input, and send/stop buttons.

**Basic Usage (with internal state management):**

```kotlin
import io.getstream.chat.android.ai.compose.ui.component.ChatComposer
import io.getstream.chat.android.ai.compose.ui.component.MessageData

@Composable
fun ChatScreen(isStreaming: Boolean) {
    ChatComposer(
        onSendClick = { messageData: MessageData ->
            // Handle message send
            // messageData.text contains the text
            // messageData.attachments contains Set<Uri> of selected images
        },
        onStopClick = {
            // Handle stop streaming
        },
        isStreaming = isStreaming,
    )
}
```

**Features:**
- Text input with placeholder ("Ask Assistant")
- Image attachment support (up to 3 images via photo picker)
- Voice input button with speech-to-text integration
- Send button (shown when text is entered)
- Stop button (shown during AI streaming)
- Attachment preview with remove functionality

**MessageData Structure:**

```kotlin
data class MessageData(
    val text: String = "",
    val attachments: Set<Uri> = emptySet(),
)
```

### SpeechToTextButton

`SpeechToTextButton` provides speech-to-text functionality with animated waveform visualization
and automatic permission handling. When not recording, it displays a microphone icon button.
When recording, it transforms into a circular button with animated bars that respond to voice input.

**Basic Usage:**

```kotlin
import io.getstream.chat.android.ai.compose.ui.component.SpeechToTextButton

@Composable
fun MyComposer() {
    var text by remember { mutableStateOf("") }

    SpeechToTextButton(
        onTextRecognized = { recognizedText ->
            // Called with partial results as user speaks
            // Caller is responsible for accumulating text
            text = recognizedText
        }
    )
}
```

**Advanced Usage with State Tracking:**

```kotlin
import io.getstream.chat.android.ai.compose.ui.component.SpeechToTextButton
import io.getstream.chat.android.ai.compose.ui.component.rememberSpeechToTextButtonState

@Composable
fun MyComposer() {
    val speechState = rememberSpeechToTextButtonState()
    var text by remember { mutableStateOf("") }

    // Remember the text that existed before starting speech recognition
    val textBeforeSpeech = remember { mutableStateOf("") }

    // Capture text when recording starts
    LaunchedEffect(speechState.isRecording()) {
        if (speechState.isRecording()) {
            textBeforeSpeech.value = text
        }
    }

    SpeechToTextButton(
        state = speechState,
        onTextRecognized = { recognizedText ->
            // Partial results contain full recognized text, so replace (don't accumulate)
            text = if (textBeforeSpeech.value.isBlank()) {
                recognizedText
            } else {
                "${textBeforeSpeech.value} $recognizedText"
            }
        }
    )

    // Check if currently recording
    if (speechState.isRecording()) {
        Text("Recording...")
    }
}
```

**Features:**
- Automatic audio permission requests (RECORD_AUDIO)
- Animated waveform visualization during recording
- Real-time streaming of recognized text (partial results)
- Automatic UI transformation between idle and recording states
- State tracking for recording status

**Parameters:**
- `modifier`: Modifier to be applied to the root container
- `state`: Optional state holder for tracking recording status (defaults to remembered state)
- `onTextRecognized`: Callback invoked with each partial result as speech is detected

**SpeechToTextButtonState API:**

```kotlin
// Check if currently recording
val isRecording: Boolean = state.isRecording()
```

## 🛥 What is Stream?

Stream allows developers to rapidly deploy scalable feeds, chat messaging and video with an industry leading 99.999% uptime SLA guarantee.

Stream provides UI components and state handling that make it easy to build real-time chat and video calling for your app. Stream runs and maintains a global network of edge servers around the world, ensuring optimal latency and reliability regardless of where your users are located.

## 📕 Tutorials

To learn more about integrating AI and chatbots into your application, we recommend checking out the full list of tutorials across all of our supported frontend SDKs and providers. Stream's Chat SDK is natively supported across:
* [React](https://getstream.io/chat/react-chat/tutorial/)
* [React Native](https://getstream.io/chat/react-native-chat/tutorial/)
* [Angular](https://getstream.io/chat/angular/tutorial/)
* [Jetpack Compose](https://getstream.io/tutorials/android-chat/)
* [SwiftUI](https://getstream.io/tutorials/ios-chat/)
* [Flutter](https://getstream.io/chat/flutter/tutorial/)
* [Javascript/Bring your own](https://getstream.io/chat/docs/javascript/)

## 👩‍💻 Free for Makers 👨‍💻

Stream is free for most side and hobby projects.
To qualify, your project/company needs to have < 5 team members and < $10k in monthly revenue.
Makers get $100 in monthly credit for video for free.
For more details, check out the [Maker Account](https://getstream.io/maker-account).

## 💼 We are hiring!

We've recently closed a [\$38 million Series B funding round](https://techcrunch.com/2021/03/04/stream-raises-38m-as-its-chat-and-activity-feed-apis-power-communications-for-1b-users/) and we keep actively growing.
Our APIs are used by more than a billion end-users, and you'll have a chance to make a huge impact on the product within a team of the strongest engineers all over the world.
Check out our current openings and apply via [Stream's website](https://getstream.io/team/#jobs).

## License

```
Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.

Licensed under the Stream License;
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   https://github.com/GetStream/stream-chat-android-ai/blob/main/LICENSE

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
