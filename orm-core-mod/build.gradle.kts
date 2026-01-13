plugins {
    kotlin("jvm")
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
    api(project(":core-runtime"))
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifactId = "orm-core-mod"
        }
    }
}

