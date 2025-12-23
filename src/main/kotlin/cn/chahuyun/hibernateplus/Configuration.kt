package cn.chahuyun.hibernateplus

import java.util.*

/**
 * 数据库配置
 *
 * @author Moyuyanli
 * @date 2024/7/18 14:07
 */
class Configuration @JvmOverloads constructor(
    /**
     * 实现本类
     */
    var baseClass: Class<*>? = null
) {

    /**
     * 驱动类型
     */
    var driveType: DriveType? = null

    /**
     * 地址
     */
    var address: String? = null

    /**
     * 用户名
     */
    var user: String? = null

    /**
     * 密码
     */
    var password: String? = null

    /**
     * 包含实体的包名
     */
    var packageName: String? = null

    /**
     * 额外实体类
     */
    val extraEntity: MutableList<Class<Any>> = mutableListOf()

    /**
     * 类加载器
     */
    var classLoader: ClassLoader? = baseClass?.classLoader

    /**
     * 数据库自动重连
     */
    var isAutoReconnect: Boolean = false

    init {
        baseClass?.let {
            if (this.classLoader == null) {
                this.classLoader = it.classLoader
            }
        }
    }

    /**
     * 转换为 properties
     *
     * @return Properties
     */
    @Throws(RuntimeException::class)
    internal fun toProperties(): Properties {
        val currentDriveType = driveType ?: throw RuntimeException("数据库配置:驱动类型为空!")
        val currentAddress = address ?: throw RuntimeException("数据库配置:数据库地址为空!")

        val properties = Properties()
        val url = if (isAutoReconnect) {
            "${currentDriveType.urlPrefix}$currentAddress?autoReconnect=true"
        } else {
            "${currentDriveType.urlPrefix}$currentAddress"
        }

        properties.setProperty("hibernate.connection.url", url)
        properties.setProperty("hibernate.connection.driver_class", currentDriveType.driverClass)
        properties.setProperty(
            "hibernate.connection.provider_class",
            "org.hibernate.hikaricp.internal.HikariCPConnectionProvider"
        )
        properties.setProperty("hibernate.hbm2ddl.auto", "update")
        properties.setProperty("hibernate-connection-autocommit", "true")

        // Hibernate 6 具备优秀的方言自动检测能力，对于标准数据库无需手动指定
        when (currentDriveType) {
            DriveType.H2 -> {
                properties.setProperty("hibernate.connection.isolation", "1")
            }

            DriveType.SQLITE -> {
                // SQLite 属于社区驱动，建议显式指定
                properties.setProperty("hibernate.dialect", "org.hibernate.community.dialect.SQLiteDialect")
                properties.setProperty("hibernate.hikari.connectionTimeout", "180000")
                properties.setProperty("hibernate.connection.isolation", "1")
                user?.let { properties.setProperty("hibernate.connection.username", it) }
                password?.let { properties.setProperty("hibernate.connection.password", it) }
                properties.setProperty("hibernate.autoReconnect", "true")
                if (user == null) properties.setProperty("hibernate.connection.username", "")
                if (password == null) properties.setProperty("hibernate.connection.password", "")
                properties.setProperty("hibernate.current_session_context_class", "thread")

                // 开启 SQLite WAL 模式以支持更好的多线程并发
                properties.setProperty("hibernate.hikari.dataSource.journal_mode", "WAL")
                properties.setProperty("hibernate.hikari.dataSource.synchronous", "NORMAL")
            }

            DriveType.HSQLDB -> {
                properties.setProperty("hibernate.connection.username", user ?: "SA")
                properties.setProperty("hibernate.connection.password", password ?: "")
                properties.setProperty("hibernate.connection.isolation", "1")
            }

            DriveType.MYSQL -> {
                properties.setProperty("hibernate.connection.CharSet", "utf8mb4")
                properties.setProperty("hibernate.connection.useUnicode", "true")
                properties.setProperty("hibernate.connection.username", user ?: "")
                properties.setProperty("hibernate.connection.password", password ?: "")
                properties.setProperty("hibernate.connection.isolation", "1")
                properties.setProperty("hibernate.autoReconnect", "true")
            }

            DriveType.MARIADB -> {
                properties.setProperty("hibernate.connection.username", user ?: "")
                properties.setProperty("hibernate.connection.password", password ?: "")
                properties.setProperty("hibernate.connection.isolation", "1")
                properties.setProperty("hibernate.autoReconnect", "true")
            }

            DriveType.DUCKDB -> {
                // DuckDB 目前仍建议显式指定方言
                properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
                properties.setProperty("hibernate.connection.username", user ?: "")
                properties.setProperty("hibernate.connection.password", password ?: "")
            }
        }
        return properties
    }

    fun resolvePackageName(): String {
        packageName?.let { return it }

        val currentBaseClass = baseClass ?: throw RuntimeException("baseClass is null !")

        val pkgName = currentBaseClass.packageName
        this.packageName = pkgName
        val packagePath = pkgName.replace('.', '/')
        val keywords = arrayOf("entry", "entity", "entities", "model", "models", "bean", "beans", "dto")

        for (keyword in keywords) {
            val resource = classLoader?.getResource("$packagePath/$keyword")
            if (resource != null) {
                val resolved = "$pkgName.$keyword"
                this.packageName = resolved
                return resolved
            }
        }

        return pkgName
    }
}
