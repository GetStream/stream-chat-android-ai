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

package io.getstream.chat.android.ai.compose.component

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

/**
 * A composable that wraps content and provides streaming animation.
 * Progressively reveals text content word-by-word when streaming is active.
 * Animation continues until all content is displayed, even if streaming stops.
 *
 * @param text The full text content to display
 * @param isStreaming Whether the content is currently being streamed.
 * When false and no animation has started, content is shown immediately.
 * @param chunkDelayMs Delay in milliseconds between each chunk reveal. Lower values result in faster animation.
 * @param content The composable content that will receive the animated text
 */
@Composable
public fun StreamingText(
    text: String,
    isStreaming: Boolean = false,
    chunkDelayMs: Int = 30,
    content: @Composable (displayedText: String) -> Unit,
) {
    // Track the displayed text for animation
    var displayedText by remember { mutableStateOf("") }

    // Update displayed text progressively
    LaunchedEffect(text, isStreaming) {
        when {
            isNewMessage(displayedText, text) || !shouldAnimate(displayedText, isStreaming) -> {
                // New message or no animation needed - show immediately
                displayedText = text
            }
            shouldAnimateNewContent(displayedText, text, isStreaming) -> {
                // Animate new content chunk-by-chunk
                animateNewContent(displayedText, text, chunkDelayMs) { newText ->
                    displayedText = newText
                }
            }
            // If shouldAnimate is true but text hasn't grown, do nothing (wait for new content)
        }
    }

    content(displayedText)
}

/**
 * Checks if the incoming text represents a new message (not a continuation).
 */
private fun isNewMessage(displayedText: String, newText: String): Boolean =
    displayedText.isNotEmpty() && !newText.startsWith(displayedText)

/**
 * Determines if animation should occur based on current state.
 * Animation occurs if there's already displayed content (to continue animation) or if streaming is active.
 */
private fun shouldAnimate(displayedText: String, isStreaming: Boolean): Boolean =
    displayedText.isNotEmpty() || isStreaming

/**
 * Checks if new content should be animated.
 */
private fun shouldAnimateNewContent(displayedText: String, text: String, isStreaming: Boolean): Boolean =
    text.length > displayedText.length && shouldAnimate(displayedText, isStreaming)

/**
 * Animates new content chunk-by-chunk.
 */
private suspend fun animateNewContent(
    currentText: String,
    fullText: String,
    chunkDelayMs: Int,
    onUpdate: (String) -> Unit,
) {
    val newContent = fullText.substring(currentText.length)
    val chunks = splitIntoWords(newContent)

    // Use StringBuilder for efficient string concatenation
    val builder = StringBuilder(currentText)
    for (chunk in chunks) {
        builder.append(chunk)
        onUpdate(builder.toString())
        delay(chunkDelayMs.toLong())
    }
}

/**
 * Splits text into chunks for animation.
 *
 * Splits text into words and whitespace chunks, preserving line breaks.
 * Each chunk is either a word/non-whitespace sequence or a whitespace sequence.
 */
private fun splitIntoWords(text: String): List<String> {
    if (text.isEmpty()) return emptyList()

    val chunks = mutableListOf<String>()
    val lines = text.split('\n')

    for ((index, line) in lines.withIndex()) {
        // Split into words and whitespace chunks
        val words = WORD_SPLIT_REGEX.findAll(line).map { it.value }.filter { it.isNotEmpty() }
        chunks.addAll(words)
        // Preserve newline if not the last line
        if (index < lines.size - 1) {
            chunks.add("\n")
        }
    }

    return chunks
}

// Compile regex once to avoid recompilation on every call
private val WORD_SPLIT_REGEX = Regex("""(\s+|\S+)""")
