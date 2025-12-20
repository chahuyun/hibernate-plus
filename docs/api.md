# Hibernate Plus API 文档

`Hibernate Plus` 提供了一系列简化的 API，用于快速进行数据库操作。

## HibernateFactory

`HibernateFactory` 是核心操作类，提供了常用的 CRUD 和查询方法。

### 1. 查询方法

#### `selectOne(tClass: Class<T>, key: Any): T?`
通过主键查询单一对象。
- **Java**: `HibernateFactory.selectOne(MyUser.class, 1)`
- **Kotlin**: `HibernateFactory.selectOne<MyUser>(1)`

#### `selectOne(tClass: Class<T>, field: String, value: Any): T?`
通过指定字段和值查询单一对象。
- **Java**: `HibernateFactory.selectOne(MyUser.class, "name", "moyuyanli")`
- **Kotlin**: `HibernateFactory.selectOne<MyUser>("name", "moyuyanli")`

#### `selectOneByHql(tClass: Class<T>, hql: String, params: Map<String, Any>): T?`
通过 HQL 查询单一对象。
- **Java**:
  ```java
  Map<String, Object> params = new HashMap<>();
  params.put("name", "moyuyanli");
  MyUser user = HibernateFactory.selectOneByHql(MyUser.class, "from MyUser where name = :name", params);
  ```
- **Kotlin**:
  ```kotlin
  val user = HibernateFactory.selectOneByHql<MyUser>("from MyUser where name = :name", mapOf("name" to "moyuyanli"))
  ```

#### `selectList(tClass: Class<T>): List<T>`
查询所有对象。
- **Kotlin**: `HibernateFactory.selectList<MyUser>()`

#### `selectListByHql(tClass: Class<T>, hql: String, params: Map<String, Any>): List<T>`
通过 HQL 查询对象集合。

### 2. 更新/删除方法

#### `merge(object: T): T?`
保存或更新对象。如果主键为 0 或 null 则执行插入。
- **Java**: `MyUser savedUser = HibernateFactory.merge(user);`

#### `delete(object: Any): Boolean`
删除对象。
- **Java**: `boolean success = HibernateFactory.delete(user);`

## HibernatePlusService

用于初始化服务。

#### `createConfiguration(clazz: Class<*>): Configuration`
创建配置对象。传入启动类以便自动扫描实体。

#### `loadingService(configuration: Configuration)`
根据配置加载服务。

## Configuration

配置类，支持以下驱动：

| 驱动类型 | 数据库 | 优点 | 缺点 | 场景建议 |
| :--- | :--- | :--- | :--- | :--- |
| `DriveType.H2` | H2 (嵌入式) | 极其轻量, 启动快, 支持内存模式 | 业界支持性一般, 某些 SQL 语法不标准 | 快速单元测试, 极其简单的 demo |
| `DriveType.SQLITE` | SQLite (嵌入式) | **默认推荐**。单文件存储, 无需部署, 极高兼容性 | 不支持高并发写入, 多线程处理较弱 | 小型桌面应用, 工具类插件 (如 Mirai 插件) |
| `DriveType.MYSQL` | MySQL | 工业级标准, 功能完善, 性能强劲, 支持高并发 | 需要单独安装部署, 体量大 | Web 后端项目, 需要持久化大量数据的场景 |
| `DriveType.MARIADB` | MariaDB | MySQL 的开源分支, 性能优化更好, 兼容性强 | 与 MySQL 类似, 需要环境部署 | 替代 MySQL 的高性能选择 |
| `DriveType.DUCKDB` | DuckDB (嵌入式) | **分析型首选**。针对 OLAP 优化, 处理大数据量聚合极快 | 生态相对较新, 不适合频繁的事务操作 | 本地大数据分析, 报表统计 |

### 配置示例
```kotlin
val configuration = HibernatePlusService.createConfiguration(Main::class.java).apply {
    driveType = DriveType.SQLITE
    address = "data.db"
    isAutoReconnect = true
}
HibernatePlusService.loadingService(configuration)
```

