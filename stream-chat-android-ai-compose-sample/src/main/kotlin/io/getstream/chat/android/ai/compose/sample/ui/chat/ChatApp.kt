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

package io.getstream.chat.android.ai.compose.sample.ui.chat

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import io.getstream.chat.android.ai.compose.sample.chat.ChatViewModel
import io.getstream.chat.android.ai.compose.sample.ui.components.ChatDrawer
import kotlinx.coroutines.launch

/**
 * Top-level chat app composable.
 */
@Composable
public fun ChatApp(
    viewModel: ChatViewModel,
    userName: String = "User",
    userEmail: String = "user@example.com",
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val currentTitle = if (uiState.currentChatId != null) {
        uiState.conversations.find { it.id == uiState.currentChatId }?.title ?: "Chat"
    } else {
        "New chat"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerShape = RectangleShape,
                windowInsets = WindowInsets(),
            ) {
                ChatDrawer(
                    userName = userName,
                    userEmail = userEmail,
                    conversations = uiState.conversations,
                    currentChatId = uiState.currentChatId,
                    onNewChatClick = {
                        viewModel.startNewChat()
                        scope.launch { drawerState.close() }
                    },
                    onConversationClick = { conversationId ->
                        viewModel.loadConversation(conversationId)
                        scope.launch { drawerState.close() }
                    },
                    onSettingsClick = {
                        // TODO: Navigate to settings
                        scope.launch { drawerState.close() }
                    },
                )
            }
        },
        modifier = modifier,
    ) {
        // Main chat content
        ChatScaffold(
            messages = uiState.messages,
            inputText = uiState.inputText,
            isStreaming = uiState.isStreaming,
            onInputTextChange = { viewModel.updateInputText(it) },
            onSend = { viewModel.sendMessage() },
            onStop = { viewModel.stopStreaming() },
            title = currentTitle,
            selectedModel = uiState.selectedModel,
            onMenuClick = {
                scope.launch {
                    drawerState.open()
                }
            },
            onModelClick = {
                // TODO: Show model selector dialog
            },
            modifier = Modifier.fillMaxSize(),
        )
    }
}
