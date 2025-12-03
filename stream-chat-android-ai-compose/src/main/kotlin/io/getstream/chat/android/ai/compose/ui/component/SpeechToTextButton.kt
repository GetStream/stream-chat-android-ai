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

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.ui.component.internal.WaveformIndicator
import io.getstream.chat.android.ai.compose.ui.component.internal.rememberSpeechRecognizerHelper

/**
 * State holder for [SpeechToTextButton] that tracks the recording status.
 *
 * Use [rememberSpeechToTextButtonState] to create an instance of this state.
 *
 * @see rememberSpeechToTextButtonState
 * @see SpeechToTextButton
 */
public class SpeechToTextButtonState internal constructor() {
    internal var isRecordingState by mutableStateOf(false)
    internal var rmsdBState by mutableStateOf(0f)

    /**
     * Returns whether the speech-to-text button is currently recording.
     *
     * @return `true` if recording is in progress, `false` otherwise
     */
    public fun isRecording(): Boolean = isRecordingState
}

/**
 * Creates and remembers a [SpeechToTextButtonState] instance.
 *
 * The state is remembered across recompositions and can be used to track the recording status
 * of a [SpeechToTextButton].
 *
 * @return A remembered instance of [SpeechToTextButtonState]
 *
 * @see SpeechToTextButtonState
 * @see SpeechToTextButton
 */
@Composable
public fun rememberSpeechToTextButtonState(): SpeechToTextButtonState =
    remember { SpeechToTextButtonState() }

/**
 * A composable button that provides speech-to-text functionality.
 *
 * This component handles microphone input, permission requests, and displays a waveform indicator
 * during recording. It provides customizable UI components for voice input, cancellation, and
 * text preview.
 *
 * The component automatically handles:
 * - Audio permission requests
 * - Starting and stopping speech recognition
 * - Accumulating recognized text across multiple recognition sessions
 * - Displaying waveform visualization during recording
 *
 * @param modifier Modifier to be applied to the root Column container
 * @param state The state holder for tracking recording status. Defaults to a remembered state
 *   created with [rememberSpeechToTextButtonState].
 * @param onTextRecognized Callback invoked when text is recognized. Receives the accumulated
 *   text from all recognition sessions since the last recording start.
 * @param voiceInputButton Composable for the voice input button. Defaults to a microphone icon
 *   that fades out when recording.
 * @param cancelButton Composable for the cancel button. Defaults to a cancel icon that appears
 *   when recording.
 * @param waveformIndicator Composable for the waveform visualization. Defaults to a
 *   [io.getstream.chat.android.ai.compose.ui.component.internal.WaveformIndicator] that shows audio levels during recording.
 * @param seeTextButton Composable for the "See text" button. Defaults to a text button that
 *   appears when recording.
 *
 * @see SpeechToTextButtonState
 * @see rememberSpeechToTextButtonState
 */
@Composable
public fun SpeechToTextButton(
    modifier: Modifier = Modifier,
    state: SpeechToTextButtonState = rememberSpeechToTextButtonState(),
    onTextRecognized: (String) -> Unit,
    voiceInputButton: @Composable (onClick: () -> Unit) -> Unit = { onClick -> DefaultVoiceInputButton(state, onClick) },
    cancelButton: @Composable (onClick: () -> Unit) -> Unit = { onClick -> DefaultCancelButton(state, onClick) },
    waveformIndicator: @Composable () -> Unit = { DefaultWaveformIndicator(state) },
    seeTextButton: @Composable (onClick: () -> Unit) -> Unit = { onClick -> DefaultSeeTextButton(state, onClick) },
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    var accumulatedText by remember { mutableStateOf("") }

    val speechRecognizerHelper = rememberSpeechRecognizerHelper(
        onResult = { text ->
            accumulatedText = if (accumulatedText.isBlank()) text else "$accumulatedText $text"
            onTextRecognized(accumulatedText)
        },
        onError = {
            state.isRecordingState = false
        },
        onRmsChanged = { db ->
            state.rmsdBState = db
        },
    )

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isGranted ->
        if (isGranted && speechRecognizerHelper.startListening()) {
            state.isRecordingState = true
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (state.isRecordingState) speechRecognizerHelper.stopListening()
        }
    }

    val hasPermission = remember(context) {
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO,
        ) == PackageManager.PERMISSION_GRANTED
    }

    val onStartRecording = {
        if (activity != null) {
            if (hasPermission && !state.isRecordingState) {
                accumulatedText = ""
                if (speechRecognizerHelper.startListening()) {
                    state.isRecordingState = true
                }
            } else if (!hasPermission) {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    val onStopRecording = {
        speechRecognizerHelper.stopListening()
        state.isRecordingState = false
    }

    val onCancel = {
        speechRecognizerHelper.cancel()
        state.isRecordingState = false
        accumulatedText = ""
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        seeTextButton(onStopRecording)

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            cancelButton(onCancel)

            Box(
                modifier = Modifier.weight(1f),
            ) {
                waveformIndicator()
            }

            voiceInputButton(onStartRecording)
        }
    }
}

@Composable
private fun DefaultVoiceInputButton(
    state: SpeechToTextButtonState,
    onClick: () -> Unit,
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (state.isRecordingState) 0f else 1f,
        label = "alpha",
    )
    IconButton(
        onClick = onClick,
        modifier = Modifier.graphicsLayer { alpha = animatedAlpha },
    ) {
        Icon(
            painter = painterResource(R.drawable.stream_ai_compose_ic_mic),
            contentDescription = "Voice input",
        )
    }
}

@Composable
private fun DefaultWaveformIndicator(state: SpeechToTextButtonState) {
    ConditionalAnimatedContent(state.isRecordingState) {
        WaveformIndicator(
            modifier = Modifier.fillMaxWidth(),
            rmsdB = state.rmsdBState,
        )
    }
}

@Composable
private fun DefaultCancelButton(
    state: SpeechToTextButtonState,
    onClick: () -> Unit,
) {
    ConditionalAnimatedContent(state.isRecordingState) {
        IconButton(onClick = onClick) {
            Icon(
                painter = painterResource(R.drawable.stream_ai_compose_ic_cancel),
                contentDescription = "Cancel voice input",
            )
        }
    }
}

@Composable
private fun DefaultSeeTextButton(
    state: SpeechToTextButtonState,
    onClick: () -> Unit,
) {
    ConditionalAnimatedContent(state.isRecordingState) {
        TextButton(onClick = onClick) {
            Text(text = "See text")
        }
    }
}

@Composable
private fun ConditionalAnimatedContent(
    condition: Boolean,
    content: @Composable () -> Unit,
) {
    AnimatedContent(targetState = condition) { isVisible ->
        if (isVisible) {
            content()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SpeechToTextButtonIdlePreview() {
    SpeechToTextButton(
        onTextRecognized = {},
    )
}

@Preview(showBackground = true)
@Composable
private fun SpeechToTextButtonRecordingPreview() {
    val state = rememberSpeechToTextButtonState().apply { isRecordingState = true }
    SpeechToTextButton(
        state = state,
        onTextRecognized = {},
    )
}
