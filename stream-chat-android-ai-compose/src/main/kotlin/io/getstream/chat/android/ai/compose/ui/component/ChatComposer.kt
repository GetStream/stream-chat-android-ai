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

@file:Suppress("TooManyFunctions")

package io.getstream.chat.android.ai.compose.ui.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickMultipleVisualMedia
import androidx.annotation.DrawableRes
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.ui.component.internal.AttachmentList
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
 * - Add button for selecting images
 * - Voice input button with speech-to-text
 * - Send button (shown when text is not empty)
 * - Stop button (shown during AI generating)
 *
 * @param onSendClick Callback invoked when the send button is clicked with the composed message data.
 * @param onStopClick Callback invoked when the stop button is clicked (during AI streaming).
 * @param isGenerating Whether the AI is currently generating a response.
 * @param modifier The modifier to be applied to the composer.
 * @param messageData The initial message data to be displayed in the input field.
 */
@Composable
public fun ChatComposer(
    onSendClick: (data: MessageData) -> Unit,
    onStopClick: () -> Unit,
    isGenerating: Boolean,
    modifier: Modifier = Modifier,
    messageData: MessageData = MessageData(),
) {
    var messageData by remember { mutableStateOf(messageData) }

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
        val snackbarHostState = remember { SnackbarHostState() }
        SnackbarHost(hostState = snackbarHostState)

        AddButton(
            enabled = !isGenerating,
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
            snackbarHostState = snackbarHostState,
            data = messageData,
            onTextChange = { messageData = messageData.copy(text = it) },
            onRemoveAttachment = { messageData = messageData.copy(attachments = messageData.attachments - it) },
            isGenerating = isGenerating,
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

@Suppress("LongParameterList")
@Composable
private fun TextField(
    modifier: Modifier,
    snackbarHostState: SnackbarHostState,
    data: MessageData,
    onTextChange: (String) -> Unit,
    onRemoveAttachment: (Uri) -> Unit,
    isGenerating: Boolean,
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
        isGenerating -> "stop"
        data.text.isNotBlank() && !speechToTextState.isRecording() -> "send"
        else -> null
    }

    val interactionSource = remember { MutableInteractionSource() }

    BasicTextField(
        modifier = modifier.defaultMinSize(minHeight = LocalMinimumInteractiveComponentSize.current),
        value = data.text,
        onValueChange = onTextChange,
        enabled = !isGenerating && !speechToTextState.isRecording(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
        keyboardActions = KeyboardActions(onSend = { onSendClick() }),
        textStyle = resolveTextFieldStyle(interactionSource, disabled = isGenerating),
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
                    AttachmentList(
                        attachments = data.attachments,
                        onRemoveAttachment = onRemoveAttachment,
                    )
                    Row(verticalAlignment = Alignment.Bottom) {
                        TextInput(
                            modifier = Modifier.weight(1f),
                            text = data.text,
                            innerTextField = innerTextField,
                        )
                        VoiceButton(
                            isGenerating = isGenerating,
                            speechToTextState = speechToTextState,
                            snackbarHostState = snackbarHostState,
                        )
                        TrailingButton(
                            button = trailingButton,
                            onSendClick = onSendClick,
                            onStopClick = onStopClick,
                        )
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
private fun AttachmentList(
    attachments: Set<Uri>,
    onRemoveAttachment: (Uri) -> Unit,
) {
    AnimatedContent(targetState = attachments.isNotEmpty()) { visible ->
        if (visible) {
            AttachmentList(
                modifier = Modifier.fillMaxWidth(),
                uris = attachments,
                onRemoveAttachment = onRemoveAttachment,
            )
        }
    }
}

@Composable
private fun TextInput(
    modifier: Modifier,
    text: String,
    innerTextField: @Composable () -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 12.dp, bottom = 12.dp),
        ) {
            if (text.isBlank()) {
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

@Composable
private fun VoiceButton(
    isGenerating: Boolean,
    speechToTextState: SpeechToTextButtonState,
    snackbarHostState: SnackbarHostState,
) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    AnimatedContent(targetState = !isGenerating) { showVoiceButton ->
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
}

@Composable
private fun TrailingButton(
    button: String?,
    onSendClick: () -> Unit,
    onStopClick: () -> Unit,
) {
    AnimatedContent(targetState = button) { button ->
        when (button) {
            "stop" -> TrailingIconButton(
                icon = R.drawable.stream_ai_compose_ic_stop,
                contentDescription = "Stop",
                onClick = onStopClick,
            )

            "send" -> TrailingIconButton(
                icon = R.drawable.stream_ai_compose_ic_send,
                contentDescription = "Send",
                onClick = onSendClick,
            )
        }
    }
}

@Composable
private fun TrailingIconButton(
    @DrawableRes icon: Int,
    contentDescription: String,
    onClick: () -> Unit,
) {
    FilledIconButton(onClick = onClick) {
        Icon(
            painter = painterResource(icon),
            contentDescription = contentDescription,
        )
    }
}

@Composable
private fun AddButton(
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
            painter = painterResource(R.drawable.stream_ai_compose_ic_add),
            contentDescription = "Add context",
        )
    }
}

private fun Context.openSettings() {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", packageName, null)
    }
    startActivity(intent)
}

@Preview(showBackground = true)
@Composable
private fun ChatComposerEmptyPreview() {
    MaterialTheme {
        ChatComposer(
            onSendClick = {},
            onStopClick = {},
            isGenerating = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatComposerFilledPreview() {
    MaterialTheme {
        ChatComposer(
            messageData = MessageData(text = "What is Stream Chat?"),
            onSendClick = {},
            onStopClick = {},
            isGenerating = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatComposerLongFilledPreview() {
    MaterialTheme {
        ChatComposer(
            messageData = MessageData(text = "Lorem ipsum dolor sit amet, consectetur adipiscing elit."),
            onSendClick = {},
            onStopClick = {},
            isGenerating = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatComposerWithAttachmentsPreview() {
    MaterialTheme {
        ChatComposer(
            messageData = MessageData(
                text = "What is Stream Chat?",
                attachments = setOf("1".toUri(), "2".toUri(), "3".toUri()),
            ),
            onSendClick = {},
            onStopClick = {},
            isGenerating = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ChatComposerStreamingPreview() {
    MaterialTheme {
        ChatComposer(
            onSendClick = {},
            onStopClick = {},
            isGenerating = true,
        )
    }
}
