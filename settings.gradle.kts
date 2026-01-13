@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        maven("https://nexus.chahuyun.cn/repository/maven-public/")
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven("https://nexus.chahuyun.cn/repository/maven-public/")
        mavenCentral()
    }
}

rootProject.name = "hibernate-plus"

include(
    ":core-api",
    ":core-runtime",
    ":impl-hibernate6",
)