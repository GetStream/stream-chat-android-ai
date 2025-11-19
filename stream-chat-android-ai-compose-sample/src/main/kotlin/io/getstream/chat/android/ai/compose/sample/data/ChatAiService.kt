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

import com.squareup.moshi.Moshi
import retrofit2.HttpException

/**
 * Service implementation for communicating with the Chat AI server.
 *
 * @param chatAiApi The Retrofit API interface for Chat AI endpoints
 * @param moshi The Moshi instance for JSON serialization/deserialization
 */
public class ChatAiService(
    private val chatAiApi: ChatAiApi,
    private val moshi: Moshi,
) : ChatAiRepository {

    /**
     * Starts an AI agent for the given channel.
     *
     * @param channelType The channel type (e.g., "messaging")
     * @param channelId The channel ID (e.g., "channel-id")
     * @param platform The AI platform to use ("openai", "anthropic", "gemini", or "xai")
     * @param model Optional model override (e.g., "gpt-4o", "claude-3-5-sonnet-20241022")
     */
    override suspend fun startAIAgent(
        channelType: String,
        channelId: String,
        platform: String,
        model: String?,
    ): Result<Unit> = try {
        val requestBody = StartAIAgentRequest(
            channel_type = channelType,
            channel_id = channelId,
            platform = platform,
            model = model,
        )
        val response = chatAiApi.startAIAgent(requestBody)

        // The API can return HTTP 200 with an error field in the response body
        // Check if response has an error field set
        if (response.error != null) {
            Result.failure(
                Exception(
                    response.error + (response.reason?.let { ": $it" } ?: ""),
                ),
            )
        } else {
            Result.success(Unit)
        }
    } catch (e: HttpException) {
        // HTTP errors (4xx, 5xx) may contain structured error responses
        // Try to parse the error body to extract a meaningful error message
        val errorMessage = try {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody != null) {
                val errorResponse = moshi.adapter(AIAgentResponse::class.java).fromJson(errorBody)
                errorResponse?.error ?: "HTTP ${e.code()}: ${e.message()}"
            } else {
                "HTTP ${e.code()}: ${e.message()}"
            }
        } catch (parseException: Exception) {
            // If parsing fails, fall back to HTTP status code and message
            "HTTP ${e.code()}: ${e.message()}"
        }
        Result.failure(Exception(errorMessage))
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Stops the AI agent for the given channel.
     *
     * @param channelId The channel ID (e.g., "channel-id")
     */
    override suspend fun stopAIAgent(channelId: String): Result<Unit> = try {
        val response = chatAiApi.stopAIAgent(StopAIAgentRequest(channel_id = channelId))

        // The API can return HTTP 200 with an error field in the response body
        // Check if response has an error field set
        if (response.error != null) {
            Result.failure(
                Exception(
                    response.error + (response.reason?.let { ": $it" } ?: ""),
                ),
            )
        } else {
            Result.success(Unit)
        }
    } catch (e: HttpException) {
        // HTTP errors (4xx, 5xx) may contain structured error responses
        // Try to parse the error body to extract a meaningful error message
        val errorMessage = try {
            val errorBody = e.response()?.errorBody()?.string()
            if (errorBody != null) {
                val errorResponse = moshi.adapter(AIAgentResponse::class.java).fromJson(errorBody)
                errorResponse?.error ?: "HTTP ${e.code()}: ${e.message()}"
            } else {
                "HTTP ${e.code()}: ${e.message()}"
            }
        } catch (parseException: Exception) {
            // If parsing fails, fall back to HTTP status code and message
            "HTTP ${e.code()}: ${e.message()}"
        }
        Result.failure(Exception(errorMessage))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
