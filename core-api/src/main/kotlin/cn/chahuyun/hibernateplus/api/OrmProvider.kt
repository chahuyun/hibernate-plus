package cn.chahuyun.hibernateplus.api

/**
 * 可插拔实现的提供者（通过 ServiceLoader 发现）。
 *
 * 业务侧只依赖 core-api/core-runtime：
 * - core-runtime 负责下载/验签/加载 provider jar
 * - provider jar 内部依赖 Hibernate6/Hikari/JDBC 等
 */
interface OrmProvider {
    /**
     * provider 的唯一标识（例如 "hibernate6"）。
     */
    val id: String

    /**
     * 创建并初始化 ORM 服务实例。
     */
    fun create(config: OrmConfig): OrmService
}

