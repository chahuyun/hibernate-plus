package cn.chahuyun.hibernateplus.impl

import cn.chahuyun.hibernateplus.api.OrmService
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.boot.registry.StandardServiceRegistry
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.intellij.lang.annotations.Language

internal class Hibernate6OrmService(
    private val sessionFactory: SessionFactory,
    private val serviceRegistry: StandardServiceRegistry,
) : OrmService {

    override fun <T : Any> selectOneById(type: Class<T>, id: Any): T? {
        return sessionFactory.fromSession { session: Session -> session.find(type, id) }
    }

    override fun <T : Any> selectOne(type: Class<T>, field: String, value: Any): T? {
        return sessionFactory.fromSession { session ->
            val builder = session.criteriaBuilder
            val query = builder.createQuery(type)
            val from = query.from(type)
            query.select(from).where(builder.equal(from.get<Any>(field), value))
            session.createQuery(query).setMaxResults(1).resultList.firstOrNull()
        }
    }

    override fun <T : Any> selectList(type: Class<T>): List<T> {
        return sessionFactory.fromSession { session ->
            val builder = session.criteriaBuilder
            val query = builder.createQuery(type)
            val from = query.from(type)
            query.select(from)
            session.createQuery(query).resultList
        } ?: emptyList()
    }

    override fun <T : Any> selectList(type: Class<T>, params: Map<String, Any?>): List<T> {
        if (params.isEmpty()) return selectList(type)
        return sessionFactory.fromSession { session ->
            val builder = session.criteriaBuilder
            val query = builder.createQuery(type)
            val from = query.from(type)
            query.select(from)

            val predicates = params.map { (k, v) ->
                builder.equal(from.get<Any>(k), v)
            }.toTypedArray()
            query.where(*predicates)

            session.createQuery(query).resultList
        } ?: emptyList()
    }

    override fun <T : Any> merge(entity: T): T {
        return sessionFactory.fromTransaction { session: Session -> session.merge(entity) }
    }

    override fun delete(entity: Any?): Boolean {
        if (entity == null) return false
        return sessionFactory.fromTransaction { session ->
            try {
                session.remove(entity)
                true
            } catch (_: Exception) {
                false
            }
        } ?: false
    }

    override fun <T : Any> selectOneByHql(type: Class<T>, @Language("HQL") hql: String, params: Map<String, Any?>): T? {
        require(hql.isNotBlank()) { "HQL cannot be blank" }
        return sessionFactory.fromSession { session ->
            val query = session.createQuery(hql, type)
            params.forEach { (k, v) -> query.setParameter(k, v) }
            query.setMaxResults(1).resultList.firstOrNull() as? T
        }
    }

    override fun <T : Any> selectListByHql(type: Class<T>, @Language("HQL") hql: String, params: Map<String, Any?>): List<T> {
        require(hql.isNotBlank()) { "HQL cannot be blank" }
        return sessionFactory.fromSession { session ->
            val query = session.createQuery(hql, type)
            params.forEach { (k, v) -> query.setParameter(k, v) }
            query.resultList as List<T>
        } ?: emptyList()
    }

    override fun <T : Any> selectOneBySql(type: Class<T>, @Language("SQL") sql: String, params: Map<String, Any?>): T? {
        require(sql.isNotBlank()) { "SQL cannot be blank" }
        return sessionFactory.fromSession { session ->
            val query = session.createNativeQuery(sql, type)
            params.forEach { (k, v) -> query.setParameter(k, v) }
            query.setMaxResults(1).resultList.firstOrNull() as? T
        }
    }

    override fun <T : Any> selectListBySql(type: Class<T>, @Language("SQL") sql: String, params: Map<String, Any?>): List<T> {
        require(sql.isNotBlank()) { "SQL cannot be blank" }
        return sessionFactory.fromSession { session ->
            val query = session.createNativeQuery(sql, type)
            params.forEach { (k, v) -> query.setParameter(k, v) }
            query.resultList as List<T>
        } ?: emptyList()
    }

    override fun close() {
        try {
            sessionFactory.close()
        } finally {
            StandardServiceRegistryBuilder.destroy(serviceRegistry)
        }
    }
}

