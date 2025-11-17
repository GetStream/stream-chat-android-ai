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

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mikepenz.markdown.compose.components.MarkdownComponent
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor

private val HighlightedCodeFence: MarkdownComponent = {
    MarkdownHighlightedCodeFence(content = it.content, node = it.node, style = it.typography.code, showHeader = true)
}

private val HighlightedCodeBlock: MarkdownComponent = {
    MarkdownHighlightedCodeBlock(content = it.content, node = it.node, style = it.typography.code, showHeader = true)
}

/**
 * Renders markdown text with proper styling.
 */
@Composable
public fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    textColor: Color? = null,
) {
    val defaultTextColor = textColor ?: MaterialTheme.colorScheme.onSurface

    Markdown(
        content = text,
        colors = markdownColor(
            text = defaultTextColor,
            codeBackground = MaterialTheme.colorScheme.surfaceVariant,
        ),
        components = markdownComponents(
            codeFence = HighlightedCodeFence,
            codeBlock = HighlightedCodeBlock,
        ),
        modifier = modifier,
    )
}
