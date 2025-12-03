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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
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
 * @param onSendClick Callback when send button is clicked
 * @param onStopClick Callback when stop button is clicked (during streaming)
 * @param isStreaming Whether the AI is currently streaming a response
 * @param modifier Modifier to be applied to the composer
 * @param isRecording Whether the microphone is currently recording (voice input)
 * @param rmsdB The current RMS dB value from the speech recognizer (0-10 range typically)
 * @param onAttachClick Callback when attach button is clicked
 * @param onVoiceClick Callback when voice button is clicked
 * @param onSeeTextClick Callback when "See text" button is clicked (stops recording and shows text)
 * @param onCancelVoiceClick Callback when cancel voice button is clicked (stops recording and clears text)
 */
@Composable
public fun ChatComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
    isRecording: Boolean = false,
    rmsdB: Float = 0f,
    onAttachClick: () -> Unit = {},
    onVoiceClick: () -> Unit = {},
    onSeeTextClick: () -> Unit = {},
    onCancelVoiceClick: () -> Unit = {},
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val handleSendClick = {
        keyboardController?.hide()
        onSendClick()
    }

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
        verticalAlignment = Alignment.Bottom,
    ) {
        ChatFloatingButton(
            onClick = onAttachClick,
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = "Add context",
            )
        }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            text = text,
            onTextChange = onTextChange,
            isStreaming = isStreaming,
            isRecording = isRecording,
            rmsdB = rmsdB,
            onSendClick = handleSendClick,
            onStopClick = onStopClick,
            onVoiceClick = onVoiceClick,
            onSeeTextClick = onSeeTextClick,
            onCancelVoiceClick = onCancelVoiceClick,
        )
    }
}

@Composable
private fun TextField(
    modifier: Modifier,
    text: String,
    onTextChange: (String) -> Unit,
    isStreaming: Boolean,
    isRecording: Boolean,
    rmsdB: Float,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
    onVoiceClick: () -> Unit,
    onSeeTextClick: () -> Unit,
    onCancelVoiceClick: () -> Unit,
) {
    val colors = OutlinedTextFieldDefaults.colors()
    val interactionSource = remember { MutableInteractionSource() }
    // If color is not provided via the text style, use content color as a default
    val textStyle = LocalTextStyle.current
    val textColor = textStyle.color.takeOrElse {
        val focused = interactionSource.collectIsFocusedAsState().value
        when {
            isStreaming -> colors.disabledTextColor
            focused -> colors.focusedTextColor
            else -> colors.unfocusedTextColor
        }
    }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textColor))

    val trailingButton = when {
        isStreaming -> "stop"
        text.isNotBlank() -> "send"
        !isRecording -> "voice"
        else -> null
    }

    BasicTextField(
        modifier = modifier.defaultMinSize(minHeight = LocalMinimumInteractiveComponentSize.current),
        value = text,
        onValueChange = onTextChange,
        enabled = !isStreaming && !isRecording,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { onSendClick() }),
        textStyle = mergedTextStyle,
        cursorBrush = SolidColor(colors.cursorColor),
        interactionSource = interactionSource,
        maxLines = 6,
        minLines = 1,
        decorationBox = { innerTextField ->
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    AnimatedContent(
                        targetState = isRecording,
                    ) { visible ->
                        if (visible) {
                            TextButton(
                                onClick = onSeeTextClick,
                            ) {
                                Text(text = "See text")
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        // Leading icon
                        AnimatedContent(
                            targetState = isRecording,
                        ) { visible ->
                            if (visible) {
                                IconButton(
                                    onClick = onCancelVoiceClick,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_cancel),
                                        contentDescription = "Cancel voice input",
                                    )
                                }
                            }
                        }

                        // Text field
                        AnimatedContent(
                            modifier = Modifier.weight(1f),
                            targetState = isRecording,
                        ) { recording ->
                            if (recording) {
                                WaveformIndicator(
                                    rmsdB = rmsdB,
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            } else {
                                Box(
                                    modifier = Modifier.padding(start = 16.dp),
                                ) {
                                    if (text.isEmpty()) {
                                        Text(
                                            text = "Ask Assistant",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        }

                        // Trailing icon
                        AnimatedContent(
                            targetState = trailingButton,
                        ) { button ->
                            when (button) {
                                "stop" -> FilledIconButton(
                                    onClick = onStopClick,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_stop),
                                        contentDescription = "Stop",
                                    )
                                }

                                "send" -> FilledIconButton(
                                    onClick = onSendClick,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_send),
                                        contentDescription = "Send",
                                    )
                                }

                                "voice" -> IconButton(
                                    onClick = onVoiceClick,
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.ic_mic),
                                        contentDescription = "Voice input",
                                    )
                                }

                                else -> Spacer(
                                    modifier = Modifier.minimumInteractiveComponentSize(),
                                )
                            }
                        }
                    }
                }
            }
        },
    )
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatComposerEmptyPreview() {
    AppTheme {
        ChatComposer(
            text = "",
            onTextChange = {},
            onSendClick = {},
            onStopClick = {},
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
            onSendClick = {},
            onStopClick = {},
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
            onSendClick = {},
            onStopClick = {},
            isStreaming = true,
        )
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatComposerRecordingPreview() {
    AppTheme {
        ChatComposer(
            text = "",
            onTextChange = {},
            onSendClick = {},
            onStopClick = {},
            isStreaming = false,
            isRecording = true,
            rmsdB = 10f,
        )
    }
}
