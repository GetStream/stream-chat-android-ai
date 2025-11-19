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

package io.getstream.chat.android.ai.compose.sample.di

import com.squareup.moshi.Moshi
import io.getstream.chat.android.ai.compose.sample.data.ChatAiApi
import io.getstream.chat.android.ai.compose.sample.data.ChatAiRepository
import io.getstream.chat.android.ai.compose.sample.data.ChatAiService
import okhttp3.OkHttpClient
import retrofit2.Retrofit

/**
 * Application-level dependency container.
 * This is a simple manual DI container that provides all application dependencies.
 *
 * In a production app, you might want to use a DI framework like Hilt or Koin,
 * but for simplicity, we use manual DI here.
 */
public class AppContainer(
    private val baseUrl: String = "http://10.0.2.2:3000", // Android emulator localhost
    private val enableLogging: Boolean = true,
) {
    // Network dependencies
    private val moshi: Moshi = NetworkModule.createMoshi()
    private val okHttpClient: OkHttpClient = NetworkModule.createOkHttpClient(enableLogging)
    private val retrofit: Retrofit = NetworkModule.createRetrofit(baseUrl, okHttpClient, moshi)

    // API dependencies
    private val chatAiApi: ChatAiApi = retrofit.create(ChatAiApi::class.java)

    // Repository dependencies
    public val chatAiRepository: ChatAiRepository = ChatAiService(
        chatAiApi = chatAiApi,
        moshi = moshi,
    )
}
