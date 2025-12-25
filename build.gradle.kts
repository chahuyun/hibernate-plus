plugins {
    id("org.jetbrains.kotlin.jvm") version "1.9.20"
    id("java-library")
    id("org.jetbrains.dokka") version "1.9.10"
    id("maven-publish")
    id("signing")
}

group = "cn.chahuyun"
version = "2.1.0"

dependencies {
    api("com.zaxxer:HikariCP:5.1.0")
    api("org.slf4j:slf4j-api:2.0.9")
    api("org.xerial:sqlite-jdbc:3.45.3.0")
    api("org.hsqldb:hsqldb:2.7.2")
    api("com.mysql:mysql-connector-j:9.3.0")
    api("org.mariadb.jdbc:mariadb-java-client:3.3.3")
    api("org.duckdb:duckdb_jdbc:0.10.0")
    api("com.h2database:h2:2.2.224")

    api("org.reflections:reflections:0.10.2")

    // hibernate orm基本
    api("org.hibernate.orm:hibernate-core:6.5.2.Final")
    api("org.hibernate.orm:hibernate-hikaricp:6.5.2.Final")
    api("org.hibernate.orm:hibernate-community-dialects:6.5.2.Final")

    // logback 日志基本
    testImplementation("ch.qos.logback:logback-classic:1.5.13")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

kotlin {
    jvmToolchain(11)
}

java {
    withSourcesJar()
    // 由于是 Kotlin 项目，我们之后手动关联 Dokka 生成的 javadoc
}

// Dokka 配置
tasks.dokkaHtml {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))
}

// 注册 Dokka Javadoc Jar 任务
val dokkaJavadocJar by tasks.registering(Jar::class) {
    from(tasks.dokkaHtml)
    archiveClassifier.set("javadoc")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            // 替换默认的空 javadoc 为 Dokka 生成的内容
            artifact(dokkaJavadocJar)

            pom {
                name.set("hibernate-plus")
                description.set("一个Hibernate 工具库，提供便捷的连接方式和简单的基本查询。")
                url.set("https://github.com/chahuyun/hibernate-plus")
                licenses {
                    license {
                        name.set("Apache-2.0 License")
                        url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("moyuyanli")
                        name.set("Moyu yanli")
                        url.set("https://github.com/Moyuyanli")
                    }
                }
                scm {
                    connection.set("scm:git:git://github.com/chahuyun/hibernate-plus.git")
                    developerConnection.set("scm:git:ssh://git@github.com/chahuyun/hibernate-plus.git")
                    url.set("https://github.com/chahuyun/hibernate-plus")
                }
            }
        }
    }

    repositories {
        maven {
            name = "localRepo"
            url = uri(layout.buildDirectory.dir("repo"))
        }
    }
}

// 签名配置
signing {
    // 如果你想通过命令行手动签名，实际上可以不配置这个插件
    // 但如果你想在执行 publish 时自动调用本地 gpg 命令，可以如下配置：
    useGpgCmd()
    sign(publishing.publications["mavenJava"])
}

// 解决编译时的编码问题
tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Javadoc> {
    options.encoding = "UTF-8"
}
