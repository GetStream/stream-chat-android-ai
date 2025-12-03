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

import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

/**
 * Creates and remembers a photo picker launcher for selecting multiple images from the device gallery.
 *
 * This composable wraps [rememberLauncherForActivityResult] with [ActivityResultContracts.PickMultipleVisualMedia]
 * to provide a simple way to launch the system photo picker and receive selected image URIs.
 *
 * @param onResult Callback invoked with the list of selected image [Uri]s. Returns an empty list if the user
 * cancels the picker without selecting any images.
 * @return A [ManagedActivityResultLauncher] that can be used to launch the photo picker via
 * [ManagedActivityResultLauncher.launch] with a [PickVisualMediaRequest].
 */
@Composable
public fun rememberPhotoPickerLauncher(onResult: (List<Uri>) -> Unit): ManagedActivityResultLauncher<PickVisualMediaRequest, List<@JvmSuppressWildcards Uri>> =
    rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia()) { uris ->
        onResult(uris)
    }
