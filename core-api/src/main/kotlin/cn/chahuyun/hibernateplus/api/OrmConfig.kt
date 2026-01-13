package cn.chahuyun.hibernateplus.api

/**
 * 通用 ORM 配置（尽量保持“小而稳定”）。
 *
 * 约束：
 * - **不要**在这里暴露 Hibernate / Hikari / JDBC 的具体类型，避免跨 ClassLoader 冲突。
 * - settings 用于承载具体实现所需的键值对（例如 hibernate.*）。
 */
data class OrmConfig(
    /**
     * 业务侧（例如 Forge Mod）使用的 ClassLoader。
     *
     * impl 侧需要能看到你的实体类（Entity），因此通常需要把它作为 parent 或上下文加载器使用。
     */
    val appClassLoader: ClassLoader,

    /**
     * 实体扫描的根包名（可选）。如果为 null，具体实现可自行推断或要求显式指定。
     */
    val entityPackage: String? = null,

    /**
     * 额外实体类（可选）。
     *
     * 注意：这些 Class 对象来自业务侧 ClassLoader，但 impl 侧可以通过 parent 访问到它们。
     */
    val extraEntities: List<Class<*>> = emptyList(),

    /**
     * 具体实现可用的键值对配置（例如 hibernate.connection.url 等）。
     */
    val settings: Map<String, String> = emptyMap(),
)

