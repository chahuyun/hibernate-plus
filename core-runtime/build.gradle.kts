plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("java-library")
    id("maven-publish")
}

kotlin {
    jvmToolchain(17)
}

java {
    withSourcesJar()
}

dependencies {
    api(project(":core-api"))
    api("org.slf4j:slf4j-api:2.0.9")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "orm-core-runtime"
        }
    }
}

