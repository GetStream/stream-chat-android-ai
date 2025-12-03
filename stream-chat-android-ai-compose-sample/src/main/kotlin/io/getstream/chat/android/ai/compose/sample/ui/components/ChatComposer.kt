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
import android.net.Uri
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
import io.getstream.chat.android.ai.compose.ui.component.SpeechToTextButton
import io.getstream.chat.android.ai.compose.ui.component.internal.SelectedAttachmentList
import io.getstream.chat.android.ai.compose.ui.component.internal.rememberPhotoPickerLauncher
import io.getstream.chat.android.ai.compose.ui.component.rememberSpeechToTextButtonState
import kotlin.collections.emptyList

public data class MessageData(
    val text: String = "",
    val attachments: List<Uri> = emptyList(),
)

@Composable
public fun ChatComposer(
    onSendClick: (MessageData) -> Unit,
    onStopClick: () -> Unit,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
) {
    var state by remember { mutableStateOf(MessageData()) }
    ChatComposer(
        text = state.text,
        attachments = state.attachments,
        onTextChange = { newText ->
            state = state.copy(text = newText)
        },
        onAttachmentsAdded = { newAttachments ->
            state = state.copy(attachments = state.attachments + newAttachments)
        },
        onAttachmentRemoved = { attachmentToRemove ->
            state = state.copy(attachments = state.attachments - attachmentToRemove)
        },
        onSendClick = {
            onSendClick(state)
            state = MessageData()
        },
        onStopClick = onStopClick,
        isStreaming = isStreaming,
        modifier = modifier,
    )
}

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
 */
@Composable
public fun ChatComposer(
    text: String,
    attachments: List<Uri>,
    onTextChange: (String) -> Unit,
    onAttachmentsAdded: (List<Uri>) -> Unit,
    onAttachmentRemoved: (Uri) -> Unit,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val handleSendClick = {
        keyboardController?.hide()
        onSendClick()
    }

    val photoPickerLauncher = rememberPhotoPickerLauncher { uris ->
        onAttachmentsAdded(uris)
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
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageOnly,
                        3,
                    ),
                )
            },
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_add),
                contentDescription = "Add context",
            )
        }

        TextField(
            modifier = Modifier.fillMaxWidth(),
            text = text,
            attachments = attachments.toList(),
            onTextChange = onTextChange,
            onRemoveAttachment = onAttachmentRemoved,
            isStreaming = isStreaming,
            onSendClick = handleSendClick,
            onStopClick = onStopClick,
        )
    }
}

@Composable
private fun TextField(
    modifier: Modifier,
    text: String,
    attachments: List<Uri>,
    onTextChange: (String) -> Unit,
    onRemoveAttachment: (Uri) -> Unit,
    isStreaming: Boolean,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
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

    val speechToTextState = rememberSpeechToTextButtonState()

    val trailingButton = when {
        isStreaming -> "stop"
        text.isNotBlank() && !speechToTextState.isRecording() -> "send"
        else -> null
    }

    val currentText by rememberUpdatedState(text)

    BasicTextField(
        modifier = modifier.defaultMinSize(minHeight = LocalMinimumInteractiveComponentSize.current),
        value = text,
        onValueChange = onTextChange,
        enabled = !isStreaming && !speechToTextState.isRecording(),
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
                Column {
                    val hasAttachments = attachments.isNotEmpty()
                    if (hasAttachments) {
                        SelectedAttachmentList(
                            uris = attachments,
                            onRemoveAttachment = onRemoveAttachment,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            AnimatedContent(
                                targetState = !speechToTextState.isRecording(),
                            ) { visible ->
                                if (visible) {
                                    Box(
                                        Modifier.padding(start = 16.dp),
                                    ) {
                                        if (text.isEmpty()) {
                                            Text(
                                                text = "Ask Assistant",
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                                    alpha = 0.6f,
                                                ),
                                                style = MaterialTheme.typography.bodyLarge,
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            }
                            SpeechToTextButton(
                                state = speechToTextState,
                                onTextRecognized = { recognizedText ->
                                    onTextChange(if (currentText.isBlank()) recognizedText else "$currentText $recognizedText")
                                },
                            )
                        }

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
            attachments = emptyList(),
            onTextChange = {},
            onAttachmentsAdded = {},
            onAttachmentRemoved = {},
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
            attachments = emptyList(),
            onTextChange = {},
            onAttachmentsAdded = {},
            onAttachmentRemoved = {},
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
            attachments = emptyList(),
            onTextChange = {},
            onAttachmentsAdded = {},
            onAttachmentRemoved = {},
            onSendClick = {},
            onStopClick = {},
            isStreaming = true,
        )
    }
}
