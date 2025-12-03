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

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.getstream.chat.android.ai.compose.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Displays a horizontal scrollable list of selected image attachments with remove functionality.
 *
 * Each attachment is shown as a thumbnail with a remove button overlay, allowing users to
 * deselect previously chosen images.
 *
 * @param uris The list of [Uri]s representing the selected image attachments to display.
 * @param onRemoveAttachment Callback invoked when the user taps the remove button on an attachment,
 * providing the [Uri] of the attachment to be removed.
 * @param modifier Optional [Modifier] for customizing the layout of the list.
 */
@Composable
internal fun SelectedAttachmentList(
    uris: List<Uri>,
    onRemoveAttachment: (Uri) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = uris,
            key = { it.toString() },
        ) { uri ->
            SelectedAttachment(
                uri = uri,
                onRemove = { onRemoveAttachment(uri) },
            )
        }
    }
}

/**
 * Displays a single selected attachment as a thumbnail image with a remove button overlay.
 *
 * @param uri The [Uri] of the image to display.
 * @param onRemove Callback invoked when the user taps the remove button.
 */
@Composable
private fun SelectedAttachment(
    uri: Uri,
    onRemove: () -> Unit,
) {
    AttachmentTile {
        UriImage(
            uri = uri,
            modifier = Modifier.matchParentSize(),
            placeholder = {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.LightGray),
                )
            },
            error = {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.stream_ai_compose_ic_image_placeholder),
                        contentDescription = null,
                    )
                }
            },
        )
        RemoveButton(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            onRemove = onRemove,
        )
    }
}

/**
 * A container composable that provides a rounded square tile for displaying attachment content.
 *
 * @param content The content to display inside the tile, scoped to [BoxScope] for alignment options.
 */
@Composable
private fun AttachmentTile(content: @Composable BoxScope.() -> Unit) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .size(100.dp)
            .background(Color.Transparent, shape)
            .clip(shape),
    ) {
        content()
    }
}

/**
 * Loads and displays an image from a content [Uri] with support for placeholder and error states.
 *
 * The image is loaded asynchronously on a background thread and downsampled to the target size
 * for memory efficiency.
 *
 * @param uri The content [Uri] of the image to load and display.
 * @param modifier Optional [Modifier] for customizing the image layout.
 * @param contentScale The [ContentScale] to apply when rendering the image. Defaults to [ContentScale.Crop].
 * @param placeholder Composable to display while the image is loading.
 * @param error Composable to display if the image fails to load.
 */
@Composable
private fun UriImage(
    uri: Uri,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholder: @Composable () -> Unit = { },
    error: @Composable () -> Unit = { },
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val targetSizePx = with(density) { 100.dp.toPx().toInt() }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }

    LaunchedEffect(uri) {
        isLoading = true
        hasError = false
        withContext(Dispatchers.IO) {
            try {
                bitmap = decodeSampledBitmap(context.contentResolver, uri, targetSizePx)
                hasError = bitmap == null
            } catch (_: Exception) {
                hasError = true
            }
        }
        isLoading = false
    }

    when {
        isLoading -> placeholder()
        hasError -> error()
        bitmap != null -> {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = null,
                modifier = modifier,
                contentScale = contentScale,
            )
        }
    }
}

/**
 * A circular button with a remove icon, typically used as an overlay on attachments.
 *
 * @param modifier [Modifier] for positioning and sizing the button.
 * @param onRemove Callback invoked when the button is clicked.
 */
@Composable
private fun RemoveButton(
    modifier: Modifier,
    onRemove: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .background(Color.Black.copy(alpha = 0.7f), CircleShape)
            .clip(CircleShape)
            .clickable { onRemove() },
    ) {
        Icon(
            painter = painterResource(R.drawable.stream_ai_compose_ic_cancel),
            tint = Color.LightGray,
            contentDescription = "Remove attachment",
        )
    }
}

@Preview
@Composable
private fun PreviewTest() {
    SelectedAttachment(Uri.parse("asd")) { }
}
