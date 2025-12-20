package cn.chahuyun.hibernateplus

/**
 * 驱动类型
 *
 * @author Moyuyanli
 * @date 2024/7/18 14:12
 */
enum class DriveType(val urlPrefix: String, val driverClass: String) {
    /**
     * H2类型数据库
     */
    H2("jdbc:h2:file:", "org.h2.Driver"),

    /**
     * sqlite类型数据库
     */
    SQLITE("jdbc:sqlite:", "org.sqlite.JDBC"),

    /**
     * HSQLDB 类型数据库 (Java 界的 SQLite，更强的并发支持)
     */
    HSQLDB("jdbc:hsqldb:file:", "org.hsqldb.jdbc.JDBCDriver"),

    /**
     * mysql类型数据库
     */
    MYSQL("jdbc:mysql://", "com.mysql.cj.jdbc.Driver"),

    /**
     * MariaDB类型数据库
     */
    MARIADB("jdbc:mariadb://", "org.mariadb.jdbc.Driver"),

    /**
     * DuckDB类型数据库
     */
    DUCKDB("jdbc:duckdb:", "org.duckdb.DuckDBDriver");
}
