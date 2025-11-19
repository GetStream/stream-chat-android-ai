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

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.annotator.annotatorSettings
import com.mikepenz.markdown.compose.components.MarkdownComponent
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeFence
import com.mikepenz.markdown.compose.elements.MarkdownTable
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor

/**
 * Renders markdown text with proper styling.
 * Supports code blocks, tables, and other markdown features.
 *
 * @param text The markdown text to render
 * @param modifier Modifier to be applied to the text
 */
@Composable
public fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    Markdown(
        content = text,
        colors = markdownColor(),
        components = markdownComponents(
            codeFence = HighlightedCodeFence,
            codeBlock = HighlightedCodeBlock,
            table = Table,
        ),
        modifier = modifier,
    )
}

private val Table: MarkdownComponent = {
    val annotatorSettings = annotatorSettings()
    MarkdownTable(
        content = it.content,
        node = it.node,
        style = it.typography.table,
        annotatorSettings = annotatorSettings,
        headerBlock = { content, header, tableWidth, style ->
            MarkdownTableHeader(
                content = content,
                header = header,
                tableWidth = tableWidth,
                style = style,
                annotatorSettings = annotatorSettings,
            )
        },
        rowBlock = { content, header, tableWidth, style ->
            MarkdownTableRow(
                content = content,
                header = header,
                tableWidth = tableWidth,
                style = style,
                annotatorSettings = annotatorSettings,
            )
        },
    )
}

private val HighlightedCodeFence: MarkdownComponent = {
    MarkdownHighlightedCodeFence(
        content = it.content,
        node = it.node,
        style = it.typography.code,
        showHeader = true,
    )
}

private val HighlightedCodeBlock: MarkdownComponent = {
    MarkdownHighlightedCodeBlock(
        content = it.content,
        node = it.node,
        style = it.typography.code,
        showHeader = true,
    )
}
