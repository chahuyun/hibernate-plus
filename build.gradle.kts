import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
    id("java")

    `java-library`
    `maven-publish`
    id("com.vanniktech.maven.publish") version "0.31.0"
    id("signing")
}

group = "cn.chahuyun"
version = "1.0.17"


repositories {
    mavenCentral()
    maven { url = uri("https://plugins.gradle.org/m2/") }
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
    implementation("org.projectlombok:lombok:1.18.30")
    annotationProcessor("org.projectlombok:lombok:1.18.30")

}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        charSet("UTF-8") // 设置输出 HTML 字符集
        encoding("UTF-8") // 显式指定输入编码
        addStringOption("docencoding", "UTF-8") // 指定文档本身的编码

        // 可选：关闭严格检查，避免非法HTML标签报错
        addBooleanOption("Xdoclint:none", true)
    }

    // 如果你使用的是 Java 8+，可以加上以下选项：
    options.source = "17" // 改为你实际使用的 JDK 版本
}


mavenPublishing {
    coordinates(group.toString(), project.name, version.toString())

    //配置 POM 文件内容
    pom {
        name.set("hibernate-plus")
        description.set("一个Hibernate 工具库，用户提供便捷的连接方式和简单的基本查询。")
        inceptionYear.set("2024")
        url.set("https://github.com/chahuyun/hibernate-plus")

        //开源许可
        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }
        //开发者信息
        developers {
            developer {
                id.set("moyuyanli")
                name.set("Moyu yanli")
                url.set("https://github.com/Moyuyanli")
            }
        }
        //源码仓库信息
        scm {
            url.set("https://github.com/chahuyun/hibernate-plus")
            connection.set("scm:git:git://github.com/chahuyun/hibernate-plus.git")
            developerConnection.set("scm:git:ssh://git@github.com/chahuyun/hibernate-plus.git")
        }
    }

//    publishToMavenCentral(SonatypeHost.DEFAULT)
    // or when publishing to https://s01.oss.sonatype.org
//    publishToMavenCentral(SonatypeHost.S01)
    // or when publishing to https://central.sonatype.com/
    // 自动发布
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    //等价
//    publishToMavenCentral()

    signAllPublications()
}