@file:Suppress("SqlSourceToSinkFlow", "unused")

package cn.chahuyun.hibernateplus

import cn.chahuyun.hibernateplus.HibernateFactory.Companion.selectList
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.query.criteria.HibernateCriteriaBuilder
import org.hibernate.query.criteria.JpaCriteriaQuery
import org.hibernate.query.criteria.JpaRoot
import org.slf4j.LoggerFactory

/**
 * hibernate工厂
 *
 * @author Moyuyanli
 * @date 2024/7/18 10:42
 */
class HibernateFactory internal constructor(private val sessionFactory: SessionFactory) {

    companion object {
        private val log = LoggerFactory.getLogger(HibernateFactory::class.java)

        @Volatile
        private lateinit var factory: HibernateFactory

        /**
         * 获取 SessionFactory
         */
        @JvmStatic
        fun getSessionFactory(): SessionFactory {
            return factory.sessionFactory
        }

        /**
         * 查询一个单一对象
         *
         * @param tClass 对象类
         * @param key    主键
         * @param <T>    对象类Class
         * @return 对象 或 null
         */
        @JvmStatic
        fun <T : Any> selectOne(tClass: Class<T>, key: Any): T? {
            return factory.sessionFactory.fromSession { session: Session -> session.find(tClass, key) }
        }

        /**
         * Kotlin 友好的查询一个单一对象 (通过主键)
         */
        inline fun <reified T : Any> selectOne(key: Any): T? = selectOne(T::class.java, key)

        /**
         * 查询一个单一对象
         *
         * @param tClass 对象类
         * @param field  字段
         * @param value  值
         * @param <T>    对象类Class
         * @return 对象 或 null
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> selectOne(tClass: Class<T>, field: String, value: Any): T? {
            return factory.sessionFactory.fromSession { session: Session ->
                session.createQuery("from ${tClass.simpleName} where $field = :value", tClass)
                    .setParameter("value", value)
                    .setMaxResults(1)
                    .resultList
                    .firstOrNull()
            }
        }

        /**
         * Kotlin 友好的查询一个单一对象 (通过字段)
         */
        inline fun <reified T : Any> selectOne(field: String, value: Any): T? = selectOne(T::class.java, field, value)

        /**
         * 查询一个单一对象
         *
         * 如果查询结果为多个，只拿第一个
         * 如果想获取多个结果，请使用 [selectList]
         * 更多自定义查询请自行使用 [SessionFactory] 建立查询
         *
         * @param tClass 对象类
         * @param params 参数列表
         * @param <T>    对象类Class
         * @return 对象 或 null
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> selectOne(tClass: Class<T>, params: Map<String, Any>): T? {
            if (params.isEmpty()) {
                return null
            }
            return factory.sessionFactory.fromSession { session: Session ->
                val query = getQuery(tClass, params, session)
                session.createQuery(query)
                    .setMaxResults(1)
                    .resultList
                    .firstOrNull()
            }
        }

        /**
         * Kotlin 友好的查询一个单一对象 (通过参数 Map)
         */
        inline fun <reified T : Any> selectOne(params: Map<String, Any>): T? = selectOne(T::class.java, params)

        /**
         * 通过 HQL 查询单一对象
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> selectOneByHql(tClass: Class<T>, hql: String, params: Map<String, Any> = emptyMap()): T? {
            return factory.sessionFactory.fromSession { session ->
                val query = session.createQuery(hql, tClass)
                params.forEach { (k, v) -> query.setParameter(k, v) }
                query.setMaxResults(1)
                    .resultList
                    .firstOrNull()
            }
        }

        /**
         * Kotlin 友好的 HQL 查询单一对象
         */
        inline fun <reified T : Any> selectOneByHql(hql: String, params: Map<String, Any> = emptyMap()): T? =
            selectOneByHql(T::class.java, hql, params)

        /**
         * 通过 SQL 查询单一对象
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST", "USELESS_CAST")
        fun <T : Any> selectOneBySql(tClass: Class<T>, sql: String, params: Map<String, Any> = emptyMap()): T? {
            return factory.sessionFactory.fromSession { session ->
                val query = session.createNativeQuery(sql, tClass)
                params.forEach { (k, v) -> query.setParameter(k, v) }
                query.setMaxResults(1)
                    .resultList
                    .firstOrNull() as? T?
            }
        }

        /**
         * Kotlin 友好的 SQL 查询单一对象
         */
        inline fun <reified T : Any> selectOneBySql(sql: String, params: Map<String, Any> = emptyMap()): T? =
            selectOneBySql(T::class.java, sql, params)

        /**
         * 查询集合
         *
         * @param tClass 对象类
         * @param params 参数列表
         * @param <T>    对象类Class
         * @return 结果集
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> selectList(tClass: Class<T>, params: Map<String, Any>): List<T> {
            if (params.isEmpty()) {
                return selectList(tClass)
            }
            return factory.sessionFactory.fromSession { session: Session ->
                val query = getQuery(tClass, params, session)
                session.createQuery(query).resultList as List<T>
            } ?: emptyList()
        }

        /**
         * Kotlin 友好的查询集合 (通过参数 Map)
         */
        inline fun <reified T : Any> selectList(params: Map<String, Any>): List<T> = selectList(T::class.java, params)

        /**
         * 查询集合
         *
         * @param tClass 对象类
         * @param <T>    对象类Class
         * @return 结果集
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> selectList(tClass: Class<T>): List<T> {
            return factory.sessionFactory.fromSession { session: Session ->
                val query = getQuery(tClass, emptyMap(), session)
                session.createQuery(query).resultList as List<T>
            } ?: emptyList()
        }

        /**
         * Kotlin 友好的查询集合 (全部)
         */
        inline fun <reified T : Any> selectList(): List<T> = selectList(T::class.java)

        /**
         * 查询集合
         *
         * @param tClass 对象类
         * @param key    条件字段
         * @param param  条件值
         * @param <T>    对象类Class
         * @return 结果集
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> selectList(tClass: Class<T>, key: String?, param: Any?): List<T> {
            if (key == null || param == null) {
                return emptyList()
            }
            return factory.sessionFactory.fromSession { session: Session ->
                val builder: HibernateCriteriaBuilder = session.criteriaBuilder
                val query: JpaCriteriaQuery<T> = builder.createQuery(tClass)
                val from: JpaRoot<T> = query.from(tClass)
                query.select(from)
                query.where(builder.equal(from.get<Any>(key), param))
                session.createQuery(query).resultList as List<T>
            } ?: emptyList()
        }

        /**
         * Kotlin 友好的查询集合 (通过字段)
         */
        inline fun <reified T : Any> selectList(key: String?, param: Any?): List<T> =
            selectList(T::class.java, key, param)

        /**
         * 通过 HQL 查询集合
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> selectListByHql(tClass: Class<T>, hql: String, params: Map<String, Any> = emptyMap()): List<T> {
            return factory.sessionFactory.fromSession { session ->
                val query = session.createQuery(hql, tClass)
                params.forEach { (k, v) -> query.setParameter(k, v) }
                query.resultList as List<T>
            } ?: emptyList()
        }

        /**
         * Kotlin 友好的 HQL 查询集合
         */
        inline fun <reified T : Any> selectListByHql(hql: String, params: Map<String, Any> = emptyMap()): List<T> =
            selectListByHql(T::class.java, hql, params)

        /**
         * 通过 SQL 查询集合
         */
        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> selectListBySql(tClass: Class<T>, sql: String, params: Map<String, Any> = emptyMap()): List<T> {
            return factory.sessionFactory.fromSession { session ->
                val query = session.createNativeQuery(sql, tClass)
                params.forEach { (k, v) -> query.setParameter(k, v) }
                query.resultList as List<T>
            } ?: emptyList()
        }

        /**
         * Kotlin 友好的 SQL 查询集合
         */
        inline fun <reified T : Any> selectListBySql(sql: String, params: Map<String, Any> = emptyMap()): List<T> =
            selectListBySql(T::class.java, sql, params)

        /**
         * 保存或更新
         * 如果主键为0或null，则新增
         *
         * @param object 对象
         * @param <T>    对象class
         * @return 新对象
         */
        @JvmStatic
        fun <T : Any> merge(`object`: T): T {
            return factory.sessionFactory.fromTransaction { session: Session -> session.merge(`object`) }
        }

        /**
         * 删除一个对象
         *
         * @param object 对象
         * @return true 删除成功
         */
        @JvmStatic
        fun delete(`object`: Any?): Boolean {
            if (`object` == null) {
                return false
            }
            return factory.sessionFactory.fromTransaction { session: Session ->
                try {
                    session.remove(`object`)
                    true
                } catch (e: Exception) {
                    log.debug(e.message)
                    false
                }
            } ?: false
        }

        private fun <T : Any> getQuery(
            tClass: Class<T>,
            params: Map<String, Any>,
            session: Session
        ): JpaCriteriaQuery<T> {
            val builder: HibernateCriteriaBuilder = session.criteriaBuilder
            val query: JpaCriteriaQuery<T> = builder.createQuery(tClass)
            val from: JpaRoot<T> = query.from(tClass)
            query.select(from)
            if (params.isNotEmpty()) {
                val predicates = params.map { (key, value) ->
                    builder.equal(from.get<Any>(key), value)
                }.toTypedArray()
                query.where(*predicates)
            }
            return query
        }

        // Internal method for setting factory from the service
        internal fun initFactory(factory: HibernateFactory) {
            this.factory = factory
        }
    }
}
