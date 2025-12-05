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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import io.getstream.chat.android.ai.compose.util.internal.simpleLogger
import java.util.Locale

/**
 * Interface for speech recognition helper.
 * Manages speech recognition using Android's SpeechRecognizer API.
 */
internal interface SpeechRecognizerHelper {
    val isListening: Boolean get() = false
    val rmsdB: Float get() = 0f

    fun startListening(): Boolean = false

    fun stopListening() {
        // No-op
    }

    fun cancel() {
        // No-op
    }

    fun release() {
        // No-op
    }
}

/**
 * Default implementation of SpeechRecognizerHelper.
 */
private class DefaultSpeechRecognizerHelper(
    context: Context,
    private val onResult: (String) -> Unit,
    private val onPartialResult: ((String) -> Unit)? = null,
) : SpeechRecognizerHelper {
    private val logger = simpleLogger("SpeechRecognizerHelper")
    private var speechRecognizer: SpeechRecognizer? = null
    private val _isListening = mutableStateOf(false)
    override val isListening by _isListening
    private val _rmsdB = mutableFloatStateOf(0f)
    override val rmsdB by _rmsdB
    private var lastPartialResult: String? = null
    private var hasFinalResult = false

    init {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context)
            speechRecognizer?.setRecognitionListener(createRecognitionListener())
        } else {
            logger.w { "Speech recognition is not available on this device" }
        }
    }

    private fun resetState() {
        _isListening.value = false
        _rmsdB.value = 0f
        lastPartialResult = null
        hasFinalResult = false
    }

    @Suppress("TooGenericExceptionCaught")
    override fun startListening(): Boolean {
        if (_isListening.value) {
            return false
        }

        val recognizer = speechRecognizer ?: run {
            logger.e { "Speech recognizer is not available" }
            return false
        }

        return try {
            resetState()
            recognizer.startListening(createRecognitionIntent())
            _isListening.value = true
            true
        } catch (e: Exception) {
            logger.e(e) { "Failed to start listening: ${e.message}" }
            false
        }
    }

    override fun stopListening() {
        if (!_isListening.value) {
            return
        }
        speechRecognizer?.stopListening()
        // Don't reset state here - onResults() will be called asynchronously
        // and will handle the reset after processing the final result
        _isListening.value = false
    }

    override fun cancel() {
        speechRecognizer?.cancel()
        resetState()
    }

    override fun release() {
        speechRecognizer?.cancel()
        speechRecognizer?.destroy()
        speechRecognizer = null
        resetState()
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
            _rmsdB.value = rmsdB
        }

        // 4. Optional - may not be called, receives audio buffer
        override fun onBufferReceived(buffer: ByteArray?) {
            logger.d { "onBufferReceived: buffer size=${buffer?.size ?: 0}" }
        }

        // 5. Optional - called multiple times if EXTRA_PARTIAL_RESULTS is enabled
        override fun onPartialResults(partialResults: Bundle?) {
            if (hasFinalResult) {
                logger.d { "onPartialResults: Ignoring late partial result (final result already processed)" }
                return
            }

            extractResult(partialResults)?.takeIf(String::isNotBlank)?.let { result ->
                lastPartialResult = result
                logger.d { "onPartialResults: Streaming partial result: $result" }
                onPartialResult?.invoke(result)
            }
        }

        // 6. Called when user stops speaking
        override fun onEndOfSpeech() {
            logger.d { "onEndOfSpeech: User stopped speaking" }
        }

        // 7. Called last (in normal flow) - provides final recognition results
        override fun onResults(results: Bundle?) {
            logger.d { "onResults: Processing results: $results" }
            hasFinalResult = true

            // Use final result from bundle (most accurate), fallback to last partial result
            val finalResult = extractResult(results)?.takeIf(String::isNotBlank)
                ?: lastPartialResult
                ?: ""

            onResult(finalResult)
            resetState()
        }

        // 8. Can be called at any time if an error occurs
        override fun onError(error: Int) {
            val errorMessage = getErrorMessage(error)
            val isCritical = error in CRITICAL_ERRORS

            if (isCritical) {
                resetState()
                logger.e { "onError: Critical error - $errorMessage" }
            } else {
                logger.d { "onError: Non-critical error - $errorMessage" }
            }
        }

        // 9. Optional - handles additional events
        override fun onEvent(eventType: Int, params: Bundle?) {
            logger.d { "onEvent: eventType=$eventType, params=$params" }
        }
    }
}

@Composable
internal fun rememberSpeechRecognizerHelper(
    onResult: (String) -> Unit,
    onPartialResult: ((String) -> Unit)? = null,
): SpeechRecognizerHelper {
    val context = LocalContext.current

    val helper = remember {
        DefaultSpeechRecognizerHelper(
            context,
            onResult,
            onPartialResult,
        )
    }

    DisposableEffect(Unit) {
        onDispose { helper.release() }
    }

    return helper
}

private const val SILENCE_TIMEOUT_MS = 3000

private val CRITICAL_ERRORS = setOf(
    SpeechRecognizer.ERROR_AUDIO,
    SpeechRecognizer.ERROR_CLIENT,
    SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS,
    SpeechRecognizer.ERROR_NETWORK,
    SpeechRecognizer.ERROR_SERVER,
)

private fun createRecognitionIntent(): Intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, SILENCE_TIMEOUT_MS)
    putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, SILENCE_TIMEOUT_MS)
}

private fun extractResult(bundle: Bundle?): String? =
    bundle?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()

private fun getErrorMessage(error: Int): String = when (error) {
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
