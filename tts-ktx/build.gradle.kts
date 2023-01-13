plugins {
    id("com.android.library")
    id("kotlin-android")
    id("maven-publish")
    id("signing")
    id("io.gitlab.arturbosch.detekt") version "1.22.0"
}

android {
    namespace = "com.boswelja.tts"
    compileSdk = 33

    defaultConfig {
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs + "-Xexplicit-api=warning"
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
}

publishing {
    repositories {
        maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
            val ossrhUsername: String? by project
            val ossrhPassword: String? by project
            name = "sonatype"
            credentials {
                username = ossrhUsername
                password = ossrhPassword
            }
        }
    }
    publications {
        publications.withType<MavenPublication> {
            pom {
                name.set("tts-ktx")
                description.set("Kotlin extensions for Android's TextToSpeech class")
                url.set("https://github.com/boswelja/tts-ktx")
                licenses {
                    license {
                        name.set("Apache 2.0")
                        url.set("https://github.com/boswelja/tts-ktx/blob/main/LICENSE")
                    }
                }
                developers {
                    developer {
                        id.set("boswelja")
                        name.set("Jack Boswell")
                        email.set("boswelja@outlook.com")
                        url.set("https://github.com/boswelja")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/boswelja/tts-ktx.git")
                    developerConnection.set("scm:git:ssh://github.com/boswelja/tts-ktx.git")
                    url.set("https://github.com/boswelja/tts-ktx")
                }
            }
        }
    }
}

// Create signing config
signing {
    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)
    sign(publishing.publications)
}
