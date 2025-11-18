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

package io.getstream.chat.android.ai.compose.sample.ui.chat

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.sample.chat.Message
import io.getstream.chat.android.ai.compose.sample.ui.components.ChatComposer
import io.getstream.chat.android.ai.compose.sample.ui.components.ChatMessageItem
import io.getstream.chat.android.ai.compose.sample.ui.components.ChatTopBar
import kotlinx.coroutines.delay

/**
 * Main chat scaffold containing the top bar, message list, and composer.
 */
@Composable
public fun ChatScaffold(
    messages: List<Message>,
    inputText: String,
    isStreaming: Boolean,
    onInputTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    title: String,
    selectedModel: String,
    onMenuClick: () -> Unit,
    onModelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current
    var topBarHeight by remember { mutableStateOf(0.dp) }
    var composerHeight by remember { mutableStateOf(0.dp) }

    // Track the last message's content for streaming updates
    val lastMessage = messages.lastOrNull()
    val lastMessageContent = lastMessage?.content ?: ""

    // Auto-scroll during streaming - trigger on each content update
    // Using the content string itself as key ensures it triggers on every character change
    LaunchedEffect(isStreaming, lastMessageContent) {
        if (isStreaming && messages.isNotEmpty() && lastMessageContent.isNotEmpty()) {
            // Use immediate scroll during streaming for smooth real-time updates
            listState.scrollToItem(messages.size - 1)
        }
    }

    // Auto-scroll to bottom when new messages arrive or when streaming ends
    LaunchedEffect(messages.size, isStreaming) {
        if (messages.isNotEmpty()) {
            delay(50) // Small delay to ensure layout is complete
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        // Message list - content can scroll behind system bars and components
        // Blur gradients in ChatTopBar and ChatComposer handle visual effects
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = topBarHeight,
                bottom = composerHeight,
            ),
        ) {
            items(
                key = Message::id,
                items = messages,
            ) { message ->
                ChatMessageItem(
                    message = message,
                )
            }
        }

        // Top bar - positioned absolutely at top
        ChatTopBar(
            title = title,
            selectedModel = selectedModel,
            onMenuClick = onMenuClick,
            onModelClick = onModelClick,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .onGloballyPositioned { coordinates ->
                    topBarHeight = with(density) { coordinates.size.height.toDp() }
                },
        )

        // Composer at bottom - with proper insets for system bars and IME
        // The blur gradient is now inside ChatComposer
        ChatComposer(
            text = inputText,
            onTextChange = onInputTextChange,
            onSend = onSend,
            onStop = onStop,
            isStreaming = isStreaming,
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .onGloballyPositioned { coordinates ->
                    composerHeight = with(density) { coordinates.size.height.toDp() }
                },
        )
    }
}
