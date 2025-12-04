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
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ai.compose.R
import io.getstream.chat.android.ai.compose.ui.component.internal.rememberSpeechRecognizerHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.log
import kotlin.random.Random

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
 * When not recording, displays a microphone icon button.
 * When recording, transforms into a circle with animated bars that respond to voice input.
 *
 * The component automatically handles:
 * - Audio permission requests
 * - Starting and stopping speech recognition
 * - Streaming recognized text chunks to the callback as they're detected
 * - Animated visualization during recording
 *
 * @param modifier Modifier to be applied to the root container
 * @param state The state holder for tracking recording status. Defaults to a remembered state
 *   created with [rememberSpeechToTextButtonState].
 * @param onTextRecognized Callback invoked when text chunks are recognized. Called with each
 *   partial result as speech is detected, enabling real-time text streaming. The caller is
 *   responsible for accumulating text if needed.
 *
 * @see SpeechToTextButtonState
 * @see rememberSpeechToTextButtonState
 */
@Composable
public fun SpeechToTextButton(
    modifier: Modifier = Modifier,
    state: SpeechToTextButtonState = rememberSpeechToTextButtonState(),
    onTextRecognized: (String) -> Unit,
) {
    val context = LocalContext.current
    val activity = context as? ComponentActivity

    val speechRecognizerHelper = rememberSpeechRecognizerHelper(
        onResult = { text ->
            // Final result when speech ends
            onTextRecognized(text)
        },
        onError = {
            state.isRecordingState = false
        },
        onRmsChanged = { db ->
            state.rmsdBState = db
        },
        onPartialResult = { text ->
            // Stream partial results in real-time as user speaks
            onTextRecognized(text)
        },
        onRecordingStopped = {
            // Update UI state when recording stops (auto-stop or manual)
            state.isRecordingState = false
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

    val onToggleRecording = {
        if (activity != null) {
            if (state.isRecordingState) {
                // Stop recording - state will be updated via onRecordingStopped callback
                speechRecognizerHelper.stopListening()
            } else {
                // Start recording
                if (hasPermission) {
                    if (speechRecognizerHelper.startListening()) {
                        state.isRecordingState = true
                    }
                } else {
                    permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    AnimatedContent(
        targetState = state.isRecordingState,
        modifier = modifier,
    ) { isRecording ->
        if (isRecording) {
            VoiceRecordingIndicator(
                onClick = onToggleRecording,
                rmsdB = state.rmsdBState,
            )
        } else {
            IconButton(onClick = onToggleRecording) {
                Icon(
                    painter = painterResource(R.drawable.stream_ai_compose_ic_mic),
                    contentDescription = "Voice input",
                )
            }
        }
    }
}

/**
 * A circular button with animated vertical bars that respond to voice input.
 *
 * @param onClick Callback when the button is clicked
 * @param rmsdB The audio level in decibels, used to drive the animation
 * @param modifier Modifier for the button
 */
@Composable
private fun VoiceRecordingIndicator(
    onClick: () -> Unit,
    rmsdB: Float,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(48.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
        Box(
            contentAlignment = androidx.compose.ui.Alignment.Center,
        ) {
            VoiceRecordingBars(rmsdB = rmsdB)
        }
    }
}

/**
 * Draws animated vertical bars that simulate voice input visualization.
 * The bars animate randomly with heights influenced by the audio level.
 */
@Composable
private fun VoiceRecordingBars(
    rmsdB: Float,
    modifier: Modifier = Modifier,
) {
    val color = MaterialTheme.colorScheme.onPrimary
    val barCount = 5
    val barAnimatables = remember {
        List(barCount) { Animatable(0.3f) }
    }

    // Normalize rmsdB to a scale factor (0.5 to 1.5)
    // Typical rmsdB ranges from 0 to 10
    val audioScale = 0.5f + (rmsdB.coerceIn(0f, 10f) / 10f)

    LaunchedEffect(barAnimatables) {
        barAnimatables.forEachIndexed { index, animatable ->
            launch {
                // Stagger the start times
                delay(index * 100L)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(
                            durationMillis = 300 + (Random.nextInt(200)),
                            easing = LinearEasing,
                        ),
                        repeatMode = RepeatMode.Reverse,
                    ),
                )
            }
        }
    }

    Canvas(modifier = modifier.size(24.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val barWidth = canvasWidth / (barCount * 2 - 1)
        val spacing = barWidth
        val maxBarHeight = canvasHeight * 0.7f
        val minBarHeight = canvasHeight * 0.2f

        barAnimatables.forEachIndexed { index, animatable ->
            val barHeight = minBarHeight + (maxBarHeight - minBarHeight) * animatable.value * audioScale
            val x = index * (barWidth + spacing)
            val y = (canvasHeight - barHeight) / 2

            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(barWidth / 2, barWidth / 2),
            )
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
