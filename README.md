# hibernate-plus
hibernate enhanced integration

hibernate 的一个强化集成模块，致力于以最小的配置连接你的数据库，提供最舒适的数据持久化体验。

本项目深受 [mirai-hibernate-plugin](https://github.com/cssxsh/mirai-hibernate-plugin) 启发。

### 特性
* **极简配置**: 无需繁琐的 XML，几行代码即可完成初始化。
* **多数据库支持**: 内置支持 H2, SQLite, MySQL, MariaDB, 以及高性能的 DuckDB。
* **Kotlin 友好**: 全面使用 Kotlin 重构，支持扩展函数和 Reified 类型，同时保持对 Java 的完美兼容。
* **无 Lombok**: 移除外部依赖，代码更干净，兼容性更强。

## 🚀 v2.0.0 重大更新说明

本项目现已正式进入 **2.0.0** 时代！这是一个具有里程碑意义的版本，我们对底层架构进行了全面重构，带来了更强大的功能和更好的开发体验。

### 1. 全面拥抱 Kotlin
*   **代码重构**: 核心库已使用 Kotlin 1.9.20 全面重构，充分利用了 Kotlin 的安全性、简洁性和现代语言特性。
*   **Kotlin 友好 API**: 为 Kotlin 开发者提供了带有 `reified` 类型参数的内联扩展函数，支持 `selectOne<MyUser>(id)` 等极其简洁的调用方式。
*   **Java 完美兼容**: 保留了 `@JvmStatic` 和 `@JvmOverloads`，确保 Java 开发者可以无缝升级。

### 2. 移除 Lombok 依赖
*   为了使库更加纯净并减少潜在的编译冲突，我们移除了 Lombok 依赖。所有数据模型现在使用 Kotlin 原生属性管理。

### 3. 增强的数据库生态
*   **新增 HSQLDB 支持**: 集成纯 Java 编写的 HSQLDB，为嵌入式场景提供更强大的多线程并发支持。
*   **新增 DuckDB 支持**: 针对分析型任务（OLAP）提供嵌入式高性能支持。
*   **SQLite 性能飞跃**: 默认开启 **WAL (Write-Ahead Logging)** 模式，大幅提升多线程环境下的并发读写性能。
*   **方言修复**: 修复了 H2 和 SQLite 方言配置混淆的长期 Bug。

### 4. 现代化的构建与发布
*   **Gradle Kotlin DSL**: 构建脚本全面迁移至 KTS。
*   **新版 Maven 仓库支持**: 配置了对新版 Maven Central Portal 的自动发布支持，并集成了 Dokka 文档生成。
*   **API 扩展**: `HibernateFactory` 新增了 HQL 和原生 SQL 的支持函数。

---

### 安装

#### Gradle (Kotlin DSL)
```kotlin
repositories {
    maven("https://nexus.chahuyun.cn/repository/maven-public/")
}

dependencies {
    implementation("cn.chahuyun:hibernate-plus:2.0.0")
}
```

#### Maven
```xml
<dependency>
  <groupId>cn.chahuyun</groupId>
  <artifactId>hibernate-plus</artifactId>
  <version>2.0.0</version>
</dependency>
```

### 快速开始

#### 1. 初始化配置
```kotlin
// Kotlin 示例
val configuration = HibernatePlusService.createConfiguration(Test::class.java).apply {
    driveType = DriveType.SQLITE
    address = "my_database.db"
    // 可选：指定扫描包名，默认根据启动类自动推断
    // packageName = "com.example.entity"
}

HibernatePlusService.loadingService(configuration)
```

#### 2. 定义实体
```kotlin
@Entity
@Table
class MyUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null
    var name: String? = null
}
```

#### 3. 使用 API
```kotlin
// 查询
val users = HibernateFactory.selectList<MyUser>()

// 保存
val user = MyUser().apply { name = "Moyu" }
val saved = HibernateFactory.merge(user)

// 条件查询
val one = HibernateFactory.selectOne<MyUser>("name", "Moyu")
```

### 详细文档
更多 API 使用说明请参考：[API 文档](docs/api.md)

### 执照
Apache License 2.0
