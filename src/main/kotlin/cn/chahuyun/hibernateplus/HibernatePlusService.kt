package cn.chahuyun.hibernateplus

import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.MappedSuperclass
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*

/**
 * Hibernate Plus 服务
 *
 * @author Moyuyanli
 * @date 2024/7/10 15:54
 */
object HibernatePlusService {

    private val log = LoggerFactory.getLogger(HibernatePlusService::class.java)

    /**
     * 创建自定义配置
     *
     * @param clazz 加载类
     * @return [Configuration] 自定义配置
     */
    @JvmStatic
    fun createConfiguration(clazz: Class<*>): Configuration {
        return Configuration(clazz)
    }

    /**
     * 从自定义配置文件加载 hibernate 服务
     *
     * @param configuration 自定义配置文件
     */
    @JvmStatic
    fun loadingService(configuration: Configuration) {
        val serviceRegistry = StandardServiceRegistryBuilder()
            .applySettings(configuration.toProperties())
            .build()

        val sources = MetadataSources(serviceRegistry)
        extracted(configuration, sources)
        log.info("Hibernate 加载成功!")
    }

    /**
     * 从 hibernate.properties 文件加载 hibernate 服务
     *
     * @param clazz 启动类
     */
    @JvmStatic
    @Throws(IOException::class)
    fun loadingService(clazz: Class<*>) {
        val configuration = Configuration(clazz)

        val properties = Properties()
        val classLoader = configuration.classLoader
        classLoader?.getResourceAsStream("/hibernate.properties")?.use {
            properties.load(it)
        }

        val serviceRegistry = StandardServiceRegistryBuilder()
            .applySettings(properties)
            .build()

        val sources = MetadataSources(serviceRegistry)

        configuration.classLoader = classLoader
        extracted(configuration, sources)
        log.info("Hibernate 加载成功!")
    }

    private fun extracted(configuration: Configuration, sources: MetadataSources) {
        Thread.currentThread().contextClassLoader = configuration.classLoader

        val classes = scanEntity(configuration)

        for (aClass in classes) {
            sources.addAnnotatedClass(aClass)
        }

        if (configuration.extraEntity.isNotEmpty()) {
            configuration.extraEntity.forEach { sources.addAnnotatedClass(it) }
        }

        val metadata = sources.metadataBuilder.build()
        val sessionFactory = metadata.sessionFactoryBuilder.build()
        val factory = HibernateFactory(sessionFactory)
        HibernateFactory.initFactory(factory)
    }

    private fun scanEntity(configuration: Configuration): Set<Class<*>> {
        val classLoader = configuration.classLoader ?: throw RuntimeException("classloader is null !")
        val packageName = configuration.resolvePackageName()

        // 创建ConfigurationBuilder并设置自定义ClassLoader
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

