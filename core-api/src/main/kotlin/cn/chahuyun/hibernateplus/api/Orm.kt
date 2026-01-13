package cn.chahuyun.hibernateplus.api

/**
 * Java / Kotlin 通用的静态工具入口（类似旧版 HibernateFactory 的静态调用体验）。
 *
 * 说明：
 * - 由于 [OrmService] 本身是接口，Java 侧无法像 Kotlin object 那样自然调用扩展函数。
 * - 因此提供一个 `object Orm`，并使用 `@JvmStatic` 让 Java 可以直接静态调用：
 *
 * ```java
 * Orm.selectList(ormService, MyUser.class);
 * Orm.merge(ormService, user);
 * ```
 *
 * 这些方法只是对 [OrmService] 的薄封装，不引入任何实现细节。
 */
object Orm {

    @JvmStatic
    fun <T : Any> selectOneById(service: OrmService, type: Class<T>, id: Any): T? =
        service.selectOneById(type, id)

    @JvmStatic
    fun <T : Any> selectOne(service: OrmService, type: Class<T>, field: String, value: Any): T? =
        service.selectOne(type, field, value)

    @JvmStatic
    fun <T : Any> selectList(service: OrmService, type: Class<T>): List<T> =
        service.selectList(type)

    @JvmStatic
    fun <T : Any> selectList(service: OrmService, type: Class<T>, params: Map<String, Any?>): List<T> =
        service.selectList(type, params)

    @JvmStatic
    fun <T : Any> merge(service: OrmService, entity: T): T =
        service.merge(entity)

    @JvmStatic
    fun delete(service: OrmService, entity: Any?): Boolean =
        service.delete(entity)

    @JvmStatic
    fun <T : Any> selectOneByHql(service: OrmService, type: Class<T>, hql: String, params: Map<String, Any?> = emptyMap()): T? =
        service.selectOneByHql(type, hql, params)

    @JvmStatic
    fun <T : Any> selectListByHql(service: OrmService, type: Class<T>, hql: String, params: Map<String, Any?> = emptyMap()): List<T> =
        service.selectListByHql(type, hql, params)

    @JvmStatic
    fun <T : Any> selectOneBySql(service: OrmService, type: Class<T>, sql: String, params: Map<String, Any?> = emptyMap()): T? =
        service.selectOneBySql(type, sql, params)

    @JvmStatic
    fun <T : Any> selectListBySql(service: OrmService, type: Class<T>, sql: String, params: Map<String, Any?> = emptyMap()): List<T> =
        service.selectListBySql(type, sql, params)

    @JvmStatic
    fun close(service: OrmService) = service.close()
}

