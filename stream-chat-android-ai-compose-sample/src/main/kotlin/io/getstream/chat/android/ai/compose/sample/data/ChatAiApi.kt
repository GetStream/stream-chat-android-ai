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

package io.getstream.chat.android.ai.compose.sample.data

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for Chat AI endpoints.
 * These endpoints communicate with the backend server to manage AI agents for chat channels.
 */
public interface ChatAiApi {
    /**
     * Starts an AI agent for a specific channel.
     *
     * @param request Contains channel information and AI platform/model configuration
     * @return Response indicating success or failure with optional error details
     */
    @POST("/start-ai-agent")
    suspend fun startAIAgent(@Body request: StartAIAgentRequest): AIAgentResponse

    /**
     * Stops the AI agent for a specific channel.
     *
     * @param request Contains the channel ID to stop the agent for
     * @return Response indicating success or failure with optional error details
     */
    @POST("/stop-ai-agent")
    suspend fun stopAIAgent(@Body request: StopAIAgentRequest): AIAgentResponse
}

/**
 * Request model for starting an AI agent.
 *
 * @param channel_type The type of channel (e.g., "messaging")
 * @param channel_id The unique identifier for the channel (without type prefix)
 * @param platform The AI platform to use ("openai", "anthropic", "gemini", or "xai")
 * @param model Optional model override. If null, the platform's default model is used
 */
public data class StartAIAgentRequest(
    val channel_type: String,
    val channel_id: String,
    val platform: String,
    val model: String?,
)

/**
 * Request model for stopping an AI agent.
 *
 * @param channel_id The unique identifier for the channel (without type prefix)
 */
public data class StopAIAgentRequest(
    val channel_id: String,
)

/**
 * Response model from the Chat AI API.
 *
 * @param message Success message from the server
 * @param data Additional data returned by the server (typically empty)
 * @param error Error message if the operation failed, null otherwise
 * @param reason Additional error reason/details if available
 */
public data class AIAgentResponse(
    val message: String,
    val data: List<String> = emptyList(),
    val error: String? = null,
    val reason: String? = null,
)
