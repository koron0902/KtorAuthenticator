import org.jetbrains.kotlin.konan.properties.Properties

plugins {
    kotlin("jvm") version "1.8.10"
    id("io.ktor.plugin") version "2.2.3"
    id("maven-publish")
}

group = "com.milkcocoa.info"
version = "0.1.2"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}
application {
    mainClass.set("com.milkcocoa.info.ApplicationKt")
}


dependencies {
    implementation("com.google.firebase:firebase-admin:9.1.1")

    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation(kotlin("test"))

    val ktor_version = "2.3.4"
    implementation("io.ktor:ktor-server-core:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-server-auth:$ktor_version")

    testImplementation("io.ktor:ktor-serialization-jackson:$ktor_version")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// build.gradle.kts
fun getProp(name: String): String? {
    return rootProject.file("local.properties")
        .takeIf { it.exists() }
        ?.let { Properties().apply { load(it.reader()) }.getProperty(name) }
        ?: System.getenv(name)
}

val GITHUB_ACCESS_TOKEN = getProp("GITHUB_ACCESS_TOKEN")
val GITHUB_USER_NAME = getProp("GITHUB_USER_NAME")
println(GITHUB_USER_NAME)
println(GITHUB_ACCESS_TOKEN)
publishing {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/koron0902/KtorAuthenticator")
            credentials {
                username = GITHUB_USER_NAME
                password = GITHUB_ACCESS_TOKEN
            }
        }
    }

    publications {
        register<MavenPublication>("gpr") {
            from(components["java"])
        }
    }
}
