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

package io.getstream.chat.android.ai.compose.di

import kotlin.reflect.KClass

internal object ServiceLocator {

    private val services = mutableMapOf<KClass<*>, Any>()

    inline fun <reified T : Any> register(instance: T) {
        services[T::class] = instance
    }

    inline fun <reified T : Any> registerLazy(noinline creator: () -> T) {
        services[T::class] = lazy(creator)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T : Any> get(): T =
        services[T::class]?.let { service ->
            when (service) {
                is Lazy<*> -> (service as Lazy<T>).value
                else ->
                    service as? T
                        ?: throw IllegalStateException("Service ${T::class.simpleName} is registered with wrong type")
            }
        } ?: throw IllegalStateException("Service ${T::class.simpleName} not registered")
}
