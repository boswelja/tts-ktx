plugins {
    id("com.android.library") version "7.4.0" apply false
    id("org.jetbrains.kotlin.jvm") version "1.8.10" apply false
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

group = "io.github.boswelja.tts-ktx"
version =  findProperty("version") ?: "0.1.0"

nexusPublishing {
    repositories {
        sonatype {
            val ossrhUsername: String? by project
            val ossrhPassword: String? by project
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl
                .set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            username.set(ossrhUsername)
            password.set(ossrhPassword)
        }
    }
}
