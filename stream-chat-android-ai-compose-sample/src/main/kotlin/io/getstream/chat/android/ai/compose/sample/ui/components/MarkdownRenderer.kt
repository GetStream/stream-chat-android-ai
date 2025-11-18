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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import com.mikepenz.markdown.annotator.AnnotatorSettings
import com.mikepenz.markdown.annotator.annotatorSettings
import com.mikepenz.markdown.compose.LocalMarkdownComponents
import com.mikepenz.markdown.compose.LocalMarkdownDimens
import com.mikepenz.markdown.compose.MarkdownElement
import com.mikepenz.markdown.compose.elements.MarkdownTableBasicText
import org.intellij.markdown.MarkdownElementTypes.IMAGE
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.flavours.gfm.GFMTokenTypes.CELL

/**
 * Custom markdown table renderer components.
 *
 * **Why this code was copied from the library:**
 *
 * The `com.mikepenz:multiplatform-markdown-renderer` library (v0.38.1) uses Compose Multiplatform
 * (org.jetbrains.compose.*), which wraps Android Compose (androidx.compose.*) but provides a
 * different API surface. Even though Compose Multiplatform 1.9.2 is based on Android Compose 1.9.4,
 * there's a binary incompatibility issue.
 *
 * The library's bytecode was compiled against `org.jetbrains.compose.foundation.layout.RowScope`,
 * which generates a different `weight$default` synthetic method signature than code compiled
 * against `androidx.compose.foundation.layout.RowScope`. When the library calls `weight(1f)`,
 * Kotlin generates a `weight$default` method that expects the Compose Multiplatform API signature,
 * but at runtime it finds Android Compose's version with a different signature, causing a
 * `NoSuchMethodError`.
 *
 * By copying and compiling this code directly in our project against Android Compose, we ensure
 * the generated `weight$default` method signature matches what's available at runtime.
 *
 * This is a known binary compatibility issue between Compose Multiplatform and Android Compose
 * API surfaces, even when they're based on the same underlying Compose version.
 *
 * Source: Copied from `com.mikepenz.markdown.compose.elements.MarkdownTable` implementation.
 */
@Composable
internal fun MarkdownTableHeader(
    content: String,
    header: ASTNode,
    tableWidth: Dp,
    style: TextStyle,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    annotatorSettings: AnnotatorSettings = annotatorSettings(),
) {
    val markdownComponents = LocalMarkdownComponents.current
    val tableCellPadding = LocalMarkdownDimens.current.tableCellPadding
    Row(
        verticalAlignment = verticalAlignment,
        modifier = Modifier
            .widthIn(tableWidth)
            .height(IntrinsicSize.Max),
    ) {
        header.children.filter { it.type == CELL }.forEach { cell ->
            Column(
                modifier = Modifier
                    .padding(tableCellPadding)
                    .weight(1f),
            ) {
                if (cell.children.any { it.type == IMAGE }) {
                    MarkdownElement(node = cell, components = markdownComponents, content = content, includeSpacer = false)
                } else {
                    MarkdownTableBasicText(
                        content = content,
                        cell = cell,
                        style = style.copy(fontWeight = FontWeight.Bold),
                        maxLines = maxLines,
                        overflow = overflow,
                        annotatorSettings = annotatorSettings,
                    )
                }
            }
        }
    }
}

@Composable
internal fun MarkdownTableRow(
    content: String,
    header: ASTNode,
    tableWidth: Dp,
    style: TextStyle,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    annotatorSettings: AnnotatorSettings = annotatorSettings(),
) {
    val markdownComponents = LocalMarkdownComponents.current
    val tableCellPadding = LocalMarkdownDimens.current.tableCellPadding
    Row(
        verticalAlignment = verticalAlignment,
        modifier = Modifier.widthIn(tableWidth),
    ) {
        header.children.filter { it.type == CELL }.forEach { cell ->
            Column(
                modifier = Modifier
                    .padding(tableCellPadding)
                    .weight(1f),
            ) {
                if (cell.children.any { it.type == IMAGE }) {
                    MarkdownElement(node = cell, components = markdownComponents, content = content, includeSpacer = false)
                } else {
                    MarkdownTableBasicText(
                        content = content,
                        cell = cell,
                        style = style,
                        maxLines = maxLines,
                        overflow = overflow,
                        annotatorSettings = annotatorSettings,
                    )
                }
            }
        }
    }
}
