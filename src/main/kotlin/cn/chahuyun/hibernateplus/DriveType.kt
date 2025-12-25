package cn.chahuyun.hibernateplus

/**
 * 驱动类型
 *
 * @author Moyuyanli
 * @date 2024/7/18 14:12
 */
enum class DriveType {
    /**
     * H2类型数据库
     */
    H2,

    /**
     * sqlite类型数据库
     */
    SQLITE,

    /**
     * HSQLDB 类型数据库 (Java 界的 SQLite，更强的并发支持)
     */
    HSQLDB,

    /**
     * mysql类型数据库
     */
    MYSQL,

    /**
     * MariaDB类型数据库
     */
    MARIADB,

    /**
     * DuckDB类型数据库
     */
    DUCKDB
}

/**
 * 获取数据库驱动类型的JDBC URL前缀
 *
 * @return 对应数据库类型的JDBC URL前缀字符串
 */
internal val DriveType.urlPrefix: String
    get() = when (this) {
        DriveType.H2 -> "jdbc:h2:file:"
        DriveType.SQLITE -> "jdbc:sqlite:"
        DriveType.HSQLDB -> "jdbc:hsqldb:file:"
        DriveType.MYSQL -> "jdbc:mysql://"
        DriveType.MARIADB -> "jdbc:mariadb://"
        DriveType.DUCKDB -> "jdbc:duckdb:"
    }

/**
 * 获取数据库驱动类型的驱动程序类名
 *
 * @return 对应数据库类型的JDBC驱动程序完全限定类名
 */
internal val DriveType.driverClass: String
    get() = when (this) {
        DriveType.H2 -> "org.h2.Driver"
        DriveType.SQLITE -> "org.sqlite.JDBC"
        DriveType.HSQLDB -> "org.hsqldb.jdbc.JDBCDriver"
        DriveType.MYSQL -> "com.mysql.cj.jdbc.Driver"
        DriveType.MARIADB -> "org.mariadb.jdbc.Driver"
        DriveType.DUCKDB -> "org.duckdb.DuckDBDriver"
    }
