plugins {
    alias(libs.plugins.stream.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "io.getstream.chat.android.ai.metrics"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            signingConfig = signingConfigs.findByName("debug")
        }
    }

    flavorDimensions += "sdk"

    productFlavors {
        create("stream-chat-android-ai-compose-baseline") {
            dimension = "sdk"
        }
        create("stream-chat-android-ai-compose-stream") {
            dimension = "sdk"
        }
    }
}

afterEvaluate {
    android.productFlavors.forEach { flavor ->
        val flavorName = flavor.name
        // For compose flavors, we set up build features and add common compose dependencies.
        if (flavorName.contains("compose")) {
            android.buildFeatures.compose = true
            val configurationName = "${flavorName}Implementation"
            dependencies.add(configurationName, dependencies.platform(libs.androidx.compose.bom))
            dependencies.add(configurationName, libs.bundles.androidx.compose)
            dependencies.add(configurationName, libs.androidx.lifecycle.viewmodel.compose)
            dependencies.add(configurationName, libs.androidx.activity.compose)
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    implementation(libs.kotlinx.coroutines.core)

    "stream-chat-android-ai-compose-streamImplementation"(project(":stream-chat-android-ai-compose"))
}
