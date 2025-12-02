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

package io.getstream.chat.android.ai.compose

import io.getstream.chat.android.ai.compose.data.ChatAiApi
import io.getstream.chat.android.ai.compose.data.ChatAiRepository
import io.getstream.chat.android.ai.compose.data.ChatAiService
import io.getstream.chat.android.ai.compose.di.NetworkModule
import io.getstream.chat.android.ai.compose.di.ServiceLocator
import retrofit2.Retrofit

/**
 * Main entry point for initializing the Chat AI Compose module.
 * This object provides the initialization method to set up all required dependencies
 * for the Chat AI functionality, including network components and repositories.
 */
public object ChatAi {

    /**
     * Initializes the Chat AI module with the required configuration.
     * This method sets up all necessary dependencies including:
     * - Network components (Moshi, OkHttp, Retrofit)
     * - API interfaces
     * - Repository implementations
     *
     * Must be called before using any Chat AI functionality.
     *
     * @param baseUrl The base URL of the Chat AI backend server
     * @param enableLogging Whether to enable HTTP request/response logging. Defaults to true.
     */
    public fun initialize(
        baseUrl: String,
        enableLogging: Boolean = true,
    ) {
        with(ServiceLocator) {
            // Network dependencies
            registerLazy { NetworkModule.createMoshi() }
            registerLazy { NetworkModule.createOkHttpClient(enableLogging) }
            registerLazy { NetworkModule.createRetrofit(baseUrl, okHttpClient = get(), moshi = get()) }
            // API dependencies
            registerLazy { get<Retrofit>().create(ChatAiApi::class.java) }
            // Repository dependencies
            registerLazy<ChatAiRepository> { ChatAiService(chatAiApi = get(), moshi = get()) }
        }
    }
}
