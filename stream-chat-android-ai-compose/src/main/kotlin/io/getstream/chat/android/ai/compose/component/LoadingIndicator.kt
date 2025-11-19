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

package io.getstream.chat.android.ai.compose.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A loading indicator that displays label and an animated indicator.
 *
 * @param modifier Modifier to be applied to the indicator
 * @param label Optional label to display before the indicator
 * @param indicator Indicator to display (defaults to animated dots)
 */
@Composable
public fun LoadingIndicator(
    modifier: Modifier = Modifier,
    label: @Composable () -> Unit = {},
    indicator: @Composable () -> Unit = { AnimatedDots() },
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CompositionLocalProvider(
            LocalTextStyle provides MaterialTheme.typography.bodyMedium,
            LocalContentColor provides MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
        ) {
            label()
        }
        indicator()
    }
}

@Composable
private fun AnimatedDots() {
    val infiniteTransition = rememberInfiniteTransition(label = "dots_transition")
    val progress by infiniteTransition.animateFloat(
        initialValue = PROGRESS_START,
        targetValue = PROGRESS_FULL,
        animationSpec = infiniteRepeatable(
            animation = tween(DOT_CYCLE_DURATION, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(DOT_COUNT) { AnimatedDot(it, progress) }
    }
}

/**
 * A single animated dot with overlapping sequential highlighting.
 */
@Composable
private fun AnimatedDot(
    index: Int,
    baseProgress: Float,
) {
    val startOffset = (index * DOT_STAGGER_DELAY).toFloat() / DOT_CYCLE_DURATION
    val t = ((baseProgress - startOffset + PROGRESS_FULL) % PROGRESS_FULL) * DOT_CYCLE_DURATION / DOT_ANIMATION_DURATION

    val alpha = when {
        t <= PROGRESS_START || t >= PROGRESS_FULL -> DOT_MIN_ALPHA
        else -> {
            val eased = smoothstep(if (t <= ANIMATION_MIDPOINT) t * ANIMATION_DOUBLE else (PROGRESS_FULL - t) * ANIMATION_DOUBLE)
            DOT_MIN_ALPHA + (DOT_MAX_ALPHA - DOT_MIN_ALPHA) * eased
        }
    }

    Dot(alpha = alpha)
}

private fun smoothstep(t: Float) = t * t * (SMOOTHSTEP_FACTOR_1 - SMOOTHSTEP_FACTOR_2 * t)

/**
 * A single animated dot.
 */
@Composable
private fun Dot(
    alpha: Float,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(6.dp)
            .alpha(alpha)
            .background(
                color = LocalContentColor.current.copy(alpha = 0.6f),
                shape = CircleShape,
            ),
    )
}

private const val DOT_COUNT = 3
private const val DOT_MIN_ALPHA = 0.3f
private const val DOT_MAX_ALPHA = 1f
private const val DOT_ANIMATION_DURATION = 600
private const val DOT_STAGGER_DELAY = 200
private const val DOT_CYCLE_DURATION = DOT_ANIMATION_DURATION + (DOT_COUNT - 1) * DOT_STAGGER_DELAY

// Animation calculation constants
private const val PROGRESS_START = 0f
private const val PROGRESS_FULL = 1f
private const val ANIMATION_MIDPOINT = 0.5f
private const val ANIMATION_DOUBLE = 2f
private const val SMOOTHSTEP_FACTOR_1 = 3f
private const val SMOOTHSTEP_FACTOR_2 = 2f

@Preview(showBackground = true)
@Composable
private fun LoadingIndicatorPreview() {
    LoadingIndicator(label = { Text("Thinking") })
}
