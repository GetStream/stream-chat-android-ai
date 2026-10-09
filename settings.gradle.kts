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
    ":stream-chat-android-ai-compose",
    ":stream-chat-android-ai-ondevice",
)

// An app that builds these components from source (an included build, or a Gradle source
// dependency on a branch) gets only the SDK modules: the sample app would otherwise show up
// beside it as a second app to run, and the metrics module would build for nothing.
if (gradle.parent == null) {
    include(
        ":metrics:stream-chat-android-ai-metrics",
        ":stream-chat-android-ai-compose-sample",
    )
}
