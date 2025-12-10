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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
internal fun WaveformIndicator(
    rmsdB: Float,
    modifier: Modifier = Modifier,
) {
    // Measure width and calculate number of bars dynamically
    val density = LocalDensity.current
    var widthPx by remember { mutableIntStateOf(0) }

    // Calculate how many bars can fit: N = (width + spacing) / (barWidth + spacing)
    val barCount = remember(widthPx, density) {
        val barWidthPx = with(density) { BarWidth.toPx() }
        val barSpacingPx = with(density) { BarSpacing.toPx() }
        val totalBarWidth = barWidthPx + barSpacingPx
        // Account for the fact that the last bar doesn't have spacing after it
        val calculatedCount = ((widthPx + barSpacingPx) / totalBarWidth).toInt()
        // Ensure at least 1 bar
        calculatedCount.coerceAtLeast(1)
    }

    // Queue stores historical rmsdB values; rightmost bar shows current live value
    var rmsdBQueue by remember(barCount) { mutableStateOf(List(barCount) { 0f }) }
    var currentRmsdB by remember { mutableStateOf(rmsdB) }

    // Update current rmsdB when new value arrives
    LaunchedEffect(rmsdB) {
        currentRmsdB = rmsdB
    }

    // Periodically shift values left at fixed interval
    LaunchedEffect(barCount) {
        while (true) {
            delay(SHIFT_INTERVAL_MS)
            rmsdBQueue = rmsdBQueue.drop(1) + currentRmsdB
        }
    }

    Row(
        modifier = modifier
            .defaultMinSize(
                minWidth = MinWidth,
                minHeight = MAX_BAR_HEIGHT.dp,
            )
            .onSizeChanged { size ->
                widthPx = size.width
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        repeat(barCount) { index ->
            // Rightmost bar shows current live value, others show historical values
            val barRmsdB = if (index == barCount - 1) {
                currentRmsdB
            } else {
                rmsdBQueue[index]
            }

            // Normalize to 0-1 range and calculate height (rmsdB typically 0-10)
            val normalizedLevel = (barRmsdB / 10f).coerceIn(0f, 1f)
            val barHeight = if (normalizedLevel <= 0.1f) {
                // Show minimum height when silent or very quiet
                MIN_BAR_HEIGHT.dp
            } else {
                // Scale by normalized audio level
                val heightMultiplier = normalizedLevel.coerceIn(0.2f, 1f)
                (MIN_BAR_HEIGHT + (MAX_BAR_HEIGHT - MIN_BAR_HEIGHT) * heightMultiplier).dp
            }

            Box(
                modifier = Modifier
                    .width(BarWidth)
                    .height(barHeight)
                    .clip(MaterialTheme.shapes.small)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant),
            )
        }
    }
}

private val MinWidth = 58.dp
private val BarSpacing = 3.dp
private val BarWidth = 2.5.dp
private const val MIN_BAR_HEIGHT = 3f
private const val MAX_BAR_HEIGHT = 20f
private const val SHIFT_INTERVAL_MS = 100L

@Preview(showBackground = true)
@Composable
private fun WaveformIndicatorPreview() {
    WaveformIndicator(
        rmsdB = 5f,
    )
}
