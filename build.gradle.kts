plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    id("java")

    id("me.him188.maven-central-publish") version "1.0.0-dev-3"
}

group = "cn.chahuyun"
version = "1.0.14"

repositories {
    mavenCentral()
}

dependencies {
    api("com.zaxxer:HikariCP:5.1.0")
    api("org.slf4j:slf4j-api:2.0.9")
    api("org.xerial:sqlite-jdbc:3.45.3.0")
    api("com.mysql:mysql-connector-j:8.3.0")
    api("com.h2database:h2:2.2.224")

    api("org.reflections:reflections:0.10.2")

    //jakarta persistence api
//    implementation ("jakarta.persistence:jakarta.persistence-api:3.2.0")

    //hibernate orm基本
    api("org.hibernate.orm:hibernate-core:6.5.2.Final")
    api("org.hibernate.orm:hibernate-hikaricp:6.5.2.Final")
    api("org.hibernate.orm:hibernate-community-dialects:6.5.2.Final")

    //logback 日志基本
    implementation("ch.qos.logback:logback-classic:1.5.6")

    //lombok
    implementation("org.projectlombok:lombok:1.18.8")
    annotationProcessor("org.projectlombok:lombok:1.18.8")

}



mavenCentralPublish {
    useCentralS01()

    licenseApacheV2()

    singleDevGithubProject("chahuyun", "hibernate-plus")
    developer("moyuyanli")

    // 设置 Publish 临时目录
    workingDir = System.getenv("PUBLICATION_TEMP")?.let { file(it).resolve(projectName) }
        ?: buildDir.resolve("publishing-tmp")

    // 设置额外上传内容
//    publication {
//        artifact(tasks["jar"])
//    }

}