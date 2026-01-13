package cn.chahuyun.hibernateplus.impl

import cn.chahuyun.hibernateplus.api.OrmConfig
import cn.chahuyun.hibernateplus.api.OrmProvider
import cn.chahuyun.hibernateplus.api.OrmRuntimeException
import cn.chahuyun.hibernateplus.api.OrmService
import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.MappedSuperclass
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder
import org.hibernate.boot.registry.StandardServiceRegistry
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import java.util.*

/**
 * Hibernate6 实现（可被 core-runtime 通过 ServiceLoader 发现）。
 */
class Hibernate6OrmProvider : OrmProvider {
    override val id: String = "hibernate6"

    override fun create(config: OrmConfig): OrmService {
        val cl = config.appClassLoader
        val oldTccL = Thread.currentThread().contextClassLoader

        try {
            Thread.currentThread().contextClassLoader = cl

            val properties = Properties().apply {
                config.settings.forEach { (k, v) -> setProperty(k, v) }
                if (getProperty("hibernate.connection.provider_class") == null) {
                    setProperty(
                        "hibernate.connection.provider_class",
                        "org.hibernate.hikaricp.internal.HikariCPConnectionProvider"
                    )
                }
            }

            val bootstrapRegistry = BootstrapServiceRegistryBuilder()
                .applyClassLoader(cl)
                .build()

            val serviceRegistry = StandardServiceRegistryBuilder(bootstrapRegistry)
                .applySettings(properties)
                .build()

            val sessionFactory = buildSessionFactory(serviceRegistry, config, cl)

            return Hibernate6OrmService(sessionFactory, serviceRegistry)
        } catch (e: Exception) {
            if (e is OrmRuntimeException) throw e
            throw OrmRuntimeException("failed to create Hibernate6 OrmService", e)
        } finally {
            Thread.currentThread().contextClassLoader = oldTccL
        }
    }

    private fun buildSessionFactory(
        serviceRegistry: StandardServiceRegistry,
        config: OrmConfig,
        classLoader: ClassLoader,
    ) = MetadataSources(serviceRegistry).run {
        val entityPackage = config.entityPackage
            ?: throw OrmRuntimeException("OrmConfig.entityPackage is required for Hibernate6 provider")

        scanEntity(entityPackage, classLoader).forEach { addAnnotatedClass(it) }
        config.extraEntities.forEach { addAnnotatedClass(it) }

        val metadata = metadataBuilder.build()
        metadata.sessionFactoryBuilder.build()
    }

    private fun scanEntity(packageName: String, classLoader: ClassLoader): Set<Class<*>> {
        val reflections = Reflections(
            ConfigurationBuilder()
                .forPackage(packageName, classLoader)
                .addClassLoaders(classLoader)
        )

        val queryFunction = org.reflections.scanners.Scanners.TypesAnnotated
            .of(Entity::class.java, Embeddable::class.java, MappedSuperclass::class.java)
            .asClass<Any>(classLoader)

        return queryFunction.apply(reflections.store)
    }
}

