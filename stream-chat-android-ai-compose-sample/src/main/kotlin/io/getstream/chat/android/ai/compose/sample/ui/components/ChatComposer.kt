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

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.sample.R
import io.getstream.chat.android.ai.compose.sample.ui.theme.AppTheme

/**
 * Chat composer with attach, voice, and send buttons.
 * Displays different action buttons based on state:
 * - Stop button when streaming
 * - Send button when text is entered
 * - Voice button when text is empty
 *
 * @param text The current text input value
 * @param onTextChange Callback when text changes
 * @param onSend Callback when send button is clicked
 * @param onStop Callback when stop button is clicked (during streaming)
 * @param isStreaming Whether the AI is currently streaming a response
 * @param modifier Modifier to be applied to the composer
 * @param onAttachClick Callback when attach button is clicked
 * @param onVoiceClick Callback when voice button is clicked
 */
@Composable
public fun ChatComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onSend: () -> Unit,
    onStop: () -> Unit,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
    onAttachClick: () -> Unit = {},
    onVoiceClick: () -> Unit = {},
) {
    // Main input field content with blur gradient applied to modifier
    // The gradient creates a visual fade effect that blends with the message list behind it
    Row(
        modifier = modifier
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                        MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                        MaterialTheme.colorScheme.background,
                    ),
                ),
            )
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ChatFloatingButton(
            onClick = onAttachClick,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = "Add context",
            )
        }

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    text = "Ask Assistant",
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            },
            trailingIcon = {
                val button = when {
                    isStreaming -> "stop"
                    text.isNotBlank() -> "send"
                    else -> "voice"
                }
                AnimatedContent(
                    targetState = button,
                ) { state ->
                    when (state) {
                        "stop" -> FilledIconButton(
                            onClick = onStop,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_stop),
                                contentDescription = "Stop",
                            )
                        }

                        "send" -> FilledIconButton(
                            onClick = onSend,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_send),
                                contentDescription = "Send",
                            )
                        }

                        else -> IconButton(
                            onClick = onVoiceClick,
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_mic),
                                contentDescription = "Voice input",
                            )
                        }
                    }
                }
            },
            shape = MaterialTheme.shapes.extraLarge,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                disabledContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.outline,
            ),
            maxLines = 6,
            minLines = 1,
            enabled = !isStreaming,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatComposerEmptyPreview() {
    AppTheme {
        ChatComposer(
            text = "",
            onTextChange = {},
            onSend = {},
            onStop = {},
            isStreaming = false,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatComposerFilledPreview() {
    AppTheme {
        ChatComposer(
            text = "What is Stream Chat?",
            onTextChange = {},
            onSend = {},
            onStop = {},
            isStreaming = false,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatComposerStreamingPreview() {
    AppTheme {
        ChatComposer(
            text = "",
            onTextChange = {},
            onSend = {},
            onStop = {},
            isStreaming = true,
        )
    }
}
