# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **Stream Chat Android AI** repository containing a **pure UI component library** for Jetpack Compose along with a reference implementation sample app that demonstrates Stream Chat integration.

**SDK Module (`stream-chat-android-ai-compose`):**
A lightweight, framework-agnostic UI component library providing AI-focused Compose components. Can be integrated with any chat system, not just Stream Chat.

**Key Components (All in SDK):**
- `StreamingText`: Progressively reveals text word-by-word (ChatGPT-like streaming animation)
- `RichText`: Renders Markdown with code blocks and Chart.js diagrams
- `AITypingIndicator`: Animated typing states for AI thinking/processing
- `ChartJsDiagram`: WebView-based Chart.js rendering for AI-generated charts

**Sample Module (`stream-chat-android-ai-compose-sample`):**
Complete reference implementation showing how to integrate the UI components with Stream Chat, including:
- ChatViewModel & state management
- Network layer (Retrofit + custom backend API)
- Dependency wiring via a lightweight `ChatDependencies` holder
- Full conversation UI with AI agent lifecycle management

**Modules:**
- `stream-chat-android-ai-compose`: **Pure UI components SDK** (published to Maven Central)
- `stream-chat-android-ai-compose-sample`: **Stream Chat reference implementation**
- `metrics/stream-chat-android-ai-metrics`: SDK size tracking

**SDK Requirements:**
- Min SDK: 23 (Android 6.0 Marshmallow)
- Compile SDK: 36
- Target SDK: 36

## Build & Development Commands

### Building
```bash
# Build all modules
./gradlew build

# Build SDK module only
./gradlew :stream-chat-android-ai-compose:build

# Build sample app
./gradlew :stream-chat-android-ai-compose-sample:build

# Assemble release variant
./gradlew :stream-chat-android-ai-compose:assembleRelease

# Clean build
./gradlew clean build
```

### Testing
```bash
# Run all unit tests
./gradlew test

# Run tests for specific module with stack trace
./gradlew :stream-chat-android-ai-compose:testDebugUnitTest --stacktrace

# Run connected tests (requires device/emulator)
./gradlew connectedAndroidTest

# Run specific test class
./gradlew :stream-chat-android-ai-compose:testDebugUnitTest --tests "ClassName"
```

### Code Quality
```bash
# Run Detekt lint
./gradlew detekt

# Run Detekt with auto-correction
./gradlew detekt --auto-correct

# Run Android Lint
./gradlew lint

# Apply code formatting (Spotless)
./gradlew spotlessApply

# Check code formatting without applying
./gradlew spotlessCheck

# Generate Detekt baseline (for existing issues)
./gradlew detektBaseline
```

### Publishing
```bash
# Publish to Maven Local for testing
./gradlew publishToMavenLocal

# Publish to Maven Central (requires credentials)
./gradlew publish

# Print all artifacts that will be published
./gradlew printAllArtifacts

# Create snapshot build (appends UTC timestamp)
SNAPSHOT=true ./gradlew publish
```

### Running Sample App
```bash
# Install debug build on connected device
./gradlew :stream-chat-android-ai-compose-sample:installDebug

# Build and install debug APK
./gradlew :stream-chat-android-ai-compose-sample:assembleDebug
```

## Architecture

### SDK Module: Pure UI Components

The `stream-chat-android-ai-compose` module is a **lightweight, framework-agnostic library** containing only Jetpack Compose UI components. It has **zero dependencies** on Stream Chat, networking libraries, or ViewModels.

**Structure:**
```
stream-chat-android-ai-compose/
└── src/main/kotlin/io/getstream/chat/android/ai/compose/
    └── ui/component/
        ├── StreamingText.kt      # Word-by-word streaming animation
        ├── RichText.kt           # Markdown renderer with Chart.js support
        ├── AITypingIndicator.kt  # Animated AI state indicators
        └── ChartJsDiagram.kt     # WebView-based Chart.js renderer
```

**Dependencies (SDK):**
- Compose BOM (androidx.compose:compose-bom)
- Compose UI libraries (ui, ui-graphics, ui-tooling-preview, material3)
- Markdown Renderer (multiplatform-markdown-renderer)

**Design Principles:**
1. **Framework Agnostic**: No assumptions about chat system, state management, or networking
2. **Composable-First**: All components are pure @Composable functions
3. **Stateless**: Components don't manage their own state
4. **Explicit API**: All public APIs use `public` visibility and explicit return types
5. **Minimal Dependencies**: Only Compose and Markdown rendering

### Sample Module: Stream Chat Reference Implementation

The `stream-chat-android-ai-compose-sample` module demonstrates a **complete integration** with Stream Chat, implementing all business logic, networking, and state management.

**Structure:**
```
stream-chat-android-ai-compose-sample/
└── src/main/kotlin/io/getstream/chat/android/ai/compose/sample/
    ├── ChatDependencies.kt          # Dependency holder
    ├── App.kt                       # Application class
    ├── presentation/
    │   ├── chat/                    # Chat screen ViewModels
    │   │   ├── ChatViewModel.kt     # Conversation state management
    │   │   └── ChatUiState.kt       # UI state data classes
    │   └── conversations/           # Conversation list
    ├── domain/
    │   └── StreamMessageExt.kt      # AI message detection logic
    ├── data/
    │   ├── api/
    │   │   └── ChatAiApi.kt         # Retrofit API interface
    │   └── repository/
    │       ├── ChatAiRepository.kt   # Repository interface
    │       └── ChatAiService.kt      # Repository implementation
    ├── di/
    │   ├── ChatViewModelFactory.kt  # ViewModel factory
    │   └── NetworkModule.kt         # Retrofit/OkHttp/Moshi setup
    └── ui/                          # Full UI implementation
```

**Dependencies (Sample):**
- SDK module (`projects.streamChatAndroidAiCompose`)
- Stream Chat Android SDK (client, state, offline)
- Retrofit + OkHttp + Moshi (networking)
- Coroutines (async operations)
- Lifecycle ViewModels (state management)

## Key Architecture Patterns

### 1. SDK Component Usage

All SDK components are designed to be used independently:

```kotlin
// StreamingText: Animate text word-by-word
StreamingText(
    text = "Your AI response text here",
    animate = true,  // Enable streaming animation
    chunkDelayMs = 30  // Delay between words
) { displayedText ->
    Text(displayedText)
}

// RichText: Render markdown with code blocks
RichText(
    text = """
        # Hello
        ```kotlin
        fun example() = "code"
        ```
    """,
    modifier = Modifier.fillMaxWidth()
)

// AITypingIndicator: Show AI thinking state
AITypingIndicator(
    label = { Text("AI is thinking...") }
)

// ChartJsDiagram: Render Chart.js charts
ChartJsDiagram(
    chartJsJson = """{"type":"line","data":{...}}"""
)
```

### 2. Sample Implementation: Stream Chat Integration

The sample app shows one way to integrate with Stream Chat:

**Initialization** (`App.kt`):
```kotlin
lateinit var chatDependencies: ChatDependencies
    private set

override fun onCreate() {
    // 1. Initialize backend API client
    chatDependencies = ChatDependencies(
        baseUrl = "http://10.0.2.2:3000",
        enableLogging = BuildConfig.DEBUG,
    )

    // 2. Initialize Stream Chat
    val chatClient = ChatClient.Builder(apiKey, context)
        .withPlugins(offlinePlugin, statePlugin)
        .build()

    chatClient.connectUser(user, token).enqueue()
}
```

**ViewModel Integration** (`ChatViewModel.kt`):
- Watches Stream Chat channel state for messages
- Subscribes to AI-specific events (AIIndicatorUpdatedEvent, etc.)
- Maps Stream messages to UI state
- Handles AI agent lifecycle (start/stop)

**AI Message Detection** (`StreamMessageExt.kt`):
```kotlin
fun Message.isFromAi(): Boolean =
    extraData["ai_generated"] == true || user.id.startsWith("ai_bot-")
```

**Backend API** (`ChatAiApi.kt`):
- `POST /start-ai-agent`: Starts AI agent for channel
- `POST /stop-ai-agent`: Stops AI agent
- `POST /summarize`: Generates channel title summary

### 3. StreamingText Algorithm

**Word-by-Word Chunking:**
- Splits text using regex: `/(\s+|\S+)/`
- Preserves newlines as separate chunks
- Default delay: 30ms per chunk

**Smart Continuation:**
```kotlin
if (text.startsWith(previousText)) {
    // Continue from current position
    animateFromCurrent()
} else {
    // New text - reset and animate from start
    reset()
    animateFromStart()
}
```

### 4. Chart.js Integration

**Implementation**: WebView with Chart.js 4.4.0

**Loading Strategy:**
1. Try to load `chart.umd.min.js` from assets (offline)
2. Fallback to CDN: `https://cdn.jsdelivr.net/npm/chart.js@4.4.0/dist/chart.umd.min.js`

**Chart Detection in RichText:**
```kotlin
if (language?.lowercase() == "chartjs" && codeContent != null) {
    ChartJsDiagram(chartJsJson = codeContent)
}
```

## Configuration & Versioning

### Version Management

**Location**: `buildSrc/src/main/kotlin/io/getstream/chat/android/ai/Configuration.kt`

```kotlin
object Configuration {
    const val majorVersion = 0
    const val minorVersion = 1
    const val patchVersion = 0
    const val versionName = "$majorVersion.$minorVersion.$patchVersion"
    const val artifactGroup = "io.getstream"
}
```

**Snapshot Versioning:**
```bash
# Normal: "0.1.0"
# Snapshot: "0.1.0-20250103140530-SNAPSHOT" (UTC timestamp)
```

### Build Configuration

**SDK Kotlin Compiler Flags** (`stream-chat-android-ai-compose/build.gradle.kts`):
```kotlin
compilerOptions {
    freeCompilerArgs.addAll(
        "-progressive",                              // Enable progressive mode
        "-Xconsistent-data-class-copy-visibility",   // Match copy() visibility
        "-Xexplicit-api=strict"                      // Require explicit API declarations
    )
}
```

**Key Dependencies:**
- **SDK**: Compose BOM: 2025.11.00, Markdown Renderer: 0.38.1
- **Sample**: Stream Chat Android: 6.27.0, Retrofit: 2.11.0, Moshi: 1.15.1, OkHttp: 4.12.0

## Adding New UI Components to SDK

When adding new components to the SDK module:

1. **Place in `ui/component/` package**
2. **Use explicit API mode:**
   ```kotlin
   @Composable
   public fun MyComponent(
       text: String,
       modifier: Modifier = Modifier,
   ): Unit { ... }
   ```
3. **Keep stateless** - accept state as parameters
4. **Add comprehensive KDoc**
5. **No external dependencies** beyond Compose and Markdown
6. **Test in sample app** for real-world usage

## Integrating SDK with Other Chat Systems

The SDK components can be used with any chat system. Here's a pattern:

```kotlin
// 1. Your chat state model
data class YourChatState(
    val messages: List<YourMessage>,
    val isAiTyping: Boolean
)

// 2. Map to SDK components
@Composable
fun ChatScreen(state: YourChatState) {
    LazyColumn {
        items(state.messages) { message ->
            if (message.isFromAi) {
                // Use SDK streaming component
                StreamingText(
                    text = message.content,
                    animate = message.isGenerating
                ) { displayedText ->
                    RichText(text = displayedText)
                }
            } else {
                // Your user message UI
                Text(message.content)
            }
        }

        if (state.isAiTyping) {
            item { AITypingIndicator() }
        }
    }
}
```

## CI/CD Pipeline

**Workflow File**: `.github/workflows/ci.yml`

**Jobs:**
1. **Compile** (`assembleDebug --scan`)
2. **Spotless** (`spotlessCheck`)
3. **Lint** (`lint`)
4. **Unit Tests** (`testDebugUnitTest --stacktrace`)

**Triggered On:**
- Push to `develop` or `main`
- Pull requests to any branch
- Manual workflow dispatch

**Artifacts:**
- Unit test results uploaded on failure

**Concurrency**: Cancels in-progress runs for same ref

## Publishing & Release

### Maven Central Publishing

**Plugin**: `com.vanniktech.maven.publish` (0.35.0)

**Coordinates:**
- Group: `io.getstream`
- Artifact: `stream-chat-android-ai-compose`
- Version: From `Configuration.versionName`

**Published Artifacts:**
- Release AAR
- Sources JAR
- Javadoc JAR

**Snapshot Repository:**
```
https://central.sonatype.com/repository/maven-snapshots/io/getstream/stream-chat-android-ai-compose/
```

### Release Process

**PR Labels** (`.github/release.yaml`):
- `pr:breaking-change` → Breaking Changes 🛠
- `pr:new-feature` → New Features 🎉
- `pr:bug` → Bug Fixes 🐛
- `pr:improvement` → Improvements ✨
- `pr:documentation` → Documentation 📚
- `pr:dependencies` → Dependencies 📦
- `pr:internal` / `pr:ci` / `pr:test` → Internal 🧪
- `pr:demo-app` → Demo App 🧩

**Excluded from Changelog:**
- `pr:ignore-for-release`
- PRs by `github-actions[bot]` or `stream-public-bot`

## Testing Strategy

### SDK Module Testing
- **Unit Tests**: Basic component tests
- **Focus**: Verify component rendering logic
- **No Integration Tests**: SDK has no external dependencies

### Sample Module Testing
- **Unit Tests**: ViewModel logic, state management
- **Integration Tests**: Could test Stream Chat integration
- **Manual Testing**: Via sample app on device/emulator

### Adding Tests

**SDK Test Location:**
```
stream-chat-android-ai-compose/src/test/kotlin/
```

**Sample Test Location:**
```
stream-chat-android-ai-compose-sample/src/test/kotlin/
```

**Test Dependencies:**
```kotlin
testImplementation("junit:junit:4.13.2")
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.10.2")
```

## Code Quality Standards

### Detekt

**Config**: `config/detekt/detekt.yml`

**Key Settings:**
- Auto-correction enabled by default
- Formatting rules via `detekt-formatting` plugin
- Build-upon default config enabled

**Common Issues:**
- Unused imports (run `spotlessApply` to fix)
- Magic numbers
- Complex methods
- Naming conventions

### Spotless

**Applies:**
- Kotlin code formatting
- Kotlin Gradle script formatting
- Java formatting (if present)
- XML formatting

**Configuration**: Uses KtLint internally

### Android Lint

**Standard Rules:**
- No custom lint checks currently
- Target SDK: 36
- Reports stored in `build/reports/lint/`

## Pull Request Guidelines

### Required Checklist
- [ ] Issue linked (if exists)
- [ ] Tests updated (if applicable)
- [ ] Documentation updated (if applicable)
- [ ] Stream CLA signed (external contributors)

### Branch Strategy
- **Main Branch**: `develop` (not `main`)
- **Feature Branches**: `feature/description` or `fix/description`
- **Release Branches**: Created from `develop`

### PR Best Practices
1. Keep PRs focused on single concern
2. Add appropriate label from release.yaml categories
3. Update README if adding public API to SDK
4. Run `./gradlew build detekt spotlessCheck` before pushing
5. Test in sample app when modifying SDK components
6. Maintain backward compatibility for SDK module

## Common Development Tasks

### Adding a New SDK Component

1. Create composable in SDK's `ui/component/` package
2. Use **explicit API mode**:
   ```kotlin
   @Composable
   public fun MyComponent(
       text: String,
       modifier: Modifier = Modifier,
   ): Unit { ... }
   ```
3. Keep it stateless and framework-agnostic
4. Add comprehensive KDoc documentation
5. Update README with usage example
6. Test in sample app integration

### Modifying Sample Implementation

**Important**: Sample code is a reference, not part of the SDK API
- Free to use any state management approach
- Can modify ViewModels, networking, DI as needed
- Changes don't affect SDK module
- Consider if patterns should be documented for other integrators

### Testing SDK Independently

To verify SDK works standalone:

```bash
# 1. Publish to Maven Local
./gradlew :stream-chat-android-ai-compose:publishToMavenLocal

# 2. Create test project and add dependency
implementation("io.getstream:stream-chat-android-ai-compose:0.1.0")

# 3. Use components without any Stream Chat dependencies
```

## Troubleshooting

### Common Issues

**SDK won't compile - missing dependencies:**
- Ensure you haven't added Stream Chat or other external dependencies to SDK
- SDK should only depend on Compose and Markdown renderer

**Sample won't compile - unresolved references:**
- Verify all imports are updated after refactoring
- Run `./gradlew spotlessApply` to fix import ordering
- Check that moved files have correct package declarations

**Components not rendering correctly:**
- Verify parent composable provides necessary constraints
- Check Modifier parameters are being applied
- Test with different Compose preview configurations

### Debug Logging (Sample App Only)

**Enable All Logging:**
```kotlin
// In sample App.kt
ChatDependencies(baseUrl, enableLogging = true)
ChatClient.Builder(apiKey, context).logLevel(ChatLogLevel.ALL)
AndroidStreamLogger.installOnDebuggableApp(context)
```

**Key Log Tags:**
- `ChatViewModel`: ViewModel operations (sample)
- `HttpLoggingInterceptor`: Network requests (sample)
- `Stream`: Stream SDK events (sample)

## Additional Resources

**Stream Documentation:**
- Chat API: https://getstream.io/chat/docs/
- Android SDK: https://getstream.io/tutorials/android-chat/
- Compose UI Kit: https://getstream.io/chat/compose/tutorial/

**Library Dependencies:**
- Markdown Renderer: https://github.com/mikepenz/multiplatform-markdown-renderer
- Chart.js: https://www.chartjs.org/docs/latest/

**Internal References:**
- README.md: Public API documentation and SDK usage examples
- Build scripts: `buildSrc/` for shared Gradle configuration
- Sample app: Complete Stream Chat integration reference
