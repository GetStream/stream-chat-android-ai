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

package io.getstream.chat.android.ai.compose.sample.presentation.chat

import io.getstream.chat.android.ai.compose.sample.domain.Message

/**
 * UI state for the active chat screen.
 */
public data class ChatState(
    val title: String = "New Chat",
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val aiState: AIState? = null,
) {
    /**
     * Whether the Assistant is currently streaming a response.
     * Derived from aiState: true when aiState is not null and not ERROR.
     */
    public val isStreaming: Boolean
        get() = aiState != null && aiState != AIState.ERROR
}

/**
 * Represents the AI assistant state.
 */
public enum class AIState {
    THINKING,
    GENERATING,
    CHECKING_SOURCES,
    ERROR,
}
