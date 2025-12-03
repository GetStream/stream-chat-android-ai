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

![stream-chat-android-ai-compose](https://img.shields.io/badge/stream--chat--android--ai--compose-0%20MB-lightgreen)

</div>

This official repository for Stream Chat's UI components is designed specifically for AI-first
applications written in Jetpack Compose. When paired with our real-time [Chat API](https://getstream.io/chat/), it makes
integrating with and rendering responses from LLM providers such as ChatGPT, Gemini, Anthropic or
any custom backend easier by providing rich out-of-the-box components able to render Markdown,
code blocks, tables, thinking indicators, images, etc.

To start, this library includes the following components which assist with this task:

**StreamingText** - a composable that progressively reveals text content word-by-word with smooth
animation, perfect for displaying AI-generated responses in real-time, similar to ChatGPT.

**RichText** - a composable that renders markdown content with support for code blocks, code
fences, and Chart.js diagrams.

**LoadingIndicator** - a component that displays animated loading indicators with optional
labels, able to show different states of the LLM (thinking, checking external sources, etc).

**ChatViewModel & ChatUiState** - a ViewModel and state management solution that handles chat
conversation state, message sending, AI agent management, and UI state updates.

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

Provide the `ChatAiRepository` parameter via your DI container. The sample app obtains it by creating
`ChatDependencies(... )`, which surfaces the repository for reuse across the UI layer.

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

### LoadingIndicator

`LoadingIndicator` is a composable that displays an animated loading indicator with an optional
label. By default, it shows three animated dots that sequentially highlight.

**Basic Usage:**

```kotlin
import io.getstream.chat.android.ai.compose.ui.component.LoadingIndicator

@Composable
fun MyScreen() {
    LoadingIndicator(
        label = { Text("Thinking") }
    )
}
```

**Customization:**

```kotlin
LoadingIndicator(
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

### RichText

`RichText` is a composable that renders markdown content with support for code blocks, code
fences, and Chart.js diagrams.

**Basic Usage:**

```kotlin
import io.getstream.chat.android.ai.compose.ui.component.RichText

@Composable
fun MessageContent(text: String) {
    RichText(
        text = text,
        modifier = Modifier.fillMaxWidth()
    )
}
```

**Custom Rendering:**

```kotlin
RichText(
    text = markdownContent,
    component = { text, modifier ->
        // Custom markdown rendering
        Markdown(text = text, modifier = modifier)
    }
)
```

**Features:**
- Supports standard markdown syntax
- Code blocks and code fences with syntax highlighting
- Chart.js diagrams (when code fence language is `chartjs`)
- Customizable rendering via `RichTextComponent` type alias

**Parameters:**
- `text`: The markdown text content to render
- `modifier`: Modifier to be applied to the rich text container
- `component`: Custom component for rendering (defaults to markdown rendering with Chart.js
  support)

### StreamingText

`StreamingText` progressively reveals text content word-by-word with smooth animation, perfect
for displaying AI-generated responses.

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
        animate = isGenerating
    ) { displayedText ->
        RichText(text = displayedText)
    }
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
- `content`: Composable that receives the animated text as `displayedText`

### ChatViewModel and ChatUiState

`ChatViewModel` manages chat conversation state and interactions, while `ChatUiState`
represents the UI state.

**Setup:**

```kotlin
import io.getstream.chat.android.ai.compose.presentation.ChatViewModel
import io.getstream.chat.android.ai.compose.di.ChatViewModelFactory
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun ChatScreen(
    conversationId: String?,
    chatAiRepository: ChatAiRepository,
) {
    val viewModel = viewModel<ChatViewModel>(
        key = conversationId,
        factory = ChatViewModelFactory(
            chatAiRepository = chatAiRepository,
            conversationId = conversationId,
        ),
    )
    
    val state by viewModel.uiState.collectAsState()
    
    // Use state in your UI
    LazyColumn {
        items(state.messages) { message ->
            MessageItem(message = message)
        }
    }
}
```

Provide the `ChatAiRepository` via your own DI container. The sample app surfaces the repository through a
`ChatDependencies(...)` instance and passes it to `ChatViewModelFactory`.

**ChatViewModel API:**

```kotlin
// Observe UI state
val state: StateFlow<ChatUiState> = viewModel.uiState

// Update input text
viewModel.onInputTextChange(newText)

// Send a message
viewModel.sendMessage()

// Stop streaming response
viewModel.stopStreaming()

// Delete the current chat
viewModel.deleteChannel(
    onSuccess = { /* Handle success */ },
    onError = { error -> /* Handle error */ }
)
```

**ChatUiState Structure:**

```kotlin
data class ChatUiState(
    val isLoading: Boolean = false,
    val title: String = "New Chat",
    val actions: List<Action> = emptyList(),
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val assistantState: AssistantState = AssistantState.Idle,
)

// Message structure
data class Message(
    val id: String,
    val role: Role, // Assistant, User, or Other
    val content: String,
    val isGenerating: Boolean,
)

// Assistant states
enum class AssistantState {
    Idle,
    Thinking,
    CheckingSources,
    Generating,
    Error,
}
```

**Helper Functions:**

```kotlin
// Check if assistant is busy
val isBusy = state.assistantState.isBusy()

// Get current assistant message
val currentMessage = state.getCurrentAssistantMessage()
```

**Complete Example:**

```kotlin
@Composable
fun ChatScreen(
    conversationId: String?,
    chatAiRepository: ChatAiRepository,
) {
    val viewModel = viewModel<ChatViewModel>(
        key = conversationId,
        factory = ChatViewModelFactory(
            chatAiRepository = chatAiRepository,
            conversationId = conversationId,
        ),
    )
    
    val state by viewModel.uiState.collectAsState()
    
    Column {
        // Messages list
        LazyColumn {
            items(state.messages) { message ->
                when (message.role) {
                    ChatUiState.Message.Role.Assistant -> {
                        StreamingText(
                            text = message.content,
                            animate = message.isGenerating
                        ) { displayedText ->
                            RichText(text = displayedText)
                        }
                    }
                    ChatUiState.Message.Role.User -> {
                        RichText(text = message.content)
                    }
                    else -> { /* Other users */ }
                }
            }
        }
        
        // Loading indicator
        if (state.assistantState.isBusy()) {
            LoadingIndicator(
                label = { 
                    Text(
                        when (state.assistantState) {
                            ChatUiState.AssistantState.Thinking -> "Thinking"
                            ChatUiState.AssistantState.CheckingSources -> "Checking sources"
                            ChatUiState.AssistantState.Generating -> "Generating response"
                            else -> ""
                        }
                    )
                }
            )
        }
        
        // Input field
        TextField(
            value = state.inputText,
            onValueChange = viewModel::onInputTextChange,
            trailingIcon = {
                IconButton(onClick = viewModel::sendMessage) {
                    Icon(Icons.Default.Send)
                }
            }
        )
    }
}
```

## 🛥 What is Stream?

Stream allows developers to rapidly deploy scalable feeds, chat messaging and video with an industry leading 99.99% uptime SLA guarantee.

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
