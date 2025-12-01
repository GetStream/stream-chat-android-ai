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

package io.getstream.chat.android.ai.compose.sample.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.presentation.ChatUiState
import io.getstream.chat.android.ai.compose.ui.component.RichText
import io.getstream.chat.android.ai.compose.ui.component.StreamingText

/**
 * Displays a single chat message with proper styling based on role.
 * User messages appear on the right with a colored bubble.
 * Assistant messages fill the width without a bubble.
 *
 * @param message The message to display
 * @param modifier Modifier to be applied to the message item
 * @param isStreaming Whether this message is currently being streamed. When true, content will animate in.
 */
@Composable
public fun ChatMessageItem(
    message: ChatUiState.Message,
    modifier: Modifier = Modifier,
    isStreaming: Boolean = false,
) {
    val isUser = message.role is ChatUiState.Message.Role.User

    SelectionContainer(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        ) {
            when (message.role) {
                ChatUiState.Message.Role.Assistant -> {
                    StreamingText(
                        text = message.content,
                        animate = isStreaming,
                    ) { displayedText ->
                        RichText(
                            text = displayedText,
                        )
                    }
                }

                ChatUiState.Message.Role.User -> {
                    Spacer(modifier = Modifier.weight(.2f))
                    MessageBubble(modifier = Modifier.weight(.8f, fill = false)) {
                        RichText(
                            text = message.content,
                        )
                    }
                }

                ChatUiState.Message.Role.Other -> {
                    MessageBubble(modifier = Modifier.weight(.8f, fill = false)) {
                        RichText(
                            text = message.content,
                        )
                    }
                    Spacer(modifier = Modifier.weight(.2f))
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f))
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        content()
    }
}
