plugins {
    alias(libs.plugins.stream.project)
    alias(libs.plugins.stream.android.application) apply false
    alias(libs.plugins.stream.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.arturbosch.detekt) apply true
}

streamProject {
    repositoryName.set("stream-chat-android-ai")
    publishing {
        description.set("Official AI components for Stream Android Chat SDK")
    }
}

detekt {
    autoCorrect = true
    toolVersion = libs.versions.detekt.get()
    config.setFrom(file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}
