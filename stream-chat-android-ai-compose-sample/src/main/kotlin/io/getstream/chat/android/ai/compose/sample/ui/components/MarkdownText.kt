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

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.annotator.annotatorSettings
import com.mikepenz.markdown.coil3.Coil3ImageTransformerImpl
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownDimens
import com.mikepenz.markdown.compose.components.MarkdownComponent
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownCodeBackground
import com.mikepenz.markdown.compose.elements.MarkdownCodeFence
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCode
import com.mikepenz.markdown.compose.elements.MarkdownHighlightedCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownTable
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.m3.markdownColor
import com.mikepenz.markdown.model.rememberMarkdownState
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode

/**
 * Renders markdown text with proper styling.
 * Supports code blocks, tables, charts, and other markdown features.
 *
 * @param text The markdown text to render
 * @param modifier Modifier to be applied to the text
 */
@Composable
public fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
) {
    val markdownState = rememberMarkdownState(
        content = text,
        retainState = true,
    )

    Markdown(
        modifier = modifier,
        markdownState = markdownState,
        colors = markdownColor(),
        components = MarkdownComponents,
        imageTransformer = Coil3ImageTransformerImpl,
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

private val CodeFence: MarkdownComponent = {
    MarkdownCodeFence(
        content = it.content,
        node = it.node,
        style = it.typography.code,
    ) { code, language, style ->
        // If it's a chartjs and complete, render it as a chart diagram
        val chartCode = extractCodeFenceContent(it.content, it.node)

        if (language?.lowercase() == "chartjs" && chartCode != null) {
            val backgroundCodeColor = LocalMarkdownColors.current.codeBackground
            val codeBackgroundCornerSize = LocalMarkdownDimens.current.codeBackgroundCornerSize
            MarkdownCodeBackground(
                color = backgroundCodeColor,
                shape = RoundedCornerShape(codeBackgroundCornerSize),
            ) {
                ChartDiagram(
                    modifier = Modifier.fillMaxWidth(),
                    chartCode = chartCode,
                )
            }
        } else {
            // Otherwise, render as a regular code
            MarkdownHighlightedCode(
                code = code,
                language = language,
                style = style,
                showHeader = true,
            )
        }
    }
}

private val CodeBlock: MarkdownComponent = {
    MarkdownHighlightedCodeBlock(
        content = it.content,
        node = it.node,
        style = it.typography.code,
        showHeader = true,
    )
}

private val MarkdownComponents = markdownComponents(
    codeFence = CodeFence,
    codeBlock = CodeBlock,
    table = Table,
)

/**
 * Extracts the code content from a code fence (without the language identifier).
 */
private fun extractCodeFenceContent(content: String, node: ASTNode): String? {
    if (node.type != MarkdownElementTypes.CODE_FENCE) return null

    // Get the full text of the code fence node
    val fullText = node.getTextInNode(content).toString()
    val lines = fullText.lines()

    // Must have at least opening fence and closing fence
    if (lines.size < 2) return null

    // Check if last line is closing fence (starts with ```)
    val lastLine = lines.last().trim()
    if (!lastLine.startsWith("```")) return null

    return lines.drop(1).dropLast(1).joinToString("\n").trim()
}
