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

package io.getstream.chat.android.ai.compose.ui.component

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.ui.component.internal.ChatAiIcons
import io.getstream.chat.android.ai.compose.ui.component.internal.SelectedAttachmentList
import kotlinx.coroutines.launch

/**
 * Chat composer with attach, voice, and send buttons.
 *
 * This composable provides full control over the message state. It displays different
 * action buttons based on state:
 * - Stop button when streaming
 * - Send button when text is entered
 * - Voice button when text is empty
 *
 * The composer includes:
 * - Text input field with placeholder
 * - Attachment button for selecting images
 * - Voice input button with speech-to-text
 * - Send button (shown when text is not empty)
 * - Stop button (shown during AI streaming)
 *
 * @param onSendClick Callback invoked when the send button is clicked with the composed message data.
 * @param onStopClick Callback invoked when the stop button is clicked (during AI streaming).
 * @param isStreaming Whether the AI is currently streaming a response.
 * @param modifier The modifier to be applied to the composer.
 */
@Composable
public fun ChatComposer(
    onSendClick: (data: MessageData) -> Unit,
    onStopClick: () -> Unit,
    isStreaming: Boolean,
    modifier: Modifier = Modifier,
) {
    var messageData by remember { mutableStateOf(MessageData()) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val handleSendClick = {
        keyboardController?.hide()
        onSendClick(messageData)
        messageData = MessageData()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = PickMultipleVisualMedia(),
    ) { uris ->
        messageData = messageData.copy(attachments = messageData.attachments + uris)
    }

    Row(
        modifier = modifier
            .imePadding()
            .navigationBarsPadding()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        AttachmentButton(
            enabled = !isStreaming,
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly,
                        maxItems = 3,
                    ),
                )
            },
        )

        TextField(
            modifier = Modifier.fillMaxWidth(),
            data = messageData,
            onTextChange = { messageData = messageData.copy(text = it) },
            onRemoveAttachment = { messageData = messageData.copy(attachments = messageData.attachments - it) },
            isStreaming = isStreaming,
            onSendClick = handleSendClick,
            onStopClick = onStopClick,
        )
    }
}

/**
 * Data class representing a message composed by the user.
 *
 * @param text The text content of the message.
 * @param attachments The set of attachment URIs to include with the message.
 */
public data class MessageData(
    val text: String = "",
    val attachments: Set<Uri> = emptySet(),
)

@Composable
private fun TextField(
    modifier: Modifier,
    data: MessageData,
    onTextChange: (String) -> Unit,
    onRemoveAttachment: (Uri) -> Unit,
    isStreaming: Boolean,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
) {
    // Remember the text that existed before starting speech recognition
    var textBeforeSpeech by remember { mutableStateOf("") }

    val onTextRecognized = { recognizedText: String ->
        onTextChange(
            if (textBeforeSpeech.isBlank()) {
                recognizedText
            } else {
                "${textBeforeSpeech.trim()} $recognizedText"
            },
        )
    }

    val speechToTextState = rememberSpeechToTextButtonState(
        onPartialResult = onTextRecognized,
        onFinalResult = onTextRecognized,
    )

    // Update textBeforeSpeech when recording starts/stops
    LaunchedEffect(speechToTextState.isRecording()) {
        if (speechToTextState.isRecording()) {
            textBeforeSpeech = data.text
        }
    }

    val trailingButton = when {
        isStreaming -> "stop"
        data.text.isNotBlank() && !speechToTextState.isRecording() -> "send"
        else -> null
    }

    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    SnackbarHost(hostState = snackbarHostState)

    val interactionSource = remember { MutableInteractionSource() }

    BasicTextField(
        modifier = modifier.defaultMinSize(minHeight = LocalMinimumInteractiveComponentSize.current),
        value = data.text,
        onValueChange = onTextChange,
        enabled = !isStreaming && !speechToTextState.isRecording(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { onSendClick() }),
        textStyle = resolveTextFieldStyle(interactionSource, disabled = isStreaming),
        cursorBrush = SolidColor(OutlinedTextFieldDefaults.colors().cursorColor),
        interactionSource = interactionSource,
        maxLines = 6,
        minLines = 1,
        decorationBox = { innerTextField ->
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Column {
                    AnimatedContent(targetState = data.attachments.isNotEmpty()) { hasAttachments ->
                        if (hasAttachments) {
                            SelectedAttachmentList(
                                modifier = Modifier.fillMaxWidth(),
                                uris = data.attachments,
                                onRemoveAttachment = onRemoveAttachment,
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.Bottom) {
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.CenterStart,
                        ) {
                            TextInputField(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
                                showPlaceholder = data.text.isBlank(),
                                innerTextField = innerTextField,
                            )
                        }
                        AnimatedContent(targetState = !isStreaming) { showVoiceButton ->
                            if (showVoiceButton) {
                                SpeechToTextButton(
                                    state = speechToTextState,
                                    onPermissionDenied = {
                                        coroutineScope.launch {
                                            val result = snackbarHostState.showSnackbar(
                                                message = "Microphone permission is required to record audio",
                                                actionLabel = "Settings",
                                                withDismissAction = true,
                                            )
                                            if (result == SnackbarResult.ActionPerformed) {
                                                context.openSettings()
                                            }
                                        }
                                    },
                                )
                            }
                        }
                        AnimatedContent(targetState = trailingButton) { button ->
                            when (button) {
                                "stop" -> {
                                    TrailingIconButton(
                                        icon = ChatAiIcons.Stop,
                                        contentDescription = "Stop",
                                        onClick = onStopClick,
                                    )
                                }
                                "send" -> {
                                    TrailingIconButton(
                                        icon = ChatAiIcons.Send,
                                        contentDescription = "Send",
                                        onClick = onSendClick,
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

@Composable
private fun resolveTextFieldStyle(
    interactionSource: MutableInteractionSource,
    disabled: Boolean,
): TextStyle {
    val colors = OutlinedTextFieldDefaults.colors()
    val textStyle = LocalTextStyle.current
    val textColor = textStyle.color.takeOrElse {
        val focused = interactionSource.collectIsFocusedAsState().value
        when {
            disabled -> colors.disabledTextColor
            focused -> colors.focusedTextColor
            else -> colors.unfocusedTextColor
        }
    }
    return textStyle.merge(TextStyle(color = textColor))
}

@Composable
private fun AttachmentButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    OutlinedIconButton(
        enabled = enabled,
        onClick = onClick,
        colors = IconButtonDefaults.outlinedIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Icon(
            imageVector = ChatAiIcons.Add,
            contentDescription = "Add context",
        )
    }
}

@Composable
private fun TrailingIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    FilledIconButton(onClick = onClick) {
        Icon(imageVector = icon, contentDescription = contentDescription)
    }
}

private fun Context.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}

@Composable
private fun TextInputField(
    modifier: Modifier,
    showPlaceholder: Boolean,
    innerTextField: @Composable () -> Unit,
) {
    Box(modifier = modifier) {
        if (showPlaceholder) {
            Text(
                text = "Ask Assistant",
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        innerTextField()
    }
}

@Preview(showBackground = true)
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ChatComposerPreview() {
    MaterialTheme {
        ChatComposer(
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
    MaterialTheme {
        ChatComposer(
            onSendClick = {},
            onStopClick = {},
            isStreaming = true,
        )
    }
}
