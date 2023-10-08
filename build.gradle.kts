plugins {
    kotlin("jvm") version "1.8.10"
    id("io.ktor.plugin") version "2.2.3"
}

group = "com.milkcocoa.info"
version = "0.1.0"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}
application {
    mainClass.set("com.milkcocoa.info.ApplicationKt")
}


dependencies {
    api("com.google.firebase:firebase-admin:9.1.1")

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

