package cn.chahuyun.hibernateplus.api

/**
 * 业务侧可用的 ORM 入口。
 *
 * 设计目标：
 * - API 小而稳定（长期兼容）
 * - 不泄露 Hibernate 等实现类型（避免 Forge 环境依赖冲突）
 * - 必须可关闭（释放连接池/线程/文件锁）
 */
interface OrmService : AutoCloseable {

    fun <T : Any> selectOneById(type: Class<T>, id: Any): T?

    fun <T : Any> selectOne(type: Class<T>, field: String, value: Any): T?

    fun <T : Any> selectList(type: Class<T>): List<T>

    fun <T : Any> selectList(type: Class<T>, params: Map<String, Any?>): List<T>

    fun <T : Any> merge(entity: T): T

    fun delete(entity: Any?): Boolean

    fun <T : Any> selectOneByHql(type: Class<T>, hql: String, params: Map<String, Any?> = emptyMap()): T?

    fun <T : Any> selectListByHql(type: Class<T>, hql: String, params: Map<String, Any?> = emptyMap()): List<T>

    fun <T : Any> selectOneBySql(type: Class<T>, sql: String, params: Map<String, Any?> = emptyMap()): T?

    fun <T : Any> selectListBySql(type: Class<T>, sql: String, params: Map<String, Any?> = emptyMap()): List<T>

    override fun close()
}

