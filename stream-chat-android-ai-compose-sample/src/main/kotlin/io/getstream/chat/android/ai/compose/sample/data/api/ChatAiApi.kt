package io.getstream.chat.android.ai.compose.sample.data.api

import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Retrofit API interface for Chat AI endpoints.
 * These endpoints communicate with the backend server to manage AI agents for chat channels.
 */
internal interface ChatAiApi {
    /**
     * Starts an AI agent for a specific channel.
     */
    @POST("/start-ai-agent")
    suspend fun startAIAgent(@Body request: StartAIAgentRequest): AIAgentResponse

    /**
     * Stops the AI agent for a specific channel.
     */
    @POST("/stop-ai-agent")
    suspend fun stopAIAgent(@Body request: StopAIAgentRequest): AIAgentResponse

    /**
     * Summarizes text using the specified AI platform.
     */
    @POST("/summarize")
    suspend fun summarize(@Body request: SummarizeRequest): SummarizeResponse
}