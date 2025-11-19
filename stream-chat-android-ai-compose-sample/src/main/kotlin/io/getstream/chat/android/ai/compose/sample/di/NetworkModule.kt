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
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Network module for providing network-related dependencies.
 * This module encapsulates the creation of Retrofit, OkHttp, and Moshi instances.
 */
public object NetworkModule {
    /**
     * Creates a Moshi instance with Kotlin reflection support.
     */
    public fun createMoshi(): Moshi {
        return Moshi.Builder()
            .addLast(KotlinJsonAdapterFactory())
            .build()
    }

    /**
     * Creates an OkHttpClient with logging interceptor.
     *
     * @param enableLogging Whether to enable HTTP request/response logging (default: true in debug builds)
     */
    public fun createOkHttpClient(enableLogging: Boolean = true): OkHttpClient {
        return OkHttpClient.Builder()
            .apply {
                if (enableLogging) {
                    addInterceptor(
                        HttpLoggingInterceptor().apply {
                            level = HttpLoggingInterceptor.Level.BODY
                        },
                    )
                }
            }
            .build()
    }

    /**
     * Creates a Retrofit instance.
     *
     * @param baseUrl The base URL for the API
     * @param okHttpClient The OkHttpClient to use
     * @param moshi The Moshi instance for JSON serialization
     */
    public fun createRetrofit(
        baseUrl: String,
        okHttpClient: OkHttpClient,
        moshi: Moshi,
    ): Retrofit {
        val normalizedBaseUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        return Retrofit.Builder()
            .baseUrl(normalizedBaseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }
}
