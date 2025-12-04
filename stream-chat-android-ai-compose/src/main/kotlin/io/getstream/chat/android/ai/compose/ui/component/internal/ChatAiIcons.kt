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

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/**
 * Internal icons used by the SDK components.
 * These are simple vector icons to avoid additional dependencies.
 */
@Suppress("MagicNumber")
internal object ChatAiIcons {
    /**
     * Add icon (plus sign).
     */
    val Add: ImageVector by lazy {
        ImageVector.Builder(
            name = "Add",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(440f, 520f)
                lineTo(240f, 520f)
                quadTo(223f, 520f, 211.5f, 508.5f)
                quadTo(200f, 497f, 200f, 480f)
                quadTo(200f, 463f, 211.5f, 451.5f)
                quadTo(223f, 440f, 240f, 440f)
                lineTo(440f, 440f)
                lineTo(440f, 240f)
                quadTo(440f, 223f, 451.5f, 211.5f)
                quadTo(463f, 200f, 480f, 200f)
                quadTo(497f, 200f, 508.5f, 211.5f)
                quadTo(520f, 223f, 520f, 240f)
                lineTo(520f, 440f)
                lineTo(720f, 440f)
                quadTo(737f, 440f, 748.5f, 451.5f)
                quadTo(760f, 463f, 760f, 480f)
                quadTo(760f, 497f, 748.5f, 508.5f)
                quadTo(737f, 520f, 720f, 520f)
                lineTo(520f, 520f)
                lineTo(520f, 720f)
                quadTo(520f, 737f, 508.5f, 748.5f)
                quadTo(497f, 760f, 480f, 760f)
                quadTo(463f, 760f, 451.5f, 748.5f)
                quadTo(440f, 737f, 440f, 720f)
                lineTo(440f, 520f)
                close()
            }
        }.build()
    }

    /**
     * Stop icon (square).
     */
    val Stop: ImageVector by lazy {
        ImageVector.Builder(
            name = "Stop",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(240f, 320f)
                quadTo(240f, 287f, 263.5f, 263.5f)
                quadTo(287f, 240f, 320f, 240f)
                lineTo(640f, 240f)
                quadTo(673f, 240f, 696.5f, 263.5f)
                quadTo(720f, 287f, 720f, 320f)
                lineTo(720f, 640f)
                quadTo(720f, 673f, 696.5f, 696.5f)
                quadTo(673f, 720f, 640f, 720f)
                lineTo(320f, 720f)
                quadTo(287f, 720f, 263.5f, 696.5f)
                quadTo(240f, 673f, 240f, 640f)
                lineTo(240f, 320f)
                close()
            }
        }.build()
    }

    /**
     * Send icon (arrow up).
     */
    val Send: ImageVector by lazy {
        ImageVector.Builder(
            name = "Send",
            defaultWidth = 24.dp,
            defaultHeight = 24.dp,
            viewportWidth = 960f,
            viewportHeight = 960f,
        ).apply {
            path(fill = SolidColor(Color.Black)) {
                moveTo(440f, 352f)
                lineTo(324f, 468f)
                quadTo(313f, 479f, 296f, 479f)
                quadTo(279f, 479f, 268f, 468f)
                quadTo(257f, 457f, 257f, 440f)
                quadTo(257f, 423f, 268f, 412f)
                lineTo(452f, 228f)
                quadTo(464f, 216f, 480f, 216f)
                quadTo(496f, 216f, 508f, 228f)
                lineTo(692f, 412f)
                quadTo(703f, 423f, 703f, 440f)
                quadTo(703f, 457f, 692f, 468f)
                quadTo(681f, 479f, 664f, 479f)
                quadTo(647f, 479f, 636f, 468f)
                lineTo(520f, 352f)
                lineTo(520f, 680f)
                quadTo(520f, 697f, 508.5f, 708.5f)
                quadTo(497f, 720f, 480f, 720f)
                quadTo(463f, 720f, 451.5f, 708.5f)
                quadTo(440f, 697f, 440f, 680f)
                lineTo(440f, 352f)
                close()
            }
        }.build()
    }
}
