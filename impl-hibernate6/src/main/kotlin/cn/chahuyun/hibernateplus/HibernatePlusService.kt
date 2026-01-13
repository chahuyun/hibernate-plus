@file:Suppress("unused", "SpellCheckingInspection")

package cn.chahuyun.hibernateplus

import jakarta.persistence.Embeddable
import jakarta.persistence.Entity
import jakarta.persistence.MappedSuperclass
import org.hibernate.boot.MetadataSources
import org.hibernate.boot.registry.BootstrapServiceRegistryBuilder
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.reflections.Reflections
import org.reflections.util.ConfigurationBuilder
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.*

/**
 * Hibernate Plus 服务启动类
 *
 * 核心优化：
 * 1. 显式隔离类加载器：通过 BootstrapServiceRegistry 强制 Hibernate 使用插件自身的 ClassLoader。
 * 2. 线程上下文保护：使用 try-finally 确保 TCCL 在初始化后能正确恢复，避免污染 Mirai 主线程。
 * 3. 统一初始化逻辑：通过内部方法收口，降低维护成本。
 *
 * @author Moyuyanli
 * @date 2024/7/10 15:54
 */
object HibernatePlusService {

    private val log = LoggerFactory.getLogger(HibernatePlusService::class.java)

    /**
     * 创建自定义配置对象
     *
     * @param clazz 加载类（通常是插件的主类）
     * @return [Configuration] 自定义配置实例
     */
    @JvmStatic
    fun createConfiguration(clazz: Class<*>): Configuration {
        return Configuration(clazz)
    }

    /**
     * 方式 A：从自定义 Configuration 对象加载服务
     *
     * @param configuration 自定义配置文件
     */
    @JvmStatic
    fun loadingService(configuration: Configuration) {
        internalLoading(configuration, configuration.toProperties())
    }

    /**
     * 方式 B：自动从类路径下的 `hibernate.properties` 文件加载服务
     *
     * @param clazz 启动类（用于获取 ClassLoader 和资源路径）
     */
    @JvmStatic
    @Throws(IOException::class)
    fun loadingService(clazz: Class<*>) {
        val configuration = Configuration(clazz)
        val classLoader = configuration.classLoader ?: clazz.classLoader

        val properties = Properties()
        classLoader.getResourceAsStream("/hibernate.properties")?.use {
            properties.load(it)
        } ?: throw IOException("在类路径下未找到 hibernate.properties 配置文件")

        internalLoading(configuration, properties)
    }

    /**
     * 内部核心加载逻辑
     *
     * 这里是解决 ClassLoader 冲突的关键所在。
     */
    private fun internalLoading(configuration: Configuration, properties: Properties) {
        // 确定要使用的 ClassLoader
        val classLoader = configuration.classLoader ?: Thread.currentThread().contextClassLoader
        // 备份当前的 TCCL
        val oldTCCL = Thread.currentThread().contextClassLoader

        try {
            // 1. 【关键】在一切开始前，切换线程上下文加载器
            Thread.currentThread().contextClassLoader = classLoader

            // 2. 【关键】显式构建引导注册表，防止 Hibernate 跨插件扫描 SPI 实现
            val bootstrapRegistry = BootstrapServiceRegistryBuilder()
                .applyClassLoader(classLoader) // 强制指定只在当前插件加载器中查找 Service
                .build()

            // 3. 基于引导注册表构建标准服务注册表
            val serviceRegistry = StandardServiceRegistryBuilder(bootstrapRegistry)
                .applySettings(properties)
                .build()

            val sources = MetadataSources(serviceRegistry)

            // 4. 执行实体扫描和 Factory 初始化
            configuration.classLoader = classLoader
            extracted(configuration, sources)

            log.info("Hibernate-plus 加载成功!")
        } catch (e: Exception) {
            log.error("Hibernate-plus 加载失败: ${e.message}", e)
            throw e
        } finally {
            // 5. 【关键】务必恢复 TCCL，否则可能导致 Mirai 控制台或其他插件出现类加载异常
            Thread.currentThread().contextClassLoader = oldTCCL
        }
    }

    /**
     * 提取出的公共逻辑：扫描实体并初始化 HibernateFactory
     */
    private fun extracted(configuration: Configuration, sources: MetadataSources) {
        // 扫描带有指定注解的类
        val classes = scanEntity(configuration)

        for (aClass in classes) {
            sources.addAnnotatedClass(aClass)
        }

        // 添加手动额外指定的实体类
        if (configuration.extraEntity.isNotEmpty()) {
            configuration.extraEntity.forEach { sources.addAnnotatedClass(it) }
        }

        // 构建元数据及 SessionFactory
        val metadata = sources.metadataBuilder.build()
        val sessionFactory = metadata.sessionFactoryBuilder.build()

        // 初始化单例工厂
        val factory = HibernateFactory(sessionFactory)
        val classLoader = configuration.classLoader ?: Thread.currentThread().contextClassLoader
        HibernateFactory.initFactory(classLoader, factory)
    }

    /**
     * 扫描包下的 Entity
     */
    private fun scanEntity(configuration: Configuration): Set<Class<*>> {
        val classLoader =
            configuration.classLoader ?: throw RuntimeException("无法获取有效的 ClassLoader 进行实体扫描!")
        val packageName = configuration.resolvePackageName()

        // 使用 Reflections 库进行扫描，并强制指定 ClassLoader 范围
        val reflections = Reflections(
            ConfigurationBuilder()
                .forPackage(packageName, classLoader)
                .addClassLoaders(classLoader)
        )

        // 定义扫描规则：Entity, Embeddable, MappedSuperclass
        val queryFunction = org.reflections.scanners.Scanners.TypesAnnotated
            .of(Entity::class.java, Embeddable::class.java, MappedSuperclass::class.java)
            .asClass<Any>(classLoader)

        return queryFunction.apply(reflections.store)
    }
}

