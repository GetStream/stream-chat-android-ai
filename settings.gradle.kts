pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
        mavenLocal()
        maven("https://stream-io-repo.com")
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenLocal()
        google()
        mavenCentral()
        maven("https://stream-io-repo.com")
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "stream-chat-android-ai"

include(
    ":metrics:stream-chat-android-ai-metrics",
    ":stream-chat-android-ai-compose",
    ":stream-chat-android-ai-compose-sample",
    ":stream-chat-android-ai-ondevice",
)
