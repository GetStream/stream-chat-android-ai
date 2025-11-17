/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android-ai/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.getstream.chat.android.ai.compose.sample.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

/**
 * Data class representing a chat conversation.
 */
public data class ChatConversation(
    val id: String,
    val title: String,
    val messages: List<Message>,
    val lastMessageTime: Long = System.currentTimeMillis(),
)

/**
 * UI state for the chat screen.
 */
public data class ChatUiState(
    val currentChatId: String? = null,
    val messages: List<Message> = emptyList(),
    val isStreaming: Boolean = false,
    val inputText: String = "",
    val conversations: List<ChatConversation> = emptyList(),
    val selectedModel: String = "GPT-4o",
)

/**
 * ViewModel for managing chat state and interactions.
 */
public class ChatViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    private var currentStreamingMessageId: String? = null

    init {
        // Initialize with sample conversations showcasing markdown features
        _uiState.update { state ->
            state.copy(
                conversations = createSampleConversations(),
            )
        }
    }

    /**
     * Updates the input text.
     */
    fun updateInputText(text: String) {
        _uiState.update { it.copy(inputText = text) }
    }

    /**
     * Sends a message and simulates a streaming response.
     */
    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isStreaming) return

        val userMessage = Message(
            id = UUID.randomUUID().toString(),
            role = MessageRole.User,
            content = text,
        )

        _uiState.update { state ->
            val newMessages = state.messages + userMessage
            state.copy(
                messages = newMessages,
                inputText = "",
            )
        }

        // Simulate streaming response
        startStreamingResponse(text)
    }

    /**
     * Starts a streaming response simulation.
     */
    private fun startStreamingResponse(userInput: String) {
        viewModelScope.launch {
            val assistantMessageId = UUID.randomUUID().toString()
            currentStreamingMessageId = assistantMessageId

            val sampleResponse = when {
                userInput.contains("code", ignoreCase = true) || userInput.contains("function", ignoreCase = true) -> {
                    "Here's a code example:\n\n```kotlin\nfun greet(name: String): String {\n    return \"Hello, \$name!\"\n}\n\nfun main() {\n    println(greet(\"World\"))\n}\n```\n\nThis function demonstrates basic Kotlin syntax with **parameters** and *return types*."
                }
                userInput.contains("python", ignoreCase = true) -> {
                    "Here's a Python example:\n\n```python\ndef calculate_sum(a, b):\n    \"\"\"Calculate the sum of two numbers.\"\"\"\n    return a + b\n\nresult = calculate_sum(5, 3)\nprint(f\"The sum is {result}\")\n```\n\nThis shows a simple Python function with documentation."
                }
                userInput.contains("javascript", ignoreCase = true) || userInput.contains("js", ignoreCase = true) -> {
                    "Here's a JavaScript example:\n\n```javascript\nconst greet = (name) => {\n    return `Hello, \${name}!`;\n};\n\nconsole.log(greet(\"World\"));\n```\n\nThis uses **arrow functions** and *template literals*."
                }
                else -> {
                    "This is a sample response that demonstrates streaming. " +
                        "The response can include **markdown formatting**, `inline code`, and code blocks.\n\n" +
                        "Try asking for code examples in different languages like:\n" +
                        "- \"Show me a Kotlin function\"\n" +
                        "- \"Write a Python function\"\n" +
                        "- \"JavaScript code example\""
                }
            }

            _uiState.update { state ->
                val streamingMessage = Message(
                    id = assistantMessageId,
                    role = MessageRole.Assistant,
                    content = "",
                    isStreaming = true,
                )
                state.copy(
                    messages = state.messages + streamingMessage,
                    isStreaming = true,
                )
            }

            // Simulate delay before response starts (shows typing indicator)
            delay(2000) // 1 second delay to show typing indicator

            // Simulate streaming by adding characters in buffers
            var currentContent = ""
            val bufferSize = 5 // Number of characters to accumulate before sending
            var charIndex = 0

            while (charIndex < sampleResponse.length) {
                if (currentStreamingMessageId != assistantMessageId) {
                    // Streaming was stopped
                    return@launch
                }

                // Calculate how many characters to process in this chunk
                val remainingChars = sampleResponse.length - charIndex
                val chunkSize = minOf(bufferSize, remainingChars)

                // Extract chunk and add to current content
                val chunk = sampleResponse.substring(charIndex, charIndex + chunkSize)
                currentContent += chunk
                charIndex += chunkSize

                delay(50) // Simulate network delay per buffer

                _uiState.update { state ->
                    val updatedMessages = state.messages.map { message ->
                        if (message.id == assistantMessageId) {
                            message.copy(content = currentContent, isStreaming = true)
                        } else {
                            message
                        }
                    }
                    state.copy(messages = updatedMessages)
                }
            }

            // Finalize the message
            _uiState.update { state ->
                val updatedMessages = state.messages.map { message ->
                    if (message.id == assistantMessageId) {
                        message.copy(isStreaming = false)
                    } else {
                        message
                    }
                }
                state.copy(
                    messages = updatedMessages,
                    isStreaming = false,
                )
            }

            currentStreamingMessageId = null
        }
    }

    /**
     * Stops the current streaming response.
     */
    fun stopStreaming() {
        currentStreamingMessageId = null
        _uiState.update { state ->
            val updatedMessages = state.messages.map { message ->
                if (message.isStreaming) {
                    message.copy(isStreaming = false)
                } else {
                    message
                }
            }
            state.copy(
                messages = updatedMessages,
                isStreaming = false,
            )
        }
    }

    /**
     * Starts a new chat conversation.
     */
    fun startNewChat() {
        _uiState.update { state ->
            state.copy(
                currentChatId = null,
                messages = emptyList(),
                inputText = "",
                isStreaming = false,
            )
        }
        currentStreamingMessageId = null
    }

    /**
     * Loads a conversation by ID.
     */
    fun loadConversation(conversationId: String) {
        val conversation = _uiState.value.conversations.find { it.id == conversationId }
        _uiState.update { state ->
            state.copy(
                currentChatId = conversationId,
                messages = conversation?.messages ?: emptyList(),
                inputText = "",
                isStreaming = false,
            )
        }
        currentStreamingMessageId = null
    }

    /**
     * Updates the selected model.
     */
    fun selectModel(model: String) {
        _uiState.update { it.copy(selectedModel = model) }
    }
}
