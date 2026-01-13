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
        // appClassLoader：业务侧/Mod 的 ClassLoader（实体类在这里）
        val appCl = config.appClassLoader

        // providerCl：本 provider 自己的 ClassLoader（Hibernate/Hikari 等第三方依赖在这里）
        // 在“按需下载 + child-first”模式下，这是隔离的关键。
        val providerCl = this::class.java.classLoader

        val oldTccL = Thread.currentThread().contextClassLoader

        try {
            // 关键点：
            // - Hibernate 内部的 ServiceLoader/SPI/资源查找往往依赖 TCCL
            // - 所以这里应当把 TCCL 指向 providerCl，确保能从隔离 jar 中正确发现服务实现
            Thread.currentThread().contextClassLoader = providerCl

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
                // 强制 Hibernate 在 providerCl 范围内查找 SPI 实现，避免串台到别的 Mod 依赖
                .applyClassLoader(providerCl)
                .build()

            val serviceRegistry = StandardServiceRegistryBuilder(bootstrapRegistry)
                .applySettings(properties)
                .build()

            // 实体扫描必须使用 appCl：实体类在业务侧 ClassLoader
            // providerCl 的 parent 通常就是 appCl，因此 providerCl 也“看得见”实体类，
            // 但扫描范围/资源定位应以 appCl 为准（更符合 Mod 环境）。
            val sessionFactory = buildSessionFactory(serviceRegistry, config, appCl)

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

