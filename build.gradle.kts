import com.vanniktech.maven.publish.AndroidSingleVariantLibrary
import com.vanniktech.maven.publish.MavenPublishBaseExtension
import io.getstream.chat.android.ai.Configuration
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

plugins {
    alias(libs.plugins.stream.project)
    alias(libs.plugins.stream.android.application) apply false
    alias(libs.plugins.stream.android.library) apply false
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.arturbosch.detekt) apply true
    alias(libs.plugins.maven.publish)
}

detekt {
    autoCorrect = true
    toolVersion = libs.versions.detekt.get()
    config.setFrom(file("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
}

private val isSnapshot = System.getenv("SNAPSHOT")?.toBoolean() == true
version = if (isSnapshot) {
    val timestamp = SimpleDateFormat("yyyyMMddHHmmss").run {
        timeZone = TimeZone.getTimeZone("UTC")
        format(Date())
    }
    "${Configuration.versionName}-${timestamp}-SNAPSHOT"
} else {
    Configuration.versionName
}

subprojects {
    plugins.withId("com.vanniktech.maven.publish") {
        extensions.configure<MavenPublishBaseExtension> {
            publishToMavenCentral(automaticRelease = true)

            configure(
                AndroidSingleVariantLibrary(
                    variant = "release",
                    sourcesJar = true,
                    publishJavadocJar = true,
                )
            )

            pom {
                name.set("stream-chat-android-ai")
                description.set("Official AI components for Stream Android Chat SDK")
                url.set("https://github.com/getstream/stream-chat-android-ai")

                licenses {
                    license {
                        name.set("Stream License")
                        url.set("https://github.com/GetStream/stream-chat-android-ai/blob/main/LICENSE")
                    }
                }

                developers {
                    developer {
                        id = "aleksandar-apostolov"
                        name = "Aleksandar Apostolov"
                        email = "aleksandar.apostolov@getstream.io"
                    }
                    developer {
                        id = "VelikovPetar"
                        name = "Petar Velikov"
                        email = "petar.velikov@getstream.io"
                    }
                    developer {
                        id = "andremion"
                        name = "André Rêgo"
                        email = "andre.rego@getstream.io"
                    }
                    developer {
                        id = "rahul-lohra"
                        name = "Rahul Kumar Lohra"
                        email = "rahul.lohra@getstream.io"
                    }
                    developer {
                        id = "gpunto"
                        name = "Gianmarco David"
                        email = "gianmarco.david@getstream.io"
                    }
                    developer {
                        id = "PratimMallick"
                        name = "Pratim Mallick"
                        email = "pratim.mallick@getstream.io"
                    }
                }

                scm {
                    connection.set("scm:git:github.com/getstream/stream-chat-android-ai.git")
                    developerConnection.set("scm:git:ssh://github.com/getstream/stream-chat-android-ai.git")
                    url.set("https://github.com/getstream/stream-chat-android-ai/tree/main")
                }
            }
        }
    }
}

tasks.register("printAllArtifacts") {
    group = "publishing"
    description = "Prints all artifacts that will be published"

    doLast {
        subprojects.forEach { subproject ->
            subproject.plugins.withId("com.vanniktech.maven.publish") {
                subproject.extensions.findByType(PublishingExtension::class.java)
                    ?.publications
                    ?.filterIsInstance<MavenPublication>()
                    ?.forEach { println("${it.groupId}:${it.artifactId}:${it.version}") }
            }
        }
    }
}
