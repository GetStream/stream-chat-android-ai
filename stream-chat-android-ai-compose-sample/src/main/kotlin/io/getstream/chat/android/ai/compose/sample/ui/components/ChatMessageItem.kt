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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.sample.chat.Message
import io.getstream.chat.android.ai.compose.sample.chat.MessageRole
import io.getstream.chat.android.compose.ui.components.TypingIndicator

/**
 * Displays a single chat message with proper styling based on role.
 */
@Composable
public fun ChatMessageItem(
    message: Message,
    modifier: Modifier = Modifier,
) {
    val isUser = message.role is MessageRole.User

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        if (isUser) {
            Spacer(modifier = Modifier.weight(.2f))
            MessageBubble(modifier = Modifier.weight(.8f, fill = false)) {
                if (message.isStreaming && message.content.isEmpty()) {
                    TypingIndicator()
                } else {
                    MarkdownText(
                        text = message.content,
                        textColor = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        } else {
            if (message.isStreaming && message.content.isEmpty()) {
                TypingIndicator()
            } else {
                MarkdownText(
                    text = message.content,
                )
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
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        content = content,
    )
}
