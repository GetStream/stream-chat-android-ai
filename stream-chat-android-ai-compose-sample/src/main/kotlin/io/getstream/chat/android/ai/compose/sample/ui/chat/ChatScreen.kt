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

import android.app.Application
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.ai.compose.component.LoadingIndicator
import io.getstream.chat.android.ai.compose.sample.di.ChatViewModelFactory
import io.getstream.chat.android.ai.compose.sample.domain.Message
import io.getstream.chat.android.ai.compose.sample.domain.MessageRole
import io.getstream.chat.android.ai.compose.sample.presentation.chat.AIState
import io.getstream.chat.android.ai.compose.sample.presentation.chat.ChatViewModel
import io.getstream.chat.android.ai.compose.sample.ui.components.ChatComposer
import io.getstream.chat.android.ai.compose.sample.ui.components.ChatMessageItem
import io.getstream.chat.android.ai.compose.sample.ui.components.ChatScaffold
import io.getstream.chat.android.ai.compose.sample.ui.components.ChatTopBar
import kotlinx.coroutines.delay

@Composable
public fun ChatScreen(
    conversationId: String?,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier,
    onChatDeleted: () -> Unit = {},
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application
    val chatViewModel = viewModel<ChatViewModel>(
        key = conversationId,
        factory = ChatViewModelFactory(
            application = application,
            conversationId = conversationId,
        ),
    )

    val listState = rememberLazyListState()
    val state by chatViewModel.uiState.collectAsState()

    val title = state.title
    val messages = state.messages
    val inputText = state.inputText
    val isStreaming = state.isStreaming
    val aiState = state.aiState

    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var hasScrolledToBottomInitially by remember(conversationId) { mutableStateOf(false) }

    // Scroll to bottom when messages are initially loaded
    // In reverse layout, item 0 is at the bottom (where new messages appear)
    LaunchedEffect(messages.size, hasScrolledToBottomInitially) {
        if (messages.isNotEmpty() && !hasScrolledToBottomInitially) {
            // Small delay to ensure layout is complete
            delay(100)
            if (listState.layoutInfo.totalItemsCount > 0) {
                // In reverse layout, scroll to item 0 to show the bottom (newest messages)
                listState.animateScrollToItem(0)
                hasScrolledToBottomInitially = true
            }
        }
    }

    val confirmDelete = {
        showDeleteConfirmation = false
        chatViewModel.deleteChannel(
            onSuccess = { onChatDeleted() },
        )
    }

    val mostRecentAssistantMessage = messages.firstOrNull { message -> message.role == MessageRole.Assistant }
    val assistantMessageContent = mostRecentAssistantMessage?.content

    ChatScaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { modifier ->
            ChatTopBar(
                modifier = modifier,
                title = title,
                onMenuClick = onMenuClick,
                onDeleteClick = if (conversationId != null) {
                    { showDeleteConfirmation = true }
                } else {
                    null
                },
            )
        },
        bottomBar = { modifier ->
            ChatComposer(
                modifier = modifier,
                text = inputText,
                onTextChange = chatViewModel::onInputTextChange,
                onSend = chatViewModel::sendMessage,
                onStop = chatViewModel::stopStreaming,
                isStreaming = isStreaming,
            )
        },
    ) { contentPadding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = contentPadding,
            reverseLayout = true,
        ) {
            // Assistant loading indicator appears at the bottom
            item(key = "ai_state_indicator") {
                when (aiState) {
                    AIState.ERROR -> AssistantErrorMessage(
                        text = "Sorry, I encountered an error while processing your request. Please try again.",
                    )

                    else -> {
                        val text = when (aiState) {
                            AIState.THINKING -> "Thinking"
                            AIState.GENERATING -> "Generating response".takeIf { assistantMessageContent.isNullOrBlank() }
                            AIState.CHECKING_SOURCES -> "Checking sources"
                            else -> null
                        }
                        if (text != null) {
                            LoadingIndicator(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                label = { Text(text = text) },
                            )
                        }
                    }
                }
            }
            // Messages appear above the Assistant loading indicator
            items(
                key = Message::id,
                items = messages,
            ) { message ->
                // Determine if this message is the one currently being streamed
                val isMessageStreaming = isStreaming && mostRecentAssistantMessage?.id == message.id

                ChatMessageItem(
                    message = message,
                    isStreaming = isMessageStreaming,
                )
            }
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Chat") },
            text = { Text("Are you sure you want to delete this chat? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = confirmDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.onSurface),
                ) {
                    Text("Cancel")
                }
            },
        )
    }
}

/**
 * Displays an assistant error message.
 */
@Composable
private fun AssistantErrorMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.error,
        modifier = modifier,
    )
}
