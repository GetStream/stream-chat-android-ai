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

package io.getstream.chat.android.ai.compose.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.getstream.chat.android.ai.compose.presentation.ChatViewModel
import io.getstream.chat.android.client.ChatClient

/**
 * Factory for creating ChatViewModel with dependencies.
 *
 * @param chatClient The Stream Chat client instance
 * @param conversationId The optional conversation ID for the chat
 */
public class ChatViewModelFactory(
    private val chatClient: ChatClient = ChatClient.instance(),
    private val conversationId: String?,
) : ViewModelProvider.Factory {

    /**
     * Creates a ChatViewModel instance.
     *
     * @param modelClass The class of the ViewModel to create
     * @return An instance of ChatViewModel
     * @throws IllegalArgumentException if the ViewModel type is not ChatViewModel
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        return ChatViewModel(
            chatClient = chatClient,
            chatAiRepository = ServiceLocator.get(),
            conversationId = conversationId,
        ) as T
    }
}
