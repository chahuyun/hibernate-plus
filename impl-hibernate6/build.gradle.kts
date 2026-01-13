plugins {
    kotlin("jvm")
    id("java-library")
    id("org.jetbrains.dokka")
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

    api("com.zaxxer:HikariCP:5.1.0")
    api("org.slf4j:slf4j-api:2.0.9")
    api("org.reflections:reflections:0.10.2")

    // hibernate orm 基本
    api("org.hibernate.orm:hibernate-core:6.5.2.Final")
    api("org.hibernate.orm:hibernate-hikaricp:6.5.2.Final")
    api("org.hibernate.orm:hibernate-community-dialects:6.5.2.Final")

    // 这几个 JDBC 驱动建议未来拆分为可选 impl，这里先保持与旧版本一致
    api("org.xerial:sqlite-jdbc:3.45.3.0")
    api("org.hsqldb:hsqldb:2.7.2")
    api("com.mysql:mysql-connector-j:9.3.0")
    api("org.mariadb.jdbc:mariadb-java-client:3.3.3")
    api("org.duckdb:duckdb_jdbc:0.10.0")
    api("com.h2database:h2:2.2.224")

    testImplementation("ch.qos.logback:logback-classic:1.5.13")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
}

val dokkaJavadocJar by tasks.registering(Jar::class) {
    from(tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(dokkaJavadocJar)
            artifactId = "hibernate-plus-impl-hibernate6"
        }
    }
}

