import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.stream.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.arturbosch.detekt)
}

android {
    namespace = "io.getstream.chat.android.ai.ondevice"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        // ML Kit's GenAI APIs need Android 8.0.
        minSdk = 26
        testOptions.targetSdk = libs.versions.targetSdk.get().toInt()
        lint.targetSdk = libs.versions.targetSdk.get().toInt()

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.addAll(
            listOf(
                "-progressive",
                "-Xconsistent-data-class-copy-visibility",
                "-Xexplicit-api=strict",
            ),
        )
    }
}

dependencies {
    detektPlugins(libs.detekt.formatting)

    // Flow is part of this module's API.
    api(libs.kotlinx.coroutines.core)
    implementation(libs.mlkit.genai.prompt)

    testImplementation(libs.junit)
}
