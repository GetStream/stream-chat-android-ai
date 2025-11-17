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

package io.getstream.chat.android.ai.compose.sample.chat

import java.util.UUID

/**
 * Creates sample conversations with rich markdown content.
 * These conversations showcase markdown tables, code snippets, and various formatting features.
 */
public fun createSampleConversations(): List<ChatConversation> {
    return listOf(
        ChatConversation(
            id = "1",
            title = "Markdown Tables & Formatting",
            messages = listOf(
                Message(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.User,
                    content = "Can you show me examples of markdown tables?",
                    timestamp = System.currentTimeMillis() - 3600000,
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.Assistant,
                    content = """Here are some examples of markdown tables:

## Basic Table

| Feature | Status | Priority |
|---------|--------|----------|
| Authentication | ✅ Complete | High |
| Database | 🚧 In Progress | High |
| API Integration | ⏳ Planned | Medium |
| UI Design | ✅ Complete | Low |

## Advanced Table with Alignment

| Language | Year | Type | Popularity |
|:---------|:----:|:-----|----------:|
| Kotlin | 2011 | Static | 95% |
| Python | 1991 | Dynamic | 98% |
| JavaScript | 1995 | Dynamic | 99% |
| Rust | 2010 | Static | 87% |

## Comparison Table

| Framework | Learning Curve | Performance | Community |
|-----------|---------------|-------------|-----------|
| **React** | Medium | ⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ |
| **Vue** | Easy | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ |
| **Angular** | Hard | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ |

You can use tables to organize data, compare features, or display structured information. The alignment is controlled by colons (`:`) in the header separator.""",
                    timestamp = System.currentTimeMillis() - 3600000 + 5000,
                ),
            ),
        ),
        ChatConversation(
            id = "2",
            title = "Code Snippets & Examples",
            messages = listOf(
                Message(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.User,
                    content = "Show me code examples in different languages",
                    timestamp = System.currentTimeMillis() - 7200000,
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.Assistant,
                    content = """Here are code examples in various languages:

## Kotlin Example

```kotlin
data class User(
    val id: String,
    val name: String,
    val email: String
)

class UserRepository {
    suspend fun getUser(id: String): Result<User> {
        return try {
            val user = apiService.getUser(id)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

## Python Example

```python
from typing import List, Optional
from dataclasses import dataclass

@dataclass
class User:
    id: str
    name: str
    email: str

def get_users() -> List[User]:
    '''Fetch all users from the database.'''
    return [
        User(id="1", name="Alice", email="alice@example.com"),
        User(id="2", name="Bob", email="bob@example.com")
    ]
```

## JavaScript/TypeScript Example

```typescript
interface User {
    id: string;
    name: string;
    email: string;
}

async function fetchUser(id: string): Promise<User> {
    const response = await fetch('/api/users/' + id);
    if (!response.ok) {
        throw new Error('Failed to fetch user');
    }
    return response.json();
}
```

## Swift Example

```swift
struct User: Codable {
    let id: String
    let name: String
    let email: String
}

class UserService {
    func fetchUser(id: String) async throws -> User {
        let url = URL(string: "https://api.example.com/users/\(id)")!
        let (data, _) = try await URLSession.shared.data(from: url)
        return try JSONDecoder().decode(User.self, from: data)
    }
}
```

Each language has its own syntax and conventions!""",
                    timestamp = System.currentTimeMillis() - 7200000 + 8000,
                ),
            ),
        ),
        ChatConversation(
            id = "3",
            title = "Mixed Markdown Content",
            messages = listOf(
                Message(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.User,
                    content = "Explain REST API best practices with examples",
                    timestamp = System.currentTimeMillis() - 1800000,
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.Assistant,
                    content = """# REST API Best Practices

## 1. Use Proper HTTP Methods

| Method | Usage | Idempotent |
|--------|-------|------------|
| `GET` | Retrieve resources | ✅ Yes |
| `POST` | Create resources | ❌ No |
| `PUT` | Update/replace resources | ✅ Yes |
| `PATCH` | Partial updates | ❌ No |
| `DELETE` | Remove resources | ✅ Yes |

## 2. Status Codes

Use appropriate HTTP status codes:

- **2xx Success**: `200 OK`, `201 Created`, `204 No Content`
- **4xx Client Error**: `400 Bad Request`, `401 Unauthorized`, `404 Not Found`
- **5xx Server Error**: `500 Internal Server Error`, `503 Service Unavailable`

## 3. Example Implementation

```kotlin
@RestController
@RequestMapping("/api/users")
class UserController {

    @GetMapping("/{id}")
    fun getUser(@PathVariable id: String): ResponseEntity<User> {
        val user = userService.findById(id)
        return if (user != null) {
            ResponseEntity.ok(user)
        } else {
            ResponseEntity.notFound().build()
        }
    }

    @PostMapping
    fun createUser(@RequestBody user: CreateUserRequest): ResponseEntity<User> {
        val created = userService.create(user)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(created)
    }
}
```

## 4. Response Format

Always return consistent JSON structures:

```json
{
  "data": {
    "id": "123",
    "name": "John Doe",
    "email": "john@example.com"
  },
  "meta": {
    "timestamp": "2024-01-15T10:30:00Z"
  }
}
```

## 5. Error Handling

```kotlin
{
  "error": {
    "code": "USER_NOT_FOUND",
    "message": "User with ID 123 does not exist",
    "details": {
      "userId": "123",
      "timestamp": "2024-01-15T10:30:00Z"
    }
  }
}
```

## Key Takeaways

1. ✅ Use **RESTful** resource naming
2. ✅ Implement proper **authentication** and **authorization**
3. ✅ Use **pagination** for large datasets
4. ✅ Include **rate limiting** to prevent abuse
5. ✅ Provide **clear documentation** (OpenAPI/Swagger)

Follow these practices for robust and maintainable APIs! 🚀""",
                    timestamp = System.currentTimeMillis() - 1800000 + 10000,
                ),
            ),
        ),
        ChatConversation(
            id = "4",
            title = "Data Structures & Algorithms",
            messages = listOf(
                Message(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.User,
                    content = "Explain binary search with code examples",
                    timestamp = System.currentTimeMillis() - 5400000,
                ),
                Message(
                    id = UUID.randomUUID().toString(),
                    role = MessageRole.Assistant,
                    content = """# Binary Search Algorithm

Binary search is an efficient algorithm for finding an item in a **sorted** array. It works by repeatedly dividing the search interval in half.

## Time Complexity

| Operation | Best Case | Average Case | Worst Case |
|-----------|-----------|-------------|------------|
| Search | O(1) | O(log n) | O(log n) |
| Space | O(1) | O(1) | O(1) |

## Implementation

### Iterative Approach

```kotlin
fun binarySearch(arr: IntArray, target: Int): Int {
    var left = 0
    var right = arr.size - 1

    while (left <= right) {
        val mid = left + (right - left) / 2

        when {
            arr[mid] == target -> return mid
            arr[mid] < target -> left = mid + 1
            else -> right = mid - 1
        }
    }

    return -1 // Not found
}
```

### Recursive Approach

```kotlin
fun binarySearchRecursive(
    arr: IntArray,
    target: Int,
    left: Int = 0,
    right: Int = arr.size - 1
): Int {
    if (left > right) return -1

    val mid = left + (right - left) / 2

    return when {
        arr[mid] == target -> mid
        arr[mid] < target -> binarySearchRecursive(arr, target, mid + 1, right)
        else -> binarySearchRecursive(arr, target, left, mid - 1)
    }
}
```

## Example Usage

```kotlin
val sortedArray = intArrayOf(1, 3, 5, 7, 9, 11, 13, 15)
val target = 7

val index = binarySearch(sortedArray, target)
if (index != -1) {
    println("Found at index: " + index)
} else {
    println("Not found")
}
```

## Visual Representation

```
Searching for 7 in [1, 3, 5, 7, 9, 11, 13, 15]

Step 1: mid = 3, arr[3] = 7 ✅ Found!
```

## When to Use

- ✅ Array is **sorted**
- ✅ Need **O(log n)** performance
- ✅ Random access is available

## Limitations

- ❌ Requires **sorted** data
- ❌ Not suitable for **linked lists**
- ❌ Overhead for **small arrays**""",
                    timestamp = System.currentTimeMillis() - 5400000 + 12000,
                ),
            ),
        ),
    )
}
