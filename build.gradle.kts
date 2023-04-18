plugins {
    kotlin("jvm") version "1.8.0"
    `maven-publish`
}

group = "net.bruhitsalex.sjch"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven(url = "https://jitpack.io")
}

dependencies {
    implementation("net.dv8tion:JDA:5.0.0-alpha.20")
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("io.github.classgraph:classgraph:4.8.157")
    implementation("org.slf4j:slf4j-simple:2.0.5")
}

kotlin {
    jvmToolchain(8)
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "net.bruhitsalex.sjch"
            artifactId = "sjch"
            version = "1.0-SNAPSHOT"

            from(components["java"])
        }
    }
}