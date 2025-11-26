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

package io.getstream.chat.android.ai.compose.presentation

public data class ChatUiState(
    val title: String = "New Chat",
    val messages: List<Message> = emptyList(),
    val inputText: String = "",
    val assistantState: AssistantState = AssistantState.Idle,
) {
    public data class Message(
        val id: String,
        val role: Role,
        val content: String,
    ) {
        public sealed class Role {
            public data object Assistant : Role()
            public data object User : Role()
            public data object Other : Role()
        }
    }

    public enum class AssistantState {
        Idle,
        Thinking,
        CheckingSources,
        Generating,
        Error,
        ;

        public fun isBusy(): Boolean = this != Idle && this != Error
    }

    public fun getCurrentAssistantMessage(): Message? =
        messages.firstOrNull()?.takeIf { message -> message.role == Message.Role.Assistant }
}
