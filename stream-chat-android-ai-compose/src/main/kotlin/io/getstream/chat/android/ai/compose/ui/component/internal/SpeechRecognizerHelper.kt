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

package io.getstream.chat.android.ai.compose.ui.component.internal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import io.getstream.chat.android.ai.compose.util.simpleLogger
import java.util.Locale

/**
 * Interface for speech recognition helper.
 * Manages speech recognition using Android's SpeechRecognizer API.
 */
internal interface SpeechRecognizerHelper {
    fun startListening(): Boolean
    fun stopListening()
    fun cancel()
    fun release()
    fun isListening(): Boolean
}

/**
 * Default implementation of SpeechRecognizerHelper.
 */
private class DefaultSpeechRecognizerHelper(
    context: Context,
    private val onResult: (String) -> Unit,
    private val onError: (String) -> Unit,
    private val onRmsChanged: ((Float) -> Unit)? = null,
) : SpeechRecognizerHelper {
    private val logger = simpleLogger("SpeechRecognizerHelper")
    private var speechRecognizer: SpeechRecognizer? = null
    private var isListening = false
    private var lastPartialResult: String? = null

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
        } else {
            onError("Speech recognition is not available on this device")
        }
    }

    override fun startListening(): Boolean {
        if (isListening) return false

        val recognizer = speechRecognizer ?: run {
            onError("Speech recognizer is not available")
            return false
        }

        return try {
            lastPartialResult = null // Reset partial result when starting new session
            recognizer.startListening(createRecognitionIntent())
            isListening = true
            true
        } catch (e: Exception) {
            logger.e(e) { "Failed to start listening: ${e.message}" }
            onError("Failed to start listening: ${e.message}")
            false
        }
    }

    override fun stopListening() {
        if (!isListening) return
        speechRecognizer?.stopListening()
        isListening = false
    }

    override fun cancel() {
        speechRecognizer?.cancel()
        isListening = false
    }

    override fun release() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        isListening = false
    }

    override fun isListening(): Boolean = isListening

    private fun createRecognitionIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        // Long timeout - recording stops only when manually cancelled
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, SILENCE_TIMEOUT_MS)
        putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, SILENCE_TIMEOUT_MS)
    }

    private fun createRecognitionListener(): RecognitionListener = object : RecognitionListener {
        // 1. Called first - recognizer is ready to listen
        override fun onReadyForSpeech(params: Bundle?) {
            logger.d { "onReadyForSpeech: params=$params" }
        }

        // 2. Called when user starts speaking
        override fun onBeginningOfSpeech() {
            logger.d { "onBeginningOfSpeech: User started speaking" }
        }

        // 3. Called repeatedly during speech - provides audio level updates
        override fun onRmsChanged(rmsdB: Float) {
            logger.v { "onRmsChanged: rmsdB=$rmsdB" }
            onRmsChanged?.invoke(rmsdB)
        }

        // 4. Optional - may not be called, receives audio buffer
        override fun onBufferReceived(buffer: ByteArray?) {
            logger.d { "onBufferReceived: buffer size=${buffer?.size ?: 0}" }
        }

        // 5. Optional - called multiple times if EXTRA_PARTIAL_RESULTS is enabled
        override fun onPartialResults(partialResults: Bundle?) {
            extractResult(partialResults)?.takeIf(String::isNotBlank)?.let {
                lastPartialResult = it
                logger.d { "onPartialResults: Stored last partial result: $it" }
            }
        }

        // 6. Called when user stops speaking
        override fun onEndOfSpeech() {
            logger.d { "onEndOfSpeech: User stopped speaking" }
        }

        // 7. Called last (in normal flow) - provides final recognition results
        override fun onResults(results: Bundle?) {
            val finalResult = extractResult(results) ?: lastPartialResult

            if (finalResult != null) {
                logger.d { "onResults: Processing result: $finalResult" }
                onResult(finalResult)
                lastPartialResult = null
            } else {
                logger.w { "onResults: No results available" }
            }
        }

        // 8. Can be called at any time if an error occurs
        override fun onError(error: Int) {
            val errorMessage = when (error) {
                SpeechRecognizer.ERROR_AUDIO -> "Audio error"
                SpeechRecognizer.ERROR_CLIENT -> "Client error"
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Insufficient permissions"
                SpeechRecognizer.ERROR_NETWORK -> "Network error"
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network timeout"
                SpeechRecognizer.ERROR_NO_MATCH -> "No match"
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Recognizer busy"
                SpeechRecognizer.ERROR_SERVER -> "Server error"
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "Speech timeout"
                else -> "Unknown error: $error"
            }

            val isCritical = error in setOf(
                SpeechRecognizer.ERROR_AUDIO,
                SpeechRecognizer.ERROR_CLIENT,
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS,
                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_SERVER,
            )

            if (isCritical) {
                isListening = false
                logger.e { "onError: Critical error - $errorMessage" }
                onError(errorMessage)
            } else {
                logger.d { "onError: Non-critical error - $errorMessage" }
            }
        }

        // 9. Optional - handles additional events
        override fun onEvent(eventType: Int, params: Bundle?) {
            logger.d { "onEvent: eventType=$eventType, params=$params" }
        }

        private fun extractResult(bundle: Bundle?): String? =
            bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
    }
}

/**
 * No-op implementation of SpeechRecognizerHelper for preview mode.
 */
private class NoOpSpeechRecognizerHelper : SpeechRecognizerHelper {
    override fun startListening(): Boolean = false

    override fun stopListening() {
        // No-op
    }

    override fun cancel() {
        // No-op
    }

    override fun release() {
        // No-op
    }

    override fun isListening(): Boolean = false
}

@Composable
internal fun rememberSpeechRecognizerHelper(
    onResult: (String) -> Unit,
    onError: (String) -> Unit,
    onRmsChanged: ((Float) -> Unit)? = null,
): SpeechRecognizerHelper {
    val context = LocalContext.current
    val isInPreview = LocalInspectionMode.current
    val helper = remember {
        if (isInPreview) {
            NoOpSpeechRecognizerHelper()
        } else {
            DefaultSpeechRecognizerHelper(context, onResult, onError, onRmsChanged)
        }
    }

    DisposableEffect(Unit) {
        onDispose { helper.release() }
    }

    return helper
}

private const val SILENCE_TIMEOUT_MS = 60000
