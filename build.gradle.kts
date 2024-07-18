plugins {
    id("org.jetbrains.kotlin.jvm") version "1.8.20"
    id("java")

    id ("me.him188.maven-central-publish") version "1.0.0"
}

group = "cn.chahuyun"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    api("com.zaxxer:HikariCP:5.1.0")
    api("org.slf4j:slf4j-api:2.0.9")
    api("org.xerial:sqlite-jdbc:3.45.3.0")
    api("com.mysql:mysql-connector-j:8.3.0")
    api("com.h2database:h2:2.2.224")
    api("org.hibernate.orm:hibernate-hikaricp:6.5.2.Final")

    //hibernate orm基本
    implementation("org.hibernate.orm:hibernate-core:6.5.2.Final")
    //logback 日志基本
    implementation("ch.qos.logback:logback-classic:1.5.6")


    //lombok
    implementation("org.projectlombok:lombok:1.18.8")
    annotationProcessor("org.projectlombok:lombok:1.18.8")

}



mavenCentralPublish{
    artifactId = "hibernate-plus"
    groupId = "cn.chahuyun.data"
    projectName = "hibernate enhanced integration"

    githubProject("chahuyun", "hibernate-plus")
    developer("moyuyanli")
    licenseApacheV2()

    // and can be more simplified as
    singleDevGithubProject("moyuyanli", "hibernate-plus")
    licenseApacheV2()
}